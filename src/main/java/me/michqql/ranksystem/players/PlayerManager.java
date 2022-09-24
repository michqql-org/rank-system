package me.michqql.ranksystem.players;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.Settings;
import me.michqql.ranksystem.players.loader.JsonPlayerDataLoader;
import me.michqql.ranksystem.players.loader.PlayerDataLoader;
import me.michqql.ranksystem.permissions.CustomPermissible;
import me.michqql.ranksystem.permissions.PermissionInjector;
import me.michqql.ranksystem.ranks.PlayerRank;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayerManager implements Listener {

    private final RankSystemPlugin plugin;
    private final RankManager rankManager;

    // Player Data
    private final HashMap<UUID, PlayerData> playersData = new HashMap<>();
    private final PlayerDataLoader loader;
    private final Cache<UUID, PlayerData> offlinePlayersDataCache;

    // Custom permissible injector
    private final PermissionInjector injector;

    public PlayerManager(RankSystemPlugin plugin, RankManager rankManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;

        loader = new JsonPlayerDataLoader(plugin, rankManager);

        offlinePlayersDataCache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .removalListener(notification -> {
                    PlayerData data = (PlayerData) notification.getValue();
                    loader.savePlayerData(data);
                })
                .build();

        injector = new PermissionInjector(plugin);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playersData.get(uuid);
    }

    public CompletableFuture<PlayerData> getOfflinePlayerData(UUID uuid) {
        // Check online players
        PlayerData data = playersData.get(uuid);
        if(data != null)
            return CompletableFuture.completedFuture(data);

        // Check cached players
        data = offlinePlayersDataCache.getIfPresent(uuid);
        if(data != null)
            return CompletableFuture.completedFuture(data);

        // Otherwise, load from file/store
        CompletableFuture<PlayerData> future = loader.loadPlayerData(uuid);
        // Store offline player data to cache
        future.thenAccept(playerData -> offlinePlayersDataCache.put(uuid, playerData));
        return future;
    }

    public void savePlayerData(PlayerData data) {
        loader.savePlayerData(data);
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        final UUID uuid = e.getUniqueId();
        final boolean playedBefore = Bukkit.getOfflinePlayer(uuid).hasPlayedBefore();

        // Remove from offline cache
        offlinePlayersDataCache.invalidate(uuid);

        loader.loadPlayerData(uuid)
                .thenAccept(data -> {
                    playersData.put(uuid, data);

                    // Give default rank to new players
                    if(!playedBefore) {
                        Rank defaultRank = rankManager.getDefaultRank();
                        if(!data.hasRank(defaultRank)) {
                            PlayerRank pr = new PlayerRank(defaultRank, false, -1);
                            data.addPlayerRank(pr);
                        }
                        plugin.getLogger().info("Given " + uuid + " default rank " + defaultRank.getRankId());
                    }

                    plugin.getLogger().info("Loaded player data for user: " + uuid);
                });
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        final UUID uuid = e.getPlayer().getUniqueId();
        final int timeoutLimit = Settings.PLAYER_LOGIN_TIMEOUT_LIMIT.getIntValue();
        final boolean allowFaulty = Settings.ALLOW_PLAYER_WITHOUT_INJECTION.getBooleanValue();

        new BukkitRunnable() {
            int tries;

            @Override
            public void run() {
                PlayerData data = getPlayerData(uuid);
                if(data != null) {
                    // Inject
                    CustomPermissible permissible = new CustomPermissible(e.getPlayer(), data);
                    boolean success = injector.inject(e.getPlayer(), permissible);
                    if(!success) {
                        // Injection failed
                        plugin.getLogger().severe("Could not inject custom permissible for user: " + uuid);
                        if(allowFaulty && e.getResult() == PlayerLoginEvent.Result.ALLOWED) {
                            e.allow();
                        } else {
                            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Error has occurred, please try again or contact an admin");
                        }
                    }

                    this.cancel();
                    return;
                }

                tries++;
                if(tries >= timeoutLimit) {
                    plugin.getLogger().severe("Could not fetch player data to inject custom permissible for user: " + uuid);
                    if(allowFaulty && e.getResult() == PlayerLoginEvent.Result.ALLOWED) {
                        e.allow();
                    } else {
                        e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Error has occurred, please try again or contact an admin");
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        PlayerData data = playersData.remove(e.getPlayer().getUniqueId());
        if(data != null) {
            loader.savePlayerData(data);
        }
    }

    /**
     * Called when the server shuts down
     * Saves online AND offline player data as offline player data can still be edited
     */
    public void onDisable() {
        loader.saveAllPlayerData(playersData.values());
        offlinePlayersDataCache.invalidateAll();
    }
}

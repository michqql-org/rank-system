package me.michqql.ranksystem.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.Settings;
import me.michqql.ranksystem.players.PlayerData;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.PlayerRank;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PlayerRankPAPIExpansion extends PlaceholderExpansion {

    private final RankSystemPlugin plugin;
    private final PlayerManager playerManager;

    public PlayerRankPAPIExpansion(RankSystemPlugin plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        params = params.toLowerCase(Locale.ENGLISH);
        CompletableFuture<PlayerData> future = playerManager.getOfflinePlayerData(player.getUniqueId());
        try {
            PlayerData data = future.get();
            return switch (params) {
                case "prefix" -> data.getPrefix();
                case "suffix" -> data.getSuffix();
                case "namecolour" -> {
                    PlayerRank rank = data.getProminentRankOrHighest();
                    // yield works exactly like return
                    yield rank != null ? rank.getNameColour() : "";
                }
                case "chatcolour" -> {
                    PlayerRank rank = data.getProminentRankOrHighest();
                    // yield works exactly like return
                    yield rank != null ? rank.getChatColour() : "";
                }
                default -> params;
            };
        } catch (InterruptedException | ExecutionException e) {
            if(Settings.DEBUG.getBooleanValue()) {
                plugin.getLogger().warning("Error occurred when getting player data for placeholder");
                e.printStackTrace();
            }
            return "";
        }
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playerrank";
    }

    @Override
    public @NotNull String getAuthor() {
        return "michqql";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }
}

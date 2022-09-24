package me.michqql.ranksystem.commands.grant;

import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.Settings;
import me.michqql.ranksystem.guis.grant.GrantGui;
import me.michqql.ranksystem.players.PlayerData;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.PlayerRank;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.ranksystem.util.Placeholders;
import me.michqql.servercoreutils.commands.BaseCommand;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.util.MessageHandler;
import me.michqql.servercoreutils.util.OfflineUUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GrantCommandManager extends BaseCommand {

    private final GuiHandler guiHandler;
    private final RankManager rankManager;
    private final PlayerManager playerManager;

    public GrantCommandManager(Plugin plugin, MessageHandler messageHandler, GuiHandler guiHandler,
                               RankManager rankManager, PlayerManager playerManager) {
        super(plugin, messageHandler);
        this.guiHandler = guiHandler;
        this.rankManager = rankManager;
        this.playerManager = playerManager;
    }

    @Override
    public void commandDefault(CommandSender sender, String input, String[] args) {
        if(!sender.hasPermission(RankSystemPlugin.GRANT_RANK_PERMISSION)) {
            messageHandler.sendList(sender, "no-permission", new HashMap<>() {{
                put("permission", RankSystemPlugin.GRANT_RANK_PERMISSION);
                final Rank reqRank = rankManager.getLowestRankForPermission(RankSystemPlugin.GRANT_RANK_PERMISSION);
                putAll(Placeholders.ofRank(reqRank, "reqrank"));
            }});
            return;
        }

        final boolean isPlayer = sender instanceof Player;
        // grant <player> <rank> [time] <- console sender
        if(!isPlayer && args.length <= 1) {
            messageHandler.sendList(sender, "grant-command-messages.usage.console");
            return;
        }

        // grant <player> [time]        <- player sender
        if(args.length == 0) {
            messageHandler.sendList(sender, "grant-command-messages.usage.player");
            return;
        }

        // Get the rank id if the sender is a console sender
        Rank rank = null;
        if(!isPlayer) {
            String id = args[1];
            rank = rankManager.getRankById(id);
            if(rank == null) {
                messageHandler.sendList(sender, "no-rank-with-id", Placeholders.of("id", id));
                return;
            }
        }
        final Rank finalRank = rank;

        // Get the specified time
        final int timePos = isPlayer ? 1 : 2;
        String suffixData = "";
        if(args.length > timePos) {
            StringBuilder builder = new StringBuilder();
            for(int i = timePos; i < args.length; i++) {
                builder.append(args[i]).append(' ');
            }
            suffixData = builder.toString();
        }

        // Convert suffix data into time
        long time = convertStringToTimeInMs(suffixData);
        boolean permanent = (time == 0 && Settings.PERMANENT_GRANTS_AS_DEFAULT.getBooleanValue());
        if(!permanent && time == 0)
            time = Settings.DEFAULT_TEMPORARY_TIME_MS.getLongValue();
        final long finalTime = time;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = OfflineUUID.getUUID(input);
            if(uuid == null) {
                messageHandler.sendList(sender, "grant-command-messages.player-not-found",
                        Placeholders.of("name", input));
                return;
            }

            CompletableFuture<PlayerData> future = playerManager.getOfflinePlayerData(uuid);
            future.thenAccept(playerData -> {
                if(isPlayer) {
                    new GrantGui(guiHandler, (Player) sender, playerManager, rankManager, playerData, permanent, finalTime).openGui();
                } else {
                    PlayerRank pr = new PlayerRank(finalRank, !permanent, System.currentTimeMillis() + finalTime);
                    playerData.addPlayerRank(pr);
                    sender.sendMessage("Granted " + input + " rank " + finalRank.getRankId());
                    messageHandler.sendList(sender, "grant-command-messages.granted", new HashMap<>() {{
                        put("player.name", input);
                        put("player.uuid", playerData.getUuid().toString());
                        put("rank.id", finalRank.getRankId());
                    }});
                }
            });
        });
    }

    @Override
    protected void registerSubCommands() {
        // grant <player>
    }

    private long convertStringToTimeInMs(String data) {
        if(data.isEmpty())
            return 0;

        final String[] split = data.split("[\\W]+");
        long totalTime = 0;

        for(String part : split) {
            if(part.length() < 2)
                continue;

            char unit = part.charAt(part.length() - 1);
            String time = part.substring(0, part.length() - 1);
            int integer;
            try {
                integer = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                continue;
            }

            switch (unit) {
                case 's' -> totalTime += TimeUnit.SECONDS.toMillis(integer);
                case 'm' -> totalTime += TimeUnit.MINUTES.toMillis(integer);
                case 'h' -> totalTime += TimeUnit.HOURS.toMillis(integer);
                case 'd' -> totalTime += TimeUnit.DAYS.toMillis(integer);
                case 'W' -> totalTime += (TimeUnit.DAYS.toMillis(integer) * 7);
                case 'M' -> totalTime += (TimeUnit.DAYS.toMillis(integer) * 30);
                case 'Y' -> totalTime += (TimeUnit.DAYS.toMillis(integer) * 365);
            }
        }
        return totalTime;
    }
}

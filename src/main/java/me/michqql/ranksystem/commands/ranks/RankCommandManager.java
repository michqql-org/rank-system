package me.michqql.ranksystem.commands.ranks;

import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.guis.RanksGui;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.ranksystem.util.Placeholders;
import me.michqql.servercoreutils.commands.BaseCommand;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.util.MessageHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RankCommandManager extends BaseCommand {

    private final GuiHandler guiHandler;
    private final RankManager rankManager;
    private final PlayerManager playerManager;

    public RankCommandManager(Plugin plugin, MessageHandler messageHandler, GuiHandler guiHandler, RankManager rankManager, PlayerManager playerManager) {
        super(plugin, messageHandler);
        this.guiHandler = guiHandler;
        this.rankManager = rankManager;
        this.playerManager = playerManager;
    }

    @Override
    public void commandDefault(CommandSender sender, String input, String[] args) {
        if(args.length > 0) {
            messageHandler.sendList(sender, "rank-command-messages.unknown-subcommand", new HashMap<>() {{
                put("label", "rank");
                put("input", input);
            }});
            return;
        }

        if(!(sender instanceof Player player)) {
            LinkedList<Rank> ranks = rankManager.getOrderedRanks();
            messageHandler.sendList(sender, "rank-command-messages.list-ranks.header", new HashMap<>() {{
                put("order", "DESCENDING");
            }});
            if(ranks.isEmpty()) {
                messageHandler.sendList(sender, "rank-command-messages.list-ranks.no-elements");
            } else {
                for(Rank rank : ranks) {
                    messageHandler.sendList(sender, "rank-command-messages.list-ranks.list-element",
                            Placeholders.ofRank(rank, "rank"));
                }
            }
            return;
        }

        if(!player.hasPermission(RankSystemPlugin.ADMIN_PERMISSION)) {
            messageHandler.sendList(sender, "no-permission", new HashMap<>() {{
                put("permission", RankSystemPlugin.ADMIN_PERMISSION);
                final Rank reqRank = rankManager.getLowestRankForPermission(RankSystemPlugin.ADMIN_PERMISSION);
                putAll(Placeholders.ofRank(reqRank, "reqrank"));
            }});
            return;
        }

        new RanksGui(guiHandler, player, rankManager, playerManager).openGui();
    }

    @Override
    protected void registerSubCommands() {
        children.addAll(List.of(
                new CreateRankSubCommand(plugin, messageHandler, rankManager),
                new EditRankSubCommand(plugin, messageHandler, guiHandler, rankManager, playerManager)
        ));
    }
}

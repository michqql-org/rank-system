package me.michqql.ranksystem.commands.ranks;

import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.ranksystem.util.Placeholders;
import me.michqql.servercoreutils.commands.SubCommand;
import me.michqql.servercoreutils.util.MessageHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

public class CreateRankSubCommand extends SubCommand {

    private final RankManager rankManager;

    public CreateRankSubCommand(Plugin plugin, MessageHandler messageHandler, RankManager rankManager) {
        super(plugin, messageHandler);
        this.rankManager = rankManager;
    }


    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            messageHandler.sendList(sender, "rank-command-messages.create.no-id");
            return;
        }

        String id = args[0];
        if(rankManager.doesRankExist(id)) {
            messageHandler.sendList(sender, "rank-command-messages.create.rank-with-id-exists",
                    Placeholders.of("id", id));
            return;
        }

        Rank rank = rankManager.createRank(id);
        Map<String, String> placeholders = Placeholders.ofRank(rank, "rank");
        placeholders.put("id", id);
        messageHandler.sendList(sender, "rank-command-messages.create.success", placeholders);
    }

    @Override
    protected String getName() {
        return "create";
    }

    @Override
    protected List<String> getAliases() {
        return null;
    }

    @Override
    protected String getPermission() {
        return RankSystemPlugin.ADMIN_PERMISSION;
    }

    @Override
    protected List<String> getArguments(CommandSender commandSender, String[] strings) {
        return null;
    }

    @Override
    protected boolean requiresPlayer() {
        return false;
    }
}

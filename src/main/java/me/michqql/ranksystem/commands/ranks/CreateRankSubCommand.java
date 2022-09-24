package me.michqql.ranksystem.commands.ranks;

import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.servercoreutils.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CreateRankSubCommand extends SubCommand {

    private final RankManager rankManager;

    public CreateRankSubCommand(Plugin plugin, RankManager rankManager) {
        super(plugin);
        this.rankManager = rankManager;
    }


    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please specify an ID, usage: /rank create <id>");
            return;
        }

        String id = args[0];
        if(rankManager.doesRankExist(id)) {
            sender.sendMessage(ChatColor.RED + "A rank with ID " + id + " already exists!");
            return;
        }

        rankManager.createRank(id);
        sender.sendMessage(ChatColor.GREEN + "Created a new rank with ID " + id);
        sender.sendMessage(ChatColor.GREEN + "You can not edit it with " + ChatColor.YELLOW + "/rank edit " + id);
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

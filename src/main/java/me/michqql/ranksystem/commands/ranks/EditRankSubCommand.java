package me.michqql.ranksystem.commands.ranks;

import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.guis.editor.RankEditorGui;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.servercoreutils.commands.SubCommand;
import me.michqql.servercoreutils.gui.GuiHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class EditRankSubCommand extends SubCommand {

    private final GuiHandler guiHandler;
    private final RankManager rankManager;
    private final PlayerManager playerManager;

    public EditRankSubCommand(Plugin plugin, GuiHandler guiHandler, RankManager rankManager, PlayerManager playerManager) {
        super(plugin);
        this.guiHandler = guiHandler;
        this.rankManager = rankManager;
        this.playerManager = playerManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use the in-game editor");
            sender.sendMessage(ChatColor.RED + "Instead, please edit the rank files directly");
            return;
        }

        if(args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /rank edit <id>");
            return;
        }

        String id = args[0];
        Rank rank = rankManager.getRankById(id);
        if(rank == null) {
            sender.sendMessage(ChatColor.RED + "A rank with id " + id + " does not exist");
            return;
        }

        new RankEditorGui(guiHandler, player, rankManager, playerManager, rank).openGui();
    }

    @Override
    protected String getName() {
        return "edit";
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
    protected List<String> getArguments(CommandSender commandSender, String[] args) {
        String input = args[0];
        List<String> results = new ArrayList<>();

        for(Rank rank : rankManager.getRanks()) {
            if(rank.getRankId().startsWith(input))
                results.add(rank.getRankId());
        }
        return results;
    }

    @Override
    protected boolean requiresPlayer() {
        return false;
    }
}

package me.michqql.ranksystem.commands.ranks;

import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.guis.editor.RankEditorGui;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.ranksystem.util.Placeholders;
import me.michqql.servercoreutils.commands.SubCommand;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.util.MessageHandler;
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

    public EditRankSubCommand(Plugin plugin, MessageHandler messageHandler, GuiHandler guiHandler, RankManager rankManager, PlayerManager playerManager) {
        super(plugin, messageHandler);
        this.guiHandler = guiHandler;
        this.rankManager = rankManager;
        this.playerManager = playerManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            messageHandler.sendList(sender, "rank-command-messages.edit.requires-player");
            return;
        }

        if(args.length == 0) {
            messageHandler.sendList(sender, "rank-command-messages.edit.no-id");
            return;
        }

        String id = args[0];
        Rank rank = rankManager.getRankById(id);
        if(rank == null) {
            messageHandler.sendList(sender, "rank-command-messages.edit.rank-with-id-doesnt-exist",
                    Placeholders.of("id", id));
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

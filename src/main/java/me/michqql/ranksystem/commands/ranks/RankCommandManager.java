package me.michqql.ranksystem.commands.ranks;

import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.guis.RanksGui;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.servercoreutils.commands.BaseCommand;
import me.michqql.servercoreutils.gui.GuiHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class RankCommandManager extends BaseCommand {

    private final GuiHandler guiHandler;
    private final RankManager rankManager;
    private final PlayerManager playerManager;

    public RankCommandManager(Plugin plugin, GuiHandler guiHandler, RankManager rankManager, PlayerManager playerManager) {
        super(plugin);
        this.guiHandler = guiHandler;
        this.rankManager = rankManager;
        this.playerManager = playerManager;
    }

    @Override
    public void commandDefault(CommandSender sender, String input, String[] args) {
        if(args.length > 0) {
            // TODO: Send unknown command message
            return;
        }

        if(!(sender instanceof Player player)) {
            // TODO: List ranks as message
            return;
        }

        if(!player.hasPermission(RankSystemPlugin.ADMIN_PERMISSION)) {
            // TODO: Send no permission message
            return;
        }

        new RanksGui(guiHandler, player, rankManager, playerManager).openGui();
    }

    @Override
    protected void registerSubCommands() {
        children.addAll(List.of(
                new CreateRankSubCommand(plugin, rankManager),
                new EditRankSubCommand(plugin, guiHandler, rankManager, playerManager)
        ));
    }
}

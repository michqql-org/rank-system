package me.michqql.ranksystem;

import me.michqql.ranksystem.commands.grant.GrantCommandManager;
import me.michqql.ranksystem.commands.ranks.RankCommandManager;
import me.michqql.ranksystem.permissions.PermissionsManager;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.ranksystem.util.PlayerRankPAPIExpansion;
import me.michqql.ranksystem.util.RankPAPIExpansion;
import me.michqql.servercoreutils.gui.GuiHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class RankSystemPlugin extends JavaPlugin {

    public final static String ADMIN_PERMISSION = "ranks.admin";
    public final static String GRANT_RANK_PERMISSION = "ranks.grant";

    private RankManager rankManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        // Tracks permissions and their source plugin
        PermissionsManager.setPluginInstance(this); // Loads permission cache automatically

        // Load config
        Settings.loadSettings(this);

        // Load managers
        final GuiHandler guiHandler = new GuiHandler(this);
        // Rank manager must be loaded before player manager, as the player data loader requires ranks to be loaded
        rankManager = new RankManager(this);
        playerManager = new PlayerManager(this, rankManager);
        // Register player manager as a listener
        Bukkit.getPluginManager().registerEvents(playerManager, this);

        // Register commands
        PluginCommand rankCommand = getCommand("rank");
        if(rankCommand != null) {
            rankCommand.setExecutor(new RankCommandManager(this, guiHandler, rankManager, playerManager));
        }
        PluginCommand grantCommand = getCommand("grant");
        if(grantCommand != null) {
            grantCommand.setExecutor(new GrantCommandManager(this, guiHandler, rankManager, playerManager));
        }

        // Register placeholders - optional
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            // Placeholder API is installed on the server
            new RankPAPIExpansion(rankManager).register();
            new PlayerRankPAPIExpansion(this, playerManager).register();

            getLogger().info("Integrated Placeholder API support");
        }
    }

    @Override
    public void onDisable() {
        if(rankManager != null) {
            rankManager.onDisable();
        }

        if(playerManager != null) {
            playerManager.onDisable();
        }

        PermissionsManager.saveData(); // Save permissions to a cache
    }
}

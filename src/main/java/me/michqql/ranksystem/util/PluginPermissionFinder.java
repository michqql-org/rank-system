package me.michqql.ranksystem.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PluginPermissionFinder {

    public static PluginPermissionInfo getPluginPermissionInfo(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        //return new PluginPermissionInfo(plugin, plugin.getDescription().getPermissions());
        return null;
    }

    public static class PluginPermissionInfo {

        private final Plugin plugin;
        private final Set<String> permissions;

        public PluginPermissionInfo(Plugin plugin) {
            this.plugin = plugin;
            this.permissions = new HashSet<>();
        }

        public PluginPermissionInfo(Plugin plugin, Collection<String> permissions) {
            this.plugin = plugin;
            this.permissions = new HashSet<>(permissions);
        }

        public Plugin getPlugin() {
            return plugin;
        }

        public Set<String> getPermissions() {
            return permissions;
        }
    }
}

package me.michqql.ranksystem.permissions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.michqql.ranksystem.PermissionUtil;
import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.util.FindBestLongForm;
import me.michqql.servercoreutils.io.IO;
import me.michqql.servercoreutils.io.JsonFile;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsManager {

    private static RankSystemPlugin PLUGIN;
    private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private static final ConcurrentHashMap<String, Set<PermissionInfo>> KEYWORD_TO_PERMISSIONS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Plugin, Set<PermissionInfo>> PLUGIN_TO_PERMISSIONS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, PermissionInfo> PERMISSIONS_TO_INFO = new ConcurrentHashMap<>();

    public static void setPluginInstance(RankSystemPlugin pluginIn) {
        PLUGIN = pluginIn;
        loadData();
        registerPluginsDelayed();
    }

    public static Set<PermissionInfo> getPermissionsBySearch(String input) {
        String[] split = input.toLowerCase(PermissionUtil.LOCALE).split("[. ]+");
        Set<PermissionInfo> result = new HashSet<>();
        for(String part : split) {
            Set<PermissionInfo> set = KEYWORD_TO_PERMISSIONS.get(part);
            if(set != null)
                result.addAll(set);
        }

        return result;
    }

    public static Set<PermissionInfo> getPermissionsByPlugin(Plugin plugin) {
        return PLUGIN_TO_PERMISSIONS.getOrDefault(plugin, Collections.emptySet());
    }

    public static PermissionInfo getPermissionInfo(String permission) {
        return PERMISSIONS_TO_INFO.get(permission);
    }

    public static Collection<PermissionInfo> getAllPermissionInfo() {
        return PERMISSIONS_TO_INFO.values();
    }

    static void registerPermission(String permissionIn) {
        // Run async
        SCHEDULER.runTaskAsynchronously(PLUGIN, () -> {
            final String permission = permissionIn.toLowerCase(PermissionUtil.LOCALE);
            // 1. Check if permission is already registered - if so ignore
            if(PERMISSIONS_TO_INFO.containsKey(permission))
                return;

            // Create a permission info object
            PermissionInfo info = new PermissionInfo(permission);

            // Decipher a source plugin(s)
            Set<Plugin> possibilities = getPossibleSourcePlugins(permission);
            if(!possibilities.isEmpty()) {
                info.setPossibleSourcePlugins(possibilities);
            }

            PERMISSIONS_TO_INFO.put(permission, info);
            registerKeywordsAndPlugins(info);
        });
    }

    private static void registerBatch(List<String> permissionsIn) {
        // Run async
        SCHEDULER.runTaskAsynchronously(PLUGIN, () -> {
            for(String permissionIn : permissionsIn) {
                final String permission = permissionIn.toLowerCase(PermissionUtil.LOCALE);
                // 1. Check if permission is already registered - if so ignore
                if (PERMISSIONS_TO_INFO.containsKey(permission))
                    return;

                // Create a permission info object
                PermissionInfo info = new PermissionInfo(permission);

                // Decipher a source plugin(s)
                Set<Plugin> possibilities = getPossibleSourcePlugins(permission);
                if (!possibilities.isEmpty()) {
                    info.setPossibleSourcePlugins(possibilities);
                }

                PERMISSIONS_TO_INFO.put(permission, info);
                registerKeywordsAndPlugins(info);
            }
        });
    }

    private static void registerPluginsDelayed() {
        SCHEDULER.runTaskLater(PLUGIN, () -> {
            Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
            for(Plugin plugin : plugins) {
                List<Permission> wrapped = plugin.getDescription().getPermissions();
                List<String> permissions = new ArrayList<>();
                wrapped.forEach(permission -> permissions.add(permission.getName())); // Unwrap bukkit's permissions
                registerBatch(permissions);
            }
        }, 20 * 20); // 20 seconds delay

    }

    private static Set<Plugin> getPossibleSourcePlugins(String permission) {
        final Set<Plugin> possibilities = new HashSet<>();
        final Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

        // Permissions are broken by .
        String[] split = permission.split(PermissionUtil.PERMISSION_SPLIT_REGEX);
        String src = split[0]; // Permissions often start with the source plugin
        for(Plugin plugin : plugins) {
            // Check if exact or similar
            String pluginName = plugin.getName();
            if(src.equalsIgnoreCase(pluginName)) {
                possibilities.add(plugin);
                continue;
            }

            // FindBestLongForm will check if the src is an abbreviation of the plugin name
            // which will work in a lot of cases
            if(pluginName.equalsIgnoreCase(FindBestLongForm.find(src, pluginName))) {
                possibilities.add(plugin);
            }
        }
        return possibilities;
    }

    private static void registerKeywordsAndPlugins(PermissionInfo info) {
        // Register keywords
        String[] split = info.permission.split(PermissionUtil.PERMISSION_SPLIT_REGEX);
        for(String part : split) {
            KEYWORD_TO_PERMISSIONS.compute(part, (k, set) -> {
                if(set == null)
                    set = new HashSet<>();

                set.add(info);
                return set;
            });
        }

        // Register source plugins
        if(info.hasPossibleSourcePlugins()) {
            for (Plugin plugin : info.possibleSourcePlugins) {
                PLUGIN_TO_PERMISSIONS.compute(plugin, (k, set) -> {
                    if(set == null)
                        set = new HashSet<>();

                    set.add(info);
                    return set;
                });
            }
        }
    }

    public static class PermissionInfo {
        private final String permission;
        private Set<Plugin> possibleSourcePlugins;

        public PermissionInfo(String permission) {
            this.permission = permission;
        }

        public void setPossibleSourcePlugins(Set<Plugin> possibleSourcePlugins) {
            this.possibleSourcePlugins = possibleSourcePlugins;
        }

        public String getPermission() {
            return permission;
        }

        public Set<Plugin> getPossibleSourcePlugins() {
            return possibleSourcePlugins;
        }

        public boolean hasPossibleSourcePlugins() {
            return possibleSourcePlugins != null && !possibleSourcePlugins.isEmpty();
        }
    }

    private static void loadData() {
        JsonFile file = IO.getJsonFile(PLUGIN, "permissions/cache.yml");

        JsonElement element = file.getElement();
        if(!element.isJsonArray())
            return;

        JsonArray array = element.getAsJsonArray();
        List<String> permissions = new ArrayList<>();
        for(JsonElement arrayElement : array) {
            if(arrayElement.isJsonPrimitive())
                permissions.add(arrayElement.getAsString());
        }

        registerBatch(permissions);
    }

    public static void saveData() {
        JsonFile file = IO.getJsonFile(PLUGIN, "permissions/cache.yml");

        JsonArray array = new JsonArray();
        file.setElement(array);

        Enumeration<String> permissions = PERMISSIONS_TO_INFO.keys();
        while(permissions.hasMoreElements()) {
            array.add(permissions.nextElement());
        }

        file.save();
    }
}

package me.michqql.ranksystem.permissions;

import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.Settings;
import me.michqql.ranksystem.util.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.lang.reflect.Field;

public class PermissionInjector {

    private final RankSystemPlugin plugin;
    private final String classPath;
    private final String fieldName;

    private final Field field;

    public PermissionInjector(RankSystemPlugin plugin) {
        this.plugin = plugin;
        this.classPath = ReflectionUtil.getCraftClassName("entity.CraftHumanEntity");
        this.fieldName = "perm";

        this.field = getPermissibleField();
    }

    public PermissionInjector(RankSystemPlugin plugin, String classPath, String fieldName) {
        this.plugin = plugin;
        this.classPath = classPath;
        this.fieldName = fieldName;

        this.field = getPermissibleField();
    }

    public boolean inject(Player player, Permissible permissible) {
        final Field permField = field;
        if (permField == null) {
            if(Settings.DEBUG.getBooleanValue())
                plugin.getLogger().severe("Permissible field is null at point of injection");
            return false; // Error has occurred
        }

        // Inject permissible
        try {
            permField.set(player, permissible);
            return true;
        } catch (IllegalAccessException e) {
            plugin.getLogger().warning("Could not set custom permissisble for player " + player.getName() + " (" + player.getUniqueId() + ")");
            if(Settings.DEBUG.getBooleanValue())
                e.printStackTrace();
            return false;
        }
    }

    private Field getPermissibleField() {
        Class<?> humanEntity;
        try {
            humanEntity = Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Could not get permissisble field; could not find class");
            if(Settings.DEBUG.getBooleanValue())
                e.printStackTrace();
            return null;
        }

        try {
            Field permField = humanEntity.getDeclaredField(this.fieldName);
            permField.setAccessible(true);
            return permField;
        } catch (NoSuchFieldException e) {
            plugin.getLogger().severe("Could not get permissisble field; could not find field");
            if(Settings.DEBUG.getBooleanValue())
                e.printStackTrace();
            return null;
        }
    }
}

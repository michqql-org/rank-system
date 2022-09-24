package me.michqql.ranksystem.util;

import org.bukkit.Bukkit;

public class ReflectionUtil {

    public final static String CRAFT_BUKKIT_PREFIX = "org.bukkit.craftbukkit";
    public final static String CRAFT_BUKKIT_VERSION;

    static {
        CRAFT_BUKKIT_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static String getCraftClassName(String className) {
        return CRAFT_BUKKIT_PREFIX + "." + CRAFT_BUKKIT_VERSION + "." + className;
    }
}

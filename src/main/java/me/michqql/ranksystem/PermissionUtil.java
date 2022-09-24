package me.michqql.ranksystem;

import me.michqql.ranksystem.players.PlayerData;
import me.michqql.ranksystem.ranks.PlayerRank;
import me.michqql.ranksystem.ranks.Rank;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PermissionUtil {

    public final static Locale LOCALE = Locale.ENGLISH;
    public final static String PERMISSION_SPLIT_REGEX = "[.]+";

    public static boolean hasPermission(PlayerData playerData, String permission, boolean exact) {
        permission = permission.toLowerCase(LOCALE);
        Set<String> permissions = getAllPermissions(playerData);

        // If the permission needs to match exactly, the set needs to contain the string
        boolean match = permissions.contains(permission);
        if(match || exact) // Either the permission matches or we want an exact permission match
            return match;

        // Otherwise, check parents
        String[] parts = permission.split(PERMISSION_SPLIT_REGEX);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < parts.length - 1; i++) { // Does not check exact permission - already checked
            builder.append(parts[i]).append('.');

            // Check if the rank has the parent permission
            String currentPermission = builder + "*";
            if(permissions.contains(currentPermission))
                return true;
        }

        // No match found, player does not have permission
        return false;
    }

    /**
     * Returns all the permissions that this player has - excluding duplicates and excluded permissions
     * @param playerData The player to get the permissions of
     * @return           A set containing all permissions
     */
    public static Set<String> getAllPermissions(PlayerData playerData) {
        Set<String> permissions = new HashSet<>();

        for(PlayerRank pr : playerData.getPlayerRanks()) {
            if(pr.hasExpired())
                continue;

            permissions.addAll(pr.getRank().getPermissions());
        }

        return permissions;
    }

    /**
     * Checks whether this rank has the specified permission. <br>
     * This method also checks whether the parent permission is present.
     * @param rank       The rank to check
     * @param permission The permission to check for
     * @return           {@code true} if the rank has this permission or parent permission, {@code false} otherwise
     */
    public static boolean hasPermissionOrParentPermission(Rank rank, String permission) {
        // Check exact permission first
        if(hasExactPermission(rank, permission))
            return true;

        permission = permission.toLowerCase(LOCALE);
        Set<String> included = rank.getPermissions();

        // Break the permission into individual nodes
        String[] parts = permission.split(PERMISSION_SPLIT_REGEX);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < parts.length - 1; i++) { // Does not check exact permission - already checked
            builder.append(parts[i]).append('.');

            // Check if the rank has the parent permission
            String currentPermission = builder + "*";
            if(included.contains(currentPermission))
                return true;
        }

        // Permission was not found
        return false;
    }

    /**
     * Checks whether this rank has the exact specified permission
     * @param rank       The rank to check
     * @param permission The permission to check for
     * @return           {@code true} if the rank has this permission, {@code false} otherwise
     */
    public static boolean hasExactPermission(Rank rank, String permission) {
        permission = permission.toLowerCase(LOCALE);
        Set<String> included = rank.getPermissions();

        return included.contains(permission);
    }
}

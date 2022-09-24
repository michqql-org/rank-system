package me.michqql.ranksystem.permissions;

import me.michqql.ranksystem.PermissionUtil;
import me.michqql.ranksystem.players.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.permissions.*;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.Set;

@SuppressWarnings("NullableProblems")
public class CustomPermissible extends PermissibleBase {

    private static final UnsupportedOperationException UNSUPPORTED = new UnsupportedOperationException(
            "Permission Attachments not currently supported by RankSystem"
    );

    private final Player player;
    private final PlayerData data;

    public CustomPermissible(Player player, PlayerData data) {
        super(player);
        this.player = player;
        this.data = data;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return hasPermission(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return hasPermission(perm.getName());
    }

    @Override
    public boolean hasPermission(String name) {
        PermissionsManager.registerPermission(name);
        return isOp() || PermissionUtil.hasPermission(data, name, false);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return hasPermission(perm.getName());
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        throw UNSUPPORTED;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        throw UNSUPPORTED;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        throw UNSUPPORTED;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        throw UNSUPPORTED;
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        throw UNSUPPORTED;
    }

    @Override
    public void recalculatePermissions() {}

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Collections.emptySet();
    }

    @Override
    public boolean isOp() {
        return player.isOp();
    }

    @Override
    public void setOp(boolean value) {
        player.setOp(value);
    }

    @Override
    public synchronized void clearPermissions() {}
}

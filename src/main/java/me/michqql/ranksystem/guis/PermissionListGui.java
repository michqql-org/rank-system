package me.michqql.ranksystem.guis;

import me.michqql.ranksystem.ranks.Rank;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.gui.type.PagedGui;
import me.michqql.servercoreutils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Set;

public class PermissionListGui extends PagedGui {

    private final Rank rank;

    public PermissionListGui(GuiHandler guiHandler, Player player, Rank rank) {
        super(guiHandler, player, PagedGui.getMaxSize());
        this.rank = rank;

        build("&2Listing &f" + rank.getRankId() + "'s &2permissions", 6);
    }

    @Override
    protected void createInventory() {
        // Add back button first
        addElement(
                new ItemBuilder(Material.RED_BED).displayName("&c<-- Go Back").getItem(),
                new Button((integer, type) -> guiHandler.openPreviousGui(player))
        );

        final Set<String> permissions = rank.getPermissions();
        for(String permission : permissions) {
            addElement(
                    new ItemBuilder(Material.PAPER).displayName("&a" + permission).getItem(),
                    new Button((integer, type) -> {})
            );
        }

        updateInventory();
    }

    @Override
    protected void onClose() {

    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int i, ClickType clickType) {
        return true;
    }
}

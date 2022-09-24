package me.michqql.ranksystem.guis.editor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.michqql.ranksystem.guis.PermissionFinderGui;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.servercoreutils.gui.Gui;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PermissionsEditorGui extends Gui {

    private final int BACK_SLOT = 45;
    private final int LEFT_SLOT = 38;
    private final int RIGHT_SLOT = 50;
    private final int INHERIT_RANKS_SLOT = 52;
    private final int ADD_SLOT = 53;

    private final Cache<Integer, Boolean> cooldownMap = CacheBuilder.newBuilder()
            .expireAfterWrite(500, TimeUnit.MILLISECONDS).build();

    private final RankManager rankManager;
    private final Rank rank;
    private final List<String> permissions;
    private final Set<String> deletedPermissions = new HashSet<>();

    // Pane and current page
    private InventoryPane pane;
    private int pageNumber;

    public PermissionsEditorGui(GuiHandler guiHandler, Player player, RankManager rankManager, Rank rank) {
        super(guiHandler, player);
        this.rankManager = rankManager;
        this.rank = rank;
        this.permissions = new ArrayList<>(rank.getPermissions());
        permissions.sort(Comparator.naturalOrder());
        build("&2Editing &f" + rank.getRankId() + "'s &2permissions", 6);
    }

    @Override
    protected void createInventory() {
        this.pane = new InventoryPane(inventory);
        this.inventory.setItem(BACK_SLOT, new ItemBuilder(Material.RED_BED)
                .displayName("&c<-- Back").getItem());

        this.inventory.setItem(INHERIT_RANKS_SLOT, new ItemBuilder(Material.SKULL_BANNER_PATTERN)
                .displayName("&7Inherit Ranks")
                .lore(
                        "&eClick &7to inherit other ranks permissions"
                ).getItem());

        this.inventory.setItem(ADD_SLOT, new ItemBuilder(Material.SKULL_BANNER_PATTERN)
                .displayName("&7Add Permissions")
                .lore(
                        "&eClick &7to find and add new permissions to this rank"
                ).getItem());

        updateInventory();
    }

    @Override
    protected void updateInventory() {
        if(pane != null) {
            int page = 0;
            if (pane.requiresPages()) {
                page = this.pageNumber;

                if (pageNumber > 0) {
                    this.inventory.setItem(LEFT_SLOT, new ItemBuilder(Material.ARROW)
                            .displayName("&7Page left").getItem());
                }

                if (pageNumber < pane.getRequiredPages() - 1) {
                    this.inventory.setItem(RIGHT_SLOT, new ItemBuilder(Material.ARROW)
                            .displayName("&7Page right").getItem());
                }
            }

            pane.updatePane(page);
        }
    }

    @Override
    protected void onClose() {
        this.permissions.removeAll(deletedPermissions);
        this.rank.setPermissions(new HashSet<>(permissions));

        // Recalculate the permissions
        rankManager.recalculatePermissions(() -> {
            if(guiHandler.getCurrentGui(player.getUniqueId()) instanceof RankEditorGui gui)
                gui.updateInventory();
        });
    }

    @Override
    protected boolean onClickEvent(int slot, ClickType clickType) {
        if(slot == BACK_SLOT) {
            guiHandler.openPreviousGui(player);
            return true;
        }

        if(slot == INHERIT_RANKS_SLOT) {
            new InheritRanksEditorGui(guiHandler, player, rankManager, rank).openGui();
            return true;
        }

        if(slot == ADD_SLOT) {
            new PermissionFinderGui(guiHandler, player, s -> {
                if(permissions.contains(s)) {
                    player.sendMessage(ChatColor.WHITE + rank.getRankId() + ChatColor.RED + " already has permission " + ChatColor.WHITE + s);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);
                    return;
                }
                permissions.add(s);
                // Set current page to the page this permission was added to
                this.pageNumber = pane != null ? pane.getRequiredPages() - 1 : 0;
                guiHandler.openPreviousGui(player);
            }).openGui();
            return true;
        }

        if((slot == LEFT_SLOT || slot == RIGHT_SLOT) && pane != null && pane.requiresPages()) {
            if (slot == LEFT_SLOT && pageNumber > 0) {
                pageNumber--;
            } else if (slot == RIGHT_SLOT && pageNumber < pane.getRequiredPages() - 1) {
                pageNumber++;
            }
            updateInventory();
            return true;
        }

        if(pane != null)
            pane.onClick(pageNumber, slot);
        return true;
    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int i, ClickType clickType) {
        return true;
    }

    private class InventoryPane {
        protected final static int MIN = 0, MAX = 44, PER_PAGE = 45;

        private final Inventory inventory;

        public InventoryPane(Inventory inventory) {
            this.inventory = inventory;
        }

        public void setItem(int slot, ItemStack item) {
            if(slot < MIN || slot > MAX)
                return;

            this.inventory.setItem(slot, item);
        }

        public void updatePane(int page) {
            int start = page * PER_PAGE;
            for(int i = 0; i < PER_PAGE; i++) {
                int index = start + i;
                if(index >= permissions.size())
                    break;

                String permission = permissions.get(index);
                if(deletedPermissions.contains(permission)) {
                    setItem(i, new ItemBuilder(Material.BARRIER)
                            .displayName("&c" + permission)
                            .lore("", "&eClick &7to &aundo &7permission removal").getItem());
                } else {
                    setItem(i, new ItemBuilder(Material.PAPER)
                            .displayName("&a" + permission)
                            .lore("", "&eClick &7to &cremove &7this permission").getItem());
                }
            }
        }

        public void onClick(int page, int slot) {
            if(slot < MIN || slot > MAX)
                return;

            int index = page * PER_PAGE + slot;
            if(index >= permissions.size())
                return;

            if(cooldownMap.getIfPresent(index) != null) // This item is on cooldown
                return;

            String permission = permissions.get(index);
            if(!deletedPermissions.remove(permission))
                deletedPermissions.add(permission);

            cooldownMap.put(index, true);
            updateInventory();
        }

        public boolean requiresPages() {
            return permissions.size() > PER_PAGE;
        }

        public int getRequiredPages() {
            return (int) Math.ceil((double) permissions.size() / PER_PAGE);
        }
    }
}

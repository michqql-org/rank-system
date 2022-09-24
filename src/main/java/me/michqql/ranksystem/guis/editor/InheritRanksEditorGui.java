package me.michqql.ranksystem.guis.editor;

import me.michqql.ranksystem.guis.PermissionListGui;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.gui.type.PagedGui;
import me.michqql.servercoreutils.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InheritRanksEditorGui extends PagedGui {

    private final RankManager rankManager;
    private final Rank rank;
    private final List<String> inherited;

    private boolean recalculatingPermissions = false;

    public InheritRanksEditorGui(GuiHandler guiHandler, Player player, RankManager rankManager, Rank rank) {
        super(guiHandler, player, PagedGui.getMaxSize());
        this.rankManager = rankManager;
        this.rank = rank;
        this.inherited = rank.getInheritedRanks();

        build("&2Inherit Ranks", 6);
    }

    @Override
    protected void createInventory() {
        // Add back button first
        addElement(
                new ItemBuilder(Material.RED_BED).displayName("&c<-- Go Back").getItem(),
                new Button((integer, type) -> guiHandler.openPreviousGui(player))
        );

        List<Rank> ranks = rankManager.getRanks();
        ranks.remove(rank);
        ranks.sort((r1, r2) -> r2.getWeight() - r1.getWeight()); // Sort the ranks into descending weight order

        for(Rank rank : ranks) {

            List<String> lore = new ArrayList<>();
            lore.add("&7Weight: &f" + rank.getWeight());
            lore.add("&7Prefix: &f" + rank.getPrefix());
            lore.add("");
            lore.add("&7Permissions:");
            Set<String> included = rank.getPermissions();
            if(included == null || included.isEmpty()) {
                lore.add(" &7> &cNone");
            } else {
                lore.add(" &7> &e" + included.size() + " &7permissions");
                included.removeAll(this.rank.getPermissions());
                lore.add(" &7> &e" + included.size() + " &7permissions NOT in " + this.rank.getRankId() + "'s permission list");
                lore.add("&eLeft-Click &7to view permissions");
            }
            lore.add("&eShift-Right-Click &7to inherit this rank's permissions");

            addElement(
                    new ItemBuilder(Material.NAME_TAG)
                            .displayName(ChatColor.WHITE + rank.getRankId())
                            .lore(lore).glow(inherited.contains(rank.getRankId())).getItem(),
                    new Button((slot, type) -> {
                        if(recalculatingPermissions) {
                            player.sendMessage(ChatColor.RED + "Currently recalculating permissions, please wait...");
                            return;
                        }

                        if(type == ClickType.LEFT) {
                            new PermissionListGui(guiHandler, player, rank).openGui();
                            return;
                        }

                        if(!inherited.remove(rank.getRankId()))
                            inherited.add(rank.getRankId());

                        recalculatingPermissions = true;
                        rankManager.recalculatePermissions(() -> {
                            recalculatingPermissions = false;
                            rebuildInventory();
                        });
                    })
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

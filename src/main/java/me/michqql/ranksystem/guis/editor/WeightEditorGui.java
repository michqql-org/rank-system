package me.michqql.ranksystem.guis.editor;

import me.michqql.ranksystem.ranks.Rank;
import me.michqql.servercoreutils.gui.Gui;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class WeightEditorGui extends Gui {

    private final static int BACK = 9;
    private final static int DECREASE_WEIGHT_TEN = 11;
    private final static int DECREASE_WEIGHT_ONE = 12;
    private final static int WEIGHT = 13;
    private final static int INCREASE_WEIGHT_ONE = 14;
    private final static int INCREASE_WEIGHT_TEN = 15;
    private final static int RESET = 17;

    private final Rank rank;
    private final int initialWeight;

    public WeightEditorGui(GuiHandler guiHandler, Player player, Rank rank) {
        super(guiHandler, player);
        this.rank = rank;
        this.initialWeight = rank.getWeight();
        build("&2Editing &f" + rank.getRankId() + "'s &2weight", 3);
    }

    @Override
    protected void createInventory() {
        this.inventory.setItem(BACK, new ItemBuilder(Material.RED_BED)
                .displayName("&c<-- Back").getItem());

        this.inventory.setItem(DECREASE_WEIGHT_TEN, new ItemBuilder(Material.CRIMSON_BUTTON)
                .displayName("&cDecrease weight by 10").amount(10).getItem());
        this.inventory.setItem(DECREASE_WEIGHT_ONE, new ItemBuilder(Material.CRIMSON_BUTTON)
                .displayName("&cDecrease weight by 1").getItem());

        this.inventory.setItem(INCREASE_WEIGHT_ONE, new ItemBuilder(Material.WARPED_BUTTON)
                .displayName("&aIncrease weight by 1").getItem());
        this.inventory.setItem(INCREASE_WEIGHT_TEN, new ItemBuilder(Material.WARPED_BUTTON)
                .displayName("&aIncrease weight by 10").amount(10).getItem());

        this.inventory.setItem(RESET, new ItemBuilder(Material.BARRIER)
                .displayName("&4Reset to " + initialWeight).getItem());

        updateInventory();
    }

    @Override
    protected void updateInventory() {
        this.inventory.setItem(WEIGHT, new ItemBuilder(getMaterialByWeight(rank.getWeight()))
                .displayName("&7Weight: &e" + rank.getWeight()).getItem());
    }

    @Override
    protected void onClose() {

    }

    @Override
    protected boolean onClickEvent(int slot, ClickType type) {
        final int cw = rank.getWeight(); // Current weight
        if(slot == BACK) {
            guiHandler.openPreviousGui(player);
        } else if (slot == DECREASE_WEIGHT_TEN) {
            rank.setWeight(Math.max(0, cw - 10));
            playSound(cw, -10);
            updateInventory();
        } else if (slot == DECREASE_WEIGHT_ONE) {
            rank.setWeight(Math.max(0, cw - 1));
            playSound(cw, -1);
            updateInventory();
        } else if (slot == INCREASE_WEIGHT_ONE) {
            rank.setWeight(Math.min(100, cw + 1));
            playSound(cw, 1);
            updateInventory();
        } else if (slot == INCREASE_WEIGHT_TEN) {
            rank.setWeight(Math.min(100, cw + 10));
            playSound(cw, 10);
            updateInventory();
        } else if(slot == RESET) {
            rank.setWeight(initialWeight);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 1.0f);
            updateInventory();
        }
        return true;
    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int i, ClickType clickType) {
        return true;
    }

    private Material getMaterialByWeight(int weight) {
        if(weight < 20)
            return Material.FEATHER;
        else if (weight < 40)
            return Material.IRON_NUGGET;
        else if (weight < 60)
            return Material.IRON_INGOT;
        else if (weight < 80)
            return Material.IRON_BLOCK;
        else
            return Material.ANVIL;
    }

    private void playSound(int weight, int change) {
        if((weight <= 0 && change < 0) || (weight >= 100 && change > 0)) {
            // Cannot happen
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
        } else if((change < 0 && weight + change < 0) || (change > 0 && weight + change > 100)) {
            // Can only partially happen
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.5f, 1.0f);
        } else {
            // Can happen
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }
}

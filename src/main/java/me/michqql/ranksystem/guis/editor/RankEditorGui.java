package me.michqql.ranksystem.guis.editor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.servercoreutils.gui.Gui;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.item.ItemBuilder;
import me.michqql.servercoreutils.util.collections.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RankEditorGui extends Gui {

    private final static int BACK_SLOT = 0;
    private final static int WEIGHT_SLOT = 2;
    private final static int PREFIX_SUFFIX_SLOT = 3;
    private final static int NAME_COLOUR_SLOT = 4;
    private final static int CHAT_COLOUR_SLOT = 5;
    private final static int INHERIT_SLOT = 6;
    private final static int INHERITABLE_SLOT = 7;
    private final static int PERMISSIONS_SLOT = 8;

    private final Cache<UUID, Boolean> cooldown;

    private final RankManager rankManager;
    private final PlayerManager playerManager;
    private final Rank rank;

    public RankEditorGui(GuiHandler guiHandler, Player player, RankManager rankManager, PlayerManager playerManager, Rank rank) {
        super(guiHandler, player);
        this.rankManager = rankManager;
        this.cooldown = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

        this.playerManager = playerManager;
        this.rank = rank;
        build("&2Editing &f" + rank.getRankId(), 1);
    }

    @Override
    protected void createInventory() {
        this.inventory.setItem(BACK_SLOT, new ItemBuilder(Material.RED_BED)
                .displayName("&c<-- Back").getItem());

        updateInventory();
    }

    @Override
    protected void updateInventory() {
        this.inventory.setItem(WEIGHT_SLOT, new ItemBuilder(Material.ANVIL)
                .displayName("&7Weight: &e" + rank.getWeight())
                .lore("", "&7Click to edit!").getItem());

        this.inventory.setItem(PREFIX_SUFFIX_SLOT, new ItemBuilder(Material.NAME_TAG)
                .displayName("&7Prefix: &f" + rank.getPrefix() + " &7- Suffix: &f" + rank.getSuffix())
                .lore("", "&7Click to edit!").getItem());

        ChatColor colour = rank.getNameColour() != null ? rank.getNameColour().getKey() : ChatColor.WHITE;
        this.inventory.setItem(NAME_COLOUR_SLOT, new ItemBuilder(getMaterial(colour))
                .displayName("&7Name colour: " + nameColourToString() + player.getName())
                .lore("", "&7Click to edit!").getItem());

        colour = rank.getChatColour() != null ? rank.getChatColour().getKey() : ChatColor.WHITE;
        this.inventory.setItem(CHAT_COLOUR_SLOT, new ItemBuilder(getMaterial(colour))
                .displayName("&7Chat colour: " + chatColourToString() + "Welcome to the server " + player.getName())
                .lore("", "&7Click to edit!").getItem());

        this.inventory.setItem(INHERIT_SLOT, new ItemBuilder(getBooleanMaterial(rank.inheritBelow()))
                .displayName("&7Inherit")
                .lore(
                        "&7&o(Should this rank inherit",
                        "&7&opermissions from lower ranks?)",
                        "", "&7Click to toggle!"
                ).getItem());

        this.inventory.setItem(INHERITABLE_SLOT, new ItemBuilder(getBooleanMaterial(rank.isInheritable()))
                .displayName("&7Inheritable")
                .lore(
                        "&7&o(Is this rank's permissions",
                        "&7&oinheritable by higher ranks?)",
                        "", "&7Click to toggle!"
                ).getItem());

        this.inventory.setItem(PERMISSIONS_SLOT, new ItemBuilder(Material.BOOK)
                .displayName("&7Permissions: &e" + rank.getPermissions().size())
                .lore("", "&7Click to add/remove permissions", "&7and inherit permissions from other ranks").getItem());
    }

    @Override
    protected void onClose() {

    }

    @Override
    protected boolean onClickEvent(int slot, ClickType type) {
        if(slot == BACK_SLOT) {
            guiHandler.openPreviousGui(player);
            return true;
        }

        switch (slot) {
            case WEIGHT_SLOT -> new WeightEditorGui(guiHandler, player, rank).openGui();
            case PREFIX_SUFFIX_SLOT -> new PrefixSuffixEditorGui(guiHandler, player, rank).openGui();
            case NAME_COLOUR_SLOT -> new NameColourEditorGui(guiHandler, player, playerManager, rank).openGui();
            case CHAT_COLOUR_SLOT -> new ChatColourEditorGui(guiHandler, player, playerManager, rank).openGui();
            case INHERIT_SLOT -> {
                if(cooldown.getIfPresent(player.getUniqueId()) != null) // player is on cooldown
                    return true;

                boolean value = !rank.shouldInheritPermissionsFromLowerRanks();
                rank.setShouldInheritPermissionsFromLowerRanks(value);
                cooldown.put(player.getUniqueId(), true);
                updateInventory();
                float pitch = value ? 0.4f : 1.6f;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.3f, pitch);
            }
            case INHERITABLE_SLOT -> {
                if(cooldown.getIfPresent(player.getUniqueId()) != null) // player is on cooldown
                    return true;

                boolean value = !rank.isInheritable();
                rank.setInheritable(value);
                cooldown.put(player.getUniqueId(), true);
                updateInventory();
                float pitch = value ? 0.4f : 1.6f;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.3f, pitch);
            }
            case PERMISSIONS_SLOT -> new PermissionsEditorGui(guiHandler, player, rankManager, rank).openGui();
        }
        return true;
    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int slot, ClickType type) {
        return true;
    }

    private String nameColourToString() {
        Pair<ChatColor, Set<ChatColor>> nc = rank.getNameColour();
        if(nc == null)
            return "";

        StringBuilder builder = new StringBuilder();
        builder.append('&').append(nc.getKey().getChar());
        for(ChatColor colour : nc.getValue()) {
            builder.append('&').append(colour.getChar());
        }
        return builder.toString();
    }

    private String chatColourToString() {
        Pair<ChatColor, Set<ChatColor>> cc = rank.getChatColour();
        if(cc == null)
            return "";

        StringBuilder builder = new StringBuilder();
        builder.append('&').append(cc.getKey().getChar());
        for(ChatColor colour : cc.getValue()) {
            builder.append('&').append(colour.getChar());
        }
        return builder.toString();
    }

    private Material getMaterial(ChatColor colour) {
        return NameColourEditorGui.COLOUR_TO_MATERIAL.getOrDefault(colour, Material.WHITE_WOOL);
    }

    private Material getBooleanMaterial(boolean b) {
        return b ? Material.LIME_DYE : Material.GRAY_DYE;
    }
}

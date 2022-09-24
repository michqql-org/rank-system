package me.michqql.ranksystem.guis;

import me.michqql.ranksystem.guis.editor.RankEditorGui;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.gui.type.PagedGui;
import me.michqql.servercoreutils.item.ItemBuilder;
import me.michqql.servercoreutils.util.collections.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class RanksGui extends PagedGui {

    private final RankManager rankManager;
    private final PlayerManager playerManager;

    public RanksGui(GuiHandler guiHandler, Player player, RankManager rankManager, PlayerManager playerManager) {
        super(guiHandler, player, PagedGui.getMaxSize());
        this.rankManager = rankManager;
        this.playerManager = playerManager;

        build("&2Ranks", 6);
    }

    @Override
    protected void createInventory() {
        List<Rank> ranks = rankManager.getRanks();
        ranks.sort((r1, r2) -> r2.getWeight() - r1.getWeight()); // Sort the ranks into descending weight order
        ranks.sort(Comparator.comparingInt(Rank::getWeight));

        for(Rank rank : ranks) {
            List<String> lore = new ArrayList<>();
            lore.add("&7Weight: &f" + rank.getWeight());
            lore.add("&7Prefix: &f" + rank.getPrefix());
            lore.add("&7Suffix: &f" + rank.getSuffix());
            lore.add("&7Name colour: " + getColour(rank.getNameColour()) + getColourString(rank.getNameColour()));
            lore.add("&7Chat colour: " + getColour(rank.getChatColour()) + getColourString(rank.getChatColour()));
            lore.add("");
            lore.add("&7Inherit permissions from lower ranks:");
            lore.add(" &7> " + booleanChatColour(rank.inheritBelow()) + rank.inheritBelow());
            lore.add("&7Inheritable by higher ranks:");
            lore.add(" &7> " + booleanChatColour(rank.isInheritable()) + rank.isInheritable());
            lore.add("&7Always inherit these ranks:");
            List<String> inherit = rank.getInheritedRanks();
            if(inherit == null || inherit.isEmpty()) {
                lore.add(" &7> &cNone");
            } else {
                lore.add(" &7> &e" + inherit.size() + " ranks");
            }
            lore.add("&7Permissions:");
            Set<String> included = rank.getPermissions();
            if(included == null || included.isEmpty()) {
                lore.add(" &7> &cNone");
            } else {
                lore.add(" &7> &e" + included.size() + " permissions");
            }

            addElement(
                    new ItemBuilder(Material.NAME_TAG)
                            .displayName(ChatColor.WHITE + rank.getRankId())
                            .lore(lore).getItem(),
                    new Button((slot, type) ->
                            new RankEditorGui(guiHandler, player, rankManager, playerManager, rank).openGui())
            );
        }

        // Call to display the page
        updateInventory();
    }

    @Override
    protected void onClose() {

    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int slot, ClickType type) {
        return true;
    }

    private ChatColor booleanChatColour(boolean b) {
        return b ? ChatColor.GREEN : ChatColor.RED;
    }

    public String getColour(Pair<ChatColor, Set<ChatColor>> pair) {
        if(pair == null)
            return "";

        StringBuilder builder = new StringBuilder();
        builder.append('&').append(pair.getKey().getChar());
        for(ChatColor colour : pair.getValue()) {
            builder.append('&').append(colour.getChar());
        }
        return builder.toString();
    }

    public String getColourString(Pair<ChatColor, Set<ChatColor>> pair) {
        if(pair == null)
            return "";

        StringBuilder builder = new StringBuilder();
        builder.append(pair.getKey().getChar());

        Iterator<ChatColor> iterator = pair.getValue().iterator();
        if(iterator.hasNext())
            builder.append(", ");

        while(iterator.hasNext()) {
            ChatColor colour = iterator.next();
            builder.append(colour.getChar());
            if(iterator.hasNext())
                builder.append(", ");
        }
        return builder.toString();
    }
}

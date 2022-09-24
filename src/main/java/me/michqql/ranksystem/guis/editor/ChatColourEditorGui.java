package me.michqql.ranksystem.guis.editor;

import me.michqql.ranksystem.players.PlayerData;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.PlayerRank;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.servercoreutils.gui.Gui;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.item.ItemBuilder;
import me.michqql.servercoreutils.util.collections.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class ChatColourEditorGui extends Gui {

    private static final int BACK_SLOT = 0;
    private static final int[] COLOUR_SLOTS = {
            9, 10, 11, 12,
            18, 19, 20, 21,
            27, 28, 29, 30,
            36, 37, 38, 39
    };
    private static final int PREVIEW_SLOT = 22;
    private static final int[] FORMAT_SLOTS = {
            15, 16,
            24, 25,
            33
    };

    private static final Random RANDOM = new Random();
    private static final String[] PREVIEW_MESSAGES = {
            "I love minecraft so much!",
            "Hope you are enjoying this plugin :)",
            "RankSystemPlugin sys32 err: log.txt err",
            "Shld I dd mr ff ths fr vry pdt?",
            "I play minecraft every day! I play minecraftttt!",
            "Cats are top tier animals",
            "I have a cat named penguin",
            "Do you believe whether aliens are real?",
    };
    private static final List<ChatColor> CHAT_COLOUR_LIST = new ArrayList<>() {{
        add(ChatColor.BLACK);
        add(ChatColor.DARK_BLUE);
        add(ChatColor.DARK_GREEN);
        add(ChatColor.DARK_AQUA);
        add(ChatColor.DARK_RED);
        add(ChatColor.DARK_PURPLE);
        add(ChatColor.GOLD);
        add(ChatColor.GRAY);
        add(ChatColor.DARK_GRAY);
        add(ChatColor.BLUE);
        add(ChatColor.GREEN);
        add(ChatColor.AQUA);
        add(ChatColor.RED);
        add(ChatColor.LIGHT_PURPLE);
        add(ChatColor.YELLOW);
        add(ChatColor.WHITE);
    }};
    public static final HashMap<ChatColor, Material> COLOUR_TO_MATERIAL = new HashMap<>() {{
        put(ChatColor.BLACK, Material.BLACK_WOOL);
        put(ChatColor.DARK_BLUE, Material.BLUE_WOOL);
        put(ChatColor.DARK_GREEN, Material.GREEN_WOOL);
        put(ChatColor.DARK_AQUA, Material.CYAN_WOOL);
        put(ChatColor.DARK_RED, Material.RED_CONCRETE);
        put(ChatColor.DARK_PURPLE, Material.PURPLE_WOOL);
        put(ChatColor.GOLD, Material.ORANGE_WOOL);
        put(ChatColor.GRAY, Material.LIGHT_GRAY_WOOL);
        put(ChatColor.DARK_GRAY, Material.GRAY_WOOL);
        put(ChatColor.BLUE, Material.BLUE_CONCRETE);
        put(ChatColor.GREEN, Material.LIME_WOOL);
        put(ChatColor.AQUA, Material.LIGHT_BLUE_WOOL);
        put(ChatColor.RED, Material.RED_WOOL);
        put(ChatColor.LIGHT_PURPLE, Material.MAGENTA_WOOL);
        put(ChatColor.YELLOW, Material.YELLOW_WOOL);
        put(ChatColor.WHITE, Material.WHITE_WOOL);
    }};
    private static final List<ChatColor> FORMATS_LIST = new ArrayList<>() {{
        add(ChatColor.MAGIC);
        add(ChatColor.BOLD);
        add(ChatColor.STRIKETHROUGH);
        add(ChatColor.UNDERLINE);
        add(ChatColor.ITALIC);
    }};
    private static final HashMap<ChatColor, Material> FORMAT_TO_MATERIAL = new HashMap<>() {{
        put(ChatColor.MAGIC, Material.CAULDRON);
        put(ChatColor.BOLD, Material.IRON_BLOCK);
        put(ChatColor.STRIKETHROUGH, Material.STICK);
        put(ChatColor.UNDERLINE, Material.REDSTONE);
        put(ChatColor.ITALIC, Material.STRING);
    }};

    /* ChatColor has:
       - 16 colours
       - 5 formats
     */

    private final PlayerManager playerManager;
    private final Pair<ChatColor, Set<ChatColor>> colours;

    // Random message index
    private final int randomMessageIndex = RANDOM.nextInt(PREVIEW_MESSAGES.length);

    public ChatColourEditorGui(GuiHandler guiHandler, Player player, PlayerManager playerManager, Rank rank) {
        super(guiHandler, player);
        this.playerManager = playerManager;
        if(rank.getChatColour() == null)
            rank.setChatColour(new Pair<>(ChatColor.RESET, new HashSet<>()));
        this.colours = rank.getChatColour();
        build("&2Editing &f" + rank.getRankId() + "'s &2chat colour", 6);
    }

    @Override
    protected void createInventory() {
        this.inventory.setItem(BACK_SLOT, new ItemBuilder(Material.RED_BED)
                .displayName("&c<-- Back").getItem());

        updateInventory();
    }

    @Override
    protected void updateInventory() {
        // Preview
        this.inventory.setItem(PREVIEW_SLOT, new ItemBuilder(Material.OAK_SIGN)
                .displayName("&7Preview")
                .lore(
                        "",
                        createPreviewLine(),
                        ""
                ).getItem());

        // Colours
        for(int i = 0; i < COLOUR_SLOTS.length; i++) {
            if(i >= CHAT_COLOUR_LIST.size())
                break;

            int slot = COLOUR_SLOTS[i];
            ChatColor colour = CHAT_COLOUR_LIST.get(i);

            this.inventory.setItem(slot, new ItemBuilder(COLOUR_TO_MATERIAL.getOrDefault(colour, Material.WHITE_WOOL))
                    .displayName("&" + colour.getChar() + colour)
                    .glow(colour.equals(colours.getKey()))
                    .getItem());
        }

        // Formats
        for(int i = 0; i < FORMAT_SLOTS.length; i++) {
            if(i >= FORMATS_LIST.size())
                break;

            int slot = FORMAT_SLOTS[i];
            ChatColor format = FORMATS_LIST.get(i);

            this.inventory.setItem(slot, new ItemBuilder(FORMAT_TO_MATERIAL.getOrDefault(format, Material.WHITE_WOOL))
                    .displayName("&" + format.getChar() + format)
                    .glow(colours.getValue().contains(format))
                    .getItem());
        }
    }

    @Override
    protected void onClose() {

    }

    @Override
    protected boolean onClickEvent(int slot, ClickType clickType) {
        if(slot == BACK_SLOT) {
            guiHandler.openPreviousGui(player);
            return true;
        }

        int index = getIndex(COLOUR_SLOTS, slot);
        if(index >= 0) {
            ChatColor colour = CHAT_COLOUR_LIST.get(index);
            colours.setKey(colour);
            updateInventory();
            return true;
        }

        index = getIndex(FORMAT_SLOTS, slot);
        if(index >= 0) {
            ChatColor format = FORMATS_LIST.get(index);
            if(!colours.getValue().remove(format))
                colours.getValue().add(format);
            updateInventory();
            return true;
        }
        return true;
    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int i, ClickType clickType) {
        return true;
    }

    private int getIndex(int[] arr, int val) {
        for(int i = 0; i < arr.length; i++)
            if(arr[i] == val)
                return i;
        return -1;
    }

    private String createPreviewLine() {
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        PlayerRank pr = data.getProminentRankOrHighest();

        String prefix = pr != null ? pr.getPrefix() : "";
        String suffix = pr != null ? pr.getSuffix() : "";
        String name = pr != null ? pr.getNameColour() : "&f";
        String message = PREVIEW_MESSAGES[randomMessageIndex];

        return prefix + " &f" + name +  player.getName() + "&f " + suffix + " " + setToString() + message;
    }

    private String setToString() {
        StringBuilder builder = new StringBuilder();
        builder.append('&').append(colours.getKey().getChar());
        for(ChatColor colour : colours.getValue()) {
            builder.append('&').append(colour.getChar());
        }
        return builder.toString();
    }
}

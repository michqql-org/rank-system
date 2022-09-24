package me.michqql.ranksystem.guis.editor;

import me.michqql.ranksystem.ranks.Rank;
import me.michqql.servercoreutils.gui.Gui;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrefixSuffixEditorGui extends Gui {

    private final static int BACK = 9;
    private final static int PREFIX = 12;
    private final static int SUFFIX = 14;

    private final Rank rank;

    private final StringPrompt prefixPrompt;
    private final StringPrompt suffixPrompt;

    public PrefixSuffixEditorGui(GuiHandler guiHandler, Player player, Rank rank) {
        super(guiHandler, player);
        this.rank = rank;

        this.prefixPrompt = new StringPrompt() {
            @NotNull
            @Override
            public String getPromptText(@NotNull ConversationContext context) {
                return ChatColor.GRAY + "Type " + ChatColor.RED + "\"-stop\"" + ChatColor.GRAY + " to cancel";
            }

            @Nullable
            @Override
            public Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                rank.setPrefix(input);
                guiHandler.openSavedGuis(player);
                return null;
            }
        };

        this.suffixPrompt = new StringPrompt() {
            @NotNull
            @Override
            public String getPromptText(@NotNull ConversationContext context) {
                return ChatColor.GRAY + "Type " + ChatColor.RED + "\"-stop\"" + ChatColor.GRAY + " to cancel";
            }

            @Nullable
            @Override
            public Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                rank.setSuffix(input);
                guiHandler.openSavedGuis(player);
                return null;
            }
        };

        build("&2Editing &f" + rank.getRankId() + "'s &2prefix & suffix", 3);
    }

    @Override
    protected void createInventory() {
        this.inventory.setItem(BACK, new ItemBuilder(Material.RED_BED)
                .displayName("&c<-- Back").getItem());

        updateInventory();
    }

    @Override
    protected void updateInventory() {
        this.inventory.setItem(PREFIX, new ItemBuilder(Material.NAME_TAG)
                .displayName("&7Prefix: &f" + rank.getPrefix())
                .lore(
                        "&eLeft-Click &7to type a new prefix in chat",
                        "&eShift-Right-Click &7to&c reset &7the prefix"
                ).getItem());
        this.inventory.setItem(SUFFIX, new ItemBuilder(Material.NAME_TAG)
                .displayName("&7Suffix: &f" + rank.getSuffix())
                .lore(
                        "&eLeft-Click &7to type a new prefix in chat",
                        "&eShift-Right-Click &7to&c reset &7the suffix"
                ).getItem());
    }

    @Override
    protected void onClose() {}

    @Override
    protected boolean onClickEvent(int slot, ClickType type) {
        if(slot == BACK) {
            guiHandler.openPreviousGui(player);
            return true;
        } else if(slot == PREFIX) {
            if(type == ClickType.SHIFT_RIGHT) {
                rank.setPrefix("");
                updateInventory();
                return true;
            }

            player.sendMessage(ChatColor.GRAY + "Please enter a new prefix for rank: "
                    + ChatColor.WHITE + rank.getRankId());

            Conversation conversation = new ConversationFactory(plugin)
                    .withEscapeSequence("-stop")
                    .addConversationAbandonedListener(abandonedEvent -> {
                        if(!abandonedEvent.gracefulExit()) {
                            player.sendMessage(ChatColor.RED + "Prefix input cancelled");
                            guiHandler.openSavedGuis(player);
                        }
                    }).withLocalEcho(false)
                    .withFirstPrompt(prefixPrompt)
                    .buildConversation(player);

            guiHandler.saveGuisAndClose(player);
            conversation.begin();
        } else if(slot == SUFFIX) {
            if(type == ClickType.SHIFT_RIGHT) {
                rank.setSuffix("");
                updateInventory();
                return true;
            }

            player.sendMessage(ChatColor.GRAY + "Please enter a new suffix for rank: "
                    + ChatColor.WHITE + rank.getRankId());

            Conversation conversation = new ConversationFactory(plugin)
                    .withEscapeSequence("-stop")
                    .addConversationAbandonedListener(abandonedEvent -> {
                        if(!abandonedEvent.gracefulExit()) {
                            player.sendMessage(ChatColor.RED + "Suffix input cancelled");
                            guiHandler.openSavedGuis(player);
                        }
                    }).withLocalEcho(false)
                    .withFirstPrompt(suffixPrompt)
                    .buildConversation(player);

            guiHandler.saveGuisAndClose(player);
            conversation.begin();
        }
        return true;
    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int i, ClickType clickType) {
        return true;
    }
}

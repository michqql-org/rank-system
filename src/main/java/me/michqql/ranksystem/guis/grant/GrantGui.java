package me.michqql.ranksystem.guis.grant;

import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.players.PlayerData;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.PlayerRank;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.servercoreutils.gui.Gui;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.item.ItemBuilder;
import me.michqql.servercoreutils.util.OfflineUUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GrantGui extends Gui {

    private static final int ERROR_SLOT = 22;
    private static final int LEFT_SLOT = 45;
    private static final int RIGHT_SLOT = 53;
    private static final int RANKS_PER_PAGE = 9 * 5;

    private final PlayerManager playerManager;
    private final PlayerData data;
    private final boolean hasPermission;

    private final boolean permanent;
    private final long tempTime;
    private final String durationString;

    // Pages
    private final List<Rank> ranks;
    private int pageNumber;
    private boolean requiresPages;

    public GrantGui(GuiHandler guiHandler, Player player,
                    PlayerManager playerManager, RankManager rankManager, PlayerData data,
                    boolean permanent, long tempTime) {
        super(guiHandler, player);
        this.playerManager = playerManager;
        this.data = data;
        this.hasPermission = player.hasPermission(RankSystemPlugin.GRANT_RANK_PERMISSION);
        String playerName = OfflineUUID.getName(data.getUuid());
        this.permanent = permanent;
        this.tempTime = tempTime;

        if(permanent) {
            this.durationString = "forever";
        } else {
            Duration duration = Duration.ofMillis(tempTime);
            int days = (int) duration.toDaysPart();
            duration = duration.minusDays(days);
            double hours = duration.toHours() / 24D;
            double time = days + hours;
            this.durationString = time + " " + (time == 1.0D ? "day" : "days");
        }

        this.ranks = rankManager.getRanks();
        ranks.sort((r1, r2) -> r2.getWeight() - r1.getWeight()); // Descending order

        build("&2Grant &f" + playerName, 6);
    }

    @Override
    protected void createInventory() {
        updateInventory();
    }

    @Override
    protected void updateInventory() {
        if(!hasPermission) {
            this.inventory.setItem(ERROR_SLOT, new ItemBuilder(Material.BARRIER)
                    .displayName("&4No permission")
                    .lore("&cYou do not have permission to grant ranks!").getItem());
            return;
        }

        requiresPages = ranks.size() >= RANKS_PER_PAGE;
        if(requiresPages) {
            if(pageNumber > 0) {
                this.inventory.setItem(LEFT_SLOT, new ItemBuilder(Material.ARROW)
                        .displayName("&7Page Left").getItem());
            }

            int requiredPages = (int) Math.ceil((double) ranks.size() / RANKS_PER_PAGE);
            if(pageNumber < requiredPages - 1) {
                this.inventory.setItem(RIGHT_SLOT, new ItemBuilder(Material.ARROW)
                        .displayName("&7Page Right").getItem());
            }
        }

        for(int i = 0; i < RANKS_PER_PAGE; i++) {
            int index = pageNumber * RANKS_PER_PAGE + i;
            if(index >= ranks.size())
                break;

            Rank rank = ranks.get(index);
            boolean hasRank = data.hasRank(rank);

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
            }
            lore.add("");
            if(hasRank) {
                lore.add("&eClick &7to&c permanently remove &7this rank");
            } else {
                lore.add("&eClick &7to&a grant &7this rank for &e" + durationString);
            }

            this.inventory.setItem(i, new ItemBuilder(getMaterialByBoolean(hasRank))
                    .displayName(getColourByBoolean(hasRank) + rank.getRankId())
                    .lore(lore)
                    .glow(hasRank)
                    .getItem());
        }
    }

    @Override
    protected void onClose() {
        playerManager.savePlayerData(data);
    }

    @Override
    protected boolean onClickEvent(int slot, ClickType clickType) {
        if(!hasPermission)
            return true;

        if(requiresPages) {
            if(slot == LEFT_SLOT && pageNumber > 0) {
                pageNumber--;
                updateInventory();
                return true;
            }

            int requiredPages = (int) Math.ceil((double) ranks.size() / RANKS_PER_PAGE);
            if(slot == RIGHT_SLOT && pageNumber < requiredPages - 1) {
                pageNumber++;
                updateInventory();
                return true;
            }
        }

        if(slot >= RANKS_PER_PAGE)
            return true;

        int index = pageNumber * RANKS_PER_PAGE + slot;
        if(index >= ranks.size())
            return true;

        Rank rank = ranks.get(index);
        PlayerRank pr = data.getPlayerRank(rank);
        if(pr != null) {
            // Remove this rank
            data.removePlayerRank(pr);
            updateInventory();
            return true;
        }

        // Otherwise, add the rank
        long expiry = permanent ? -1 : System.currentTimeMillis() + tempTime;
        pr = new PlayerRank(rank, !permanent, expiry);
        data.addPlayerRank(pr);
        updateInventory();
        return true;
    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int i, ClickType clickType) {
        return true;
    }

    private Material getMaterialByBoolean(boolean b) {
        return b ? Material.LIME_DYE : Material.GRAY_DYE;
    }

    private ChatColor getColourByBoolean(boolean b) {
        return b ? ChatColor.GREEN : ChatColor.RED;
    }
}

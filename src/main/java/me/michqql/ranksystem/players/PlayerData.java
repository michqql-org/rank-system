package me.michqql.ranksystem.players;

import me.michqql.ranksystem.ranks.PlayerRank;
import me.michqql.ranksystem.ranks.Rank;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private List<PlayerRank> playerRanks = new ArrayList<>();
    private PlayerRank prominentRank;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPrefix() {
        PlayerRank rank = getProminentRankOrHighest();
        return rank != null ? rank.getPrefix() : "";
    }

    public String getSuffix() {
        PlayerRank rank = getProminentRankOrHighest();
        return rank != null ? rank.getSuffix() : "";
    }

    public List<PlayerRank> getHighestRanks() {
        // Determine highest weighing rank
        int highest = Integer.MIN_VALUE;
        for(PlayerRank pr : playerRanks) {
            if(pr.getRank().getWeight() > highest)
                highest = pr.getRank().getWeight();
        }

        List<PlayerRank> result = new ArrayList<>();
        for(PlayerRank pr : playerRanks) {
            if(pr.getRank().getWeight() >= highest)
                result.add(pr);
        }

        return result;
    }

    /**
     * Sorts the ranks from highest to lowest and returns this in a linked list to maintain order
     * @return an ordered descending list of ranks
     */
    public LinkedList<PlayerRank> getOrderedRanks() {
        LinkedList<PlayerRank> result = new LinkedList<>();

        // Sort the ranks
        playerRanks.stream()
                .sorted((o1, o2) -> o2.getRank().getWeight() - o1.getRank().getWeight())
                .forEach(result::addLast);

        return result;
    }

    public boolean hasRank(Rank rank) {
        return getPlayerRank(rank) != null;
    }

    public PlayerRank getPlayerRank(Rank rank) {
        for(PlayerRank pr : playerRanks) {
            if(pr.getRank().equals(rank))
                return pr;
        }

        return null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public List<PlayerRank> getPlayerRanks() {
        return playerRanks;
    }

    public void setPlayerRanks(List<PlayerRank> playerRanks) {
        this.playerRanks = playerRanks;
    }

    public void addPlayerRank(PlayerRank rank) {
        this.playerRanks.add(rank);
    }

    public void removePlayerRank(PlayerRank rank) {
        this.playerRanks.remove(rank);
    }

    public PlayerRank getProminentRank() {
        return prominentRank;
    }

    public PlayerRank getProminentRankOrHighest() {
        if(prominentRank != null)
            return prominentRank;

        List<PlayerRank> ranks = getHighestRanks();
        if(ranks.size() > 0)
            return ranks.get(0);

        return null;
    }

    public void setProminentRank(PlayerRank prominentRank) {
        this.prominentRank = prominentRank;
    }
}

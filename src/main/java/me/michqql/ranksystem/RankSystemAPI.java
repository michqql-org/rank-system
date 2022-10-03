package me.michqql.ranksystem;

import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.RankManager;

public class RankSystemAPI {

    private static RankManager rankManager;
    private static PlayerManager playerManager;

    static void setRankManager(RankManager rankManager) {
        RankSystemAPI.rankManager = rankManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    static void setPlayerManager(PlayerManager playerManager) {
        RankSystemAPI.playerManager = playerManager;
    }

    public static PlayerManager getPlayerManager() {
        return playerManager;
    }
}

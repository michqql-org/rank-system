package me.michqql.ranksystem.ranks.loader;

import me.michqql.ranksystem.ranks.Rank;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface RankLoader {

    void saveRank(Rank rank);

    void saveAllRanks(Collection<Rank> ranks);

    CompletableFuture<Rank> loadRank(String id);

    CompletableFuture<Collection<Rank>> loadAllRanks();
}

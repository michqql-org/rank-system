package me.michqql.ranksystem.ranks;

import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.Settings;
import me.michqql.ranksystem.ranks.loader.JsonRankLoader;
import me.michqql.ranksystem.ranks.loader.RankLoader;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RankManager {

    private final RankSystemPlugin plugin;

    private final HashMap<String, Rank> ranks = new HashMap<>();
    private final RankLoader loader;
    private final boolean debug;

    private Rank defaultRank;

    public RankManager(RankSystemPlugin plugin) {
        this.plugin = plugin;
        this.loader = new JsonRankLoader(plugin);
        this.debug = Settings.DEBUG.getBooleanValue();

        // Load all ranks and calculate their permissions
        init();
    }

    private void init() {
        CompletableFuture<Collection<Rank>> future = loader.loadAllRanks();
        future.thenAccept(loaded -> {
            // Firstly, add all ranks to the hashmap
            loaded.forEach(rank -> {
                Rank previous = ranks.putIfAbsent(rank.getRankId(), rank);
                if(previous != null) {
                    plugin.getLogger().severe("Two ranks with conflicting identifiers exist: " + rank.getRankId()
                            + " and " + previous.getRankId());
                }
            });

            // Secondly, calculate permissions
            inheritPermissions(ranks.values());

            // Thirdly, set the default rank
            String defaultRankId = Settings.DEFAULT_RANK.getString();
            if(!defaultRankId.isEmpty()) {
                Rank rank = ranks.get(defaultRankId);
                if(rank == null) {
                    plugin.getLogger().warning("Default rank id " + defaultRankId +
                            " in config.yml does not have an existing rank");
                }

                this.defaultRank = rank;
            }
        });
    }

    public void recalculatePermissions(Runnable runnable) {
        CompletableFuture.runAsync(() -> inheritPermissions(ranks.values()))
                .thenRun(runnable);
    }

    private void inheritPermissions(Collection<Rank> ranks) {
        if(debug) plugin.getLogger().info("Starting inherit permissions algorithm...");
        passes = 0;
        List<Rank> sorted = ranks.stream().sorted(Comparator.comparingInt(Rank::getWeight)).toList();

        Set<Rank> visited = new HashSet<>();
        for(Rank rank : ranks) {
            inherit(sorted, visited, rank, null);
        }
    }

    // Debug pass value
    private int passes = 0;

    private void inherit(List<Rank> sorted, Set<Rank> visited, Rank currentRank, Rank previousRank) {
        visited.add(currentRank);
        if(debug) {
            plugin.getLogger().info("Pass[" + passes + "]");
            passes++;
            plugin.getLogger().info("Visited: " + visited);
            plugin.getLogger().info("Current rank: " + currentRank);
            plugin.getLogger().info("Previous rank: " + previousRank);
        }

        for(String inheritRankId : currentRank.getInheritedRanks()) {
            if(debug) plugin.getLogger().info("   Inheriting rank: " + inheritRankId);
            Rank toInherit = ranks.get(inheritRankId);
            if(toInherit == null) {
                plugin.getLogger().warning("Rank " + currentRank.getRankId() + " tries to inherit permissions" +
                        " from unknown rank: " + inheritRankId);
                continue;
            }

            if(toInherit.equals(previousRank)) {
                plugin.getLogger().warning("Ranks " + previousRank.getRankId() + " and " + toInherit.getRankId() +
                        " inherit each other - creating an infinite loop, skipping inheritance");
                continue;
            }

            if(!visited.contains(toInherit)) {
                inherit(sorted, visited, toInherit, currentRank);
            }

            if(debug) plugin.getLogger().info("   Inheriting permissions from rank: " + inheritRankId);
            Set<String> newPerms = currentRank.getPermissions();
            newPerms.addAll(toInherit.getPermissions());
            currentRank.setPermissions(newPerms);
            if(debug) plugin.getLogger().info("   New permissions: " + currentRank);
        }

        if(currentRank.shouldInheritPermissionsFromLowerRanks()) {
            int index = sorted.indexOf(currentRank);
            for(int i = 0; i < index; i++) {
                Rank toInherit = sorted.get(i);
                if(debug) plugin.getLogger().info("   Inheriting rank (lower): " + toInherit.getRankId());

                if(!visited.contains(toInherit)) {
                    inherit(sorted, visited, toInherit, currentRank);
                }

                Set<String> newPerms = currentRank.getPermissions();
                newPerms.addAll(toInherit.getPermissions());
                currentRank.setPermissions(newPerms);
            }
        }
    }

    public void onDisable() {
        loader.saveAllRanks(ranks.values());
    }

    public List<Rank> getRanks() {
        return new ArrayList<>(ranks.values());
    }

    public Rank getRankById(String id) {
        return ranks.get(id);
    }

    public boolean doesRankExist(String id) {
        return getRankById(id) != null;
    }

    public boolean hasDefaultRank() {
        return defaultRank != null;
    }

    public Rank getDefaultRank() {
        return defaultRank;
    }

    public void createRank(String id) {
        ranks.computeIfAbsent(id, id1 -> {
            Rank rank = new Rank(id1);
            rank.giveDefaultValues();
            // Save the newly created rank
            loader.saveRank(rank);
            return rank;
        });
    }
}

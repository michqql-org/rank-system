package me.michqql.ranksystem.players.loader;

import me.michqql.ranksystem.players.PlayerData;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDataLoader {

    void savePlayerData(PlayerData playerData);

    void saveAllPlayerData(Collection<PlayerData> data);

    CompletableFuture<PlayerData> loadPlayerData(UUID uuid);

    CompletableFuture<Collection<PlayerData>> loadAllPlayerData();
}

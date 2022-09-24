package me.michqql.ranksystem.players.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.players.PlayerData;
import me.michqql.ranksystem.ranks.PlayerRank;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.servercoreutils.io.IO;
import me.michqql.servercoreutils.io.JsonFile;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class JsonPlayerDataLoader implements PlayerDataLoader {

    private final static String DIRECTORY = "player_data";

    private final RankSystemPlugin plugin;
    private final RankManager rankManager;

    public JsonPlayerDataLoader(RankSystemPlugin plugin, RankManager rankManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
    }

    @Override
    public void savePlayerData(PlayerData data) {
        CompletableFuture.runAsync(() -> save(data));
    }

    @Override
    public void saveAllPlayerData(Collection<PlayerData> data) {
        CompletableFuture.runAsync(() -> data.forEach(this::save));
    }

    @Override
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            JsonFile file = IO.getJsonFile(plugin, DIRECTORY + "/" + uuid.toString() + ".json");
            return load(file);
        });
    }

    @Override
    public CompletableFuture<Collection<PlayerData>> loadAllPlayerData() {
        return CompletableFuture.supplyAsync(() -> {
            List<File> dataFiles = IO.getFilesInDirectory(plugin.getDataFolder(), DIRECTORY);
            Collection<PlayerData> result = new ArrayList<>();

            for(File f : dataFiles) {
                JsonFile file = new JsonFile(plugin, f);
                PlayerData data = load(file);
                if(data != null)
                    result.add(data);
            }
            return result;
        });
    }

    private PlayerData load(JsonFile file) {
        JsonElement element = file.getElement();
        if(element == null || !element.isJsonObject()) {
            String uuidString = IO.stripExtension(file.getFile().getName());
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Could not load player data for user: " + uuidString);
                return null;
            }
            return new PlayerData(uuid);
        }

        JsonObject object = element.getAsJsonObject();

        String uuidString = object.get("uuid").getAsString();
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Could not load player data for user: " + uuidString);
            return null;
        }

        final PlayerData data = new PlayerData(uuid);

        JsonArray ranksArray = object.get("player_ranks").getAsJsonArray();
        List<PlayerRank> playerRanks = new ArrayList<>();
        for(JsonElement arrayElement : ranksArray) {
            if(!arrayElement.isJsonObject())
                continue;

            JsonObject rankObject = arrayElement.getAsJsonObject();

            String rankId = rankObject.get("rank_id").getAsString();
            Rank rank = rankManager.getRankById(rankId);
            if(rank == null) {
                plugin.getLogger().warning("Player '" + uuid + "' has invalid rank: " + rankId);
                continue;
            }

            boolean temp = rankObject.get("temporary").getAsBoolean();
            long expiryTimestamp = rankObject.get("expiry_timestamp").getAsLong();
            HashMap<String, String> prefixPlaceholders = new HashMap<>();
            HashMap<String, String> suffixPlaceholders = new HashMap<>();

            JsonObject prefixObject = rankObject.get("prefix_placeholders").getAsJsonObject();
            for(String key : prefixObject.keySet()) {
                String val = prefixObject.get(key).getAsString();
                prefixPlaceholders.put(key, val);
            }

            JsonObject suffixObject = rankObject.get("suffix_placeholders").getAsJsonObject();
            for(String key : suffixObject.keySet()) {
                String val = suffixObject.get(key).getAsString();
                suffixPlaceholders.put(key, val);
            }

            PlayerRank pr = new PlayerRank(rank, temp, expiryTimestamp);
            pr.setPrefixPlaceholders(prefixPlaceholders);
            pr.setSuffixPlaceholders(suffixPlaceholders);

            playerRanks.add(pr);
        }

        data.setPlayerRanks(playerRanks);

        if(object.has("prominent_rank")) {
            String prominentRankId = object.get("prominent_rank").getAsString();
            Rank prominentRank = rankManager.getRankById(prominentRankId);
            if(prominentRank == null) {
                plugin.getLogger().warning("Player '" + uuid + "' has invalid prominent rank: " + prominentRankId);
            } else {
                data.setProminentRank(data.getPlayerRank(prominentRank));
            }
        }

        return data;
    }

    private void save(PlayerData data) {
        JsonFile file = IO.getJsonFile(plugin, DIRECTORY + "/" + data.getUniqueId().toString() + ".json");

        JsonObject object = new JsonObject();
        file.setElement(object);

        object.addProperty("uuid", data.getUniqueId().toString());

        JsonArray ranksArray = new JsonArray();
        object.add("player_ranks", ranksArray);

        for(PlayerRank pr : data.getPlayerRanks()) {
            JsonObject rankObject = new JsonObject();
            ranksArray.add(rankObject);

            rankObject.addProperty("rank_id", pr.getRank().getRankId());
            rankObject.addProperty("temporary", pr.isTemporary());
            rankObject.addProperty("expiry_timestamp", pr.getTemporaryRankExpiryTimestamp());

            JsonObject prefixObject = new JsonObject();
            rankObject.add("prefix_placeholders", prefixObject);
            pr.getPrefixPlaceholders().forEach(prefixObject::addProperty);

            JsonObject suffixObject = new JsonObject();
            rankObject.add("suffix_placeholders", suffixObject);
            pr.getSuffixPlaceholders().forEach(suffixObject::addProperty);
        }

        if(data.getProminentRank() != null) {
            object.addProperty("prominent_rank", data.getProminentRank().getRank().getRankId());
        }

        file.save();
    }
}

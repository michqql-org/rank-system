package me.michqql.ranksystem.ranks.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.michqql.ranksystem.RankSystemPlugin;
import me.michqql.ranksystem.Settings;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.servercoreutils.io.IO;
import me.michqql.servercoreutils.io.JsonFile;
import me.michqql.servercoreutils.util.collections.Pair;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class JsonRankLoader implements RankLoader {

    private final static String DIRECTORY = "ranks";

    private final RankSystemPlugin plugin;

    public JsonRankLoader(RankSystemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void saveRank(Rank rank) {
        CompletableFuture.runAsync(() -> save(rank));
    }

    @Override
    public void saveAllRanks(Collection<Rank> ranks) {
        CompletableFuture.runAsync(() -> ranks.forEach(this::save));
    }

    @Override
    public CompletableFuture<Rank> loadRank(String id) {
        return CompletableFuture.supplyAsync(() -> {
            JsonFile file = IO.getJsonFile(plugin, DIRECTORY + "/" + id + ".json");
            return load(file);
        });
    }

    @Override
    public CompletableFuture<Collection<Rank>> loadAllRanks() {
        return CompletableFuture.supplyAsync(() -> {
            List<File> dataFiles = IO.getFilesInDirectory(plugin.getDataFolder(), DIRECTORY);
            Collection<Rank> result = new ArrayList<>();

            for(File f : dataFiles) {
                try {
                    JsonFile file = new JsonFile(plugin, f);
                    Rank rank = load(file);
                    if (rank != null)
                        result.add(rank);
                } catch (Exception e) {
                    if(Settings.DEBUG.getBooleanValue()) {
                        plugin.getLogger().severe("Failed to load rank from file: " + f.getName());
                        e.printStackTrace();
                    }
                }
            }
            return result;
        });
    }

    private Rank load(JsonFile file) {
        JsonElement element = file.getElement();
        if(element == null || !element.isJsonObject()) {
            plugin.getLogger().warning("Could not load rank from file: " + file.getFile().getName());
            return null;
        }

        JsonObject object = element.getAsJsonObject();
        String rankId = object.get("rank_id").getAsString();

        final Rank rank = new Rank(rankId);

        rank.setWeight(object.get("weight").getAsInt());
        rank.setPrefix(object.get("prefix").getAsString());
        rank.setSuffix(object.get("suffix").getAsString());

        { // Name colour
            JsonObject nameObject = object.get("name_colour").getAsJsonObject();
            Pair<ChatColor, Set<ChatColor>> nameColour = new Pair<>();

            char c = nameObject.get("colour").getAsString().charAt(0);
            nameColour.setKey(ChatColor.getByChar(c));

            JsonArray formatArray = nameObject.get("formats").getAsJsonArray();
            Set<ChatColor> formats = new HashSet<>();
            for(JsonElement nameElement : formatArray) {
                if(!(nameElement.isJsonPrimitive() && nameElement.getAsJsonPrimitive().isString()))
                    continue;

                char f = nameElement.getAsString().charAt(0);
                formats.add(ChatColor.getByChar(f));
            }
            nameColour.setValue(formats);
            rank.setNameColour(nameColour);
        }

        { // Chat colour
            JsonObject chatObject = object.get("chat_colour").getAsJsonObject();
            Pair<ChatColor, Set<ChatColor>> chatColour = new Pair<>();

            char c = chatObject.get("colour").getAsString().charAt(0);
            chatColour.setKey(ChatColor.getByChar(c));

            JsonArray formatArray = chatObject.get("formats").getAsJsonArray();
            Set<ChatColor> formats = new HashSet<>();
            for(JsonElement chatElement : formatArray) {
                if(!(chatElement.isJsonPrimitive() && chatElement.getAsJsonPrimitive().isString()))
                    continue;

                char f = chatElement.getAsString().charAt(0);
                formats.add(ChatColor.getByChar(f));
            }
            chatColour.setValue(formats);
            rank.setChatColour(chatColour);
        }

        rank.setShouldInheritPermissionsFromLowerRanks(object.get("inherit_below").getAsBoolean());
        rank.setInheritable(object.get("inheritable").getAsBoolean());

        JsonArray inheritArray = object.get("inherit").getAsJsonArray();
        List<String> inherit = new ArrayList<>();
        for(JsonElement arrayElement : inheritArray) {
            if(!(arrayElement.isJsonPrimitive() && arrayElement.getAsJsonPrimitive().isString()))
                continue;

            inherit.add(arrayElement.getAsString());
        }
        rank.setInheritRanks(inherit);

        JsonArray includeArray = object.get("included_permissions").getAsJsonArray();
        Set<String> include = new HashSet<>();
        for(JsonElement arrayElement : includeArray) {
            if(!(arrayElement.isJsonPrimitive() && arrayElement.getAsJsonPrimitive().isString()))
                continue;

            include.add(arrayElement.getAsString());
        }
        rank.setPermissions(include);

        return rank;
    }

    private void save(Rank rank) {
        JsonFile file = IO.getJsonFile(plugin, DIRECTORY + "/" + rank.getRankId() + ".json");

        JsonObject object = new JsonObject();
        file.setElement(object);

        object.addProperty("rank_id", rank.getRankId());
        object.addProperty("weight", rank.getWeight());
        object.addProperty("prefix", rank.getPrefix());
        object.addProperty("suffix", rank.getSuffix());

        { // Name colour
            Pair<ChatColor, Set<ChatColor>> nameColour = rank.getNameColour();
            JsonObject nameObject = new JsonObject();
            object.add("name_colour", nameObject);
            nameObject.addProperty("colour", nameColour.getKey().getChar());

            JsonArray formatArray = new JsonArray();
            nameObject.add("formats", formatArray);
            for (ChatColor colour : nameColour.getValue()) {
                formatArray.add(colour.getChar());
            }
        }

        { // Chat colour
            Pair<ChatColor, Set<ChatColor>> chatColour = rank.getChatColour();
            JsonObject chatObject = new JsonObject();
            object.add("chat_colour", chatObject);
            chatObject.addProperty("colour", chatColour.getKey().getChar());

            JsonArray formatArray = new JsonArray();
            chatObject.add("formats", formatArray);
            for (ChatColor colour : chatColour.getValue()) {
                formatArray.add(colour.getChar());
            }
        }

        object.addProperty("inherit_below", rank.shouldInheritPermissionsFromLowerRanks());
        object.addProperty("inheritable", rank.isInheritable());

        JsonArray inheritArray = new JsonArray();
        object.add("inherit", inheritArray);
        for(String rankId : rank.getInheritedRanks()) {
            inheritArray.add(rankId);
        }

        JsonArray includeArray = new JsonArray();
        object.add("included_permissions", includeArray);
        for(String permission : rank.getPermissions()) {
            includeArray.add(permission);
        }

        file.save();
    }
}

package me.michqql.ranksystem.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import me.michqql.ranksystem.util.Placeholders;
import me.michqql.servercoreutils.util.MessageHandler;
import me.michqql.servercoreutils.util.collections.Pair;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

public class RankPAPIExpansion extends PlaceholderExpansion {

    private final MessageHandler messageHandler;
    private final RankManager rankManager;

    public RankPAPIExpansion(MessageHandler messageHandler, RankManager rankManager) {
        this.messageHandler = messageHandler;
        this.rankManager = rankManager;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        params = params.toLowerCase(Locale.ENGLISH);
        String[] args = params.split("[.-]+");
        if(args.length == 0)
            return "";

        String rankId = args[0];
        Rank rank = rankManager.getRankById(rankId);
        if(rank == null) {
            String errorMessage = messageHandler.getMessage("placeholder-api-messages.rank-placeholder-error");
            return MessageHandler.replacePlaceholdersStatic(errorMessage, Placeholders.of("id", rankId));
        }

        if(args.length <= 1)
            return rank.getRankId();

        return switch (args[1]) {
            case "weight" -> String.valueOf(rank.getWeight());
            case "prefix" -> rank.getPrefix();
            case "suffix" -> rank.getSuffix();
            case "namecolour" -> getNameColour(rank);
            case "chatcolour" -> getChatColour(rank);
            case "inheritbelow" -> String.valueOf(rank.shouldInheritPermissionsFromLowerRanks());
            case "inheritable" -> String.valueOf(rank.isInheritable());
            case "inherit" -> String.valueOf(rank.getInheritedRanks());
            case "permissions", "included" -> String.valueOf(rank.getPermissions());
            default -> args[1];
        };
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rank";
    }

    @Override
    public @NotNull String getAuthor() {
        return "michqql";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    private String getNameColour(Rank rank) {
        if(rank.getNameColour() == null)
            return "";

        Pair<ChatColor, Set<ChatColor>> nameColour = rank.getNameColour();
        StringBuilder builder = new StringBuilder();
        builder.append('&').append(nameColour.getKey());
        for(ChatColor colour : nameColour.getValue()) {
            builder.append('&').append(colour.getChar());
        }
        return builder.toString();
    }

    private String getChatColour(Rank rank) {
        if(rank.getChatColour() == null)
            return "";

        Pair<ChatColor, Set<ChatColor>> chatColour = rank.getNameColour();
        StringBuilder builder = new StringBuilder();
        builder.append('&').append(chatColour.getKey());
        for(ChatColor colour : chatColour.getValue()) {
            builder.append('&').append(colour.getChar());
        }
        return builder.toString();
    }
}

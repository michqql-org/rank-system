package me.michqql.ranksystem.ranks;

import me.michqql.servercoreutils.util.collections.Pair;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerRank {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%[a-zA-Z.-]*%");

    private final Rank rank;

    // Temporary rank
    private final boolean temporary;
    private final long expiryTimestamp;

    // Prefix & Suffix placeholder data
    private HashMap<String, String> prefixPlaceholders = new HashMap<>();
    private HashMap<String, String> suffixPlaceholders = new HashMap<>();

    /**
     * Constructor for temporary ranks
     * @param rank the rank to give the player
     * @param isTemporary if the rank will expire
     * @param expiryTimestamp the timestamp at which the rank will expire
     */
    public PlayerRank(Rank rank, boolean isTemporary, long expiryTimestamp) {
        this.rank = rank;
        this.temporary = isTemporary;
        this.expiryTimestamp = isTemporary ? expiryTimestamp : -1;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public long getTemporaryRankExpiryTimestamp() {
        return expiryTimestamp;
    }

    public boolean hasExpired() {
        return temporary && System.currentTimeMillis() > expiryTimestamp;
    }

    public HashMap<String, String> getPrefixPlaceholders() {
        return prefixPlaceholders;
    }

    public void setPrefixPlaceholders(HashMap<String, String> prefixPlaceholders) {
        this.prefixPlaceholders = prefixPlaceholders;
    }

    public String getPrefix() {
        String prefix = rank.getPrefix();

        Matcher match = PLACEHOLDER_PATTERN.matcher(prefix);
        while(match.find()) {
            String placeholder = prefix.substring(match.start(), match.end());
            String placeholderInner = prefix.substring(match.start() + 1, match.end() - 1);

            String replacement = prefixPlaceholders.getOrDefault(placeholderInner, "");
            if(replacement == null)
                replacement = "";

            prefix = prefix.replace(placeholder, replacement);
            match = PLACEHOLDER_PATTERN.matcher(prefix);
        }

        return prefix;
    }

    public HashMap<String, String> getSuffixPlaceholders() {
        return suffixPlaceholders;
    }

    public void setSuffixPlaceholders(HashMap<String, String> suffixPlaceholders) {
        this.suffixPlaceholders = suffixPlaceholders;
    }

    public String getSuffix() {
        String suffix = rank.getSuffix();

        Matcher match = PLACEHOLDER_PATTERN.matcher(suffix);
        while(match.find()) {
            String placeholder = suffix.substring(match.start(), match.end());
            String placeholderInner = suffix.substring(match.start() + 1, match.end() - 1);

            String replacement = suffixPlaceholders.getOrDefault(placeholderInner, "");
            if(replacement == null)
                replacement = "";

            suffix = suffix.replace(placeholder, replacement);
            match = PLACEHOLDER_PATTERN.matcher(suffix);
        }

        return suffix;
    }
    
    public String getNameColour() {
        if(rank.getNameColour() == null)
            return "";

        Pair<ChatColor, Set<ChatColor>> nameColour = rank.getNameColour();
        StringBuilder builder = new StringBuilder();
        builder.append('&').append(nameColour.getKey().getChar());
        for(ChatColor colour : nameColour.getValue()) {
            builder.append('&').append(colour.getChar());
        }
        return builder.toString();
    }

    public String getChatColour() {
        if(rank.getChatColour() == null)
            return "";

        Pair<ChatColor, Set<ChatColor>> chatColour = rank.getNameColour();
        StringBuilder builder = new StringBuilder();
        builder.append('&').append(chatColour.getKey().getChar());
        for(ChatColor colour : chatColour.getValue()) {
            builder.append('&').append(colour.getChar());
        }
        return builder.toString();
    }
}

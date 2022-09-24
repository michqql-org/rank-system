package me.michqql.ranksystem.ranks;

import me.michqql.ranksystem.PermissionUtil;
import me.michqql.servercoreutils.util.collections.Pair;
import org.bukkit.ChatColor;

import java.util.*;

public class Rank {

    // Unique identifier
    private final String rankId;

    // Rank components
    private int weight;
    private String prefix;
    private String suffix;
    private Pair<ChatColor, Set<ChatColor>> nameColour;
    private Pair<ChatColor, Set<ChatColor>> chatColour;

    // Inheritance
    private boolean inheritBelow; // Should this rank inherit lower rank's permissions
    private boolean inheritable; // Can other ranks inherit this rank's permissions
    private List<String> inherit = new ArrayList<>(); // Specified list of ranks that should be inherited - weight disregarded

    // Permissions
    private Set<String> permissions = new HashSet<>();

    public Rank(String rankId) {
        this.rankId = rankId;
    }

    protected void giveDefaultValues() {
        this.weight = 0;
        this.prefix = "";
        this.suffix = "";
        this.nameColour = new Pair<>(ChatColor.RESET, new HashSet<>());
        this.chatColour = new Pair<>(ChatColor.RESET, new HashSet<>());
        this.inheritBelow = false;
        this.inheritable = false;
    }

    public boolean hasPermission(String permission) {
        return PermissionUtil.hasPermissionOrParentPermission(this, permission);
    }

    public boolean isHigherRanking(Rank other) {
        return weight > other.weight;
    }

    public boolean isHigherOrEqualRanking(Rank other) {
        return weight >= other.weight;
    }

    public boolean isLowerRanking(Rank other) {
        return weight < other.weight;
    }

    public boolean isLowerOrEqualRanking(Rank other) {
        return weight <= other.weight;
    }

    public String getRankId() {
        return rankId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Pair<ChatColor, Set<ChatColor>> getNameColour() {
        return nameColour;
    }

    public String getTranslatableNameColour() {
        if(nameColour == null)
            return "";

        StringBuilder builder = new StringBuilder();
        builder.append('&').append(nameColour.getKey().getChar());
        for(ChatColor colour : nameColour.getValue()) {
            builder.append('&').append(colour.getChar());
        }
        return builder.toString();
    }

    public String getReadableNameColour() {
        StringBuilder builder = new StringBuilder();
        builder.append(nameColour.getKey().getChar());

        Iterator<ChatColor> iterator = nameColour.getValue().iterator();
        if(iterator.hasNext())
            builder.append(", ");

        while(iterator.hasNext()) {
            ChatColor colour = iterator.next();
            builder.append(colour.getChar());
            if(iterator.hasNext())
                builder.append(", ");
        }
        return builder.toString();
    }

    public void setNameColour(Pair<ChatColor, Set<ChatColor>> nameColour) {
        this.nameColour = nameColour;
    }

    public Pair<ChatColor, Set<ChatColor>> getChatColour() {
        return chatColour;
    }

    public String getTranslatableChatColour() {
        if(chatColour == null)
            return "";

        StringBuilder builder = new StringBuilder();
        builder.append('&').append(chatColour.getKey().getChar());
        for(ChatColor colour : chatColour.getValue()) {
            builder.append('&').append(colour.getChar());
        }
        return builder.toString();
    }

    public String getReadableChatColour() {
        StringBuilder builder = new StringBuilder();
        builder.append(chatColour.getKey().getChar());

        Iterator<ChatColor> iterator = chatColour.getValue().iterator();
        if(iterator.hasNext())
            builder.append(", ");

        while(iterator.hasNext()) {
            ChatColor colour = iterator.next();
            builder.append(colour.getChar());
            if(iterator.hasNext())
                builder.append(", ");
        }
        return builder.toString();
    }

    public void setChatColour(Pair<ChatColor, Set<ChatColor>> chatColour) {
        this.chatColour = chatColour;
    }

    public boolean inheritBelow() {
        return inheritBelow;
    }

    public boolean shouldInheritPermissionsFromLowerRanks() {
        return inheritBelow;
    }

    public void setShouldInheritPermissionsFromLowerRanks(boolean inheritBelow) {
        this.inheritBelow = inheritBelow;
    }

    public boolean isInheritable() {
        return inheritable;
    }

    public void setInheritable(boolean inheritable) {
        this.inheritable = inheritable;
    }

    public List<String> getInheritedRanks() {
        return inherit;
    }

    public void setInheritRanks(List<String> inherit) {
        this.inherit = inherit;
    }

    public Set<String> getPermissions() {
        return new HashSet<>(permissions); // Copy of permissions set
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "Rank{" +
                "rankId='" + rankId + '\'' +
                ", weight=" + weight +
                ", permissions=" + permissions +
                '}';
    }
}

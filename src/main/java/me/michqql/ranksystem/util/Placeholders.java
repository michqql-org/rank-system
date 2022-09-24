package me.michqql.ranksystem.util;

import me.michqql.ranksystem.ranks.Rank;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Placeholders {

    public static Map<String, String> of(String key, String value) {
        return new HashMap<>() {{
            put(key, value);
        }};
    }

    public static Map<String, String> ofRank(Rank rank, String prefix) {
        if(rank == null)
            return Collections.emptyMap();
        return new HashMap<>() {{
            put(prefix + ".id", rank.getRankId());
            put(prefix + ".weight", String.valueOf(rank.getWeight()));
            put(prefix + ".prefix", rank.getPrefix());
            put(prefix + ".suffix", rank.getSuffix());
            put(prefix + ".nameColour", rank.getTranslatableNameColour() + rank.getReadableNameColour());
            put(prefix + ".chatColour", rank.getTranslatableChatColour() + rank.getReadableChatColour());
            put(prefix + ".inheritBelow", String.valueOf(rank.shouldInheritPermissionsFromLowerRanks()));
            put(prefix + ".inheritable", String.valueOf(rank.isInheritable()));
            put(prefix + ".permissions.size", String.valueOf(rank.getPermissions().size()));
            put(prefix + ".permissions", String.valueOf(rank.getPermissions()));
        }};
    }
}

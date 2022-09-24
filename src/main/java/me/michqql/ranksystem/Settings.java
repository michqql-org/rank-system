package me.michqql.ranksystem;

import me.michqql.servercoreutils.io.IO;
import me.michqql.servercoreutils.io.YamlFile;
import org.bukkit.configuration.ConfigurationSection;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public enum Settings {

    DEBUG("debug", false), // Hidden setting
    PLAYER_LOGIN_TIMEOUT_LIMIT("player-login-timeout-limit", 100), // Hidden setting
    ALLOW_PLAYER_WITHOUT_INJECTION("allow-player-without-injection", false), // Hidden setting

    /* RANKS */
    DEFAULT_RANK("default-rank", ""),

    /* GRANTS */
    PERMANENT_GRANTS_AS_DEFAULT("grants.permanent-grants-as-default", true),
    DEFAULT_TEMPORARY_TIME_MS("grants.default-temporary-time-in-ms", TimeUnit.DAYS.toMillis(30)),
    ;

    private final String configPath;
    private String stringValue = null;
    private Integer intValue = null;
    private Long longValue = null;
    private Boolean booleanValue = null;
    private Double doubleValue = null;

    Settings(String configPath, String stringValue) {
        this.configPath = configPath;
        this.stringValue = stringValue;
    }

    Settings(String configPath, int intValue) {
        this.configPath = configPath;
        this.intValue = intValue;
    }

    Settings(String configPath, long longValue) {
        this.configPath = configPath;
        this.longValue = longValue;
    }

    Settings(String configPath, boolean booleanValue) {
        this.configPath = configPath;
        this.booleanValue = booleanValue;
    }

    Settings(String configPath, double doubleValue) {
        this.configPath = configPath;
        this.doubleValue = doubleValue;
    }

    public void load(ConfigurationSection config) {
        if(stringValue != null) {
            stringValue = config.getString(configPath, stringValue);
        } else if(intValue != null) {
            intValue = config.getInt(configPath, intValue);
        } else if(longValue != null) {
            longValue = config.getLong(configPath, longValue);
        } else if(booleanValue != null) {
            booleanValue = config.getBoolean(configPath, booleanValue);
        } else if(doubleValue != null) {
            doubleValue = config.getDouble(configPath, doubleValue);
        }
    }

    public String getString() {
        return stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public static void loadSettings(RankSystemPlugin plugin) {
        YamlFile file = IO.getYamlFile(plugin, "config.yml");
        if(file.isNewFile()) {
            file.copyDefaultResource();
        }

        ConfigurationSection config = file.getConfig();
        for(Settings setting : values())
            setting.load(config);
    }
}

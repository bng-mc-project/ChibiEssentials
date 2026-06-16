package ru.chibiessentials.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import ru.chibiessentials.ChibiEssentials;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ChibiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ConfigData data = new ConfigData();

    public static void init() {
        reload();
    }

    public static void reload() {
        Path path = getConfigPath();
        try {
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                    if (loaded != null) {
                        data = loaded;
                    }
                }
            } else {
                Files.createDirectories(path.getParent());
                try (var stream = ChibiConfig.class.getResourceAsStream("/chibiessentials.default.json")) {
                    if (stream != null) {
                        ConfigData loaded = GSON.fromJson(new java.io.InputStreamReader(stream), ConfigData.class);
                        if (loaded != null) {
                            data = loaded;
                        }
                    }
                }
                save();
            }
        } catch (IOException e) {
            ChibiEssentials.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() throws IOException {
        Path path = getConfigPath();
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(data, writer);
        }
    }

    public static Path getConfigPath() {
        return Platform.getConfigFolder().resolve("chibiessentials.json");
    }

    public static HomesConfig homes() { return data.homes; }
    public static BackConfig back() { return data.back; }
    public static TimedConfig spawn() { return data.spawn; }
    public static TimedConfig warp() { return data.warp; }
    public static TpaConfig tpa() { return data.tpa; }
    public static MessagesConfig messages() { return data.messages; }
    public static VanishConfig vanish() { return data.vanish; }

    public static final class ConfigData {
        public HomesConfig homes = new HomesConfig();
        public BackConfig back = new BackConfig();
        public TimedConfig spawn = new TimedConfig(3, 5);
        public TimedConfig warp = new TimedConfig(3, 5);
        public TpaConfig tpa = new TpaConfig();
        public MessagesConfig messages = new MessagesConfig();
        public VanishConfig vanish = new VanishConfig();
    }

    public static final class HomesConfig {
        public int defaultMax = 1;
        public int warmup = 3;
        public int cooldown = 5;
    }

    public static final class BackConfig {
        public int maxHistory = 5;
        public int warmup = 0;
        public int cooldown = 30;
        public boolean onDeathOnly = false;
    }

    public static final class TimedConfig {
        public int warmup;
        public int cooldown;

        public TimedConfig() {}

        public TimedConfig(int warmup, int cooldown) {
            this.warmup = warmup;
            this.cooldown = cooldown;
        }
    }

    public static final class TpaConfig {
        public int warmup = 3;
        public int cooldown = 10;
        public int requestTimeoutSeconds = 120;
    }

    public static final class MessagesConfig {
        public String format = "&7[&b{sender}&7] &f{message}";
    }

    public static final class VanishConfig {
        public boolean hideFromTab = true;
        public boolean hideChat = false;
    }
}

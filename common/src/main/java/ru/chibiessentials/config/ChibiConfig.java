package ru.chibiessentials.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import ru.chibiessentials.ChibiEssentials;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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
            Files.createDirectories(getConfigDir());
            if (Files.exists(path)) {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                if (isConfigComplete(content)) {
                    ConfigData loaded = GSON.fromJson(content, ConfigData.class);
                    if (loaded != null) {
                        data = loaded;
                    }
                } else {
                    backupConfig(path);
                    data = loadDefaults();
                    save();
                }
            } else {
                data = loadDefaults();
                save();
            }
            ChibiLang.load();
        } catch (IOException e) {
            ChibiEssentials.LOGGER.error("Failed to load config", e);
        }
    }

    private static ConfigData loadDefaults() throws IOException {
        try (var stream = ChibiConfig.class.getResourceAsStream("/chibiessentials/config.default.json")) {
            if (stream != null) {
                ConfigData loaded = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), ConfigData.class);
                if (loaded != null) {
                    return loaded;
                }
            }
        }
        return new ConfigData();
    }

    private static boolean isConfigComplete(String content) {
        try {
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            return hasString(root, "language")
                    && hasSection(root, "homes", "defaultMax", "warmup", "cooldown")
                    && hasSection(root, "back", "maxHistory", "warmup", "cooldown", "onDeathOnly")
                    && hasSection(root, "spawn", "warmup", "cooldown")
                    && hasSection(root, "warp", "warmup", "cooldown")
                    && hasSection(root, "tpa", "warmup", "cooldown", "requestTimeoutSeconds")
                    && hasSection(root, "messages", "format")
                    && hasSection(root, "vanish", "hideFromTab", "hideChat");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasString(JsonObject root, String key) {
        return root.has(key) && root.get(key).isJsonPrimitive() && root.get(key).getAsJsonPrimitive().isString();
    }

    private static boolean hasSection(JsonObject root, String section, String... keys) {
        if (!root.has(section) || !root.get(section).isJsonObject()) {
            return false;
        }
        JsonObject object = root.getAsJsonObject(section);
        for (String key : keys) {
            if (!object.has(key)) {
                return false;
            }
        }
        return true;
    }

    private static void backupConfig(Path path) throws IOException {
        Path dir = path.getParent();
        int index = 1;
        Path backup;
        do {
            backup = dir.resolve("config." + index + ".bkp");
            index++;
        } while (Files.exists(backup));
        Files.move(path, backup);
        ChibiEssentials.LOGGER.warn("Config is incomplete or invalid, backed up to {} and regenerated", backup.getFileName());
    }

    public static void save() throws IOException {
        Path path = getConfigPath();
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(data, writer);
        }
    }

    public static Path getConfigDir() {
        return Platform.getConfigFolder().resolve("chibiessentials");
    }

    public static Path getConfigPath() {
        return getConfigDir().resolve("config.json");
    }

    public static String language() {
        return data.language != null && !data.language.isBlank() ? data.language : "en_us";
    }

    public static HomesConfig homes() { return data.homes; }
    public static BackConfig back() { return data.back; }
    public static TimedConfig spawn() { return data.spawn; }
    public static TimedConfig warp() { return data.warp; }
    public static TpaConfig tpa() { return data.tpa; }
    public static MessagesConfig messages() { return data.messages; }
    public static VanishConfig vanish() { return data.vanish; }

    public static final class ConfigData {
        public String language = "en_us";
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

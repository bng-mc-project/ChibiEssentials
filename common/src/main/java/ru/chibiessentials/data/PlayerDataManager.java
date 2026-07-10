package ru.chibiessentials.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import ru.chibiessentials.ChibiEssentials;
import ru.chibiessentials.util.TeleportPos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PlayerDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, PlayerData> CACHE = new HashMap<>();
    private static Path dataDir;

    private PlayerDataManager() {}

    public static void init(MinecraftServer server) {
        dataDir = server.getWorldPath(LevelResource.ROOT).resolve("chibiessentials/playerdata");
        CACHE.clear();
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            ChibiEssentials.LOGGER.error("Failed to create player data directory", e);
        }
    }

    public static void clear() {
        CACHE.clear();
        dataDir = null;
    }

    public static Optional<PlayerData> get(UUID uuid) {
        return Optional.ofNullable(CACHE.get(uuid));
    }

    public static Optional<PlayerData> getOrCreate(ServerPlayer player) {
        return Optional.of(CACHE.computeIfAbsent(player.getUUID(), id -> loadOrNew(id, player.getGameProfile().getName())));
    }

    public static boolean isFirstJoin(ServerPlayer player) {
        if (dataDir == null) {
            return false;
        }
        return !Files.exists(dataDir.resolve(player.getUUID() + ".json"));
    }

    public static void unload(ServerPlayer player) {
        PlayerData data = CACHE.get(player.getUUID());
        if (data != null) {
            save(data);
            CACHE.remove(player.getUUID());
        }
    }

    public static void saveAll() {
        CACHE.values().forEach(PlayerDataManager::saveIfChanged);
    }

    public static void saveIfChanged(PlayerData data) {
        if (data.isDirty()) {
            save(data);
        }
    }

    private static void save(PlayerData data) {
        if (dataDir == null) return;
        Path file = dataDir.resolve(data.getUuid() + ".json");
        try {
            Files.createDirectories(dataDir);
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(data.toJson(), writer);
            }
            data.clearDirty();
        } catch (IOException e) {
            ChibiEssentials.LOGGER.error("Failed to save player data for {}", data.getUuid(), e);
        }
    }

    private static PlayerData loadOrNew(UUID uuid, String name) {
        if (dataDir == null) {
            return new PlayerData(uuid, name);
        }
        Path file = dataDir.resolve(uuid + ".json");
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String storedName = json.has("name") ? json.get("name").getAsString() : name;
                return PlayerData.fromJson(uuid, storedName, json);
            } catch (Exception e) {
                ChibiEssentials.LOGGER.error("Failed to load player data for {}", uuid, e);
            }
        }
        return new PlayerData(uuid, name);
    }

    public static void addTeleportHistory(ServerPlayer player) {
        getOrCreate(player).ifPresent(data -> data.addTeleportHistory(player));
    }
}

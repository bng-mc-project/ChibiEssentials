package ru.chibiessentials.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import ru.chibiessentials.ChibiEssentials;
import ru.chibiessentials.util.TeleportPos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class WorldData {
    public static WorldData instance;

    private final Path file;
    private boolean dirty;
    private TeleportPos spawn;
    private final Map<String, TeleportPos> warps = new LinkedHashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public WorldData(MinecraftServer server) {
        this.file = server.getWorldPath(LevelResource.ROOT).resolve("chibiessentials/world.json");
    }

    public TeleportPos getSpawn() {
        return spawn;
    }

    public void setSpawn(TeleportPos spawn) {
        this.spawn = spawn;
        markDirty();
    }

    public Map<String, TeleportPos> getWarps() {
        return warps;
    }

    public TeleportPos getWarp(String name) {
        return warps.get(normalizeWarpName(name));
    }

    public boolean addWarp(String name, TeleportPos pos) {
        String key = normalizeWarpName(name);
        warps.put(key, pos);
        markDirty();
        return true;
    }

    public boolean deleteWarp(String name) {
        TeleportPos removed = warps.remove(normalizeWarpName(name));
        if (removed != null) {
            markDirty();
            return true;
        }
        return false;
    }

    public Set<String> getWarpNames() {
        return Collections.unmodifiableSet(warps.keySet());
    }

    public static String normalizeWarpName(String name) {
        return name.toLowerCase();
    }

    public static boolean isValidWarpName(String name) {
        String normalized = normalizeWarpName(name);
        return normalized.length() >= 1 && normalized.length() <= 32 && normalized.matches("[a-z0-9_-]+");
    }

    public void markDirty() {
        dirty = true;
    }

    public void saveIfChanged() {
        if (dirty) {
            save();
        }
    }

    public void load() {
        if (!Files.exists(file)) {
            dirty = false;
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("spawn")) {
                spawn = new TeleportPos(json.getAsJsonObject("spawn"));
            }
            warps.clear();
            if (json.has("warps")) {
                JsonObject warpsJson = json.getAsJsonObject("warps");
                for (Map.Entry<String, JsonElement> entry : warpsJson.entrySet()) {
                    warps.put(entry.getKey(), new TeleportPos(entry.getValue().getAsJsonObject()));
                }
            }
            dirty = false;
        } catch (Exception e) {
            ChibiEssentials.LOGGER.error("Failed to load world data", e);
        }
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            JsonObject json = new JsonObject();
            if (spawn != null) json.add("spawn", spawn.toJson());
            JsonObject warpsJson = new JsonObject();
            warps.forEach((k, v) -> warpsJson.add(k, v.toJson()));
            json.add("warps", warpsJson);
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(json, writer);
            }
            dirty = false;
        } catch (IOException e) {
            ChibiEssentials.LOGGER.error("Failed to save world data", e);
        }
    }
}

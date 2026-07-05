package ru.chibiessentials.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import ru.chibiessentials.config.ChibiConfig;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.TeleportPos;
import ru.chibiessentials.util.WarmupCooldownTeleporter;

import java.util.*;

public final class PlayerData {
    private final UUID uuid;
    private final String name;
    private boolean dirty;

    private final Map<String, TeleportPos> homes = new LinkedHashMap<>();
    private final Deque<TeleportPos> teleportHistory = new ArrayDeque<>();

    private boolean fly;
    private boolean god;
    private boolean vanished;
    private boolean chatGlobal;
    private String preVanishGameMode;
    private UUID lastMessaged;

    public final WarmupCooldownTeleporter backTeleporter;
    public final WarmupCooldownTeleporter spawnTeleporter;
    public final WarmupCooldownTeleporter homeTeleporter;
    public final WarmupCooldownTeleporter warpTeleporter;
    public final WarmupCooldownTeleporter tpaTeleporter;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        backTeleporter = new WarmupCooldownTeleporter(this,
                p -> ChibiPermissions.getInt(p, PermissionNodes.metaCooldown("back"), ChibiConfig.back().cooldown),
                p -> ChibiPermissions.getInt(p, PermissionNodes.metaWarmup("back"), ChibiConfig.back().warmup),
                true);
        spawnTeleporter = new WarmupCooldownTeleporter(this,
                p -> ChibiPermissions.getInt(p, PermissionNodes.metaCooldown("spawn"), ChibiConfig.spawn().cooldown),
                p -> ChibiPermissions.getInt(p, PermissionNodes.metaWarmup("spawn"), ChibiConfig.spawn().warmup));
        homeTeleporter = new WarmupCooldownTeleporter(this,
                p -> ChibiPermissions.getInt(p, PermissionNodes.metaCooldown("home"), ChibiConfig.homes().cooldown),
                p -> ChibiPermissions.getInt(p, PermissionNodes.metaWarmup("home"), ChibiConfig.homes().warmup));
        warpTeleporter = new WarmupCooldownTeleporter(this,
                p -> ChibiPermissions.getInt(p, PermissionNodes.metaCooldown("warp"), ChibiConfig.warp().cooldown),
                p -> ChibiPermissions.getInt(p, PermissionNodes.metaWarmup("warp"), ChibiConfig.warp().warmup));
        tpaTeleporter = new WarmupCooldownTeleporter(this,
                p -> ChibiPermissions.getInt(p, PermissionNodes.metaCooldown("tpa"), ChibiConfig.tpa().cooldown),
                p -> ChibiPermissions.getInt(p, PermissionNodes.metaWarmup("tpa"), ChibiConfig.tpa().warmup));
        chatGlobal = "global".equalsIgnoreCase(ChibiConfig.chat().defaultMode);
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public Map<String, TeleportPos> getHomes() { return homes; }
    public boolean isFly() { return fly; }
    public boolean isGod() { return god; }
    public boolean isVanished() { return vanished; }
    public boolean isChatGlobal() { return chatGlobal; }
    public UUID getLastMessaged() { return lastMessaged; }

    public void setFly(boolean fly) {
        if (this.fly != fly) {
            this.fly = fly;
            markDirty();
        }
    }

    public void setGod(boolean god) {
        if (this.god != god) {
            this.god = god;
            markDirty();
        }
    }

    public void setVanished(boolean vanished) {
        if (this.vanished != vanished) {
            this.vanished = vanished;
            markDirty();
        }
    }

    public void setChatGlobal(boolean chatGlobal) {
        if (this.chatGlobal != chatGlobal) {
            this.chatGlobal = chatGlobal;
            markDirty();
        }
    }

    public GameType getPreVanishGameMode() {
        if (preVanishGameMode == null) return null;
        return GameType.byName(preVanishGameMode);
    }

    public void setPreVanishGameMode(GameType type) {
        String value = type != null ? type.getName() : null;
        if (!Objects.equals(preVanishGameMode, value)) {
            preVanishGameMode = value;
            markDirty();
        }
    }

    public void clearPreVanishGameMode() {
        if (preVanishGameMode != null) {
            preVanishGameMode = null;
            markDirty();
        }
    }

    public void setLastMessaged(UUID uuid) {
        if (!Objects.equals(lastMessaged, uuid)) {
            lastMessaged = uuid;
            markDirty();
        }
    }

    public int getMaxHomes(ServerPlayer player) {
        return ChibiPermissions.getInt(player, PermissionNodes.META_HOME_MAX, ChibiConfig.homes().defaultMax);
    }

    public void addTeleportHistory(TeleportPos pos) {
        markDirty();
        teleportHistory.addFirst(pos);
    }

    public void addTeleportHistory(ServerPlayer player) {
        int max = ChibiPermissions.getInt(player, PermissionNodes.META_BACK_MAX, ChibiConfig.back().maxHistory);
        while (teleportHistory.size() >= max) {
            teleportHistory.removeLast();
        }
        addTeleportHistory(new TeleportPos(player));
    }

    public TeleportPos popTeleportHistory() {
        TeleportPos pos = teleportHistory.pollFirst();
        if (pos != null) markDirty();
        return pos;
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        dirty = false;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("fly", fly);
        json.addProperty("god", god);
        json.addProperty("vanished", vanished);
        json.addProperty("chatGlobal", chatGlobal);
        if (preVanishGameMode != null) json.addProperty("preVanishGameMode", preVanishGameMode);
        if (lastMessaged != null) json.addProperty("lastMessaged", lastMessaged.toString());

        JsonObject homesJson = new JsonObject();
        homes.forEach((k, v) -> homesJson.add(k, v.toJson()));
        json.add("homes", homesJson);

        JsonArray history = new JsonArray();
        teleportHistory.forEach(pos -> history.add(pos.toJson()));
        json.add("teleportHistory", history);
        return json;
    }

    public void fromJson(JsonObject json) {
        if (json.has("fly")) fly = json.get("fly").getAsBoolean();
        if (json.has("god")) god = json.get("god").getAsBoolean();
        if (json.has("vanished")) vanished = json.get("vanished").getAsBoolean();
        if (json.has("chatGlobal")) chatGlobal = json.get("chatGlobal").getAsBoolean();
        if (json.has("preVanishGameMode")) preVanishGameMode = json.get("preVanishGameMode").getAsString();
        if (json.has("lastMessaged")) lastMessaged = UUID.fromString(json.get("lastMessaged").getAsString());

        homes.clear();
        if (json.has("homes")) {
            JsonObject homesJson = json.getAsJsonObject("homes");
            for (Map.Entry<String, JsonElement> entry : homesJson.entrySet()) {
                homes.put(entry.getKey(), new TeleportPos(entry.getValue().getAsJsonObject()));
            }
        }

        teleportHistory.clear();
        if (json.has("teleportHistory")) {
            JsonArray history = json.getAsJsonArray("teleportHistory");
            for (JsonElement element : history) {
                teleportHistory.addLast(new TeleportPos(element.getAsJsonObject()));
            }
        }
    }

    public static PlayerData fromJson(UUID uuid, String name, JsonObject json) {
        PlayerData data = new PlayerData(uuid, name);
        data.fromJson(json);
        data.clearDirty();
        return data;
    }
}

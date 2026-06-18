package ru.chibiessentials.util;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import ru.chibiessentials.config.ChibiLang;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class TeleportPos {
    private final ResourceKey<Level> dimension;
    private final BlockPos pos;
    private final Float yRot;
    private final Float xRot;
    private final long time;

    public TeleportPos(ResourceKey<Level> dimension, BlockPos pos) {
        this(dimension, pos, null, null);
    }

    public TeleportPos(ResourceKey<Level> dimension, BlockPos pos, Float yRot, Float xRot) {
        this.dimension = dimension;
        this.pos = pos;
        this.yRot = yRot;
        this.xRot = xRot;
        this.time = System.currentTimeMillis();
    }

    public TeleportPos(Entity entity) {
        this(entity.level().dimension(), entity.blockPosition(), entity.getYRot(), entity.getXRot());
    }

    public TeleportPos(JsonObject json) {
        this.dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(json.get("dim").getAsString()));
        this.pos = new BlockPos(json.get("x").getAsInt(), json.get("y").getAsInt(), json.get("z").getAsInt());
        this.yRot = json.has("yRot") ? json.get("yRot").getAsFloat() : null;
        this.xRot = json.has("xRot") ? json.get("xRot").getAsFloat() : null;
        this.time = json.has("time") ? json.get("time").getAsLong() : System.currentTimeMillis();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("dim", dimension.location().toString());
        json.addProperty("x", pos.getX());
        json.addProperty("y", pos.getY());
        json.addProperty("z", pos.getZ());
        json.addProperty("time", time);
        if (yRot != null) json.addProperty("yRot", yRot);
        if (xRot != null) json.addProperty("xRot", xRot);
        return json;
    }

    public TeleportResult teleport(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(dimension);
        if (level == null) {
            return TeleportResult.failed(ChibiLang.get("chibiessentials.teleport.dimension_not_found"));
        }

        int xpLevel = player.experienceLevel;
        float xrot = xRot != null ? xRot : player.getXRot();
        float yrot = yRot != null ? yRot : player.getYRot();
        TeleportHistoryHelper.runWithoutHistory(() ->
                player.teleportTo(level, pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D, yrot, xrot));
        player.setExperienceLevels(xpLevel);
        return TeleportResult.SUCCESS;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }

    @FunctionalInterface
    public interface TeleportResult {
        TeleportResult SUCCESS = new TeleportResult() {
            @Override
            public int runCommand(ServerPlayer player) {
                return 1;
            }

            @Override
            public boolean isSuccess() {
                return true;
            }
        };

        static TeleportResult failed(Component msg) {
            return player -> {
                player.displayClientMessage(msg, false);
                return 0;
            };
        }

        int runCommand(ServerPlayer player);

        default boolean isSuccess() {
            return false;
        }
    }

    @FunctionalInterface
    public interface CooldownTeleportResult extends TeleportResult {
        long getCooldownMillis();

        @Override
        default int runCommand(ServerPlayer player) {
            long seconds = Math.max(1, getCooldownMillis() / 1000L);
            player.displayClientMessage(ChibiLang.get("chibiessentials.teleport.cooldown", seconds), false);
            return 0;
        }

        @Override
        default boolean isSuccess() {
            return false;
        }
    }
}

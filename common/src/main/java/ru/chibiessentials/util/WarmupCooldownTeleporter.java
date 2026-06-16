package ru.chibiessentials.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import ru.chibiessentials.config.ChibiConfig;
import ru.chibiessentials.data.PlayerData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class WarmupCooldownTeleporter {
    private final PlayerData playerData;
    private final ToIntFunction<ServerPlayer> cooldownConfig;
    private final ToIntFunction<ServerPlayer> warmupConfig;
    private final boolean popHistoryOnTeleport;
    private final boolean pushHistoryOnTeleport;

    private long cooldown;

    private static final Map<UUID, Warmup> WARMUPS = new HashMap<>();
    private static final Map<UUID, Warmup> PENDING_ADDITIONS = new HashMap<>();
    private static final Set<UUID> PENDING_REMOVALS = new HashSet<>();

    public WarmupCooldownTeleporter(PlayerData playerData, ToIntFunction<ServerPlayer> cooldownConfig,
                                    ToIntFunction<ServerPlayer> warmupConfig) {
        this(playerData, cooldownConfig, warmupConfig, false, true);
    }

    public WarmupCooldownTeleporter(PlayerData playerData, ToIntFunction<ServerPlayer> cooldownConfig,
                                    ToIntFunction<ServerPlayer> warmupConfig, boolean popHistoryOnTeleport) {
        this(playerData, cooldownConfig, warmupConfig, popHistoryOnTeleport, !popHistoryOnTeleport);
    }

    public WarmupCooldownTeleporter(PlayerData playerData, ToIntFunction<ServerPlayer> cooldownConfig,
                                    ToIntFunction<ServerPlayer> warmupConfig, boolean popHistoryOnTeleport,
                                    boolean pushHistoryOnTeleport) {
        this.playerData = playerData;
        this.cooldownConfig = cooldownConfig;
        this.warmupConfig = warmupConfig;
        this.popHistoryOnTeleport = popHistoryOnTeleport;
        this.pushHistoryOnTeleport = pushHistoryOnTeleport;
    }

    public TeleportPos.TeleportResult checkCooldown() {
        long now = System.currentTimeMillis();
        if (now < cooldown) {
            long remaining = cooldown - now;
            return (TeleportPos.CooldownTeleportResult) () -> remaining;
        }
        return TeleportPos.TeleportResult.SUCCESS;
    }

    public TeleportPos.TeleportResult teleport(ServerPlayer player, Function<ServerPlayer, TeleportPos> positionGetter) {
        TeleportPos.TeleportResult cooldownResult = checkCooldown();
        if (!cooldownResult.isSuccess()) {
            return cooldownResult;
        }

        int warmupTime = warmupConfig.applyAsInt(player);
        if (warmupTime <= 0) {
            return teleportNow(player, positionGetter);
        }

        PENDING_ADDITIONS.put(player.getUUID(), new Warmup(
                System.currentTimeMillis() + warmupTime * 1000L,
                this,
                player.position(),
                positionGetter
        ));
        return TeleportPos.TeleportResult.SUCCESS;
    }

    private TeleportPos.TeleportResult teleportNow(ServerPlayer player, Function<ServerPlayer, TeleportPos> positionGetter) {
        cooldown = System.currentTimeMillis() + Math.max(0L, cooldownConfig.applyAsInt(player) * 1000L);

        TeleportPos target = positionGetter.apply(player);
        if (target == null) {
            return TeleportPos.TeleportResult.failed(Component.translatable("chibiessentials.teleport.unknown_destination"));
        }

        TeleportPos currentPos = new TeleportPos(player);
        TeleportPos.TeleportResult result = target.teleport(player);
        if (result.isSuccess()) {
            if (popHistoryOnTeleport) {
                playerData.popTeleportHistory();
            } else if (pushHistoryOnTeleport && !ChibiConfig.back().onDeathOnly) {
                playerData.addTeleportHistory(currentPos);
            }
        }
        return result;
    }

    public static void tickWarmups(MinecraftServer server) {
        WARMUPS.putAll(PENDING_ADDITIONS);
        PENDING_ADDITIONS.clear();
        PENDING_REMOVALS.forEach(WARMUPS::remove);
        PENDING_REMOVALS.clear();

        if (WARMUPS.isEmpty()) {
            return;
        }

        Set<UUID> toRemove = new HashSet<>();
        long now = System.currentTimeMillis();

        for (Map.Entry<UUID, Warmup> entry : WARMUPS.entrySet()) {
            UUID playerId = entry.getKey();
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) {
                toRemove.add(playerId);
                continue;
            }

            Warmup warmup = entry.getValue();
            if (warmup.when <= now) {
                TeleportPos.TeleportResult res = warmup.teleporter.teleportNow(player, warmup.positionGetter);
                toRemove.add(playerId);
                res.runCommand(player);
            } else if (player.position().distanceToSqr(warmup.initialPos) > 0.25) {
                toRemove.add(playerId);
                player.displayClientMessage(Component.translatable("chibiessentials.teleport.interrupted").withStyle(ChatFormatting.RED), true);
            } else {
                long seconds = Math.max(1, (warmup.when - now) / 1000L);
                player.displayClientMessage(Component.translatable("chibiessentials.teleport.warmup", seconds).withStyle(ChatFormatting.YELLOW), true);
            }
        }

        toRemove.forEach(WARMUPS::remove);
    }

    public static void cancelWarmup(ServerPlayer player) {
        if (WARMUPS.containsKey(player.getUUID())) {
            PENDING_REMOVALS.add(player.getUUID());
            player.displayClientMessage(Component.translatable("chibiessentials.teleport.interrupted").withStyle(ChatFormatting.RED), true);
        }
    }

    private record Warmup(long when, WarmupCooldownTeleporter teleporter, Vec3 initialPos,
                          Function<ServerPlayer, TeleportPos> positionGetter) {}
}

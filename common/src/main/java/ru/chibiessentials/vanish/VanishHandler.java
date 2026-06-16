package ru.chibiessentials.vanish;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import ru.chibiessentials.data.PlayerDataManager;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class VanishHandler {
    private static final Set<UUID> VANISHED = new HashSet<>();

    private VanishHandler() {}

    public static boolean isVanished(ServerPlayer player) {
        return VANISHED.contains(player.getUUID());
    }

    public static boolean canSee(ServerPlayer viewer, ServerPlayer target) {
        if (!isVanished(target)) return true;
        return viewer == target || ChibiPermissions.has(viewer, PermissionNodes.VANISH_SEE, 2);
    }

    public static int toggle(ServerPlayer player) {
        if (isVanished(player)) {
            disable(player);
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("chibiessentials.vanish.off"), false);
        } else {
            enable(player);
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("chibiessentials.vanish.on"), false);
        }
        return 1;
    }

    public static void enable(ServerPlayer player) {
        PlayerDataManager.getOrCreate(player).ifPresent(data -> {
            if (!data.isVanished()) {
                data.setPreVanishGameMode(player.gameMode.getGameModeForPlayer());
            }
            data.setVanished(true);
            VANISHED.add(player.getUUID());
            player.setGameMode(GameType.SPECTATOR);
            player.setInvisible(true);
        });
    }

    public static void disable(ServerPlayer player) {
        PlayerDataManager.getOrCreate(player).ifPresent(data -> {
            data.setVanished(false);
            VANISHED.remove(player.getUUID());
            GameType restore = data.getPreVanishGameMode();
            if (restore == null) restore = GameType.SURVIVAL;
            player.setGameMode(restore);
            data.clearPreVanishGameMode();
            player.setInvisible(false);
        });
    }

    public static void applyOnJoin(ServerPlayer player) {
        PlayerDataManager.getOrCreate(player).ifPresent(data -> {
            if (data.isVanished()) {
                VANISHED.add(player.getUUID());
                player.setGameMode(GameType.SPECTATOR);
                player.setInvisible(true);
            }
        });
    }

    public static void clear() {
        VANISHED.clear();
    }
}

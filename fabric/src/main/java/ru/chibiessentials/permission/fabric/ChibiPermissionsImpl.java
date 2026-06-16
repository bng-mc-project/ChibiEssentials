package ru.chibiessentials.permission.fabric;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public final class ChibiPermissionsImpl {
    private ChibiPermissionsImpl() {
    }

    public static boolean platformHas(CommandSourceStack source, String fullNode, int fallbackOpLevel) {
        return ru.chibiessentials.fabric.ChibiPermissionsImpl.platformHas(source, fullNode, fallbackOpLevel);
    }

    public static boolean platformHas(ServerPlayer player, String fullNode, int fallbackOpLevel) {
        return ru.chibiessentials.fabric.ChibiPermissionsImpl.platformHas(player, fullNode, fallbackOpLevel);
    }

    public static int platformGetInt(ServerPlayer player, String fullNode, int defaultValue) {
        return ru.chibiessentials.fabric.ChibiPermissionsImpl.platformGetInt(player, fullNode, defaultValue);
    }
}

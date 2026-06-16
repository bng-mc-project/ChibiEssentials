package ru.chibiessentials.fabric;

import me.lucko.fabric.api.permissions.v0.Options;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unused")
public final class ChibiPermissionsImpl {
    public static boolean platformHas(CommandSourceStack source, String fullNode, int fallbackOpLevel) {
        return Permissions.check(source, fullNode, fallbackOpLevel);
    }

    public static boolean platformHas(ServerPlayer player, String fullNode, int fallbackOpLevel) {
        return Permissions.check(player, fullNode, fallbackOpLevel);
    }

    public static int platformGetInt(ServerPlayer player, String fullNode, int defaultValue) {
        return Options.get(player, fullNode, defaultValue, Integer::parseInt);
    }
}

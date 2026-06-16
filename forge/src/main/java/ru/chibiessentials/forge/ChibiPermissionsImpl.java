package ru.chibiessentials.forge;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;

@SuppressWarnings("unused")
public final class ChibiPermissionsImpl {
    public static boolean platformHas(CommandSourceStack source, String fullNode, int fallbackOpLevel) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return platformHas(player, fullNode, fallbackOpLevel);
        }
        return source.hasPermission(fallbackOpLevel);
    }

    public static boolean platformHas(ServerPlayer player, String fullNode, int fallbackOpLevel) {
        var node = ForgePermissionRegistration.NODES.get(fullNode);
        if (node != null) {
            return PermissionAPI.getPermission(player, node);
        }
        return player.hasPermissions(fallbackOpLevel);
    }

    public static int platformGetInt(ServerPlayer player, String fullNode, int defaultValue) {
        var node = ForgePermissionRegistration.META_NODES.get(fullNode);
        if (node != null) {
            String value = PermissionAPI.getPermission(player, node);
            if (value != null) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return defaultValue;
    }
}

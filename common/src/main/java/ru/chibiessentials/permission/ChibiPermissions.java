package ru.chibiessentials.permission;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public final class ChibiPermissions {
    public static final String PREFIX = "chibiessentials.";

    private ChibiPermissions() {}

    public static String fullNode(String node) {
        return node.startsWith(PREFIX) ? node : PREFIX + node;
    }

    public static boolean has(CommandSourceStack source, String node) {
        return has(source, node, 0);
    }

    public static boolean has(CommandSourceStack source, String node, int fallbackOpLevel) {
        return platformHas(source, fullNode(node), fallbackOpLevel);
    }

    public static boolean has(ServerPlayer player, String node) {
        return has(player, node, 0);
    }

    public static boolean has(ServerPlayer player, String node, int fallbackOpLevel) {
        return platformHas(player, fullNode(node), fallbackOpLevel);
    }

    public static int getInt(ServerPlayer player, String node, int defaultValue) {
        return platformGetInt(player, fullNode(node), defaultValue);
    }

    public static Predicate<CommandSourceStack> require(String node, int fallbackOpLevel) {
        return source -> has(source, node, fallbackOpLevel);
    }

    @ExpectPlatform
    public static boolean platformHas(CommandSourceStack source, String fullNode, int fallbackOpLevel) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean platformHas(ServerPlayer player, String fullNode, int fallbackOpLevel) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int platformGetInt(ServerPlayer player, String fullNode, int defaultValue) {
        throw new AssertionError();
    }
}

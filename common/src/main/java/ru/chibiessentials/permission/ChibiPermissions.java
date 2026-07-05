package ru.chibiessentials.permission;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

public final class ChibiPermissions {
    public static final String PERMISSION_NAMESPACE = "chibiessentials";
    public static final String PREFIX = PERMISSION_NAMESPACE + ".";
    public static final int DEFAULT_OP_FALLBACK = 2;

    private ChibiPermissions() {}

    public static String fullNode(String node) {
        return node.startsWith(PREFIX) ? node : PREFIX + node;
    }

    public static boolean has(CommandSourceStack source, String node) {
        return has(source, node, DEFAULT_OP_FALLBACK);
    }

    public static boolean has(CommandSourceStack source, String node, int fallbackOpLevel) {
        return platformHas(source, fullNode(node), fallbackOpLevel);
    }

    public static boolean has(ServerPlayer player, String node) {
        return has(player, node, DEFAULT_OP_FALLBACK);
    }

    public static boolean has(ServerPlayer player, String node, int fallbackOpLevel) {
        return platformHas(player, fullNode(node), fallbackOpLevel);
    }

    public static int getInt(ServerPlayer player, String node, int defaultValue) {
        return platformGetInt(player, fullNode(node), defaultValue);
    }

    public static String getString(ServerPlayer player, String node, String defaultValue) {
        return platformGetString(player, fullNode(node), defaultValue);
    }

    public static Predicate<CommandSourceStack> require(String node) {
        return require(node, DEFAULT_OP_FALLBACK);
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

    @ExpectPlatform
    public static String platformGetString(ServerPlayer player, String fullNode, String defaultValue) {
        throw new AssertionError();
    }
}

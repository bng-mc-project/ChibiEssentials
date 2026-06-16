package ru.chibiessentials.command.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import ru.chibiessentials.data.PlayerDataManager;
import ru.chibiessentials.data.WorldData;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.TeleportPos;

public final class WarpCommands {
    private WarpCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("warp")
                .then(Commands.literal("create")
                        .requires(ChibiPermissions.require(PermissionNodes.WARP_CREATE, 2))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> create(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("delete")
                        .requires(ChibiPermissions.require(PermissionNodes.WARP_DELETE, 2))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(getWarpSuggestions(), b))
                                .executes(ctx -> delete(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("list")
                        .requires(ChibiPermissions.require(PermissionNodes.WARP_LIST, 0))
                        .executes(ctx -> list(ctx.getSource().getPlayerOrException())))
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(getWarpSuggestions(), b))
                        .executes(ctx -> warp(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name")))));
    }

    private static Iterable<String> getWarpSuggestions() {
        return WorldData.instance != null ? WorldData.instance.getWarpNames() : java.util.Set.of();
    }

    public static int warp(ServerPlayer player, String name) {
        if (WorldData.instance == null) return 0;
        String key = WorldData.normalizeWarpName(name);
        TeleportPos pos = WorldData.instance.getWarp(key);
        if (pos == null) {
            player.displayClientMessage(Component.translatable("chibiessentials.warp.not_found", key), false);
            return 0;
        }
        if (!ChibiPermissions.has(player, PermissionNodes.WARP, 0)
                && !ChibiPermissions.has(player, PermissionNodes.warpNode(key), 0)) {
            player.displayClientMessage(Component.translatable("chibiessentials.no_permission"), false);
            return 0;
        }
        return PlayerDataManager.getOrCreate(player)
                .map(data -> data.warpTeleporter.teleport(player, p -> pos).runCommand(player))
                .orElse(0);
    }

    public static int create(ServerPlayer player, String name) {
        if (WorldData.instance == null) return 0;
        if (!WorldData.isValidWarpName(name)) {
            player.displayClientMessage(Component.translatable("chibiessentials.warp.invalid_name"), false);
            return 0;
        }
        String key = WorldData.normalizeWarpName(name);
        boolean existed = WorldData.instance.getWarp(key) != null;
        WorldData.instance.addWarp(key, new TeleportPos(player));
        player.displayClientMessage(Component.translatable(existed ? "chibiessentials.warp.updated" : "chibiessentials.warp.created", key), false);
        return 1;
    }

    public static int delete(ServerPlayer player, String name) {
        if (WorldData.instance == null) return 0;
        String key = WorldData.normalizeWarpName(name);
        if (WorldData.instance.deleteWarp(key)) {
            player.displayClientMessage(Component.translatable("chibiessentials.warp.deleted", key), false);
            return 1;
        }
        player.displayClientMessage(Component.translatable("chibiessentials.warp.not_found", key), false);
        return 0;
    }

    public static int list(ServerPlayer player) {
        if (WorldData.instance == null) return 0;
        if (WorldData.instance.getWarpNames().isEmpty()) {
            player.displayClientMessage(Component.translatable("chibiessentials.warp.list_empty"), false);
            return 1;
        }
        player.displayClientMessage(Component.translatable("chibiessentials.warp.list", String.join(", ", WorldData.instance.getWarpNames())), false);
        return 1;
    }
}

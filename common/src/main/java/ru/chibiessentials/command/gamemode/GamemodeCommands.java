package ru.chibiessentials.command.gamemode;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;

public final class GamemodeCommands {
    private GamemodeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gm")
                .then(Commands.argument("mode", IntegerArgumentType.integer(0, 3))
                        .executes(ctx -> setMode(ctx.getSource(), ctx.getSource().getPlayerOrException(),
                                IntegerArgumentType.getInteger(ctx, "mode")))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> setMode(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                        IntegerArgumentType.getInteger(ctx, "mode"))))));

        registerAlias(dispatcher, "gmc", GameType.CREATIVE, PermissionNodes.GM_CREATIVE);
        registerAlias(dispatcher, "gms", GameType.SURVIVAL, PermissionNodes.GM_SURVIVAL);
        registerAlias(dispatcher, "gmsp", GameType.SPECTATOR, PermissionNodes.GM_SPECTATOR);
    }

    private static void registerAlias(CommandDispatcher<CommandSourceStack> dispatcher, String literal, GameType type, String node) {
        dispatcher.register(Commands.literal(literal)
                .requires(ChibiPermissions.require(node, 2))
                .executes(ctx -> apply(ctx.getSource().getPlayerOrException(), type))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> apply(EntityArgument.getPlayer(ctx, "player"), type))));
    }

    private static int setMode(CommandSourceStack source, ServerPlayer target, int mode) {
        GameType type = GameType.byId(mode);
        String node = permissionFor(type);
        if (!ChibiPermissions.has(source, node, 2)) {
            source.sendFailure(Component.translatable("chibiessentials.no_permission"));
            return 0;
        }
        return apply(target, type);
    }

    private static int apply(ServerPlayer target, GameType type) {
        target.setGameMode(type);
        target.displayClientMessage(Component.translatable("chibiessentials.gamemode.set", type.getName()), false);
        return 1;
    }

    private static String permissionFor(GameType type) {
        return switch (type) {
            case CREATIVE -> PermissionNodes.GM_CREATIVE;
            case SURVIVAL -> PermissionNodes.GM_SURVIVAL;
            case ADVENTURE -> PermissionNodes.GM_ADVENTURE;
            case SPECTATOR -> PermissionNodes.GM_SPECTATOR;
        };
    }
}

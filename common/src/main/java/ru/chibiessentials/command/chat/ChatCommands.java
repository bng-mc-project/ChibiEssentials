package ru.chibiessentials.command.chat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import ru.chibiessentials.chat.ChatHandler;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;

public final class ChatCommands {
    private ChatCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerModeCommand(dispatcher, "g", true);
        registerModeCommand(dispatcher, "l", false);
        registerModeCommand(dispatcher, "global", true);
        registerModeCommand(dispatcher, "local", false);
    }

    private static void registerModeCommand(CommandDispatcher<CommandSourceStack> dispatcher, String name, boolean global) {
        dispatcher.register(Commands.literal(name)
                .requires(ChibiPermissions.require(PermissionNodes.CHAT))
                .executes(ctx -> ChatHandler.setGlobalMode(ctx.getSource().getPlayerOrException(), global))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> ChatHandler.send(
                                ctx.getSource().getPlayerOrException(),
                                StringArgumentType.getString(ctx, "message"),
                                global))));
    }
}

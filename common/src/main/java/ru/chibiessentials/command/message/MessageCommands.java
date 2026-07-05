package ru.chibiessentials.command.message;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import ru.chibiessentials.config.ChibiLang;
import net.minecraft.server.level.ServerPlayer;
import ru.chibiessentials.data.PlayerDataManager;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.MessageUtil;

public final class MessageCommands {
    private MessageCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("msg")
                .requires(ChibiPermissions.require(PermissionNodes.MSG))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> send(ctx.getSource().getPlayerOrException(),
                                        EntityArgument.getPlayer(ctx, "target"),
                                        StringArgumentType.getString(ctx, "message"))))));

        dispatcher.register(Commands.literal("m")
                .requires(ChibiPermissions.require(PermissionNodes.MSG))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> send(ctx.getSource().getPlayerOrException(),
                                        EntityArgument.getPlayer(ctx, "target"),
                                        StringArgumentType.getString(ctx, "message"))))));

        dispatcher.register(Commands.literal("reply")
                .requires(ChibiPermissions.require(PermissionNodes.REPLY))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> reply(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "message")))));
    }

    public static int send(ServerPlayer sender, ServerPlayer target, String message) {
        if (sender.getUUID().equals(target.getUUID())) {
            sender.displayClientMessage(ChibiLang.get("chibiessentials.msg.self"), false);
            return 0;
        }

        Component formatted = MessageUtil.formatPrivateMessage(sender, message);
        sender.displayClientMessage(ChibiLang.get("chibiessentials.msg.to", target.getDisplayName(), formatted), false);
        target.displayClientMessage(ChibiLang.get("chibiessentials.msg.from", sender.getDisplayName(), formatted), false);

        PlayerDataManager.getOrCreate(sender).ifPresent(d -> d.setLastMessaged(target.getUUID()));
        PlayerDataManager.getOrCreate(target).ifPresent(d -> d.setLastMessaged(sender.getUUID()));
        return 1;
    }

    public static int reply(ServerPlayer sender, String message) {
        return PlayerDataManager.getOrCreate(sender).map(data -> {
            if (data.getLastMessaged() == null) {
                sender.displayClientMessage(ChibiLang.get("chibiessentials.msg.no_reply_target"), false);
                return 0;
            }
            ServerPlayer target = sender.server.getPlayerList().getPlayer(data.getLastMessaged());
            if (target == null) {
                sender.displayClientMessage(ChibiLang.get("chibiessentials.msg.offline"), false);
                return 0;
            }
            return send(sender, target, message);
        }).orElse(0);
    }
}

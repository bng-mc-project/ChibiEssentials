package ru.chibiessentials.command.message;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
    private static final String[] MESSAGE_ALIASES = {"m", "msg", "tell"};
    private static final String[] REPLY_ALIASES = {"reply", "r"};
    private static final String[] VANILLA_MESSAGE_ALIASES = {"tell", "msg", "w"};

    private MessageCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String alias : MESSAGE_ALIASES) {
            dispatcher.register(privateMessageCommand(alias));
        }
        for (String alias : REPLY_ALIASES) {
            dispatcher.register(replyCommand(alias));
        }
    }

    public static void applyOverrides(CommandDispatcher<CommandSourceStack> dispatcher) {
        removeCommands(dispatcher, VANILLA_MESSAGE_ALIASES);
        removeCommands(dispatcher, MESSAGE_ALIASES);
        removeCommands(dispatcher, REPLY_ALIASES);
        register(dispatcher);
    }

    private static void removeCommands(CommandDispatcher<CommandSourceStack> dispatcher, String... names) {
        var children = dispatcher.getRoot().getChildren();
        for (String name : names) {
            children.removeIf(child -> child.getName().equals(name));
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> privateMessageCommand(String name) {
        return Commands.literal(name)
                .requires(ChibiPermissions.require(PermissionNodes.MSG))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> send(ctx.getSource().getPlayerOrException(),
                                        EntityArgument.getPlayer(ctx, "target"),
                                        StringArgumentType.getString(ctx, "message")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> replyCommand(String name) {
        return Commands.literal(name)
                .requires(ChibiPermissions.require(PermissionNodes.REPLY))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> reply(ctx.getSource().getPlayerOrException(),
                                StringArgumentType.getString(ctx, "message"))));
    }

    public static int send(ServerPlayer sender, ServerPlayer target, String message) {
        if (sender.getUUID().equals(target.getUUID())) {
            sender.displayClientMessage(ChibiLang.get("chibiessentials.msg.self"), false);
            return 0;
        }

        Component body = MessageUtil.formatPlayerMessage(sender, message);
        sender.displayClientMessage(ChibiLang.get("chibiessentials.msg.to", target.getGameProfile().getName(), body), false);
        target.displayClientMessage(ChibiLang.get("chibiessentials.msg.from", sender.getGameProfile().getName(), body), false);

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

package ru.chibiessentials.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import ru.chibiessentials.config.ChibiConfig;
import ru.chibiessentials.config.ChibiLang;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;

public final class AdminCommands {
    private AdminCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var reload = Commands.literal("chibireload")
                .requires(ChibiPermissions.require(PermissionNodes.RELOAD, 2))
                .executes(ctx -> reload(ctx.getSource()));

        dispatcher.register(reload);
        dispatcher.register(Commands.literal("cereload")
                .requires(ChibiPermissions.require(PermissionNodes.RELOAD, 2))
                .executes(ctx -> reload(ctx.getSource())));
        dispatcher.register(Commands.literal("essentialsreload")
                .requires(ChibiPermissions.require(PermissionNodes.RELOAD, 2))
                .executes(ctx -> reload(ctx.getSource())));
    }

    private static int reload(CommandSourceStack source) {
        ChibiConfig.reload();
        source.sendSuccess(() -> ChibiLang.get("chibiessentials.reload.done"), true);
        return 1;
    }
}

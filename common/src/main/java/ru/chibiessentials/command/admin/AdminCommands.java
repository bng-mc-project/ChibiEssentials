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
        dispatcher.register(Commands.literal("chibiessentials")
                .then(Commands.literal("reload")
                        .requires(ChibiPermissions.require(PermissionNodes.RELOAD))
                        .executes(ctx -> reload(ctx.getSource()))));
    }

    private static int reload(CommandSourceStack source) {
        ChibiConfig.reload();
        source.sendSuccess(() -> ChibiLang.get("chibiessentials.reload.done"), true);
        return 1;
    }
}

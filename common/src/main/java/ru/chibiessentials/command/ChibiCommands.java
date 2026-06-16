package ru.chibiessentials.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import ru.chibiessentials.command.admin.AdminCommands;
import ru.chibiessentials.command.cheat.CheatCommands;
import ru.chibiessentials.command.gamemode.GamemodeCommands;
import ru.chibiessentials.command.home.HomeCommands;
import ru.chibiessentials.command.message.MessageCommands;
import ru.chibiessentials.command.misc.MiscCommands;
import ru.chibiessentials.command.teleport.TeleportCommands;
import ru.chibiessentials.command.teleport.WarpCommands;
import ru.chibiessentials.command.tpa.TpaCommands;
import ru.chibiessentials.command.vanish.VanishCommands;

public final class ChibiCommands {
    private ChibiCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        TeleportCommands.register(dispatcher);
        HomeCommands.register(dispatcher);
        WarpCommands.register(dispatcher);
        TpaCommands.register(dispatcher);
        MessageCommands.register(dispatcher);
        CheatCommands.register(dispatcher);
        VanishCommands.register(dispatcher);
        MiscCommands.register(dispatcher);
        GamemodeCommands.register(dispatcher);
        AdminCommands.register(dispatcher);
    }
}

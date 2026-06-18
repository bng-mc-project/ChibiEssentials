package ru.chibiessentials.command.teleport;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import ru.chibiessentials.config.ChibiLang;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import ru.chibiessentials.data.PlayerData;
import ru.chibiessentials.data.PlayerDataManager;
import ru.chibiessentials.data.WorldData;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.TeleportPos;

public final class TeleportCommands {
    private TeleportCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("back")
                .requires(ChibiPermissions.require(PermissionNodes.BACK, 0))
                .executes(ctx -> back(ctx.getSource().getPlayerOrException())));

        dispatcher.register(Commands.literal("spawn")
                .requires(ChibiPermissions.require(PermissionNodes.SPAWN, 0))
                .executes(ctx -> spawn(ctx.getSource().getPlayerOrException())));

        dispatcher.register(Commands.literal("setspawn")
                .requires(ChibiPermissions.require(PermissionNodes.SETSPAWN, 2))
                .executes(ctx -> setSpawn(ctx.getSource().getPlayerOrException())));

        TpposCommands.register(dispatcher);
    }

    public static int back(ServerPlayer player) {
        return PlayerDataManager.getOrCreate(player).map(data -> {
            TeleportPos pos = data.popTeleportHistory();
            if (pos == null) {
                player.displayClientMessage(ChibiLang.get("chibiessentials.back.empty"), false);
                return 0;
            }
            return data.backTeleporter.teleport(player, p -> pos).runCommand(player);
        }).orElse(0);
    }

    public static int spawn(ServerPlayer player) {
        return PlayerDataManager.getOrCreate(player).map(data ->
                data.spawnTeleporter.teleport(player, p -> resolveSpawn(p)).runCommand(player)
        ).orElse(0);
    }

    public static int setSpawn(ServerPlayer player) {
        if (WorldData.instance == null) return 0;
        WorldData.instance.setSpawn(new TeleportPos(player));
        player.displayClientMessage(ChibiLang.get("chibiessentials.spawn.set"), false);
        return 1;
    }

    private static TeleportPos resolveSpawn(ServerPlayer player) {
        if (WorldData.instance != null && WorldData.instance.getSpawn() != null) {
            return WorldData.instance.getSpawn();
        }
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return null;
        }
        return new TeleportPos(Level.OVERWORLD, overworld.getSharedSpawnPos());
    }
}

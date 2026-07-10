package ru.chibiessentials.command.teleport;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import ru.chibiessentials.config.ChibiLang;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
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
                .requires(ChibiPermissions.require(PermissionNodes.BACK))
                .executes(ctx -> back(ctx.getSource().getPlayerOrException())));

        dispatcher.register(Commands.literal("spawn")
                .requires(ChibiPermissions.require(PermissionNodes.SPAWN))
                .executes(ctx -> spawn(ctx.getSource().getPlayerOrException())));

        dispatcher.register(Commands.literal("setspawn")
                .requires(ChibiPermissions.require(PermissionNodes.SETSPAWN))
                .executes(ctx -> setSpawn(ctx.getSource().getPlayerOrException())));

        dispatcher.register(Commands.literal("tphere")
                .requires(ChibiPermissions.require(PermissionNodes.TPHERE))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> tpHere(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))));

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

    public static int tpHere(ServerPlayer executor, ServerPlayer target) {
        if (executor.equals(target)) {
            executor.displayClientMessage(ChibiLang.get("chibiessentials.tphere.self"), false);
            return 0;
        }
        PlayerDataManager.addTeleportHistory(target);
        int result = new TeleportPos(executor).teleport(target).runCommand(target);
        if (result > 0) {
            executor.displayClientMessage(ChibiLang.get("chibiessentials.tphere.done", target.getDisplayName()), false);
            target.displayClientMessage(ChibiLang.get("chibiessentials.tphere.teleported", executor.getDisplayName()), false);
        }
        return result;
    }

    public static int setSpawn(ServerPlayer player) {
        if (WorldData.instance == null) return 0;
        TeleportPos pos = new TeleportPos(player);
        WorldData.instance.setSpawn(pos);
        syncVanillaSpawn(player.server, pos);
        player.displayClientMessage(ChibiLang.get("chibiessentials.spawn.set"), false);
        return 1;
    }

    public static void applyRespawnSpawn(ServerPlayer player) {
        applySpawnPoint(player);
    }

    public static void applySpawnPoint(ServerPlayer player) {
        if (WorldData.instance == null) return;
        if (player.getRespawnPosition() != null) return;
        TeleportPos spawn = WorldData.instance.getSpawn();
        if (spawn == null) return;
        spawn.teleport(player);
    }

    public static void syncLoadedSpawn(MinecraftServer server) {
        if (WorldData.instance == null) return;
        TeleportPos spawn = WorldData.instance.getSpawn();
        if (spawn != null) {
            syncVanillaSpawn(server, spawn);
        }
    }

    private static void syncVanillaSpawn(MinecraftServer server, TeleportPos pos) {
        if (!pos.getDimension().equals(Level.OVERWORLD)) {
            return;
        }
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        level.setDefaultSpawnPos(pos.getPos(), 0f);
        server.getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(0, server);
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

package ru.chibiessentials;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.common.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import ru.chibiessentials.command.ChibiCommands;
import ru.chibiessentials.command.cheat.CheatCommands;
import ru.chibiessentials.command.tpa.TpaCommands;
import ru.chibiessentials.config.ChibiConfig;
import ru.chibiessentials.data.PlayerDataManager;
import ru.chibiessentials.data.WorldData;
import ru.chibiessentials.util.WarmupCooldownTeleporter;
import ru.chibiessentials.vanish.VanishHandler;

public final class ChibiEventHandler {
    private ChibiEventHandler() {}

    public static void init() {
        LifecycleEvent.SERVER_BEFORE_START.register(ChibiEventHandler::serverStarting);
        LifecycleEvent.SERVER_STOPPED.register(ChibiEventHandler::serverStopped);
        LifecycleEvent.SERVER_LEVEL_SAVE.register(ChibiEventHandler::levelSave);

        TickEvent.SERVER_POST.register(ChibiEventHandler::serverTick);
        TickEvent.PLAYER_POST.register(ChibiEventHandler::playerTick);

        CommandRegistrationEvent.EVENT.register(ChibiEventHandler::registerCommands);

        PlayerEvent.PLAYER_JOIN.register(ChibiEventHandler::playerJoin);
        PlayerEvent.PLAYER_QUIT.register(ChibiEventHandler::playerQuit);
        PlayerEvent.PLAYER_CLONE.register(ChibiEventHandler::playerClone);
        PlayerEvent.CHANGE_DIMENSION.register(ChibiEventHandler::dimensionChange);

        EntityEvent.LIVING_HURT.register(ChibiEventHandler::livingHurt);

        ChibiPlatformEvents.init();
    }

    private static void serverStarting(MinecraftServer server) {
        ChibiConfig.reload();
        PlayerDataManager.init(server);
        WorldData.instance = new WorldData(server);
        WorldData.instance.load();
    }

    private static void serverStopped(MinecraftServer server) {
        WorldData.instance = null;
        PlayerDataManager.clear();
        TpaCommands.REQUESTS.clear();
        VanishHandler.clear();
    }

    private static void levelSave(ServerLevel level) {
        if (WorldData.instance != null) {
            WorldData.instance.saveIfChanged();
        }
        PlayerDataManager.saveAll();
    }

    private static void serverTick(MinecraftServer server) {
        WarmupCooldownTeleporter.tickWarmups(server);
        TpaCommands.tickTimeouts();
    }

    private static void playerTick(net.minecraft.world.entity.player.Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            CheatCommands.reapplyStates(serverPlayer);
        }
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher,
                                         CommandBuildContext registryAccess,
                                         Commands.CommandSelection selection) {
        ChibiCommands.register(dispatcher);
    }

    private static void playerJoin(ServerPlayer player) {
        PlayerDataManager.getOrCreate(player);
        CheatCommands.reapplyStates(player);
        VanishHandler.applyOnJoin(player);
    }

    private static void playerQuit(ServerPlayer player) {
        WarmupCooldownTeleporter.cancelWarmup(player);
        PlayerDataManager.unload(player);
    }

    private static void playerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame) {
        if (!wonGame) {
            PlayerDataManager.addTeleportHistory(oldPlayer);
        }
        PlayerDataManager.getOrCreate(newPlayer);
    }

    private static void dimensionChange(ServerPlayer player, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> oldLevel,
                                        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> newLevel) {
        WarmupCooldownTeleporter.cancelWarmup(player);
    }

    private static dev.architectury.event.EventResult livingHurt(net.minecraft.world.entity.LivingEntity entity,
                                                                 net.minecraft.world.damagesource.DamageSource source,
                                                                 float amount) {
        if (entity instanceof ServerPlayer player) {
            WarmupCooldownTeleporter.cancelWarmup(player);
        }
        return dev.architectury.event.EventResult.pass();
    }
}

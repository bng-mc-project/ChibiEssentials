package ru.chibiessentials.fabric;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.ResourceLocation;
import ru.chibiessentials.ChibiEssentials;
import ru.chibiessentials.command.message.MessageCommands;
import ru.chibiessentials.data.PlayerDataManager;

@SuppressWarnings("unused")
public final class ChibiPlatformEventsImpl {
    private static final ResourceLocation COMMAND_OVERRIDE_PHASE =
            new ResourceLocation(ChibiEssentials.MOD_ID, "command_overrides");

    public static void registerPlatformEvents() {
        CommandRegistrationCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, COMMAND_OVERRIDE_PHASE);
        CommandRegistrationCallback.EVENT.register(COMMAND_OVERRIDE_PHASE,
                (dispatcher, registryAccess, environment) -> MessageCommands.applyOverrides(dispatcher));
    }

    public static void onVanillaTeleport(net.minecraft.server.level.ServerPlayer player) {
        PlayerDataManager.addTeleportHistory(player);
    }
}

package ru.chibiessentials.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import ru.chibiessentials.command.message.MessageCommands;

@SuppressWarnings("unused")
public final class ChibiPlatformEventsImpl {
    public static void registerPlatformEvents() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RegisterCommandsEvent.class,
                (RegisterCommandsEvent event) -> MessageCommands.applyOverrides(event.getDispatcher()));
    }

    public static void onVanillaTeleport(net.minecraft.server.level.ServerPlayer player) {
        // Unused on Forge; mixin handles teleport tracking.
    }
}

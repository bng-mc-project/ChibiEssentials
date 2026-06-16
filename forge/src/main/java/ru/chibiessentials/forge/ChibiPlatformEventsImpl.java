package ru.chibiessentials.forge;

@SuppressWarnings("unused")
public final class ChibiPlatformEventsImpl {
    public static void registerPlatformEvents() {
        // Back history for teleports is handled by ServerPlayerMixin in common.
    }

    public static void onVanillaTeleport(net.minecraft.server.level.ServerPlayer player) {
        // Unused on Forge; mixin handles teleport tracking.
    }
}

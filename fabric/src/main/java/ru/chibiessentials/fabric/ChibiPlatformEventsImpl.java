package ru.chibiessentials.fabric;

import ru.chibiessentials.data.PlayerDataManager;

@SuppressWarnings("unused")
public final class ChibiPlatformEventsImpl {
    public static void registerPlatformEvents() {
    }

    public static void onVanillaTeleport(net.minecraft.server.level.ServerPlayer player) {
        PlayerDataManager.addTeleportHistory(player);
    }
}

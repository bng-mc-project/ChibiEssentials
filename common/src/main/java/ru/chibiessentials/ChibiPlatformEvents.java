package ru.chibiessentials;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.level.ServerPlayer;

public final class ChibiPlatformEvents {
    private ChibiPlatformEvents() {}

    public static void init() {
        registerPlatformEvents();
    }

    @ExpectPlatform
    public static void registerPlatformEvents() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onVanillaTeleport(ServerPlayer player) {
        throw new AssertionError();
    }
}

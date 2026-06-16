package ru.chibiessentials.fabric;

import net.fabricmc.api.ModInitializer;
import ru.chibiessentials.ChibiEssentials;

public final class ChibiEssentialsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ChibiEssentials.init();
    }
}

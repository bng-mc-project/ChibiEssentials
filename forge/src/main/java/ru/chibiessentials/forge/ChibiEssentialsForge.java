package ru.chibiessentials.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import ru.chibiessentials.ChibiEssentials;

@Mod(ChibiEssentials.MOD_ID)
public final class ChibiEssentialsForge {
    public ChibiEssentialsForge() {
        EventBuses.registerModEventBus(ChibiEssentials.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.addListener(ForgePermissionRegistration::onGatherNodes);
        ChibiEssentials.init();
    }
}

package ru.chibiessentials.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.chibiessentials.config.ChibiConfig;
import ru.chibiessentials.data.PlayerDataManager;
import ru.chibiessentials.util.TeleportHistoryHelper;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"))
    private void chibiessentials$trackTeleport(double x, double y, double z, CallbackInfo ci) {
        if (!ChibiConfig.back().onDeathOnly && !TeleportHistoryHelper.shouldSkip()) {
            PlayerDataManager.addTeleportHistory((ServerPlayer) (Object) this);
        }
    }
}

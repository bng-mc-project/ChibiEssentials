package ru.chibiessentials.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SitHandler {
    private static final Map<UUID, Integer> SITTING = new HashMap<>();

    private SitHandler() {}

    public static boolean isSitting(ServerPlayer player) {
        return SITTING.containsKey(player.getUUID());
    }

    public static void unsit(ServerPlayer player) {
        Integer standId = SITTING.remove(player.getUUID());
        if (standId != null) {
            Entity stand = player.level().getEntity(standId);
            if (stand != null) {
                stand.discard();
            }
        }
        if (player.isPassenger()) {
            player.stopRiding();
        }
    }

    public static int sit(ServerPlayer player) {
        if (isSitting(player)) {
            unsit(player);
            return 2;
        }

        ArmorStand stand = EntityType.ARMOR_STAND.create(player.level());
        if (stand == null) {
            return 0;
        }

        stand.moveTo(player.getX(), player.getY() - 0.5, player.getZ(), player.getYRot(), 0f);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        byte flags = (byte) (ArmorStand.CLIENT_FLAG_SMALL | ArmorStand.CLIENT_FLAG_NO_BASEPLATE | ArmorStand.CLIENT_FLAG_MARKER);
        stand.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, flags);
        player.level().addFreshEntity(stand);
        player.startRiding(stand, true);
        SITTING.put(player.getUUID(), stand.getId());
        return 1;
    }

    public static void cleanup(ServerPlayer player) {
        unsit(player);
    }

    public static void tick(ServerPlayer player) {
        if (!isSitting(player)) {
            return;
        }
        Integer standId = SITTING.get(player.getUUID());
        Entity vehicle = player.getVehicle();
        if (vehicle == null || standId == null || vehicle.getId() != standId) {
            SITTING.remove(player.getUUID());
            if (standId != null) {
                Entity stand = player.level().getEntity(standId);
                if (stand != null) {
                    stand.discard();
                }
            }
        }
    }
}

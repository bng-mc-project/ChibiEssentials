package ru.chibiessentials.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public final class PlayerResolver {
    public record Target(UUID uuid, String name, ServerPlayer online) {
        public boolean isOnline() {
            return online != null;
        }
    }

    private PlayerResolver() {}

    public static Optional<Target> resolve(MinecraftServer server, String name) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) {
            return Optional.of(new Target(online.getUUID(), online.getGameProfile().getName(), online));
        }

        return server.getProfileCache().get(name)
                .map(profile -> new Target(profile.getId(), profile.getName(), null));
    }

    public static Optional<Target> resolve(MinecraftServer server, UUID uuid) {
        ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        if (online != null) {
            return Optional.of(new Target(uuid, online.getGameProfile().getName(), online));
        }

        return server.getProfileCache().get(uuid)
                .map(profile -> new Target(uuid, profile.getName(), null));
    }

    public static Optional<Target> resolve(MinecraftServer server, GameProfile profile) {
        if (profile.getId() != null) {
            return resolve(server, profile.getId());
        }
        if (profile.getName() != null) {
            return resolve(server, profile.getName());
        }
        return Optional.empty();
    }
}

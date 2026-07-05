package ru.chibiessentials.chat;

import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import ru.chibiessentials.config.ChibiConfig;
import ru.chibiessentials.config.ChibiLang;
import ru.chibiessentials.data.PlayerData;
import ru.chibiessentials.data.PlayerDataManager;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.MessageUtil;
import ru.chibiessentials.vanish.VanishHandler;

public final class ChatHandler {
    private ChatHandler() {}

    public static EventResult onReceived(ServerPlayer player, Component component) {
        if (player == null) {
            return EventResult.pass();
        }
        return handle(player, component.getString(), null) ? EventResult.interruptFalse() : EventResult.pass();
    }

    public static int send(ServerPlayer player, String message, Boolean forceGlobal) {
        return handle(player, message, forceGlobal) ? 1 : 0;
    }

    public static int setGlobalMode(ServerPlayer player, boolean global) {
        return PlayerDataManager.getOrCreate(player).map(data -> {
            data.setChatGlobal(global);
            player.displayClientMessage(ChibiLang.get(global
                    ? "chibiessentials.chat.mode.global"
                    : "chibiessentials.chat.mode.local"), false);
            return 1;
        }).orElse(0);
    }

    private static boolean handle(ServerPlayer sender, String rawMessage, Boolean forceGlobal) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return true;
        }

        if (!ChibiPermissions.has(sender, PermissionNodes.CHAT)) {
            sender.displayClientMessage(ChibiLang.get("chibiessentials.chat.no_permission"), false);
            return true;
        }

        String message = rawMessage;
        boolean global;
        if (forceGlobal != null) {
            global = forceGlobal;
        } else {
            String globalPrefix = ChibiConfig.chat().globalPrefix;
            if (globalPrefix != null && !globalPrefix.isEmpty() && message.startsWith(globalPrefix)) {
                message = message.substring(globalPrefix.length()).stripLeading();
                global = true;
            } else {
                global = PlayerDataManager.getOrCreate(sender).map(PlayerData::isChatGlobal).orElse(isDefaultGlobal());
            }
        }

        if (message.isBlank()) {
            return true;
        }

        Component formatted = formatMessage(sender, message, global);
        broadcast(sender, formatted, global);
        return true;
    }

    private static boolean isDefaultGlobal() {
        return "global".equalsIgnoreCase(ChibiConfig.chat().defaultMode);
    }

    private static Component formatMessage(ServerPlayer sender, String message, boolean global) {
        String formatKey = global ? "chibiessentials.chat.format.global" : "chibiessentials.chat.format.local";
        String format = ChibiLang.getString(formatKey);
        String prefix = resolveMetaOrDefault(sender, PermissionNodes.META_CHAT_PREFIX, "chibiessentials.chat.prefix.default");
        String suffix = resolveMetaOrDefault(sender, PermissionNodes.META_CHAT_SUFFIX, "chibiessentials.chat.suffix.default");
        String nick = resolveNick(sender);
        String playerName = sender.getGameProfile().getName();

        String parsed = format
                .replace("{prefix}", prefix)
                .replace("{suffix}", suffix)
                .replace("{nick}", nick)
                .replace("{player}", playerName)
                .replace("{world}", sender.level().dimension().location().toString());

        Component messageComponent = MessageUtil.formatPlayerMessage(sender, message);
        return MessageUtil.insertMessage(parsed, messageComponent);
    }

    private static String resolveMetaOrDefault(ServerPlayer player, String metaNode, String langKey) {
        String meta = ChibiPermissions.getString(player, metaNode, "");
        if (meta != null && !meta.isBlank()) {
            return meta;
        }
        return ChibiLang.getString(langKey);
    }

    private static String resolveNick(ServerPlayer player) {
        String nick = ChibiPermissions.getString(player, PermissionNodes.META_CHAT_NICK, "");
        if (nick == null || nick.isBlank()) {
            nick = ChibiLang.getString("chibiessentials.chat.nick.default");
        }
        return nick.replace("{player}", player.getGameProfile().getName());
    }

    private static void broadcast(ServerPlayer sender, Component message, boolean global) {
        MinecraftServer server = sender.server;
        if (VanishHandler.isVanished(sender) && ChibiConfig.vanish().hideChat) {
            sender.sendSystemMessage(message);
            return;
        }

        double radiusSq = (double) ChibiConfig.chat().localRadius * ChibiConfig.chat().localRadius;
        for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
            if (!canReceive(sender, recipient, global, radiusSq)) {
                continue;
            }
            recipient.sendSystemMessage(message);
        }
    }

    private static boolean canReceive(ServerPlayer sender, ServerPlayer recipient, boolean global, double radiusSq) {
        if (!VanishHandler.canSee(recipient, sender)) {
            return false;
        }
        if (global) {
            return true;
        }
        if (sender.level() != recipient.level()) {
            return false;
        }
        return sender.distanceToSqr(recipient) <= radiusSq;
    }
}

package ru.chibiessentials.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import ru.chibiessentials.config.ChibiConfig;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;

public final class MessageUtil {
    private MessageUtil() {}

    public static Component formatPrivateMessage(ServerPlayer sender, String message) {
        String format = ChibiConfig.messages().format;
        String parsed = format
                .replace("{sender}", sender.getGameProfile().getName())
                .replace("{message}", "");
        return insertMessage(parsed, formatPlayerMessage(sender, message));
    }

    public static Component formatPlayerMessage(ServerPlayer player, String message) {
        if (ChibiPermissions.has(player, PermissionNodes.CHAT_COLOR)) {
            return colorize(message);
        }
        return Component.literal(stripColorCodes(message));
    }

    public static Component insertMessage(String template, Component message) {
        String placeholder = "{message}";
        int index = template.indexOf(placeholder);
        if (index < 0) {
            return colorize(template).append(message);
        }
        MutableComponent result = Component.empty();
        if (index > 0) {
            result.append(colorize(template.substring(0, index)));
        }
        result.append(message);
        int after = index + placeholder.length();
        if (after < template.length()) {
            result.append(colorize(template.substring(after)));
        }
        return result;
    }

    public static String stripColorCodes(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                if (ChatFormatting.getByCode(code) != null) {
                    i++;
                    continue;
                }
            }
            result.append(c);
        }
        return result.toString();
    }

    public static MutableComponent colorize(String text) {
        MutableComponent result = Component.empty();
        Style style = Style.EMPTY;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()) {
                if (!current.isEmpty()) {
                    result.append(Component.literal(current.toString()).withStyle(style));
                    current.setLength(0);
                }
                char code = text.charAt(++i);
                if (code == 'r' || code == 'R') {
                    style = Style.EMPTY;
                } else {
                    ChatFormatting formatting = ChatFormatting.getByCode(code);
                    if (formatting != null) {
                        style = styleForCode(code, formatting, style);
                    }
                }
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            result.append(Component.literal(current.toString()).withStyle(style));
        }
        return result;
    }

    private static Style styleForCode(char code, ChatFormatting formatting, Style current) {
        return switch (Character.toLowerCase(code)) {
            case 'l' -> current.withBold(true);
            case 'o' -> current.withItalic(true);
            case 'n' -> current.withUnderlined(true);
            case 'm' -> current.withStrikethrough(true);
            case 'k' -> current.withObfuscated(true);
            default -> Style.EMPTY.withColor(formatting);
        };
    }
}

package ru.chibiessentials.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import ru.chibiessentials.config.ChibiConfig;

public final class MessageUtil {
    private MessageUtil() {}

    public static Component formatPrivateMessage(String sender, String message) {
        String format = ChibiConfig.messages().format;
        String parsed = format
                .replace("{sender}", sender)
                .replace("{message}", message);
        return colorize(parsed);
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
                ChatFormatting formatting = ChatFormatting.getByCode(text.charAt(++i));
                if (formatting != null) {
                    style = Style.EMPTY.withColor(formatting);
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
}

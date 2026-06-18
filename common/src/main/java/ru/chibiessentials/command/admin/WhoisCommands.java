package ru.chibiessentials.command.admin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import ru.chibiessentials.config.ChibiLang;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class WhoisCommands {
    private WhoisCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("whois")
                .requires(ChibiPermissions.require(PermissionNodes.WHOIS, 2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> whois(ctx.getSource().getPlayerOrException(),
                                EntityArgument.getPlayer(ctx, "player")))));
    }

    public static int whois(ServerPlayer executor, ServerPlayer target) {
        String ip = target.getIpAddress();
        if (ip == null || ip.isBlank()) {
            executor.displayClientMessage(ChibiLang.get("chibiessentials.whois.unavailable", target.getDisplayName()), false);
            return 0;
        }

        String url = "https://2ip.ru/ip/" + URLEncoder.encode(ip, StandardCharsets.UTF_8) + "/";
        MutableComponent ipComponent = Component.literal(ip).setStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(url))));

        executor.displayClientMessage(ChibiLang.get("chibiessentials.whois", target.getDisplayName(), ipComponent), false);
        return 1;
    }
}

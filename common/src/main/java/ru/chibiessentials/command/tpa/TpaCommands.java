package ru.chibiessentials.command.tpa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import ru.chibiessentials.config.ChibiConfig;
import ru.chibiessentials.config.ChibiLang;
import ru.chibiessentials.data.PlayerData;
import ru.chibiessentials.data.PlayerDataManager;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.TeleportPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class TpaCommands {
    public record TpaRequest(String id, UUID source, UUID target, boolean here, long created) {}

    public static final Map<String, TpaRequest> REQUESTS = new HashMap<>();

    private TpaCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpa")
                .requires(ChibiPermissions.require(PermissionNodes.TPA))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> tpa(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "target"), false))));

        dispatcher.register(Commands.literal("tpahere")
                .requires(ChibiPermissions.require(PermissionNodes.TPA))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> tpa(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "target"), true))));

        dispatcher.register(Commands.literal("tpaccept")
                .requires(ChibiPermissions.require(PermissionNodes.TPACCEPT))
                .executes(ctx -> tpaccept(ctx.getSource().getPlayerOrException(), findLatestFor(ctx.getSource().getPlayerOrException())))
                .then(Commands.argument("id", StringArgumentType.string())
                        .executes(ctx -> tpaccept(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id")))));

        dispatcher.register(Commands.literal("tpdeny")
                .requires(ChibiPermissions.require(PermissionNodes.TPDENY))
                .executes(ctx -> tpdeny(ctx.getSource().getPlayerOrException(), findLatestFor(ctx.getSource().getPlayerOrException())))
                .then(Commands.argument("id", StringArgumentType.string())
                        .executes(ctx -> tpdeny(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id")))));

        dispatcher.register(Commands.literal("tpacancel")
                .requires(ChibiPermissions.require(PermissionNodes.TPACANCEL))
                .executes(ctx -> tpacancel(ctx.getSource().getPlayerOrException())));
    }

    private static String findLatestFor(ServerPlayer player) {
        TpaRequest latest = null;
        for (TpaRequest request : REQUESTS.values()) {
            if (request.target().equals(player.getUUID())) {
                if (latest == null || request.created() > latest.created()) {
                    latest = request;
                }
            }
        }
        return latest != null ? latest.id() : "";
    }

    public static void tickTimeouts() {
        long timeout = ChibiConfig.tpa().requestTimeoutSeconds * 1000L;
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, TpaRequest>> it = REQUESTS.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue().created() > timeout) {
                it.remove();
            }
        }
    }

    private static TpaRequest create(UUID source, UUID target, boolean here) {
        String key;
        do {
            key = String.format("%08X", new Random().nextInt());
        } while (REQUESTS.containsKey(key));
        TpaRequest request = new TpaRequest(key, source, target, here, System.currentTimeMillis());
        REQUESTS.put(key, request);
        return request;
    }

    public static int tpa(ServerPlayer player, ServerPlayer target, boolean here) {
        if (player.getUUID().equals(target.getUUID())) {
            player.displayClientMessage(ChibiLang.get("chibiessentials.tpa.self"), false);
            return 0;
        }

        PlayerData sourceData = PlayerDataManager.getOrCreate(player).orElse(null);
        PlayerData targetData = PlayerDataManager.getOrCreate(target).orElse(null);
        if (sourceData == null || targetData == null) return 0;

        if (REQUESTS.values().stream().anyMatch(r -> r.source().equals(player.getUUID()) && r.target().equals(target.getUUID()))) {
            player.displayClientMessage(ChibiLang.get("chibiessentials.tpa.already_sent"), false);
            return 0;
        }

        TeleportPos.TeleportResult cooldown = here ? targetData.tpaTeleporter.checkCooldown() : sourceData.tpaTeleporter.checkCooldown();
        if (!cooldown.isSuccess()) {
            return cooldown.runCommand(player);
        }

        TpaRequest request = create(player.getUUID(), target.getUUID(), here);

        MutableComponent header = ChibiLang.get("chibiessentials.tpa.request",
                (here ? target : player).getDisplayName().copy().withStyle(ChatFormatting.YELLOW),
                (here ? player : target).getDisplayName().copy().withStyle(ChatFormatting.YELLOW));
        target.sendSystemMessage(header);

        MutableComponent actions = ChibiLang.get("chibiessentials.tpa.click");
        actions.append(ChibiLang.get("chibiessentials.tpa.accept").setStyle(Style.EMPTY
                .withColor(ChatFormatting.GREEN).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + request.id()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ChibiLang.get("chibiessentials.tpa.accept.hover")))));
        actions.append(Component.literal(" | "));
        actions.append(ChibiLang.get("chibiessentials.tpa.deny").setStyle(Style.EMPTY
                .withColor(ChatFormatting.RED).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + request.id()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ChibiLang.get("chibiessentials.tpa.deny.hover")))));
        target.sendSystemMessage(actions);

        player.displayClientMessage(ChibiLang.get("chibiessentials.tpa.sent"), false);
        return 1;
    }

    public static int tpaccept(ServerPlayer player, String id) {
        TpaRequest request = REQUESTS.get(id);
        if (request == null || !request.target().equals(player.getUUID())) {
            player.displayClientMessage(ChibiLang.get("chibiessentials.tpa.invalid"), false);
            return 0;
        }

        ServerPlayer sourcePlayer = player.server.getPlayerList().getPlayer(request.source());
        if (sourcePlayer == null) {
            player.displayClientMessage(ChibiLang.get("chibiessentials.tpa.offline"), false);
            REQUESTS.remove(id);
            return 0;
        }

        PlayerData sourceData = PlayerDataManager.getOrCreate(sourcePlayer).orElse(null);
        PlayerData targetData = PlayerDataManager.getOrCreate(player).orElse(null);
        if (sourceData == null || targetData == null) return 0;

        ServerPlayer teleporter = request.here() ? player : sourcePlayer;
        Runnable onSuccess = () -> {
            REQUESTS.remove(id);
            sourcePlayer.displayClientMessage(ChibiLang.get("chibiessentials.tpa.accepted"), false);
            player.displayClientMessage(ChibiLang.get("chibiessentials.tpa.accepted"), false);
        };

        TeleportPos.TeleportResult result = request.here()
                ? targetData.tpaTeleporter.teleport(player, p -> new TeleportPos(sourcePlayer), onSuccess)
                : sourceData.tpaTeleporter.teleport(sourcePlayer, p -> new TeleportPos(player), onSuccess);

        return result.runCommand(teleporter);
    }

    public static int tpdeny(ServerPlayer player, String id) {
        TpaRequest request = REQUESTS.get(id);
        if (request == null || !request.target().equals(player.getUUID())) {
            player.displayClientMessage(ChibiLang.get("chibiessentials.tpa.invalid"), false);
            return 0;
        }
        REQUESTS.remove(id);
        player.displayClientMessage(ChibiLang.get("chibiessentials.tpa.denied"), false);
        ServerPlayer source = player.server.getPlayerList().getPlayer(request.source());
        if (source != null) {
            source.displayClientMessage(ChibiLang.get("chibiessentials.tpa.denied_source"), false);
        }
        return 1;
    }

    public static int tpacancel(ServerPlayer player) {
        boolean removed = REQUESTS.entrySet().removeIf(e -> e.getValue().source().equals(player.getUUID()));
        if (removed) {
            player.displayClientMessage(ChibiLang.get("chibiessentials.tpa.cancelled"), false);
            return 1;
        }
        player.displayClientMessage(ChibiLang.get("chibiessentials.tpa.nothing_to_cancel"), false);
        return 0;
    }
}

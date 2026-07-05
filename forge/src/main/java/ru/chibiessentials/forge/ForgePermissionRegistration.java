package ru.chibiessentials.forge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;

import java.util.HashMap;
import java.util.Map;

public final class ForgePermissionRegistration {
    public static final Map<String, PermissionNode<Boolean>> NODES = new HashMap<>();
    public static final Map<String, PermissionNode<String>> META_NODES = new HashMap<>();

    private ForgePermissionRegistration() {}

    public static void onGatherNodes(PermissionGatherEvent.Nodes event) {
        registerBoolean(event, PermissionNodes.BACK);
        registerBoolean(event, PermissionNodes.HOME);
        registerBoolean(event, PermissionNodes.SETHOME);
        registerBoolean(event, PermissionNodes.SPAWN);
        registerBoolean(event, PermissionNodes.SETSPAWN);
        registerBoolean(event, PermissionNodes.TPPOS);
        registerBoolean(event, PermissionNodes.TPHERE);
        registerBoolean(event, PermissionNodes.WARP);
        registerBoolean(event, PermissionNodes.WARP_CREATE);
        registerBoolean(event, PermissionNodes.WARP_DELETE);
        registerBoolean(event, PermissionNodes.WARP_LIST);
        registerBoolean(event, PermissionNodes.TPA);
        registerBoolean(event, PermissionNodes.TPACCEPT);
        registerBoolean(event, PermissionNodes.TPDENY);
        registerBoolean(event, PermissionNodes.TPACANCEL);
        registerBoolean(event, PermissionNodes.CHAT);
        registerBoolean(event, PermissionNodes.CHAT_COLOR);
        registerBoolean(event, PermissionNodes.MSG);
        registerBoolean(event, PermissionNodes.REPLY);
        registerBoolean(event, PermissionNodes.EC);
        registerBoolean(event, PermissionNodes.EC_OTHERS);
        registerBoolean(event, PermissionNodes.HEAD);
        registerBoolean(event, PermissionNodes.HAT);
        registerBoolean(event, PermissionNodes.SIT);
        registerBoolean(event, PermissionNodes.FEED);
        registerBoolean(event, PermissionNodes.WORKBENCH);
        registerBoolean(event, PermissionNodes.FLY);
        registerBoolean(event, PermissionNodes.INVSEE);
        registerBoolean(event, PermissionNodes.VANISH);
        registerBoolean(event, PermissionNodes.VANISH_SEE);
        registerBoolean(event, PermissionNodes.GOD);
        registerBoolean(event, PermissionNodes.HEAL);
        registerBoolean(event, PermissionNodes.RELOAD);
        registerBoolean(event, PermissionNodes.WHOIS);
        registerBoolean(event, PermissionNodes.GM_CREATIVE);
        registerBoolean(event, PermissionNodes.GM_SURVIVAL);
        registerBoolean(event, PermissionNodes.GM_ADVENTURE);
        registerBoolean(event, PermissionNodes.GM_SPECTATOR);

        registerMeta(event, PermissionNodes.META_HOME_MAX);
        registerMeta(event, PermissionNodes.META_BACK_MAX);
        registerMeta(event, PermissionNodes.META_CHAT_PREFIX);
        registerMeta(event, PermissionNodes.META_CHAT_SUFFIX);
        registerMeta(event, PermissionNodes.META_CHAT_NICK);
        registerMeta(event, PermissionNodes.metaCooldown("back"));
        registerMeta(event, PermissionNodes.metaCooldown("spawn"));
        registerMeta(event, PermissionNodes.metaCooldown("home"));
        registerMeta(event, PermissionNodes.metaCooldown("warp"));
        registerMeta(event, PermissionNodes.metaCooldown("tpa"));
        registerMeta(event, PermissionNodes.metaWarmup("back"));
        registerMeta(event, PermissionNodes.metaWarmup("spawn"));
        registerMeta(event, PermissionNodes.metaWarmup("home"));
        registerMeta(event, PermissionNodes.metaWarmup("warp"));
        registerMeta(event, PermissionNodes.metaWarmup("tpa"));
    }

    private static void registerBoolean(PermissionGatherEvent.Nodes event, String node) {
        int fallbackOp = ChibiPermissions.DEFAULT_OP_FALLBACK;
        PermissionNode<Boolean> permissionNode = new PermissionNode<>(ChibiPermissions.PERMISSION_NAMESPACE, node, PermissionTypes.BOOLEAN,
                (player, playerUuid, context) -> player instanceof ServerPlayer && ((ServerPlayer) player).hasPermissions(fallbackOp));
        NODES.put(ChibiPermissions.fullNode(node), permissionNode);
        event.addNodes(permissionNode);
    }

    private static void registerMeta(PermissionGatherEvent.Nodes event, String node) {
        String[] parts = node.split("\\.", 2);
        String namespace = parts.length > 1 ? parts[0] : ChibiPermissions.PERMISSION_NAMESPACE;
        String name = parts.length > 1 ? parts[1] : node;
        PermissionNode<String> permissionNode = new PermissionNode<>(namespace, name, PermissionTypes.STRING,
                (player, playerUuid, context) -> null);
        META_NODES.put(node, permissionNode);
        event.addNodes(permissionNode);
    }
}

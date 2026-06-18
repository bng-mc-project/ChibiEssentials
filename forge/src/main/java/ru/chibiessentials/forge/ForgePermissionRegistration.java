package ru.chibiessentials.forge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import ru.chibiessentials.ChibiEssentials;
import ru.chibiessentials.permission.PermissionNodes;

import java.util.HashMap;
import java.util.Map;

public final class ForgePermissionRegistration {
    public static final Map<String, PermissionNode<Boolean>> NODES = new HashMap<>();
    public static final Map<String, PermissionNode<String>> META_NODES = new HashMap<>();

    private ForgePermissionRegistration() {}

    public static void onGatherNodes(PermissionGatherEvent.Nodes event) {
        registerBoolean(event, PermissionNodes.BACK, 0);
        registerBoolean(event, PermissionNodes.HOME, 0);
        registerBoolean(event, PermissionNodes.SETHOME, 0);
        registerBoolean(event, PermissionNodes.SPAWN, 0);
        registerBoolean(event, PermissionNodes.SETSPAWN, 2);
        registerBoolean(event, PermissionNodes.TPPOS, 2);
        registerBoolean(event, PermissionNodes.WARP, 0);
        registerBoolean(event, PermissionNodes.WARP_CREATE, 2);
        registerBoolean(event, PermissionNodes.WARP_DELETE, 2);
        registerBoolean(event, PermissionNodes.WARP_LIST, 0);
        registerBoolean(event, PermissionNodes.TPA, 0);
        registerBoolean(event, PermissionNodes.TPACCEPT, 0);
        registerBoolean(event, PermissionNodes.TPDENY, 0);
        registerBoolean(event, PermissionNodes.TPACANCEL, 0);
        registerBoolean(event, PermissionNodes.MSG, 0);
        registerBoolean(event, PermissionNodes.REPLY, 0);
        registerBoolean(event, PermissionNodes.EC, 0);
        registerBoolean(event, PermissionNodes.EC_OTHERS, 2);
        registerBoolean(event, PermissionNodes.HEAD, 2);
        registerBoolean(event, PermissionNodes.FLY, 2);
        registerBoolean(event, PermissionNodes.INVSEE, 2);
        registerBoolean(event, PermissionNodes.VANISH, 2);
        registerBoolean(event, PermissionNodes.VANISH_SEE, 2);
        registerBoolean(event, PermissionNodes.GOD, 2);
        registerBoolean(event, PermissionNodes.HEAL, 2);
        registerBoolean(event, PermissionNodes.RELOAD, 2);
        registerBoolean(event, PermissionNodes.WHOIS, 2);
        registerBoolean(event, PermissionNodes.GM_CREATIVE, 2);
        registerBoolean(event, PermissionNodes.GM_SURVIVAL, 2);
        registerBoolean(event, PermissionNodes.GM_ADVENTURE, 2);
        registerBoolean(event, PermissionNodes.GM_SPECTATOR, 2);
        
        registerMeta(event, PermissionNodes.META_HOME_MAX);
        registerMeta(event, PermissionNodes.META_BACK_MAX);
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

    private static void registerBoolean(PermissionGatherEvent.Nodes event, String node, int fallbackOp) {
        PermissionNode<Boolean> permissionNode = new PermissionNode<>(ChibiEssentials.MOD_ID, node, PermissionTypes.BOOLEAN,
                (player, playerUuid, context) -> player instanceof ServerPlayer && ((ServerPlayer) player).hasPermissions(fallbackOp));
        NODES.put(ChibiEssentials.MOD_ID + "." + node, permissionNode);
        event.addNodes(permissionNode);
    }

    private static void registerMeta(PermissionGatherEvent.Nodes event, String node) {
        String[] parts = node.split("\\.", 2);
        String namespace = parts.length > 1 ? parts[0] : ChibiEssentials.MOD_ID;
        String name = parts.length > 1 ? parts[1] : node;
        PermissionNode<String> permissionNode = new PermissionNode<>(namespace, name, PermissionTypes.STRING,
                (player, playerUuid, context) -> null);
        META_NODES.put(node, permissionNode);
        event.addNodes(permissionNode);
    }
}

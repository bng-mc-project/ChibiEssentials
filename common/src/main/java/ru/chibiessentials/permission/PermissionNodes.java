package ru.chibiessentials.permission;

public final class PermissionNodes {
    public static final String BACK = "back";
    public static final String HOME = "home";
    public static final String SETHOME = "sethome";
    public static final String SPAWN = "spawn";
    public static final String SETSPAWN = "setspawn";
    public static final String TPPOS = "tppos";
    public static final String TPHERE = "tphere";
    public static final String WARP = "warp";
    public static final String WARP_CREATE = "warp.create";
    public static final String WARP_DELETE = "warp.delete";
    public static final String WARP_LIST = "warp.list";
    public static final String TPA = "tpa";
    public static final String TPACCEPT = "tpaccept";
    public static final String TPDENY = "tpdeny";
    public static final String TPACANCEL = "tpacancel";
    public static final String CHAT = "chat";
    public static final String CHAT_COLOR = "chat.color";
    public static final String MSG = "msg";
    public static final String REPLY = "reply";
    public static final String EC = "ec";
    public static final String EC_OTHERS = "ec.others";
    public static final String HEAD = "head";
    public static final String HAT = "hat";
    public static final String SIT = "sit";
    public static final String FEED = "feed";
    public static final String WORKBENCH = "workbench";
    public static final String FLY = "fly";
    public static final String INVSEE = "invsee";
    public static final String INVSEE_EDIT = "invsee.edit";
    public static final String VANISH = "vanish";
    public static final String VANISH_SEE = "vanish.see";
    public static final String GOD = "god";
    public static final String HEAL = "heal";
    public static final String RELOAD = "reload";
    public static final String WHOIS = "whois";
    public static final String GM_CREATIVE = "gamemode.creative";
    public static final String GM_SURVIVAL = "gamemode.survival";
    public static final String GM_ADVENTURE = "gamemode.adventure";
    public static final String GM_SPECTATOR = "gamemode.spectator";

    public static final String META_HOME_MAX = "chibiessentials.home.max";
    public static final String META_BACK_MAX = "chibiessentials.back.max";
    public static final String META_CHAT_PREFIX = "chibiessentials.chat.prefix";
    public static final String META_CHAT_SUFFIX = "chibiessentials.chat.suffix";
    public static final String META_CHAT_NICK = "chibiessentials.chat.nick";

    public static String warpNode(String name) {
        return "warp." + name.toLowerCase();
    }

    public static String metaCooldown(String cmd) {
        return "chibiessentials." + cmd + ".cooldown";
    }

    public static String metaWarmup(String cmd) {
        return "chibiessentials." + cmd + ".warmup";
    }

    private PermissionNodes() {}
}

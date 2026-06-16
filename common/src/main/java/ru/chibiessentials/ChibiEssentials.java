package ru.chibiessentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.chibiessentials.config.ChibiConfig;

public final class ChibiEssentials {
    public static final String MOD_ID = "chibi_essentials";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        ChibiConfig.init();
        ChibiEventHandler.init();
    }
}

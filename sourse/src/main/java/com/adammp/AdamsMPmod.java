package com.adammp;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdamsMPmod
implements ModInitializer {
    public static final String MOD_ID = "adams-mp-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"adams-mp-mod");

    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
    }
}


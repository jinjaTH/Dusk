package com.dusk;

import com.dusk.event.PhobiaEventHandler;
import com.dusk.network.DuskNetwork;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dusk implements ModInitializer {

    public static final String MOD_ID = "dusk";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        DuskNetwork.registerServer();
        PhobiaEventHandler.register();
        LOGGER.info("Dusk initialized.");
    }
}

package com.example.unlock;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnlockMod implements ModInitializer {

    public static final String MOD_ID = "unlock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        UnlockConfig.load();
        ReachAttributeHandler.register();
        LOGGER.info("Unlock mod initialized.");
    }
}

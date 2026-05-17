package com.example.unlock;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class UnlockModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Client-side initialization; config already loaded in main entrypoint
    }
}

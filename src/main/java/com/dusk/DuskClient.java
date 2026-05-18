package com.dusk;

import com.dusk.client.ClientDreadState;
import com.dusk.effect.MovementEffects;
import com.dusk.effect.SoundEffects;
import com.dusk.effect.VisualEffects;
import com.dusk.network.DreadStagePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class DuskClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(DreadStagePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                float score = payload.normalizedScore();
                if (score <= 0f) {
                    ClientDreadState.reset();
                } else {
                    ClientDreadState.targetScore = score;
                }
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) return;
            ClientDreadState.tick();
            SoundEffects.clientTick();
            MovementEffects.clientTick();
        });

        VisualEffects.register();
    }
}

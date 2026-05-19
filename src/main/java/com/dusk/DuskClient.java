package com.dusk;

import com.dusk.client.ClientDreadState;
import com.dusk.effect.MovementEffects;
import com.dusk.effect.PhantomEffects;
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
                if (payload.hardReset()) {
                    ClientDreadState.reset();
                    SoundEffects.stopAllDuskSounds();
                    context.client().getSoundManager().play(
                        net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.PLAYER_BREATH, 0.55f, 0.85f));
                } else {
                    // Normal update — let lerp handle the transition.
                    // For light recovery (target=0), the lerp feels like consciousness returning.
                    ClientDreadState.targetScore = payload.normalizedScore();
                }
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) return;
            ClientDreadState.tick();
            SoundEffects.clientTick();
            MovementEffects.clientTick();
            PhantomEffects.clientTick();

            // Block inventory when hotbar has fallen (score >= 0.78)
            if (ClientDreadState.score >= 0.78f
                    && client.screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen) {
                client.setScreen(null);
            }
        });

        VisualEffects.register();
    }
}

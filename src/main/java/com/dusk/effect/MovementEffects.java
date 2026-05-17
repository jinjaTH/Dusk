package com.dusk.effect;

import com.dusk.client.ClientDreadState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MovementEffects {

    public static void clientTick() {
        // Countdown freeze timer
        if (ClientDreadState.frozen && ClientDreadState.freezeTicks > 0) {
            ClientDreadState.freezeTicks--;
            if (ClientDreadState.freezeTicks <= 0) {
                ClientDreadState.frozen = false;
            }
        }

        // Slowly drift the drift angle for Nyctophobia stage 4
        if (ClientDreadState.driftAngle != 0) {
            ClientDreadState.driftAngle = (float)(ClientDreadState.driftAngle
                + Math.sin(System.currentTimeMillis() * 0.001) * 0.3f);
            ClientDreadState.driftAngle = Math.max(-12f, Math.min(12f, ClientDreadState.driftAngle));
        }
    }
}

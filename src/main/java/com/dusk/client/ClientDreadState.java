package com.dusk.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientDreadState {

    public static int nyctophobiaStage = 0;

    // Visual
    public static float fovOffset      = 0f;
    public static float vignetteAlpha  = 0f;
    public static float shakeIntensity = 0f;
    public static float fadeAlpha      = 0f;

    // Movement
    public static float driftAngle     = 0f;
    public static float speedMultiplier = 1f;

    // Sound
    public static boolean silenceAmbient = false;

    public static void updateFromPacket(int phobiaId, int stage) {
        if (phobiaId == 0) applyNycto(stage);
    }

    private static void applyNycto(int stage) {
        nyctophobiaStage = stage;
        silenceAmbient   = stage >= 1;
        vignetteAlpha    = stage >= 3 ? 0.35f : 0f;
        fovOffset        = stage >= 3 ? -5f   : 0f;
        driftAngle       = stage >= 4 ? 8f    : 0f;
        shakeIntensity   = 0f;

        if (stage == 6) fadeAlpha = 1f;
        if (stage == 0) {
            fadeAlpha       = 0f;
            speedMultiplier = 1f;
            driftAngle      = 0f;
        }
    }
}

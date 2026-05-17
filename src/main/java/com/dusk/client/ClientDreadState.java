package com.dusk.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientDreadState {

    public static int nyctophobiaStage   = 0;
    public static int acrophobiaStage    = 0;
    public static int thalassophobiaStage = 0;

    // Visual
    public static float fovOffset       = 0f;
    public static float vignetteAlpha   = 0f;
    public static float shakeIntensity  = 0f;
    public static float fadeAlpha       = 0f;  // 0 = transparent, 1 = full black
    public static float fogMultiplier   = 1f;

    // Movement
    public static float driftAngle      = 0f;
    public static float speedMultiplier = 1f;
    public static boolean frozen        = false;
    public static int    freezeTicks    = 0;

    // Sound
    public static boolean silenceAmbient = false;

    public static void updateFromPacket(int phobiaId, int stage) {
        switch (phobiaId) {
            case 0 -> applyNycto(stage);
            case 1 -> applyAcro(stage);
            case 2 -> applyThala(stage);
        }
    }

    private static void applyNycto(int stage) {
        nyctophobiaStage = stage;
        silenceAmbient   = stage >= 1;
        vignetteAlpha    = stage >= 3 ? 0.35f : 0f;
        fovOffset        = stage >= 3 ? -5f   : 0f;
        driftAngle       = stage >= 4 ? 8f    : 0f;
        shakeIntensity   = 0f;
        fogMultiplier    = 1f;

        if (stage == 6) fadeAlpha = 1f;
        if (stage == 0) { fadeAlpha = 0f; speedMultiplier = 1f; frozen = false; driftAngle = 0f; }
    }

    private static void applyAcro(int stage) {
        acrophobiaStage  = stage;
        vignetteAlpha    = Math.max(vignetteAlpha, stage >= 1 ? 0.25f : 0f);
        fovOffset        = Math.min(fovOffset, stage >= 2 ? -8f : 0f);
        shakeIntensity   = stage >= 3 ? 0.6f : 0f;
        speedMultiplier  = stage >= 4 ? 0.55f : 1f;
        frozen           = stage == 5;
        freezeTicks      = frozen ? 40 : 0; // 2s

        if (stage == 0) { shakeIntensity = 0f; speedMultiplier = 1f; frozen = false; }
    }

    private static void applyThala(int stage) {
        thalassophobiaStage = stage;
        fogMultiplier    = stage >= 2 ? 0.35f : 1f;
        shakeIntensity   = Math.max(shakeIntensity, stage >= 5 ? 0.4f : 0f);
        speedMultiplier  = Math.min(speedMultiplier, stage >= 5 ? 0.65f : 1f);

        if (stage == 6) fadeAlpha = 1f;
        if (stage == 0) { fogMultiplier = 1f; }
    }
}

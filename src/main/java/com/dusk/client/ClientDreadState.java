package com.dusk.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientDreadState {

    public static final float SILENCE_START  = 0.08f;
    public static final float SILENCE_FULL   = 0.30f;
    public static final float VIGNETTE_START = 0.18f;
    public static final float VIGNETTE_FULL  = 0.55f;
    public static final float PHANTOM_START  = 0.35f;
    public static final float FLICKER_START  = 0.48f;
    public static final float SHADOW_START   = 0.58f;
    public static final float FOV_START      = 0.55f;
    public static final float FOV_FULL       = 0.80f;
    public static final float SHAKE_START    = 0.70f;
    public static final float FADE_START     = 0.90f;

    public static float targetScore = 0f;
    public static float score       = 0f;

    // Derived effect values
    public static float vignetteAlpha  = 0f;
    public static float ambientMult    = 1f;
    public static float fovOffset      = 0f;
    public static float shakeIntensity = 0f;
    public static float fadeAlpha      = 0f;

    // Heartbeat pulse — spikes on each beat, drives vignette throb
    public static float vignettePulse  = 0f;

    // Peripheral flicker
    public static float flickerAlpha   = 0f;
    public static int   flickerX       = 0;
    public static int   flickerY       = 0;
    public static int   flickerCooldown = 0;

    // Phantom shadow — brief dark humanoid silhouette
    public static float shadowAlpha    = 0f;
    public static int   shadowX        = 0;
    public static int   shadowY        = 0;
    public static int   shadowW        = 0;
    public static int   shadowH        = 0;
    public static int   shadowCooldown = 0;

    public static void tick() {
        float diff = targetScore - score;
        score += diff * (diff > 0 ? 0.030f : 0.055f);
        if (Math.abs(diff) < 0.0005f) score = targetScore;
        score = Math.max(0f, Math.min(1f, score));

        vignetteAlpha  = smooth(VIGNETTE_START, VIGNETTE_FULL, score) * 0.92f;
        ambientMult    = 1f - smooth(SILENCE_START, SILENCE_FULL, score);
        fovOffset      = -smooth(FOV_START, FOV_FULL, score) * 14f;
        shakeIntensity = smooth(SHAKE_START, 0.96f, score);

        if (score >= FADE_START) {
            fadeAlpha = Math.min(1f, fadeAlpha + 0.012f);
        } else {
            fadeAlpha = Math.max(0f, fadeAlpha - 0.04f);
        }

        // Pulse decay each tick
        if (vignettePulse > 0f) vignettePulse = Math.max(0f, vignettePulse - 0.06f);

        // Flicker and shadow cooldowns
        if (flickerAlpha   > 0f) flickerAlpha   = Math.max(0f, flickerAlpha   - 0.25f);
        if (flickerCooldown > 0) flickerCooldown--;
        if (shadowAlpha    > 0f) shadowAlpha    = Math.max(0f, shadowAlpha    - 0.018f);
        if (shadowCooldown  > 0) shadowCooldown--;
    }

    // Called when score=0 arrives — immediate clear, no lerp
    public static void reset() {
        targetScore    = 0f;
        score          = 0f;
        vignetteAlpha  = 0f;
        ambientMult    = 1f;
        fovOffset      = 0f;
        shakeIntensity = 0f;
        fadeAlpha      = 0f;
        vignettePulse  = 0f;
        flickerAlpha   = 0f;
        flickerCooldown = 0;
        shadowAlpha    = 0f;
        shadowCooldown = 0;
    }

    public static float smooth(float edge0, float edge1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }
}

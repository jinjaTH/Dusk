package com.dusk.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientDreadState {

    // Score thresholds — what score each effect begins and reaches full intensity
    public static final float SILENCE_START  = 0.15f;
    public static final float SILENCE_FULL   = 0.40f;
    public static final float VIGNETTE_START = 0.28f;
    public static final float VIGNETTE_FULL  = 0.70f;
    public static final float PHANTOM_START  = 0.50f;
    public static final float FLICKER_START  = 0.62f;
    public static final float FOV_START      = 0.68f;
    public static final float FOV_FULL       = 0.90f;
    public static final float SHAKE_START    = 0.80f;
    public static final float FADE_START     = 0.92f;

    // Network-received target (updated every ~4 server ticks)
    public static float targetScore = 0f;

    // Smoothly interpolated score — all effects read from this
    public static float score = 0f;

    // Derived effect values — computed from score each frame
    public static float vignetteAlpha  = 0f;  // 0.0 = invisible, 1.0 = full black edges
    public static float ambientMult    = 1f;  // 1.0 = normal, 0.0 = silent
    public static float fovOffset      = 0f;  // negative = narrower
    public static float shakeIntensity = 0f;  // 0.0 = still, 1.0 = full shake
    public static float fadeAlpha      = 0f;  // 0.0 = clear, 1.0 = full black

    // Peripheral flicker state
    public static float flickerAlpha   = 0f;
    public static int   flickerX       = 0;
    public static int   flickerY       = 0;
    public static int   flickerCooldown = 0;

    // Heartbeat state
    public static int heartbeatTick    = 0;
    public static boolean heartbeat2nd = false; // second beat of pair

    // Called every client tick
    public static void tick() {
        // Lerp score toward target — slower going up, faster going down
        float lerpUp   = 0.030f;
        float lerpDown = 0.055f;
        float diff = targetScore - score;
        score += diff * (diff > 0 ? lerpUp : lerpDown);
        if (Math.abs(diff) < 0.0005f) score = targetScore;
        score = Math.max(0f, Math.min(1f, score));

        // Compute all effect intensities from score
        vignetteAlpha  = smooth(VIGNETTE_START, VIGNETTE_FULL,  score) * 0.92f;
        ambientMult    = 1f - smooth(SILENCE_START, SILENCE_FULL, score);
        fovOffset      = -smooth(FOV_START, FOV_FULL, score) * 14f;
        shakeIntensity = smooth(SHAKE_START, 0.96f, score);

        // Fade: builds slowly when at max, clears when score drops
        if (score >= FADE_START) {
            fadeAlpha = Math.min(1f, fadeAlpha + 0.012f);
        } else {
            fadeAlpha = Math.max(0f, fadeAlpha - 0.04f);
        }

        // Flicker: decay each tick
        if (flickerAlpha > 0f) flickerAlpha = Math.max(0f, flickerAlpha - 0.25f);
        if (flickerCooldown > 0) flickerCooldown--;

        // Heartbeat: countdown timer
        if (heartbeatTick > 0) heartbeatTick--;
    }

    // Called when score=0 packet arrives (player in light / teleported)
    public static void reset() {
        targetScore    = 0f;
        flickerAlpha   = 0f;
        flickerCooldown = 0;
        heartbeatTick  = 0;
        heartbeat2nd   = false;
        // score and derived values lerp to 0 naturally
    }

    // Cubic smoothstep: 0 before edge0, 1 after edge1, smooth in between
    private static float smooth(float edge0, float edge1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }
}

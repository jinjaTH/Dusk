package com.dusk.effect;

import com.dusk.client.ClientDreadState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class SoundEffects {

    private static final RandomSource RNG = RandomSource.create();

    // --- Breathing ---
    private static int breathCooldown  = 0;
    private static boolean nextIsExhale = false;

    // --- Approaching footsteps state machine ---
    private static int  footstepState   = 0;   // 0=idle, 1-4=steps, 5=aftermath
    private static int  footstepTimer   = 0;

    // --- Heartbeat ---
    private static int  heartbeatCooldown  = 0;
    private static boolean waitSecondBeat  = false;
    private static int  secondBeatDelay    = 0;

    // --- Stumble/thud (collapse scene) ---
    private static boolean playedStumble   = false;
    private static boolean playedThud      = false;

    public static void clientTick() {
        float s = ClientDreadState.score;
        if (s <= 0.01f) {
            breathCooldown  = 0; nextIsExhale  = false;
            footstepState   = 0; footstepTimer = 0;
            heartbeatCooldown = 0; waitSecondBeat = false;
            playedStumble = false; playedThud = false;
            return;
        }

        tickBreathing(s);
        tickApproachingFootsteps(s);
        tickHeartbeat(s);
        tickCollapseSounds(s);
    }

    // Breathing — starts quiet and close, gets heavier with intensity
    // PLAYER_BREATH for inhale feel, DROWNED_AMBIENT for exhale (labored)
    private static void tickBreathing(float s) {
        if (s < ClientDreadState.PHANTOM_START) return;
        if (--breathCooldown > 0) return;

        float intensity = (s - ClientDreadState.PHANTOM_START) / (1f - ClientDreadState.PHANTOM_START);
        intensity = Math.min(1f, intensity);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (!nextIsExhale) {
            // Inhale: PLAYER_BREATH, quiet, pitched slightly high
            float vol = 0.06f + intensity * 0.10f;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_BREATH, vol, 1.1f + RNG.nextFloat() * 0.15f));
            breathCooldown = 14; // short gap between inhale and exhale
            nextIsExhale = true;
        } else {
            // Exhale: DROWNED_AMBIENT, slightly louder, low pitch — sounds like labored breath
            float vol = 0.05f + intensity * 0.09f;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.DROWNED_AMBIENT, vol, 1.4f + RNG.nextFloat() * 0.2f));
            // Cooldown until next breath cycle — shorter at high score (faster breathing)
            breathCooldown = (int)(60f - intensity * 35f) + RNG.nextInt(20);
            nextIsExhale = false;
        }
    }

    // Approaching footsteps event — 4 steps getting louder, then silence
    // The silence AFTER is the most terrifying part
    private static void tickApproachingFootsteps(float s) {
        if (s < ClientDreadState.PHANTOM_START) return;

        if (footstepState == 0) {
            // Idle — wait random time then start event
            if (--footstepTimer > 0) return;
            float intensity = (s - ClientDreadState.PHANTOM_START) / (0.9f - ClientDreadState.PHANTOM_START);
            intensity = Math.min(1f, Math.max(0f, intensity));
            if (RNG.nextFloat() > 0.004f + intensity * 0.006f) {
                footstepTimer = 20;
                return;
            }
            footstepState = 1;
            footstepTimer = 25 + RNG.nextInt(15);
            return;
        }

        if (--footstepTimer > 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) { footstepState = 0; return; }

        float intensity = (s - ClientDreadState.PHANTOM_START) / (1f - ClientDreadState.PHANTOM_START);
        intensity = Math.min(1f, intensity);

        switch (footstepState) {
            case 1 -> { // Distant — barely audible
                playStep(mc, 0.06f + intensity * 0.04f, 0.85f + RNG.nextFloat() * 0.2f);
                footstepState = 2;
                footstepTimer = 22 + RNG.nextInt(8);
            }
            case 2 -> { // Closer
                playStep(mc, 0.11f + intensity * 0.06f, 0.88f + RNG.nextFloat() * 0.15f);
                footstepState = 3;
                footstepTimer = 20 + RNG.nextInt(6);
            }
            case 3 -> { // Closer still
                playStep(mc, 0.19f + intensity * 0.08f, 0.9f + RNG.nextFloat() * 0.1f);
                footstepState = 4;
                footstepTimer = 18 + RNG.nextInt(6);
            }
            case 4 -> { // Right next to you — loud
                playStep(mc, 0.28f + intensity * 0.12f, 0.92f + RNG.nextFloat() * 0.1f);
                footstepState = 5;              // → aftermath (silence)
                footstepTimer = 120 + RNG.nextInt(100); // 6-11s of dead silence
            }
            case 5 -> { // Aftermath — nothing, reset
                footstepState = 0;
                footstepTimer = 200 + RNG.nextInt(200); // long gap before next event
            }
        }
    }

    private static void playStep(Minecraft mc, float vol, float pitch) {
        var sound = switch (RNG.nextInt(4)) {
            case 0 -> SoundEvents.SKELETON_STEP;
            case 1 -> SoundEvents.ZOMBIE_STEP;
            case 2 -> SoundEvents.WITHER_SKELETON_STEP;
            default -> SoundEvents.STONE_STEP;
        };
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, vol, pitch));
    }

    // Heartbeat — BPM scales from slow to fast as score rises
    private static void tickHeartbeat(float s) {
        if (s < ClientDreadState.SHAKE_START) return;

        if (waitSecondBeat) {
            if (--secondBeatDelay <= 0) {
                playBeat(s);
                waitSecondBeat = false;
                float intensity = (s - ClientDreadState.SHAKE_START) / (1f - ClientDreadState.SHAKE_START);
                intensity = Math.min(1f, intensity);
                // BPM 55→120: cooldown from 55 down to 25 ticks
                heartbeatCooldown = (int)(55f - intensity * 30f) + RNG.nextInt(8);
            }
            return;
        }

        if (--heartbeatCooldown > 0) return;

        playBeat(s);
        waitSecondBeat = true;
        secondBeatDelay = 7;
    }

    private static void playBeat(float s) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float intensity = Math.max(0f, (s - ClientDreadState.SHAKE_START) / (1f - ClientDreadState.SHAKE_START));
        float vol = 0.12f + intensity * 0.26f;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASEDRUM.value(), vol, 0.5f));
        ClientDreadState.vignettePulse = 0.65f + intensity * 0.35f;
    }

    // Collapse sounds — stumble when tilt starts, thud when nearly blacked out
    private static void tickCollapseSounds(float s) {
        if (s < ClientDreadState.FADE_START) {
            playedStumble = false;
            playedThud    = false;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Stumble: play once when fade starts
        if (!playedStumble) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_HURT, 0.4f, 0.7f));
            playedStumble = true;
        }

        // Thud: play once when nearly black
        if (!playedThud && ClientDreadState.fadeAlpha >= 0.75f) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_HURT, 0.6f, 0.5f));
            playedThud = true;
        }
    }
}

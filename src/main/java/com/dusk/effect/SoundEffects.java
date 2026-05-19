package com.dusk.effect;

import com.dusk.client.ClientDreadState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class SoundEffects {

    private static final RandomSource RNG = RandomSource.create();

    // --- Heavy breathing (one-shot at score 0.62, stopped on teleport) ---
    private static boolean playedBreatheHeavy = false;
    private static SoundInstance breatheHeavyInstance = null;

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

    // --- Atmosphere (long loop, tracked for explicit stop on reset) ---
    private static int  atmosphereCooldown = 0;
    private static SoundInstance atmosphereInstance = null;

    // --- Whispers (random hallucinated voices) ---
    private static int  whisperCooldown    = 0;

    public static void clientTick() {
        float s = ClientDreadState.score;
        if (s <= 0.01f) {
            stopAllDuskSounds();
            footstepState      = 0; footstepTimer = 0;
            heartbeatCooldown  = 0; waitSecondBeat = false;
            playedStumble      = false; playedThud = false;
            atmosphereCooldown = 0;
            whisperCooldown    = 0;
            return;
        }

        tickAtmosphere(s);
        tickBreathing(s);
        tickApproachingFootsteps(s);
        tickHeartbeat(s);
        tickWhispers(s);
        tickCollapseSounds(s);
    }

    // Heavy breathing — fires ONCE at score 0.60 (dread=900).
    // At light=0: 900→1500 = exactly 600 ticks = 30s file → ends at teleport.
    private static void tickBreathing(float s) {
        if (s < 0.60f) {
            playedBreatheHeavy = false;
            breatheHeavyInstance = null;
            return;
        }
        if (playedBreatheHeavy) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        breatheHeavyInstance = duskUI("breathe_heavy", 1.0f, 1.0f);
        mc.getSoundManager().play(breatheHeavyInstance);
        playedBreatheHeavy = true;
    }

    public static void stopAllDuskSounds() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            if (breatheHeavyInstance != null) mc.getSoundManager().stop(breatheHeavyInstance);
            if (atmosphereInstance  != null) mc.getSoundManager().stop(atmosphereInstance);
        }
        breatheHeavyInstance = null;
        atmosphereInstance   = null;
        playedBreatheHeavy   = false;
    }

    // Atmosphere — looping low rumble. Replays every ~30s.
    // Assumes the .ogg the user drops in is roughly 25-35s long.
    private static void tickAtmosphere(float s) {
        if (s < ClientDreadState.PHANTOM_START) return;
        if (--atmosphereCooldown > 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float intensity = (s - ClientDreadState.PHANTOM_START) / (1f - ClientDreadState.PHANTOM_START);
        intensity = Math.min(1f, intensity);

        float vol = 0.60f + intensity * 0.30f;  // 0.60 → 0.90
        atmosphereInstance = duskUI("atmosphere", vol, 1.0f);
        mc.getSoundManager().play(atmosphereInstance);

        atmosphereCooldown = 720 + RNG.nextInt(60);  // 36-39s for 38s atmosphere file
    }

    // Hallucinated whispers — ~4 times across the full episode from PHANTOM_START.
    private static void tickWhispers(float s) {
        if (s < ClientDreadState.PHANTOM_START) return;
        if (--whisperCooldown > 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float intensity = (s - ClientDreadState.PHANTOM_START) / (1f - ClientDreadState.PHANTOM_START);
        intensity = Math.min(1f, intensity);

        float vol   = 0.60f + intensity * 0.35f;  // 0.60 → 0.95
        float pitch = 0.85f + RNG.nextFloat() * 0.30f;
        mc.getSoundManager().play(duskUI("whisper", vol, pitch));

        whisperCooldown = 280 + RNG.nextInt(120);  // 14-20s → ~4 whispers per episode
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
        float vol = 3.0f + intensity * 2.0f;  // 3.0 → 5.0 (bass freq ได้ยินยาก)
        mc.getSoundManager().play(duskUI("heartbeat", vol, 1.0f));
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

        if (!playedStumble) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_HURT, 0.35f, 0.7f));
            ClientDreadState.collapseJolt = 1.0f;  // trigger impact shake + fast fade
            playedStumble = true;
        }

        // Thud: play once when nearly black — body hits the ground
        if (!playedThud && ClientDreadState.fadeAlpha >= 0.75f) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_HURT, 0.5f, 0.5f));
            playedThud = true;
        }
    }

    // Creates a UI-style (non-positional, MASTER channel) sound instance directly
    // from the mod's sounds.json — bypasses SoundEvent registry lookup entirely.
    private static SimpleSoundInstance duskUI(String name, float vol, float pitch) {
        return new SimpleSoundInstance(
            Identifier.fromNamespaceAndPath("dusk", name),
            SoundSource.MASTER, vol, pitch,
            RandomSource.createNewThreadLocalInstance(),
            false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true
        );
    }
}

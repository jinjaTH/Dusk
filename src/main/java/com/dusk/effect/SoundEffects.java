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

    // Phantom sound cooldown in ticks
    private static int phantomCooldown = 0;

    // Heartbeat: ticks between beats, second-beat delay
    private static int heartbeatCooldown = 0;
    private static boolean waitingSecondBeat = false;
    private static int secondBeatDelay = 0;

    public static void clientTick() {
        float s = ClientDreadState.score;
        if (s <= 0.01f) {
            phantomCooldown = 0;
            heartbeatCooldown = 0;
            waitingSecondBeat = false;
            return;
        }

        tickPhantomSounds(s);
        tickHeartbeat(s);
    }

    // Phantom sounds: footsteps, breathing, distant groans
    // Frequency scales with score. Only starts at PHANTOM_START (~0.50)
    private static void tickPhantomSounds(float s) {
        if (s < ClientDreadState.PHANTOM_START) return;
        if (phantomCooldown-- > 0) return;

        float intensity = (s - ClientDreadState.PHANTOM_START)
                        / (1f - ClientDreadState.PHANTOM_START);
        intensity = Math.min(1f, intensity);

        // Cooldown: 6s at start → 2.5s at full intensity, with jitter
        int baseCooldown = (int) (120f - intensity * 70f);
        phantomCooldown = baseCooldown + RNG.nextInt(60);

        playPhantomSound(intensity);
    }

    private static void playPhantomSound(float intensity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Choose sound type randomly, weighted toward footsteps (most unsettling)
        int type = RNG.nextInt(10);
        var event = switch (type) {
            case 0, 1, 2 -> SoundEvents.SKELETON_STEP;
            case 3, 4    -> SoundEvents.ZOMBIE_STEP;
            case 5       -> SoundEvents.STONE_STEP;   // heavy footstep
            case 6       -> SoundEvents.GRAVEL_STEP;  // something shifting
            case 7       -> SoundEvents.WITHER_SKELETON_STEP;
            case 8       -> SoundEvents.ZOMBIE_AMBIENT;
            default      -> SoundEvents.AMBIENT_CAVE.value();
        };

        // Volume: quiet at start, louder with intensity
        float vol = 0.08f + intensity * 0.18f + RNG.nextFloat() * 0.06f;
        // Pitch: slight variation — same sound, slightly different each time
        float pitch = 0.82f + RNG.nextFloat() * 0.36f;

        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, vol, pitch));
    }

    // Heartbeat: two quick beats (thump-thump) every 2–3s at high score
    // Mimics the physiological fight-or-flight response
    private static void tickHeartbeat(float s) {
        if (s < ClientDreadState.SHAKE_START) return;

        if (waitingSecondBeat) {
            if (--secondBeatDelay <= 0) {
                playHeartbeatBeat(s);
                waitingSecondBeat = false;
                // Cooldown until next pair: shorter at higher score
                float intensity = (s - ClientDreadState.SHAKE_START) / (1f - ClientDreadState.SHAKE_START);
                intensity = Math.min(1f, intensity);
                heartbeatCooldown = (int) (55f - intensity * 25f) + RNG.nextInt(15);
            }
            return;
        }

        if (heartbeatCooldown-- > 0) return;

        // First beat
        playHeartbeatBeat(s);
        waitingSecondBeat = true;
        secondBeatDelay = 7; // ~0.35s until second beat
    }

    private static void playHeartbeatBeat(float s) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float intensity = Math.max(0f, (s - ClientDreadState.SHAKE_START)
                        / (1f - ClientDreadState.SHAKE_START));
        float vol = 0.12f + intensity * 0.22f;

        mc.getSoundManager().play(
            SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASEDRUM.value(), vol, 0.5f)
        );
    }
}

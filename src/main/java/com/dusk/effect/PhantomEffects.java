package com.dusk.effect;

import com.dusk.client.ClientDreadState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class PhantomEffects {

    private static final RandomSource RNG = RandomSource.create();
    private static boolean collapsePhantomSpawned = false;

    public static void clientTick() {
        float s = ClientDreadState.score;

        // Reset collapse phantom flag when not in collapse
        if (ClientDreadState.fadeAlpha < 0.05f) collapsePhantomSpawned = false;

        // Phase 3 of collapse (head lifting, fade 0.55→0.82) — spawn phantom directly in front
        if (!collapsePhantomSpawned && ClientDreadState.fadeAlpha >= 0.55f) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                spawnFigureInFront(mc, mc.player);
                collapsePhantomSpawned = true;
            }
        }

        // Normal random phantom during high dread
        if (s < ClientDreadState.SHADOW_START) return;
        if (ClientDreadState.phantomFigureCooldown > 0) return;

        float intensity = (s - ClientDreadState.SHADOW_START) / (1f - ClientDreadState.SHADOW_START);
        intensity = Math.min(1f, intensity);

        // Chance per tick — rare at first, more frequent at high score
        if (RNG.nextFloat() > 0.001f + intensity * 0.005f) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        spawnFigure(mc, mc.player, intensity);

        // Long cooldown — this should feel like a rare, shocking event
        ClientDreadState.phantomFigureCooldown = 280 + RNG.nextInt(350);
    }

    // Spawn phantom directly in the player's look direction — seen when head lifts during collapse.
    private static void spawnFigureInFront(Minecraft mc, Player player) {
        var look = player.getLookAngle();
        // Horizontal only — figure stands on ground, not floating
        double len = Math.sqrt(look.x * look.x + look.z * look.z);
        if (len < 0.001) return;
        double dx = look.x / len;
        double dz = look.z / len;

        double fx = player.getX() + dx * 3.5;
        double fy = player.getY() - 0.2;  // at foot level
        double fz = player.getZ() + dz * 3.5;

        // Dense body
        for (int i = 0; i < 60; i++) {
            double bx = fx + (RNG.nextDouble() - 0.5) * 0.18;
            double by = fy + (i / 60.0) * 1.9;
            double bz = fz + (RNG.nextDouble() - 0.5) * 0.18;
            mc.level.addParticle(ParticleTypes.SOUL, bx, by, bz, 0, 0.002, 0);
        }
        // Head
        for (int i = 0; i < 25; i++) {
            double hx = fx + (RNG.nextDouble() - 0.5) * 0.26;
            double hy = fy + 1.95 + (RNG.nextDouble() - 0.5) * 0.18;
            double hz = fz + (RNG.nextDouble() - 0.5) * 0.26;
            mc.level.addParticle(ParticleTypes.SOUL, hx, hy, hz, 0, 0.001, 0);
        }
    }

    // Spawn SOUL particles in a humanoid shape — glowing blue wisps visible in darkness.
    // 4–10 blocks from player so player notices but can't immediately identify it.
    private static void spawnFigure(Minecraft mc, Player player, float intensity) {
        double angle = RNG.nextDouble() * Math.PI * 2;
        double dist  = 4.0 + RNG.nextDouble() * 6.0;

        double fx = player.getX() + Math.sin(angle) * dist;
        double fz = player.getZ() + Math.cos(angle) * dist;
        double fy = player.getY() + (RNG.nextDouble() - 0.5) * 0.5;

        // Body: dense column of soul wisps (~1.8 blocks tall)
        int bodyParticles = 40 + (int)(intensity * 20);
        for (int i = 0; i < bodyParticles; i++) {
            double bx = fx + (RNG.nextDouble() - 0.5) * 0.20;
            double by = fy + (i / (double) bodyParticles) * 1.8;
            double bz = fz + (RNG.nextDouble() - 0.5) * 0.20;
            mc.level.addParticle(ParticleTypes.SOUL, bx, by, bz, 0, 0.004, 0);
        }

        // Head: tighter cluster at top
        int headParticles = 20 + (int)(intensity * 10);
        for (int i = 0; i < headParticles; i++) {
            double hx = fx + (RNG.nextDouble() - 0.5) * 0.28;
            double hy = fy + 1.85 + (RNG.nextDouble() - 0.5) * 0.20;
            double hz = fz + (RNG.nextDouble() - 0.5) * 0.28;
            mc.level.addParticle(ParticleTypes.SOUL, hx, hy, hz, 0, 0.002, 0);
        }
    }
}

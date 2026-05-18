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

    // Called every client tick — spawns in-world particles client-side only
    // Player sees these in 3D space with proper depth and perspective
    public static void clientTick() {
        float s = ClientDreadState.score;
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

    // Spawn SMOKE particles in a humanoid shape at a random world position
    // 5–12 blocks from player — visible in 3D, drifts upward, dissipates naturally
    private static void spawnFigure(Minecraft mc, Player player, float intensity) {
        double angle = RNG.nextDouble() * Math.PI * 2;
        double dist  = 5.0 + RNG.nextDouble() * 7.0;

        double fx = player.getX() + Math.sin(angle) * dist;
        double fz = player.getZ() + Math.cos(angle) * dist;
        // Stand on roughly the same Y as player
        double fy = player.getY() + (RNG.nextDouble() - 0.5) * 1.5;

        // Body: tall column of smoke (~1.8 blocks tall, narrow)
        int bodyParticles = 18 + (int)(intensity * 10);
        for (int i = 0; i < bodyParticles; i++) {
            double bx = fx + (RNG.nextDouble() - 0.5) * 0.25;
            double by = fy + (i / (double) bodyParticles) * 1.8;
            double bz = fz + (RNG.nextDouble() - 0.5) * 0.25;
            // Very slow upward drift — figure looks like it's "breathing"
            mc.level.addParticle(ParticleTypes.LARGE_SMOKE, bx, by, bz, 0, 0.006, 0);
        }

        // Head: denser cluster at top
        int headParticles = 8 + (int)(intensity * 5);
        for (int i = 0; i < headParticles; i++) {
            double hx = fx + (RNG.nextDouble() - 0.5) * 0.35;
            double hy = fy + 1.85 + (RNG.nextDouble() - 0.5) * 0.25;
            double hz = fz + (RNG.nextDouble() - 0.5) * 0.35;
            mc.level.addParticle(ParticleTypes.LARGE_SMOKE, hx, hy, hz, 0, 0.003, 0);
        }
    }
}

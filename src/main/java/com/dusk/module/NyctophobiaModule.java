package com.dusk.module;

import com.dusk.event.PhobiaEventHandler;
import com.dusk.network.DuskNetwork;
import com.dusk.tracker.DreadTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.LightLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NyctophobiaModule {

    private static final double MAX_DREAD  = 1500.0;
    private static final int    SEND_EVERY = 4;

    private static final Map<UUID, Integer> tickCounters   = new HashMap<>();
    private static final Map<UUID, Integer> spawnImmunity  = new HashMap<>();

    public static void tick(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            reset(player);
            return;
        }

        ServerLevel level = player.level();
        if (!level.dimensionType().natural()) {
            reset(player);
            return;
        }

        BlockPos pos = player.blockPosition();
        // Combined light (sky+block, time-adjusted) — clears dread during daytime.
        // Threshold 6: daytime (15) resets, night (4-5) triggers, dusk borderline.
        int combinedLight = level.getMaxLocalRawBrightness(pos);
        if (combinedLight > 6) {
            reset(player);
            return;
        }

        if (PhobiaEventHandler.isTeleportPending(player.getUUID())) {
            applyEffect(player, MobEffects.SLOWNESS, 255);
            return;
        }

        // Post-teleport immunity — don't rebuild dread at a dark spawn for 10 seconds
        UUID uuid0 = player.getUUID();
        int imm = spawnImmunity.getOrDefault(uuid0, 0);
        if (imm > 0) {
            spawnImmunity.put(uuid0, imm - 1);
            return;
        }

        // Block light drives the multiplier — night outdoors has block=0 → full 1.00,
        // same urgency as a pitch-dark cave. Torches raise block light and slow the rate.
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        double darknessMultiplier = switch (blockLight) {
            case 0 -> 1.00;
            case 1 -> 0.80;
            case 2 -> 0.55;
            case 3 -> 0.30;
            case 4 -> 0.12;
            default -> 0.05;
        };

        UUID uuid = player.getUUID();
        double current = DreadTracker.getDread(uuid, DreadTracker.NYCTO);
        current += darknessMultiplier;
        DreadTracker.setDread(uuid, DreadTracker.NYCTO, current);

        float score = (float) Math.min(1.0, current / MAX_DREAD);

        applyEffects(player, score);

        int counter = tickCounters.merge(uuid, 1, Integer::sum);
        if (counter % SEND_EVERY == 0) {
            DuskNetwork.sendScore(player, score);
        }

        if (score >= 1.0f) {
            PhobiaEventHandler.triggerTeleport(player, 10);
        }
    }

    // Apply Minecraft potion effects silently — no particles, no HUD icon
    // Player feels the effect but doesn't see any indication of why
    private static void applyEffects(ServerPlayer player, float score) {
        // DARKNESS — world darkens around player (0.10+)
        if (score >= 0.10f) {
            int amp = score >= 0.72f ? 2 : score >= 0.42f ? 1 : 0;
            applyEffect(player, MobEffects.DARKNESS, amp);
        } else {
            player.removeEffect(MobEffects.DARKNESS);
        }

        // SLOWNESS — legs feel heavy, movement slows (0.25+)
        if (score >= 0.25f) {
            int amp = score >= 0.85f ? 2 : score >= 0.72f ? 1 : 0;
            applyEffect(player, MobEffects.SLOWNESS, amp);
        } else {
            player.removeEffect(MobEffects.SLOWNESS);
        }

        // NAUSEA — subtle dizziness, screen begins to waver (0.58+)
        if (score >= 0.58f) {
            int amp = score >= 0.85f ? 1 : 0;
            applyEffect(player, MobEffects.NAUSEA, amp);
        } else {
            player.removeEffect(MobEffects.NAUSEA);
        }
    }

    private static void applyEffect(ServerPlayer player,
                                    net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                                    int amp) {
        var current = player.getEffect(effect);
        if (current != null && current.getAmplifier() == amp && current.getDuration() > 40) return;
        // ambient=false, showParticles=false, showIcon=false → completely invisible
        player.addEffect(new MobEffectInstance(effect, 100, amp, false, false, false));
    }

    public static void removeAllEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.SLOWNESS);
        player.removeEffect(MobEffects.NAUSEA);
    }

    private static void reset(ServerPlayer player) {
        UUID uuid = player.getUUID();
        double prev = DreadTracker.getDread(uuid, DreadTracker.NYCTO);
        if (prev == 0) return;
        DreadTracker.setDread(uuid, DreadTracker.NYCTO, 0);
        tickCounters.put(uuid, 0);
        removeAllEffects(player);
        // Heal to full when finding light — HP/food lost during dread restore
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        DuskNetwork.sendScore(player, 0f);
    }

    public static void grantSpawnImmunity(UUID uuid) {
        spawnImmunity.put(uuid, 200);  // 10 seconds of grace at spawn
    }

    public static void removePlayer(UUID uuid) {
        tickCounters.remove(uuid);
        spawnImmunity.remove(uuid);
    }
}

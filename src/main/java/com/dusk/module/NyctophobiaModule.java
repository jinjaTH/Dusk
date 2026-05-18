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

    private static final Map<UUID, Integer> tickCounters = new HashMap<>();

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
        int light = level.getBrightness(LightLayer.BLOCK, pos);

        if (light > 4) {
            reset(player);
            return;
        }

        if (PhobiaEventHandler.isTeleportPending(player.getUUID())) return;

        double darknessMultiplier = switch (light) {
            case 0 -> 1.00;
            case 1 -> 0.80;
            case 2 -> 0.55;
            case 3 -> 0.30;
            default -> 0.12;
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
            PhobiaEventHandler.triggerTeleport(player);
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
        DuskNetwork.sendScore(player, 0f);
    }
}

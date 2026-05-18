package com.dusk.module;

import com.dusk.event.PhobiaEventHandler;
import com.dusk.network.DuskNetwork;
import com.dusk.tracker.DreadTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;

public class NyctophobiaModule {

    // Max dreadScore that maps to normalized score = 1.0
    // At max rate (1.2/tick, light=0, no mob): 2083 ticks ≈ 104 seconds
    private static final double MAX_DREAD = 2500.0;

    // Send score packet every N ticks for smooth client interpolation
    private static final int SEND_INTERVAL = 4;

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

        if (light > 3) {
            reset(player);
            return;
        }

        if (PhobiaEventHandler.isTeleportPending(player.getUUID())) return;

        double darknessMultiplier = switch (light) {
            case 0 -> 1.00;
            case 1 -> 0.75;
            case 2 -> 0.50;
            default -> 0.25;
        };

        // Silence amplifies fear — no mobs means nothing to ground the imagination
        double silenceMultiplier = countMobsNearby(player, level) == 0 ? 1.2 : 1.0;

        double current = DreadTracker.getDread(player.getUUID(), DreadTracker.NYCTO);
        current += darknessMultiplier * silenceMultiplier;
        DreadTracker.setDread(player.getUUID(), DreadTracker.NYCTO, current);

        float normalized = (float) Math.min(1.0, current / MAX_DREAD);

        // Send every SEND_INTERVAL ticks for smooth client lerp
        int tick = (int)(current % SEND_INTERVAL);
        if (tick == 0) {
            DuskNetwork.sendScore(player, normalized);
        }

        // Trigger teleport at full panic (score >= 1.0)
        if (normalized >= 1.0f) {
            PhobiaEventHandler.triggerTeleport(player);
        }
    }

    private static void reset(ServerPlayer player) {
        double prev = DreadTracker.getDread(player.getUUID(), DreadTracker.NYCTO);
        if (prev == 0) return;
        DreadTracker.setDread(player.getUUID(), DreadTracker.NYCTO, 0);
        DuskNetwork.sendScore(player, 0f);
    }

    private static int countMobsNearby(ServerPlayer player, ServerLevel level) {
        AABB box = player.getBoundingBox().inflate(16);
        return level.getEntities(player, box, e -> e instanceof net.minecraft.world.entity.Mob).size();
    }
}

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

    // dreadScore thresholds per stage
    private static final double[] THRESHOLDS = { 0, 300, 600, 900, 1100, 1300 };

    public static void tick(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            reset(player);
            return;
        }

        ServerLevel level = player.level();

        // Only overworld
        if (!level.dimensionType().hasSkyLight()) {
            reset(player);
            return;
        }

        BlockPos pos = player.blockPosition();
        int light = level.getBrightness(LightLayer.BLOCK, pos);

        if (light > 3) {
            reset(player);
            return;
        }

        double darknessMultiplier = switch (light) {
            case 0 -> 1.00;
            case 1 -> 0.75;
            case 2 -> 0.50;
            default -> 0.25; // 3
        };

        double silenceMultiplier = countMobsNearby(player, level) == 0 ? 1.2 : 1.0;

        // Skip accumulation while teleport is pending (waiting for fade to complete)
        if (PhobiaEventHandler.isTeleportPending(player.getUUID())) return;

        double current = DreadTracker.getDread(player.getUUID(), DreadTracker.NYCTO);
        current += darknessMultiplier * silenceMultiplier;
        DreadTracker.setDread(player.getUUID(), DreadTracker.NYCTO, current);

        int newStage = computeStage(current);
        int oldStage = DreadTracker.setStageAndGetOld(player.getUUID(), DreadTracker.NYCTO, newStage);

        if (newStage != oldStage) {
            DuskNetwork.sendStage(player, DreadTracker.NYCTO, newStage);
        }

        if (newStage == 6) {
            PhobiaEventHandler.triggerTeleport(player);
            // Don't reset here — PhobiaEventHandler resets after teleport
            // so client sees the fade before stage 0 is sent
        }
    }

    private static void reset(ServerPlayer player) {
        double prev = DreadTracker.getDread(player.getUUID(), DreadTracker.NYCTO);
        if (prev == 0) return;
        DreadTracker.setDread(player.getUUID(), DreadTracker.NYCTO, 0);
        int oldStage = DreadTracker.setStageAndGetOld(player.getUUID(), DreadTracker.NYCTO, 0);
        if (oldStage != 0) {
            DuskNetwork.sendStage(player, DreadTracker.NYCTO, 0);
        }
    }

    private static int computeStage(double score) {
        for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
            if (score >= THRESHOLDS[i]) return i + 1 > 6 ? 6 : i + 1;
        }
        return 0;
    }

    private static int countMobsNearby(ServerPlayer player, ServerLevel level) {
        AABB box = player.getBoundingBox().inflate(16);
        return level.getEntities(player, box, e -> e instanceof net.minecraft.world.entity.Mob).size();
    }
}

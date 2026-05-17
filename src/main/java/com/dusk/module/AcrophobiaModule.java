package com.dusk.module;

import com.dusk.network.DuskNetwork;
import com.dusk.tracker.DreadTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class AcrophobiaModule {

    public static void tick(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            reset(player);
            return;
        }

        if (!meetsCondition(player)) {
            reset(player);
            return;
        }

        long exposure = DreadTracker.getExposure(player.getUUID(), DreadTracker.ACRO) + 1;
        DreadTracker.setExposure(player.getUUID(), DreadTracker.ACRO, exposure);

        int newStage = computeStage(exposure);
        int oldStage = DreadTracker.setStageAndGetOld(player.getUUID(), DreadTracker.ACRO, newStage);

        if (newStage != oldStage) {
            DuskNetwork.sendStage(player, DreadTracker.ACRO, newStage);
        }

        // Stage 5: freeze 2s then reset
        if (newStage == 5 && exposure % (35 * 20) == 0) {
            // Freeze signal is handled client-side; reset after cycle
            DreadTracker.setExposure(player.getUUID(), DreadTracker.ACRO, 30 * 20); // snap back to stage 4 threshold
            int s = DreadTracker.setStageAndGetOld(player.getUUID(), DreadTracker.ACRO, 4);
            if (s != 4) DuskNetwork.sendStage(player, DreadTracker.ACRO, 4);
        }
    }

    private static boolean meetsCondition(ServerPlayer player) {
        if (player.getY() < 150) return false;
        // xRot > 0 means looking down
        float xRot = player.getXRot();
        if (xRot <= 0) return false;
        // Check that there is open air > 30 blocks below
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        int airBelow = 0;
        for (int i = 1; i <= 35; i++) {
            if (level.isEmptyBlock(pos.below(i))) {
                airBelow++;
            } else {
                break;
            }
        }
        return airBelow >= 30;
    }

    private static void reset(ServerPlayer player) {
        long prev = DreadTracker.getExposure(player.getUUID(), DreadTracker.ACRO);
        if (prev == 0) return;
        DreadTracker.setExposure(player.getUUID(), DreadTracker.ACRO, 0);
        int oldStage = DreadTracker.setStageAndGetOld(player.getUUID(), DreadTracker.ACRO, 0);
        if (oldStage != 0) {
            DuskNetwork.sendStage(player, DreadTracker.ACRO, 0);
        }
    }

    private static int computeStage(long ticks) {
        // 0-5s, 5-15s, 15-25s, 25-35s, 35s+
        if (ticks <  5 * 20) return 1;
        if (ticks < 15 * 20) return 2;
        if (ticks < 25 * 20) return 3;
        if (ticks < 35 * 20) return 4;
        return 5;
    }
}

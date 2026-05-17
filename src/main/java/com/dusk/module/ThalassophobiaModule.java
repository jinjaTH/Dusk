package com.dusk.module;

import com.dusk.event.PhobiaEventHandler;
import com.dusk.network.DuskNetwork;
import com.dusk.tracker.DreadTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

public class ThalassophobiaModule {

    public static void tick(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            reset(player);
            return;
        }

        ServerLevel level = player.level();

        if (!meetsCondition(player, level)) {
            reset(player);
            return;
        }

        long exposure = DreadTracker.getExposure(player.getUUID(), DreadTracker.THALA) + 1;
        DreadTracker.setExposure(player.getUUID(), DreadTracker.THALA, exposure);

        int newStage = computeStage(exposure);
        int oldStage = DreadTracker.setStageAndGetOld(player.getUUID(), DreadTracker.THALA, newStage);

        if (newStage != oldStage) {
            DuskNetwork.sendStage(player, DreadTracker.THALA, newStage);
        }

        if (newStage == 6) {
            PhobiaEventHandler.triggerTeleport(player);
            reset(player);
        }
    }

    private static boolean meetsCondition(ServerPlayer player, ServerLevel level) {
        BlockPos pos = player.blockPosition();

        // Must be in deep ocean biome
        var biome = level.getBiome(pos);
        if (!biome.is(BiomeTags.IS_DEEP_OCEAN)) return false;

        // Either in water, or depth below >= 30 blocks
        if (player.isInWater()) return true;

        int depth = 0;
        for (int i = 1; i <= 35; i++) {
            BlockPos below = pos.below(i);
            if (level.getBlockState(below).is(Blocks.WATER) ||
                level.getBlockState(below).is(Blocks.SEAGRASS) ||
                level.getBlockState(below).is(Blocks.TALL_SEAGRASS)) {
                depth++;
            } else if (!level.isEmptyBlock(below)) {
                break;
            }
        }
        return depth >= 30;
    }

    private static void reset(ServerPlayer player) {
        long prev = DreadTracker.getExposure(player.getUUID(), DreadTracker.THALA);
        if (prev == 0) return;
        DreadTracker.setExposure(player.getUUID(), DreadTracker.THALA, 0);
        int oldStage = DreadTracker.setStageAndGetOld(player.getUUID(), DreadTracker.THALA, 0);
        if (oldStage != 0) {
            DuskNetwork.sendStage(player, DreadTracker.THALA, 0);
        }
    }

    private static int computeStage(long ticks) {
        if (ticks <  10 * 20) return 1;
        if (ticks <  25 * 20) return 2;
        if (ticks <  40 * 20) return 3;
        if (ticks <  55 * 20) return 4;
        if (ticks <  70 * 20) return 5;
        return 6;
    }
}

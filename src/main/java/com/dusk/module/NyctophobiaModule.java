package com.dusk.module;

import com.dusk.event.PhobiaEventHandler;
import com.dusk.network.DuskNetwork;
import com.dusk.tracker.DreadTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

        // Trigger at block light <= 4 — checks only the block the player stands on
        if (light > 4) {
            reset(player);
            return;
        }

        if (PhobiaEventHandler.isTeleportPending(player.getUUID())) return;

        // Darker = faster buildup, light 4 is very slow
        double darknessMultiplier = switch (light) {
            case 0 -> 1.00;
            case 1 -> 0.80;
            case 2 -> 0.55;
            case 3 -> 0.30;
            default -> 0.12; // 4
        };

        UUID uuid = player.getUUID();
        double current = DreadTracker.getDread(uuid, DreadTracker.NYCTO);
        current += darknessMultiplier;
        DreadTracker.setDread(uuid, DreadTracker.NYCTO, current);

        float normalized = (float) Math.min(1.0, current / MAX_DREAD);

        int counter = tickCounters.merge(uuid, 1, Integer::sum);
        if (counter % SEND_EVERY == 0) {
            DuskNetwork.sendScore(player, normalized);
        }

        if (normalized >= 1.0f) {
            PhobiaEventHandler.triggerTeleport(player);
        }
    }

    private static void reset(ServerPlayer player) {
        UUID uuid = player.getUUID();
        double prev = DreadTracker.getDread(uuid, DreadTracker.NYCTO);
        if (prev == 0) return;
        DreadTracker.setDread(uuid, DreadTracker.NYCTO, 0);
        tickCounters.put(uuid, 0);
        DuskNetwork.sendScore(player, 0f);
    }
}

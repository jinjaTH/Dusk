package com.dusk.event;

import com.dusk.module.AcrophobiaModule;
import com.dusk.module.NyctophobiaModule;
import com.dusk.module.ThalassophobiaModule;
import com.dusk.network.DuskNetwork;
import com.dusk.tracker.DreadTracker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhobiaEventHandler {

    private static final Map<UUID, Integer> pendingTeleports = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                NyctophobiaModule.tick(player);
                AcrophobiaModule.tick(player);
                ThalassophobiaModule.tick(player);
            }

            pendingTeleports.entrySet().removeIf(entry -> {
                int remaining = entry.getValue() - 1;
                if (remaining <= 0) {
                    ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                    if (player != null) executeSpawnTeleport(player);
                    return true;
                }
                entry.setValue(remaining);
                return false;
            });
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof ServerPlayer player) {
                DreadTracker.init(player.getUUID());
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof ServerPlayer player) {
                DreadTracker.remove(player.getUUID());
                pendingTeleports.remove(player.getUUID());
            }
        });
    }

    public static void triggerTeleport(ServerPlayer player) {
        // putIfAbsent: don't restart countdown if already pending
        pendingTeleports.putIfAbsent(player.getUUID(), 10);
    }

    public static boolean isTeleportPending(UUID uuid) {
        return pendingTeleports.containsKey(uuid);
    }

    private static void executeSpawnTeleport(ServerPlayer player) {
        var config = player.getRespawnConfig();
        if (config != null) {
            var data = config.respawnData();
            if (data != null) {
                var spawnLevel = player.level().getServer().getLevel(data.dimension());
                if (spawnLevel != null) {
                    var pos = data.pos();
                    player.teleportTo(spawnLevel,
                        pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        Collections.emptySet(),
                        player.getYRot(), player.getXRot(), false);
                    resetAfterTeleport(player);
                    return;
                }
            }
        }
        teleportToWorldSpawn(player);
    }

    private static void teleportToWorldSpawn(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        var spawn = overworld.getRespawnData().pos();
        player.teleportTo(overworld,
            spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
            Collections.emptySet(),
            player.getYRot(), player.getXRot(), false);
        resetAfterTeleport(player);
    }

    // Called after teleport completes — resets state and notifies client
    // so fade clears only after player is already at spawn (matching CLAUDE.md design)
    private static void resetAfterTeleport(ServerPlayer player) {
        UUID uuid = player.getUUID();

        if (DreadTracker.getStage(uuid, DreadTracker.NYCTO) != 0) {
            DreadTracker.setDread(uuid, DreadTracker.NYCTO, 0);
            DreadTracker.setStageAndGetOld(uuid, DreadTracker.NYCTO, 0);
            DuskNetwork.sendStage(player, DreadTracker.NYCTO, 0);
        }
        if (DreadTracker.getStage(uuid, DreadTracker.THALA) != 0) {
            DreadTracker.setExposure(uuid, DreadTracker.THALA, 0);
            DreadTracker.setStageAndGetOld(uuid, DreadTracker.THALA, 0);
            DuskNetwork.sendStage(player, DreadTracker.THALA, 0);
        }
    }
}

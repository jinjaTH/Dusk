package com.dusk.event;

import com.dusk.module.NyctophobiaModule;
import com.dusk.network.DuskNetwork;
import com.dusk.tracker.DreadTracker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhobiaEventHandler {

    private static final Map<UUID, Integer> pendingTeleports = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                NyctophobiaModule.tick(player);
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
                UUID uuid = player.getUUID();
                DreadTracker.remove(uuid);
                pendingTeleports.remove(uuid);
                NyctophobiaModule.removePlayer(uuid);
            }
        });
    }

    public static void triggerTeleport(ServerPlayer player, int delayTicks) {
        pendingTeleports.putIfAbsent(player.getUUID(), delayTicks);
    }

    public static boolean isTeleportPending(UUID uuid) {
        return pendingTeleports.containsKey(uuid);
    }

    private static void executeSpawnTeleport(ServerPlayer player) {
        var spawnPos = player.getRespawnPosition();
        if (spawnPos != null) {
            var spawnLevel = player.getServer().getLevel(player.getRespawnDimension());
            if (spawnLevel != null) {
                player.teleportTo(spawnLevel,
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
                resetAfterTeleport(player);
                return;
            }
        }
        teleportToWorldSpawn(player);
    }

    private static void teleportToWorldSpawn(ServerPlayer player) {
        ServerLevel overworld = player.getServer().overworld();
        var spawn = overworld.getSharedSpawnPos();
        player.teleportTo(overworld,
            spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
            player.getYRot(), player.getXRot());
        resetAfterTeleport(player);
    }

    private static void resetAfterTeleport(ServerPlayer player) {
        NyctophobiaModule.removeAllEffects(player);
        DreadTracker.setDread(player.getUUID(), DreadTracker.NYCTO, 0);
        NyctophobiaModule.grantSpawnImmunity(player.getUUID());
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        DuskNetwork.sendHardReset(player);
    }
}

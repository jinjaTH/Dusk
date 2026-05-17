package com.dusk.event;

import com.dusk.module.AcrophobiaModule;
import com.dusk.module.NyctophobiaModule;
import com.dusk.module.ThalassophobiaModule;
import com.dusk.tracker.DreadTracker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

public class PhobiaEventHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                NyctophobiaModule.tick(player);
                AcrophobiaModule.tick(player);
                ThalassophobiaModule.tick(player);
            }
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof ServerPlayer player) {
                DreadTracker.init(player.getUUID());
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof ServerPlayer player) {
                DreadTracker.remove(player.getUUID());
            }
        });
    }

    public static void triggerTeleport(ServerPlayer player) {
        // Fade is handled client-side (stage 6 packet received)
        // Delay teleport by 10 ticks to let fade play out
        player.getServer().execute(() -> {
            player.getServer().tell(new net.minecraft.server.TickTask(
                player.getServer().getTickCount() + 10,
                () -> {
                    var spawnPos = player.getRespawnPosition();
                    if (spawnPos != null) {
                        var spawnLevel = player.getServer().getLevel(player.getRespawnDimension());
                        if (spawnLevel != null) {
                            player.teleportTo(spawnLevel,
                                spawnPos.getX() + 0.5,
                                spawnPos.getY(),
                                spawnPos.getZ() + 0.5,
                                player.getYRot(), player.getXRot());
                        } else {
                            teleportToWorldSpawn(player);
                        }
                    } else {
                        teleportToWorldSpawn(player);
                    }
                }
            ));
        });
    }

    private static void teleportToWorldSpawn(ServerPlayer player) {
        var overworld = player.getServer().overworld();
        var spawn = overworld.getSharedSpawnPos();
        player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
            player.getYRot(), player.getXRot());
    }
}

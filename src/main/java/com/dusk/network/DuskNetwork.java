package com.dusk.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class DuskNetwork {

    public static void registerServer() {
        PayloadTypeRegistry.playS2C().register(DreadStagePayload.TYPE, DreadStagePayload.CODEC);
    }

    public static void sendStage(ServerPlayer player, int phobiaId, int stage) {
        ServerPlayNetworking.send(player, new DreadStagePayload(phobiaId, stage));
    }
}

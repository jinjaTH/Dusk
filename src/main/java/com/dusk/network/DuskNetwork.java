package com.dusk.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class DuskNetwork {

    public static void registerServer() {
        PayloadTypeRegistry.playS2C().register(DreadStagePayload.TYPE, DreadStagePayload.CODEC);
    }

    public static void sendScore(ServerPlayer player, float score) {
        int encoded = Math.round(Math.max(0f, Math.min(1f, score)) * 10000f);
        ServerPlayNetworking.send(player, new DreadStagePayload(encoded));
    }
}

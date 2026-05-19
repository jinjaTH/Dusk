package com.dusk.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class DuskNetwork {

    public static void registerServer() {
        PayloadTypeRegistry.playS2C().register(DreadStagePayload.TYPE, DreadStagePayload.CODEC);
    }

    // Normal score update — client lerps toward this value.
    // Used for both build-up and light-recovery (soft fade back to sanity).
    public static void sendScore(ServerPlayer player, float score) {
        int encoded = Math.round(Math.max(0f, Math.min(1f, score)) * 10000f);
        ServerPlayNetworking.send(player, new DreadStagePayload(encoded, false));
    }

    // Hard reset — client snaps every effect to 0 with no transition.
    // Used after teleport so the player wakes at spawn cleanly.
    public static void sendHardReset(ServerPlayer player) {
        ServerPlayNetworking.send(player, new DreadStagePayload(0, true));
    }
}

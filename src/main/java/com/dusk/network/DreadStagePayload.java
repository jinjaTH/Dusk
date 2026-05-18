package com.dusk.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 'score' carries normalized dread (0–10000 maps to 0.0–1.0 float)
public record DreadStagePayload(int score) implements CustomPacketPayload {

    public static final Type<DreadStagePayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath("dusk", "dread_score")
    );

    public static final StreamCodec<FriendlyByteBuf, DreadStagePayload> CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DreadStagePayload::score,
            DreadStagePayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public float normalizedScore() { return score / 10000f; }
}

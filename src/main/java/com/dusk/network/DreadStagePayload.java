package com.dusk.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DreadStagePayload(int phobiaId, int stage) implements CustomPacketPayload {

    public static final Type<DreadStagePayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath("dusk", "dread_stage")
    );

    public static final StreamCodec<FriendlyByteBuf, DreadStagePayload> CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DreadStagePayload::phobiaId,
            ByteBufCodecs.VAR_INT, DreadStagePayload::stage,
            DreadStagePayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

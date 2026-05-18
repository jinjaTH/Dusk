package com.dusk.mixin.client;

import com.dusk.client.ClientDreadState;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Environment(EnvType.CLIENT)
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    /**
     * Target the INVOKE of updateBuffer inside setupFog to intercept its args.
     * Indices 3-8 are the 6 float fog distance values — scale them to increase fog density.
     */
    @ModifyArgs(
        method = "setupFog",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
        )
    )
    private void modifyFogDistances(Args args) {
        float mult = ClientDreadState.fogMultiplier;
        if (mult >= 1f) return;
        for (int i = 3; i <= 8; i++) {
            args.set(i, (float) args.get(i) * mult);
        }
    }
}

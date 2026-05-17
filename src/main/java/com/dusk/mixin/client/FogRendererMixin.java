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
     * updateBuffer(ByteBuffer, int, Vector4f color, float envStart, float renderStart,
     *              float envEnd, float renderEnd, float skyEnd, float cloudEnd)
     * Indices 3-8 are all fog distance values — scale them to increase fog density.
     */
    @ModifyArgs(method = "updateBuffer", at = @At("HEAD"))
    private void modifyFogDistances(Args args) {
        float mult = ClientDreadState.fogMultiplier;
        if (mult >= 1f) return;
        for (int i = 3; i <= 8; i++) {
            args.set(i, (float) args.get(i) * mult);
        }
    }
}

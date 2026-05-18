package com.dusk.mixin.client;

import com.dusk.client.ClientDreadState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // getFov returns float in 1.21.10
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void modifyFov(Camera camera, float partialTick, boolean useFovSetting,
                           CallbackInfoReturnable<Float> cir) {
        float offset = ClientDreadState.fovOffset;
        if (offset == 0f) return;

        // Add subtle breathing pulse on top of the narrowing — feels alive
        float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.0022) * 0.8);
        cir.setReturnValue(cir.getReturnValue() + offset + pulse * ClientDreadState.shakeIntensity);
    }

    // bobView runs every render frame — correct hook for continuous camera shake
    @Inject(method = "bobView", at = @At("RETURN"))
    private void addShake(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        float intensity = ClientDreadState.shakeIntensity;
        if (intensity < 0.01f) return;

        long t = System.currentTimeMillis();
        // Two overlapping sine waves at different frequencies — organic tremor feel
        float shakeX = (float)(Math.sin(t * 0.029) * 0.55 + Math.sin(t * 0.071) * 0.45) * intensity * 0.009f;
        float shakeY = (float)(Math.cos(t * 0.023) * 0.60 + Math.cos(t * 0.053) * 0.40) * intensity * 0.005f;
        poseStack.translate(shakeX, shakeY, 0f);
    }
}

package com.dusk.mixin.client;

import com.dusk.client.ClientDreadState;
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

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void modifyFov(Camera camera, float partialTick, boolean useFovSetting,
                           CallbackInfoReturnable<Double> cir) {
        float offset = ClientDreadState.fovOffset;
        if (offset == 0) return;

        if (ClientDreadState.nyctophobiaStage == 3) {
            offset += (float)(Math.sin(System.currentTimeMillis() * 0.003) * 3.0);
        }

        cir.setReturnValue(cir.getReturnValue() + offset);
    }

    @Inject(method = "bobHurt", at = @At("RETURN"))
    private void addShake(com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTick,
                          CallbackInfo ci) {
        float intensity = ClientDreadState.shakeIntensity;
        if (intensity <= 0) return;

        long time = System.currentTimeMillis();
        float shakeX = (float)(Math.sin(time * 0.023) * intensity);
        float shakeY = (float)(Math.cos(time * 0.017) * intensity * 0.5);
        poseStack.translate(shakeX * 0.01f, shakeY * 0.01f, 0);
    }
}

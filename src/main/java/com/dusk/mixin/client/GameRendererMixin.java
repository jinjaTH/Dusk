package com.dusk.mixin.client;

import com.dusk.client.ClientDreadState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
        float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.0022) * 0.8);
        cir.setReturnValue(cir.getReturnValue() + offset + pulse * ClientDreadState.shakeIntensity);
    }

    // bobView runs every render frame — micro-shake + collapse tilt + impact jolt
    @Inject(method = "bobView", at = @At("RETURN"))
    private void addShakeAndTilt(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        float shake = ClientDreadState.shakeIntensity;
        float tilt  = ClientDreadState.tiltAngle;
        float jolt  = ClientDreadState.collapseJolt;

        // Impact jolt: single hard camera hit when player collapses face-first
        if (jolt > 0.01f) {
            poseStack.translate(0f, jolt * 0.12f, 0f);   // lurch down
            poseStack.translate((float)(Math.random() - 0.5) * jolt * 0.06f, 0f, 0f);
        }

        // Micro-shake: two overlapping sine waves for organic tremor
        if (shake > 0.01f) {
            long t = System.currentTimeMillis();
            float sx = (float)(Math.sin(t * 0.029) * 0.55 + Math.sin(t * 0.071) * 0.45) * shake * 0.009f;
            float sy = (float)(Math.cos(t * 0.023) * 0.60 + Math.cos(t * 0.053) * 0.40) * shake * 0.005f;
            poseStack.translate(sx, sy, 0f);
        }

        // Collapse tilt: Z rotation — camera rolls as player loses consciousness
        if (tilt > 0.05f) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));
        }
    }
}

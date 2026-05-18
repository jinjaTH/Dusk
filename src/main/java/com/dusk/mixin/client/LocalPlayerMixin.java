package com.dusk.mixin.client;

import com.dusk.client.ClientDreadState;
import net.minecraft.client.player.LocalPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer)(Object)this;

        // Input drift for Nyctophobia stage 4
        float drift = ClientDreadState.driftAngle;
        if (drift != 0) {
            double driftRad = Math.toRadians(drift);
            var delta = self.getDeltaMovement();
            double driftX = Math.cos(driftRad) * delta.x - Math.sin(driftRad) * delta.z;
            double driftZ = Math.sin(driftRad) * delta.x + Math.cos(driftRad) * delta.z;
            self.setDeltaMovement(driftX, delta.y, driftZ);
        }
    }
}

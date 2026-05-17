package com.dusk.mixin.client;

import com.dusk.client.ClientDreadState;
import net.minecraft.client.renderer.FogRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @ModifyVariable(method = "setupFog", at = @At("STORE"), ordinal = 0)
    private static float modifyFogStart(float original) {
        float mult = ClientDreadState.fogMultiplier;
        if (mult >= 1f) return original;
        return original * mult;
    }

    @ModifyVariable(method = "setupFog", at = @At("STORE"), ordinal = 1)
    private static float modifyFogEnd(float original) {
        float mult = ClientDreadState.fogMultiplier;
        if (mult >= 1f) return original;
        return original * mult;
    }
}

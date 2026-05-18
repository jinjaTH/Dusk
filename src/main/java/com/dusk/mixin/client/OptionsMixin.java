package com.dusk.mixin.client;

import com.dusk.client.ClientDreadState;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Options.class)
public class OptionsMixin {

    // Reduce ambient sound volume as dread builds — stage 1 effect (hypervigilance: sounds disappear)
    // Multiplied onto the player's own volume setting so it respects their preferences
    @Inject(method = "getSoundSourceVolume", at = @At("RETURN"), cancellable = true)
    private void muteAmbient(SoundSource source, CallbackInfoReturnable<Float> cir) {
        if (source != SoundSource.AMBIENT) return;
        float mult = ClientDreadState.ambientMult;
        if (mult >= 1f) return;
        cir.setReturnValue(cir.getReturnValue() * mult);
    }
}

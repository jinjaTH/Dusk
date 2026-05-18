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
    // Reduce ambient (birds, wind, cave sounds) and music as dread builds
    // Player footsteps (PLAYERS) and block sounds (BLOCKS) stay at full volume —
    // in a silent world, those become the only things that remind you you're alive
    @Inject(method = "getSoundSourceVolume", at = @At("RETURN"), cancellable = true)
    private void muteAtmosphere(SoundSource source, CallbackInfoReturnable<Float> cir) {
        if (source != SoundSource.AMBIENT && source != SoundSource.MUSIC) return;
        float mult = ClientDreadState.ambientMult;
        if (mult >= 1f) return;
        cir.setReturnValue(cir.getReturnValue() * mult);
    }
}

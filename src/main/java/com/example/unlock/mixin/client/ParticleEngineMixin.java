package com.example.unlock.mixin.client;

import com.example.unlock.UnlockConfig;
import net.minecraft.client.particle.ParticleEngine;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Environment(EnvType.CLIENT)
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    /**
     * Replaces the hardcoded particle limit (16384) with Integer.MAX_VALUE when the feature is enabled.
     * The constant 16384 appears in the tick() method where particles are culled.
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 16384))
    private int modifyParticleLimit(int original) {
        return UnlockConfig.get().enableParticleLimit ? Integer.MAX_VALUE : original;
    }
}

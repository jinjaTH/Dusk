package com.example.unlock.mixin.client;

import com.example.unlock.UnlockConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Environment(EnvType.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    /**
     * Extends the client-side block reach distance check from ~4.5 to 10 blocks.
     * The constant 4.5 (as a double) is compared in destroyDelay / startDestroyBlock.
     */
    @ModifyConstant(
        method = "startDestroyBlock",
        constant = @Constant(doubleValue = 4.5)
    )
    private double modifyBlockReach(double original) {
        return UnlockConfig.get().enableReach ? 10.0 : original;
    }

    @ModifyConstant(
        method = "continueDestroyBlock",
        constant = @Constant(doubleValue = 4.5)
    )
    private double modifyBlockReachContinue(double original) {
        return UnlockConfig.get().enableReach ? 10.0 : original;
    }
}

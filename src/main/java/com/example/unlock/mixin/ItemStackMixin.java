package com.example.unlock.mixin;

import com.example.unlock.UnlockConfig;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "getMaxStackSize()I", at = @At("RETURN"), cancellable = true)
    private void onGetMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        UnlockConfig cfg = UnlockConfig.get();
        if (!cfg.enableStackSize) return;

        int vanilla = cir.getReturnValue();
        if (vanilla > 1) {
            cir.setReturnValue(Math.min(cfg.maxStackSize, 4096));
        }
    }
}

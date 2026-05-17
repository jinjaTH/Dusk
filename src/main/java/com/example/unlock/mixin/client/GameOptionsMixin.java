package com.example.unlock.mixin.client;

import com.example.unlock.UnlockConfig;
import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Options.class)
public class GameOptionsMixin {

    @Shadow
    public OptionInstance<Integer> renderDistance;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (!UnlockConfig.get().enableRenderDistance) return;

        int max = UnlockConfig.get().maxRenderDistance;
        // Replace the render distance option with an expanded range
        this.renderDistance = new OptionInstance<>(
            "options.renderDistance",
            OptionInstance.noTooltip(),
            (component, value) -> Options.genericValueLabel(component, value),
            new OptionInstance.IntRange(2, max),
            Math.min(this.renderDistance.get(), max),
            value -> {}
        );
    }
}

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
public class OptionInstanceFovMixin {

    @Shadow
    public OptionInstance<Integer> fov;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (!UnlockConfig.get().enableFov) return;

        int currentFov = this.fov.get();
        this.fov = new OptionInstance<>(
            "options.fov",
            OptionInstance.noTooltip(),
            (component, value) -> {
                if (value == 70) return Options.genericValueLabel(component, net.minecraft.network.chat.Component.translatable("options.fov.min"));
                if (value == 110) return Options.genericValueLabel(component, net.minecraft.network.chat.Component.translatable("options.fov.max"));
                return Options.genericValueLabel(component, value);
            },
            new OptionInstance.IntRange(15, 160),
            Math.max(15, Math.min(160, currentFov)),
            value -> {}
        );
    }
}

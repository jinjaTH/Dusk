package com.example.unlock.mixin.client;

import com.example.unlock.UnlockConfig;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Environment(EnvType.CLIENT)
@Mixin(CommandSuggestions.class)
public class CommandSuggestorMixin {

    /**
     * Vanilla limits visible command suggestions to 10. Replace with a large number when enabled.
     */
    @ModifyConstant(method = "showSuggestions", constant = @Constant(intValue = 10))
    private int modifySuggestionLimit(int original) {
        return UnlockConfig.get().enableCommandSuggestions ? Integer.MAX_VALUE : original;
    }
}

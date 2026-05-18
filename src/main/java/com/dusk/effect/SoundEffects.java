package com.dusk.effect;

import com.dusk.client.ClientDreadState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class SoundEffects {

    private static int phantomSoundCooldown = 0;

    public static void clientTick() {
        if (phantomSoundCooldown > 0) phantomSoundCooldown--;

        // Stage 2: phantom footstep/mob sounds
        if (ClientDreadState.nyctophobiaStage == 2 && phantomSoundCooldown == 0) {
            playPhantomSound();
            Minecraft mc = Minecraft.getInstance();
            phantomSoundCooldown = 80 + mc.level.getRandom().nextInt(80);
        }
    }

    private static void playPhantomSound() {
        Minecraft mc = Minecraft.getInstance();
        RandomSource rng = mc.level.getRandom();
        var sound = rng.nextBoolean()
            ? SoundEvents.AMBIENT_CAVE.value()
            : SoundEvents.ZOMBIE_STEP;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, 0.15f + rng.nextFloat() * 0.1f));
    }
}

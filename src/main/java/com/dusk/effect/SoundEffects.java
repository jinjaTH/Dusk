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

        int nStage = ClientDreadState.nyctophobiaStage;
        int tStage = ClientDreadState.thalassophobiaStage;

        // Nyctophobia stage 2: phantom footstep/mob sounds
        if (nStage == 2 && phantomSoundCooldown == 0) {
            playPhantomNyctoSound();
            phantomSoundCooldown = 80 + Minecraft.getInstance().level.getRandom().nextInt(80);
        }

        // Thalassophobia stage 3: phantom underwater sounds
        if (tStage >= 3 && phantomSoundCooldown == 0) {
            playPhantomThalaSound();
            phantomSoundCooldown = 100 + Minecraft.getInstance().level.getRandom().nextInt(60);
        }
    }

    private static void playPhantomNyctoSound() {
        Minecraft mc = Minecraft.getInstance();
        RandomSource rng = mc.level.getRandom();
        // Alternate between footstep-like and ambient cave
        var sound = rng.nextBoolean()
            ? SoundEvents.AMBIENT_CAVE.value()
            : SoundEvents.ZOMBIE_STEP;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, 0.15f + rng.nextFloat() * 0.1f));
    }

    private static void playPhantomThalaSound() {
        Minecraft mc = Minecraft.getInstance();
        RandomSource rng = mc.level.getRandom();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(
            SoundEvents.GUARDIAN_AMBIENT_LAND, 0.2f + rng.nextFloat() * 0.1f));
    }
}

package com.dusk.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class DuskSounds {

    public static final SoundEvent BREATHE_HEAVY = register("breathe_heavy");
    public static final SoundEvent HEARTBEAT     = register("heartbeat");
    public static final SoundEvent ATMOSPHERE    = register("atmosphere");
    public static final SoundEvent WHISPER       = register("whisper");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("dusk", name);
        SoundEvent event = SoundEvent.createVariableRangeEvent(id);
        Registry.register(BuiltInRegistries.SOUND_EVENT, id, event);
        return event;
    }

    public static void register() {
        // Touching the class triggers static field init → registry entries created.
    }
}

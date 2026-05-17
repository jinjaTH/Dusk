package com.example.unlock;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "unlock")
public class UnlockConfig implements ConfigData {

    // Render Distance
    public boolean enableRenderDistance = true;
    @ConfigEntry.BoundedDiscrete(min = 2, max = 128)
    public int maxRenderDistance = 128;

    // Particle Limit
    public boolean enableParticleLimit = true;

    // FOV
    public boolean enableFov = true;

    // Command Suggestions
    public boolean enableCommandSuggestions = true;

    // Stack Size
    public boolean enableStackSize = true;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 4096)
    public int maxStackSize = 4096;

    // Reach Distance
    public boolean enableReach = true;

    // --- static access helpers ---

    private static UnlockConfig instance;

    public static void load() {
        AutoConfig.register(UnlockConfig.class, GsonConfigSerializer::new);
        instance = AutoConfig.getConfigHolder(UnlockConfig.class).getConfig();
    }

    public static UnlockConfig get() {
        if (instance == null) {
            // Fallback in case called before load() (e.g. during mixin init on server)
            return new UnlockConfig();
        }
        return instance;
    }
}

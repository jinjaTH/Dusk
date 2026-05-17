package com.dusk.mixin;

import com.dusk.tracker.DreadTracker;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    /**
     * Block hostile mob spawning when any player is in Nyctophobia mode.
     * Note: exact method signature may need adjustment for target MC version.
     */
    @Inject(
        method = "spawnCategoryForChunk",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onSpawnCategory(
        MobCategory category,
        net.minecraft.server.level.ServerLevel level,
        LevelChunk chunk,
        NaturalSpawner.SpawnPredicate predicate,
        NaturalSpawner.AfterSpawnCallback callback,
        CallbackInfo ci
    ) {
        if (category == MobCategory.MONSTER && DreadTracker.anyInNyctophobia()) {
            ci.cancel();
        }
    }
}

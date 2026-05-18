package com.dusk.mixin;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    @Inject(
        method = "spawnCategoryForChunk",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void blockHostileSpawns(
        MobCategory category,
        net.minecraft.server.level.ServerLevel level,
        LevelChunk chunk,
        NaturalSpawner.SpawnPredicate predicate,
        NaturalSpawner.AfterSpawnCallback callback,
        CallbackInfo ci
    ) {
        if (category == MobCategory.MONSTER) ci.cancel();
    }
}

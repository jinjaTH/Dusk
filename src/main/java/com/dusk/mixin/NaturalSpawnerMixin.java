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
    private static void controlSpawns(
        MobCategory category,
        net.minecraft.server.level.ServerLevel level,
        LevelChunk chunk,
        NaturalSpawner.SpawnPredicate predicate,
        NaturalSpawner.AfterSpawnCallback callback,
        CallbackInfo ci
    ) {
        switch (category) {
            case MONSTER -> ci.cancel(); // ไม่มีปิศาจเลย
            case CREATURE, AMBIENT -> {
                // passive animals และ ambient creatures หายาก 85%
                if (Math.random() < 0.85) ci.cancel();
            }
            default -> {} // water creatures, underground water ฯลฯ ปกติ
        }
    }
}

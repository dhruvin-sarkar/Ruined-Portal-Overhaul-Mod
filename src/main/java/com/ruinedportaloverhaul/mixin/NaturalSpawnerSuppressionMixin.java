package com.ruinedportaloverhaul.mixin;

import com.ruinedportaloverhaul.world.ModNaturalSpawnGuards;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NaturalSpawner.class)
abstract class NaturalSpawnerSuppressionMixin {
    @Redirect(
        method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;test(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/ChunkAccess;)Z"
        )
    )
    private static boolean ruinedportaloverhaul$guardPortalNaturalSpawn(
        NaturalSpawner.SpawnPredicate predicate,
        EntityType<?> entityType,
        BlockPos spawnPos,
        ChunkAccess spawnChunk,
        MobCategory category,
        ServerLevel level,
        ChunkAccess categoryChunk,
        BlockPos categoryOrigin,
        NaturalSpawner.SpawnPredicate categoryPredicate,
        NaturalSpawner.AfterSpawnCallback afterSpawnCallback
    ) {
        // Fix: this wrapper now enforces only completed-portal suppression. Portal ambience is spawned manually by the raid manager, so the natural-spawn hook must not become a global mob-toggle for unrelated overworld terrain.
        if (ModNaturalSpawnGuards.shouldSuppressNaturalSpawn(level, spawnPos)) {
            return false;
        }
        return predicate.test(entityType, spawnPos, spawnChunk);
    }
}

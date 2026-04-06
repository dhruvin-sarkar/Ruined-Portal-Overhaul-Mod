/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class RandomSpreadStructurePlacement
extends StructurePlacement {
    public static final MapCodec<RandomSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> RandomSpreadStructurePlacement.placementCodec(instance).and(instance.group((App)Codec.intRange((int)0, (int)4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing), (App)Codec.intRange((int)0, (int)4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation), (App)RandomSpreadType.CODEC.optionalFieldOf("spread_type", (Object)RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType))).apply((Applicative)instance, RandomSpreadStructurePlacement::new)).validate(RandomSpreadStructurePlacement::validate);
    private final int spacing;
    private final int separation;
    private final RandomSpreadType spreadType;

    private static DataResult<RandomSpreadStructurePlacement> validate(RandomSpreadStructurePlacement randomSpreadStructurePlacement) {
        if (randomSpreadStructurePlacement.spacing <= randomSpreadStructurePlacement.separation) {
            return DataResult.error(() -> "Spacing has to be larger than separation");
        }
        return DataResult.success((Object)randomSpreadStructurePlacement);
    }

    public RandomSpreadStructurePlacement(Vec3i vec3i, StructurePlacement.FrequencyReductionMethod frequencyReductionMethod, float f, int i, Optional<StructurePlacement.ExclusionZone> optional, int j, int k, RandomSpreadType randomSpreadType) {
        super(vec3i, frequencyReductionMethod, f, i, optional);
        this.spacing = j;
        this.separation = k;
        this.spreadType = randomSpreadType;
    }

    public RandomSpreadStructurePlacement(int i, int j, RandomSpreadType randomSpreadType, int k) {
        this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, k, Optional.empty(), i, j, randomSpreadType);
    }

    public int spacing() {
        return this.spacing;
    }

    public int separation() {
        return this.separation;
    }

    public RandomSpreadType spreadType() {
        return this.spreadType;
    }

    public ChunkPos getPotentialStructureChunk(long l, int i, int j) {
        int k = Math.floorDiv(i, this.spacing);
        int m = Math.floorDiv(j, this.spacing);
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureWithSalt(l, k, m, this.salt());
        int n = this.spacing - this.separation;
        int o = this.spreadType.evaluate(worldgenRandom, n);
        int p = this.spreadType.evaluate(worldgenRandom, n);
        return new ChunkPos(k * this.spacing + o, m * this.spacing + p);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState chunkGeneratorStructureState, int i, int j) {
        ChunkPos chunkPos = this.getPotentialStructureChunk(chunkGeneratorStructureState.getLevelSeed(), i, j);
        return chunkPos.x == i && chunkPos.z == j;
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.RANDOM_SPREAD;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilPieces;

public class NetherFossilStructure
extends Structure {
    public static final MapCodec<NetherFossilStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(NetherFossilStructure.settingsCodec(instance), (App)HeightProvider.CODEC.fieldOf("height").forGetter(netherFossilStructure -> netherFossilStructure.height)).apply((Applicative)instance, NetherFossilStructure::new));
    public final HeightProvider height;

    public NetherFossilStructure(Structure.StructureSettings structureSettings, HeightProvider heightProvider) {
        super(structureSettings);
        this.height = heightProvider;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        WorldgenRandom worldgenRandom = generationContext.random();
        int i = generationContext.chunkPos().getMinBlockX() + worldgenRandom.nextInt(16);
        int j = generationContext.chunkPos().getMinBlockZ() + worldgenRandom.nextInt(16);
        int k = generationContext.chunkGenerator().getSeaLevel();
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(generationContext.chunkGenerator(), generationContext.heightAccessor());
        int l = this.height.sample(worldgenRandom, worldGenerationContext);
        NoiseColumn noiseColumn = generationContext.chunkGenerator().getBaseColumn(i, j, generationContext.heightAccessor(), generationContext.randomState());
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, l, j);
        while (l > k) {
            BlockState blockState = noiseColumn.getBlock(l);
            BlockState blockState2 = noiseColumn.getBlock(--l);
            if (!blockState.isAir() || !blockState2.is(Blocks.SOUL_SAND) && !blockState2.isFaceSturdy(EmptyBlockGetter.INSTANCE, mutableBlockPos.setY(l), Direction.UP)) continue;
            break;
        }
        if (l <= k) {
            return Optional.empty();
        }
        BlockPos blockPos = new BlockPos(i, l, j);
        return Optional.of(new Structure.GenerationStub(blockPos, structurePiecesBuilder -> NetherFossilPieces.addPieces(generationContext.structureTemplateManager(), structurePiecesBuilder, worldgenRandom, blockPos)));
    }

    @Override
    public StructureType<?> type() {
        return StructureType.NETHER_FOSSIL;
    }
}


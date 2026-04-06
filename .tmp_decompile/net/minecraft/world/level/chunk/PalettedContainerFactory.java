/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.chunk;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.Strategy;

public record PalettedContainerFactory(Strategy<BlockState> blockStatesStrategy, BlockState defaultBlockState, Codec<PalettedContainer<BlockState>> blockStatesContainerCodec, Strategy<Holder<Biome>> biomeStrategy, Holder<Biome> defaultBiome, Codec<PalettedContainerRO<Holder<Biome>>> biomeContainerCodec) {
    public static PalettedContainerFactory create(RegistryAccess registryAccess) {
        Strategy<BlockState> strategy = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);
        BlockState blockState = Blocks.AIR.defaultBlockState();
        HolderLookup.RegistryLookup registry = registryAccess.lookupOrThrow(Registries.BIOME);
        Strategy<Holder<Biome>> strategy2 = Strategy.createForBiomes(registry.asHolderIdMap());
        Holder.Reference reference = registry.getOrThrow(Biomes.PLAINS);
        return new PalettedContainerFactory(strategy, blockState, PalettedContainer.codecRW(BlockState.CODEC, strategy, blockState), strategy2, reference, PalettedContainer.codecRO(registry.holderByNameCodec(), strategy2, reference));
    }

    public PalettedContainer<BlockState> createForBlockStates() {
        return new PalettedContainer<BlockState>(this.defaultBlockState, this.blockStatesStrategy);
    }

    public PalettedContainer<Holder<Biome>> createForBiomes() {
        return new PalettedContainer<Holder<Biome>>(this.defaultBiome, this.biomeStrategy);
    }
}


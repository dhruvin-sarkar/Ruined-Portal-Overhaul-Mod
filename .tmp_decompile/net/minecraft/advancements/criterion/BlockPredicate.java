/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jspecify.annotations.Nullable;

public record BlockPredicate(Optional<HolderSet<Block>> blocks, Optional<StatePropertiesPredicate> properties, Optional<NbtPredicate> nbt, DataComponentMatchers components) {
    public static final Codec<BlockPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("blocks").forGetter(BlockPredicate::blocks), (App)StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(BlockPredicate::properties), (App)NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(BlockPredicate::nbt), (App)DataComponentMatchers.CODEC.forGetter(BlockPredicate::components)).apply((Applicative)instance, BlockPredicate::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPredicate> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.optional(ByteBufCodecs.holderSet(Registries.BLOCK)), BlockPredicate::blocks, ByteBufCodecs.optional(StatePropertiesPredicate.STREAM_CODEC), BlockPredicate::properties, ByteBufCodecs.optional(NbtPredicate.STREAM_CODEC), BlockPredicate::nbt, DataComponentMatchers.STREAM_CODEC, BlockPredicate::components, BlockPredicate::new);

    public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
        if (!serverLevel.isLoaded(blockPos)) {
            return false;
        }
        if (!this.matchesState(serverLevel.getBlockState(blockPos))) {
            return false;
        }
        if (this.nbt.isPresent() || !this.components.isEmpty()) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
            if (this.nbt.isPresent() && !BlockPredicate.matchesBlockEntity(serverLevel, blockEntity, this.nbt.get())) {
                return false;
            }
            if (!this.components.isEmpty() && !BlockPredicate.matchesComponents(blockEntity, this.components)) {
                return false;
            }
        }
        return true;
    }

    public boolean matches(BlockInWorld blockInWorld) {
        if (!this.matchesState(blockInWorld.getState())) {
            return false;
        }
        return !this.nbt.isPresent() || BlockPredicate.matchesBlockEntity(blockInWorld.getLevel(), blockInWorld.getEntity(), this.nbt.get());
    }

    private boolean matchesState(BlockState blockState) {
        if (this.blocks.isPresent() && !blockState.is(this.blocks.get())) {
            return false;
        }
        return !this.properties.isPresent() || this.properties.get().matches(blockState);
    }

    private static boolean matchesBlockEntity(LevelReader levelReader, @Nullable BlockEntity blockEntity, NbtPredicate nbtPredicate) {
        return blockEntity != null && nbtPredicate.matches(blockEntity.saveWithFullMetadata(levelReader.registryAccess()));
    }

    private static boolean matchesComponents(@Nullable BlockEntity blockEntity, DataComponentMatchers dataComponentMatchers) {
        return blockEntity != null && dataComponentMatchers.test(blockEntity.collectComponents());
    }

    public boolean requiresNbt() {
        return this.nbt.isPresent();
    }

    public static class Builder {
        private Optional<HolderSet<Block>> blocks = Optional.empty();
        private Optional<StatePropertiesPredicate> properties = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();
        private DataComponentMatchers components = DataComponentMatchers.ANY;

        private Builder() {
        }

        public static Builder block() {
            return new Builder();
        }

        public Builder of(HolderGetter<Block> holderGetter, Block ... blocks) {
            return this.of(holderGetter, Arrays.asList(blocks));
        }

        public Builder of(HolderGetter<Block> holderGetter, Collection<Block> collection) {
            this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, collection));
            return this;
        }

        public Builder of(HolderGetter<Block> holderGetter, TagKey<Block> tagKey) {
            this.blocks = Optional.of(holderGetter.getOrThrow(tagKey));
            return this;
        }

        public Builder hasNbt(CompoundTag compoundTag) {
            this.nbt = Optional.of(new NbtPredicate(compoundTag));
            return this;
        }

        public Builder setProperties(StatePropertiesPredicate.Builder builder) {
            this.properties = builder.build();
            return this;
        }

        public Builder components(DataComponentMatchers dataComponentMatchers) {
            this.components = dataComponentMatchers;
            return this;
        }

        public BlockPredicate build() {
            return new BlockPredicate(this.blocks, this.properties, this.nbt, this.components);
        }
    }
}


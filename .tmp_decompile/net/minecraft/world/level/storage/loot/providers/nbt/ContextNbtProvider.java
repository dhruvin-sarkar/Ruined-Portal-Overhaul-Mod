/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextArg;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import org.jspecify.annotations.Nullable;

public class ContextNbtProvider
implements NbtProvider {
    private static final Codec<LootContextArg<Tag>> GETTER_CODEC = LootContextArg.createArgCodec(argCodecBuilder -> argCodecBuilder.anyBlockEntity(BlockEntitySource::new).anyEntity(EntitySource::new));
    public static final MapCodec<ContextNbtProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)GETTER_CODEC.fieldOf("target").forGetter(contextNbtProvider -> contextNbtProvider.source)).apply((Applicative)instance, ContextNbtProvider::new));
    public static final Codec<ContextNbtProvider> INLINE_CODEC = GETTER_CODEC.xmap(ContextNbtProvider::new, contextNbtProvider -> contextNbtProvider.source);
    private final LootContextArg<Tag> source;

    private ContextNbtProvider(LootContextArg<Tag> lootContextArg) {
        this.source = lootContextArg;
    }

    @Override
    public LootNbtProviderType getType() {
        return NbtProviders.CONTEXT;
    }

    @Override
    public @Nullable Tag get(LootContext lootContext) {
        return this.source.get(lootContext);
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(this.source.contextParam());
    }

    public static NbtProvider forContextEntity(LootContext.EntityTarget entityTarget) {
        return new ContextNbtProvider(new EntitySource(entityTarget.contextParam()));
    }

    record EntitySource(ContextKey<? extends Entity> contextParam) implements LootContextArg.Getter<Entity, Tag>
    {
        @Override
        public Tag get(Entity entity) {
            return NbtPredicate.getEntityTagToCompare(entity);
        }
    }

    record BlockEntitySource(ContextKey<? extends BlockEntity> contextParam) implements LootContextArg.Getter<BlockEntity, Tag>
    {
        @Override
        public Tag get(BlockEntity blockEntity) {
            return blockEntity.saveWithFullMetadata(blockEntity.getLevel().registryAccess());
        }
    }
}


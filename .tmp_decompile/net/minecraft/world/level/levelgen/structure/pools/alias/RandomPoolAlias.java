/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;

public record RandomPoolAlias(ResourceKey<StructureTemplatePool> alias, WeightedList<ResourceKey<StructureTemplatePool>> targets) implements PoolAliasBinding
{
    static MapCodec<RandomPoolAlias> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceKey.codec(Registries.TEMPLATE_POOL).fieldOf("alias").forGetter(RandomPoolAlias::alias), (App)WeightedList.nonEmptyCodec(ResourceKey.codec(Registries.TEMPLATE_POOL)).fieldOf("targets").forGetter(RandomPoolAlias::targets)).apply((Applicative)instance, RandomPoolAlias::new));

    @Override
    public void forEachResolved(RandomSource randomSource, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> biConsumer) {
        this.targets.getRandom(randomSource).ifPresent(resourceKey -> biConsumer.accept(this.alias, (ResourceKey<StructureTemplatePool>)resourceKey));
    }

    @Override
    public Stream<ResourceKey<StructureTemplatePool>> allTargets() {
        return this.targets.unwrap().stream().map(Weighted::value);
    }

    public MapCodec<RandomPoolAlias> codec() {
        return CODEC;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.DirectPoolAlias;
import net.minecraft.world.level.levelgen.structure.pools.alias.RandomGroupPoolAlias;
import net.minecraft.world.level.levelgen.structure.pools.alias.RandomPoolAlias;

public interface PoolAliasBinding {
    public static final Codec<PoolAliasBinding> CODEC = BuiltInRegistries.POOL_ALIAS_BINDING_TYPE.byNameCodec().dispatch(PoolAliasBinding::codec, Function.identity());

    public void forEachResolved(RandomSource var1, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> var2);

    public Stream<ResourceKey<StructureTemplatePool>> allTargets();

    public static DirectPoolAlias direct(String string, String string2) {
        return PoolAliasBinding.direct(Pools.createKey(string), Pools.createKey(string2));
    }

    public static DirectPoolAlias direct(ResourceKey<StructureTemplatePool> resourceKey, ResourceKey<StructureTemplatePool> resourceKey2) {
        return new DirectPoolAlias(resourceKey, resourceKey2);
    }

    public static RandomPoolAlias random(String string, WeightedList<String> weightedList) {
        WeightedList.Builder builder = WeightedList.builder();
        weightedList.unwrap().forEach(weighted -> builder.add(Pools.createKey((String)weighted.value()), weighted.weight()));
        return PoolAliasBinding.random(Pools.createKey(string), builder.build());
    }

    public static RandomPoolAlias random(ResourceKey<StructureTemplatePool> resourceKey, WeightedList<ResourceKey<StructureTemplatePool>> weightedList) {
        return new RandomPoolAlias(resourceKey, weightedList);
    }

    public static RandomGroupPoolAlias randomGroup(WeightedList<List<PoolAliasBinding>> weightedList) {
        return new RandomGroupPoolAlias(weightedList);
    }

    public MapCodec<? extends PoolAliasBinding> codec();
}


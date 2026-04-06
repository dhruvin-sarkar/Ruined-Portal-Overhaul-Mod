/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.VariantSelector;
import net.minecraft.client.renderer.block.model.multipart.MultiPartModel;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record BlockModelDefinition(Optional<SimpleModelSelectors> simpleModels, Optional<MultiPartDefinition> multiPart) {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<BlockModelDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SimpleModelSelectors.CODEC.optionalFieldOf("variants").forGetter(BlockModelDefinition::simpleModels), (App)MultiPartDefinition.CODEC.optionalFieldOf("multipart").forGetter(BlockModelDefinition::multiPart)).apply((Applicative)instance, BlockModelDefinition::new)).validate(blockModelDefinition -> {
        if (blockModelDefinition.simpleModels().isEmpty() && blockModelDefinition.multiPart().isEmpty()) {
            return DataResult.error(() -> "Neither 'variants' nor 'multipart' found");
        }
        return DataResult.success((Object)blockModelDefinition);
    });

    public Map<BlockState, BlockStateModel.UnbakedRoot> instantiate(StateDefinition<Block, BlockState> stateDefinition, Supplier<String> supplier) {
        IdentityHashMap<BlockState, BlockStateModel.UnbakedRoot> map = new IdentityHashMap<BlockState, BlockStateModel.UnbakedRoot>();
        this.simpleModels.ifPresent(simpleModelSelectors -> simpleModelSelectors.instantiate(stateDefinition, supplier, (blockState, unbakedRoot) -> {
            BlockStateModel.UnbakedRoot unbakedRoot2 = map.put((BlockState)blockState, (BlockStateModel.UnbakedRoot)unbakedRoot);
            if (unbakedRoot2 != null) {
                throw new IllegalArgumentException("Overlapping definition on state: " + String.valueOf(blockState));
            }
        }));
        this.multiPart.ifPresent(multiPartDefinition -> {
            ImmutableList list = stateDefinition.getPossibleStates();
            MultiPartModel.Unbaked unbakedRoot = multiPartDefinition.instantiate(stateDefinition);
            for (BlockState blockState : list) {
                map.putIfAbsent(blockState, unbakedRoot);
            }
        });
        return map;
    }

    @Environment(value=EnvType.CLIENT)
    public record MultiPartDefinition(List<Selector> selectors) {
        public static final Codec<MultiPartDefinition> CODEC = ExtraCodecs.nonEmptyList(Selector.CODEC.listOf()).xmap(MultiPartDefinition::new, MultiPartDefinition::selectors);

        public MultiPartModel.Unbaked instantiate(StateDefinition<Block, BlockState> stateDefinition) {
            ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)this.selectors.size());
            for (Selector selector : this.selectors) {
                builder.add(new MultiPartModel.Selector<BlockStateModel.Unbaked>(selector.instantiate(stateDefinition), selector.variant()));
            }
            return new MultiPartModel.Unbaked((List<MultiPartModel.Selector<BlockStateModel.Unbaked>>)builder.build());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record SimpleModelSelectors(Map<String, BlockStateModel.Unbaked> models) {
        public static final Codec<SimpleModelSelectors> CODEC = ExtraCodecs.nonEmptyMap(Codec.unboundedMap((Codec)Codec.STRING, BlockStateModel.Unbaked.CODEC)).xmap(SimpleModelSelectors::new, SimpleModelSelectors::models);

        public void instantiate(StateDefinition<Block, BlockState> stateDefinition, Supplier<String> supplier, BiConsumer<BlockState, BlockStateModel.UnbakedRoot> biConsumer) {
            this.models.forEach((string, unbaked) -> {
                try {
                    Predicate predicate = VariantSelector.predicate(stateDefinition, string);
                    BlockStateModel.UnbakedRoot unbakedRoot = unbaked.asRoot();
                    for (BlockState blockState : stateDefinition.getPossibleStates()) {
                        if (!predicate.test(blockState)) continue;
                        biConsumer.accept(blockState, unbakedRoot);
                    }
                }
                catch (Exception exception) {
                    LOGGER.warn("Exception loading blockstate definition: '{}' for variant: '{}': {}", new Object[]{supplier.get(), string, exception.getMessage()});
                }
            });
        }
    }
}


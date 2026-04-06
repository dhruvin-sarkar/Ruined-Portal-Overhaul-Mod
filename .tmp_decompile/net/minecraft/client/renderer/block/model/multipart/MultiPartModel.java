/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MultiPartModel
implements BlockStateModel {
    private final SharedBakedState shared;
    private final BlockState blockState;
    private @Nullable List<BlockStateModel> models;

    MultiPartModel(SharedBakedState sharedBakedState, BlockState blockState) {
        this.shared = sharedBakedState;
        this.blockState = blockState;
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return this.shared.particleIcon;
    }

    @Override
    public void collectParts(RandomSource randomSource, List<BlockModelPart> list) {
        if (this.models == null) {
            this.models = this.shared.selectModels(this.blockState);
        }
        long l = randomSource.nextLong();
        for (BlockStateModel blockStateModel : this.models) {
            randomSource.setSeed(l);
            blockStateModel.collectParts(randomSource, list);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class SharedBakedState {
        private final List<Selector<BlockStateModel>> selectors;
        final TextureAtlasSprite particleIcon;
        private final Map<BitSet, List<BlockStateModel>> subsets = new ConcurrentHashMap<BitSet, List<BlockStateModel>>();

        private static BlockStateModel getFirstModel(List<Selector<BlockStateModel>> list) {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("Model must have at least one selector");
            }
            return (BlockStateModel)((Selector)((Object)list.getFirst())).model();
        }

        public SharedBakedState(List<Selector<BlockStateModel>> list) {
            this.selectors = list;
            BlockStateModel blockStateModel = SharedBakedState.getFirstModel(list);
            this.particleIcon = blockStateModel.particleIcon();
        }

        public List<BlockStateModel> selectModels(BlockState blockState) {
            BitSet bitSet2 = new BitSet();
            for (int i = 0; i < this.selectors.size(); ++i) {
                if (!this.selectors.get((int)i).condition.test(blockState)) continue;
                bitSet2.set(i);
            }
            return this.subsets.computeIfAbsent(bitSet2, bitSet -> {
                ImmutableList.Builder builder = ImmutableList.builder();
                for (int i = 0; i < this.selectors.size(); ++i) {
                    if (!bitSet.get(i)) continue;
                    builder.add((Object)((BlockStateModel)this.selectors.get((int)i).model));
                }
                return builder.build();
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Unbaked
    implements BlockStateModel.UnbakedRoot {
        final List<Selector<BlockStateModel.Unbaked>> selectors;
        private final ModelBaker.SharedOperationKey<SharedBakedState> sharedStateKey = new ModelBaker.SharedOperationKey<SharedBakedState>(){

            @Override
            public SharedBakedState compute(ModelBaker modelBaker) {
                ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)selectors.size());
                for (Selector<BlockStateModel.Unbaked> selector : selectors) {
                    builder.add(selector.with(((BlockStateModel.Unbaked)selector.model).bake(modelBaker)));
                }
                return new SharedBakedState((List<Selector<BlockStateModel>>)builder.build());
            }

            @Override
            public /* synthetic */ Object compute(ModelBaker modelBaker) {
                return this.compute(modelBaker);
            }
        };

        public Unbaked(List<Selector<BlockStateModel.Unbaked>> list) {
            this.selectors = list;
        }

        @Override
        public Object visualEqualityGroup(BlockState blockState) {
            IntArrayList intList = new IntArrayList();
            for (int i = 0; i < this.selectors.size(); ++i) {
                if (!this.selectors.get((int)i).condition.test(blockState)) continue;
                intList.add(i);
            }
            @Environment(value=EnvType.CLIENT)
            record Key(Unbaked model, IntList selectors) {
            }
            return new Key(this, (IntList)intList);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.selectors.forEach(selector -> ((BlockStateModel.Unbaked)selector.model).resolveDependencies(resolver));
        }

        @Override
        public BlockStateModel bake(BlockState blockState, ModelBaker modelBaker) {
            SharedBakedState sharedBakedState = modelBaker.compute(this.sharedStateKey);
            return new MultiPartModel(sharedBakedState, blockState);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Selector<T>
    extends Record {
        final Predicate<BlockState> condition;
        final T model;

        public Selector(Predicate<BlockState> predicate, T object) {
            this.condition = predicate;
            this.model = object;
        }

        public <S> Selector<S> with(S object) {
            return new Selector<S>(this.condition, object);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Selector.class, "condition;model", "condition", "model"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Selector.class, "condition;model", "condition", "model"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Selector.class, "condition;model", "condition", "model"}, this, object);
        }

        public Predicate<BlockState> condition() {
            return this.condition;
        }

        public T model() {
            return this.model;
        }
    }
}


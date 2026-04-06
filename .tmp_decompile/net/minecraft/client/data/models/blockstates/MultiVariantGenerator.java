/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.data.models.blockstates;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.PropertyValueList;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

@Environment(value=EnvType.CLIENT)
public class MultiVariantGenerator
implements BlockModelDefinitionGenerator {
    private final Block block;
    private final List<Entry> entries;
    private final Set<Property<?>> seenProperties;

    MultiVariantGenerator(Block block, List<Entry> list, Set<Property<?>> set) {
        this.block = block;
        this.entries = list;
        this.seenProperties = set;
    }

    static Set<Property<?>> validateAndExpandProperties(Set<Property<?>> set, Block block, PropertyDispatch<?> propertyDispatch) {
        List<Property<?>> list = propertyDispatch.getDefinedProperties();
        list.forEach(property -> {
            if (block.getStateDefinition().getProperty(property.getName()) != property) {
                throw new IllegalStateException("Property " + String.valueOf(property) + " is not defined for block " + String.valueOf(block));
            }
            if (set.contains(property)) {
                throw new IllegalStateException("Values of property " + String.valueOf(property) + " already defined for block " + String.valueOf(block));
            }
        });
        HashSet set2 = new HashSet(set);
        set2.addAll(list);
        return set2;
    }

    public MultiVariantGenerator with(PropertyDispatch<VariantMutator> propertyDispatch) {
        Set<Property<?>> set = MultiVariantGenerator.validateAndExpandProperties(this.seenProperties, this.block, propertyDispatch);
        List list = this.entries.stream().flatMap(entry -> entry.apply(propertyDispatch)).toList();
        return new MultiVariantGenerator(this.block, list, set);
    }

    public MultiVariantGenerator with(VariantMutator variantMutator) {
        List list = this.entries.stream().flatMap(entry -> entry.apply(variantMutator)).toList();
        return new MultiVariantGenerator(this.block, list, this.seenProperties);
    }

    @Override
    public BlockModelDefinition create() {
        HashMap<String, BlockStateModel.Unbaked> map = new HashMap<String, BlockStateModel.Unbaked>();
        for (Entry entry : this.entries) {
            map.put(entry.properties.getKey(), entry.variant.toUnbaked());
        }
        return new BlockModelDefinition(Optional.of(new BlockModelDefinition.SimpleModelSelectors(map)), Optional.empty());
    }

    @Override
    public Block block() {
        return this.block;
    }

    public static Empty dispatch(Block block) {
        return new Empty(block);
    }

    public static MultiVariantGenerator dispatch(Block block, MultiVariant multiVariant) {
        return new MultiVariantGenerator(block, List.of((Object)((Object)new Entry(PropertyValueList.EMPTY, multiVariant))), Set.of());
    }

    @Environment(value=EnvType.CLIENT)
    static final class Entry
    extends Record {
        final PropertyValueList properties;
        final MultiVariant variant;

        Entry(PropertyValueList propertyValueList, MultiVariant multiVariant) {
            this.properties = propertyValueList;
            this.variant = multiVariant;
        }

        public Stream<Entry> apply(PropertyDispatch<VariantMutator> propertyDispatch) {
            return propertyDispatch.getEntries().entrySet().stream().map(entry -> {
                PropertyValueList propertyValueList = this.properties.extend((PropertyValueList)((Object)((Object)entry.getKey())));
                MultiVariant multiVariant = this.variant.with((VariantMutator)entry.getValue());
                return new Entry(propertyValueList, multiVariant);
            });
        }

        public Stream<Entry> apply(VariantMutator variantMutator) {
            return Stream.of(new Entry(this.properties, this.variant.with(variantMutator)));
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "properties;variant", "properties", "variant"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "properties;variant", "properties", "variant"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "properties;variant", "properties", "variant"}, this, object);
        }

        public PropertyValueList properties() {
            return this.properties;
        }

        public MultiVariant variant() {
            return this.variant;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Empty {
        private final Block block;

        public Empty(Block block) {
            this.block = block;
        }

        public MultiVariantGenerator with(PropertyDispatch<MultiVariant> propertyDispatch) {
            Set<Property<?>> set = MultiVariantGenerator.validateAndExpandProperties(Set.of(), this.block, propertyDispatch);
            List list = propertyDispatch.getEntries().entrySet().stream().map(entry -> new Entry((PropertyValueList)((Object)((Object)entry.getKey())), (MultiVariant)((Object)((Object)entry.getValue())))).toList();
            return new MultiVariantGenerator(this.block, list, set);
        }
    }
}


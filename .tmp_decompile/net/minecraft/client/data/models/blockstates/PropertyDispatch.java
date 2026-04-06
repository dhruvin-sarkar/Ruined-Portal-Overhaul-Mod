/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Function3
 *  com.mojang.datafixers.util.Function4
 *  com.mojang.datafixers.util.Function5
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.data.models.blockstates;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.PropertyValueList;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.world.level.block.state.properties.Property;

@Environment(value=EnvType.CLIENT)
public abstract class PropertyDispatch<V> {
    private final Map<PropertyValueList, V> values = new HashMap<PropertyValueList, V>();

    protected void putValue(PropertyValueList propertyValueList, V object) {
        V object2 = this.values.put(propertyValueList, object);
        if (object2 != null) {
            throw new IllegalStateException("Value " + String.valueOf((Object)propertyValueList) + " is already defined");
        }
    }

    Map<PropertyValueList, V> getEntries() {
        this.verifyComplete();
        return Map.copyOf(this.values);
    }

    private void verifyComplete() {
        List<Property<?>> list = this.getDefinedProperties();
        Stream<PropertyValueList> stream = Stream.of(PropertyValueList.EMPTY);
        for (Property<?> property : list) {
            stream = stream.flatMap(propertyValueList -> property.getAllValues().map(propertyValueList::extend));
        }
        List list2 = stream.filter(propertyValueList -> !this.values.containsKey(propertyValueList)).toList();
        if (!list2.isEmpty()) {
            throw new IllegalStateException("Missing definition for properties: " + String.valueOf(list2));
        }
    }

    abstract List<Property<?>> getDefinedProperties();

    public static <T1 extends Comparable<T1>> C1<MultiVariant, T1> initial(Property<T1> property) {
        return new C1(property);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>> C2<MultiVariant, T1, T2> initial(Property<T1> property, Property<T2> property2) {
        return new C2(property, property2);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> C3<MultiVariant, T1, T2, T3> initial(Property<T1> property, Property<T2> property2, Property<T3> property3) {
        return new C3(property, property2, property3);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> C4<MultiVariant, T1, T2, T3, T4> initial(Property<T1> property, Property<T2> property2, Property<T3> property3, Property<T4> property4) {
        return new C4(property, property2, property3, property4);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> C5<MultiVariant, T1, T2, T3, T4, T5> initial(Property<T1> property, Property<T2> property2, Property<T3> property3, Property<T4> property4, Property<T5> property5) {
        return new C5(property, property2, property3, property4, property5);
    }

    public static <T1 extends Comparable<T1>> C1<VariantMutator, T1> modify(Property<T1> property) {
        return new C1(property);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>> C2<VariantMutator, T1, T2> modify(Property<T1> property, Property<T2> property2) {
        return new C2(property, property2);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> C3<VariantMutator, T1, T2, T3> modify(Property<T1> property, Property<T2> property2, Property<T3> property3) {
        return new C3(property, property2, property3);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> C4<VariantMutator, T1, T2, T3, T4> modify(Property<T1> property, Property<T2> property2, Property<T3> property3, Property<T4> property4) {
        return new C4(property, property2, property3, property4);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> C5<VariantMutator, T1, T2, T3, T4, T5> modify(Property<T1> property, Property<T2> property2, Property<T3> property3, Property<T4> property4, Property<T5> property5) {
        return new C5(property, property2, property3, property4, property5);
    }

    @Environment(value=EnvType.CLIENT)
    public static class C1<V, T1 extends Comparable<T1>>
    extends PropertyDispatch<V> {
        private final Property<T1> property1;

        C1(Property<T1> property) {
            this.property1 = property;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return List.of(this.property1);
        }

        public C1<V, T1> select(T1 comparable, V object) {
            PropertyValueList propertyValueList = PropertyValueList.of(this.property1.value(comparable));
            this.putValue(propertyValueList, object);
            return this;
        }

        public PropertyDispatch<V> generate(Function<T1, V> function) {
            this.property1.getPossibleValues().forEach(comparable -> this.select(comparable, function.apply(comparable)));
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class C2<V, T1 extends Comparable<T1>, T2 extends Comparable<T2>>
    extends PropertyDispatch<V> {
        private final Property<T1> property1;
        private final Property<T2> property2;

        C2(Property<T1> property, Property<T2> property2) {
            this.property1 = property;
            this.property2 = property2;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return List.of(this.property1, this.property2);
        }

        public C2<V, T1, T2> select(T1 comparable, T2 comparable2, V object) {
            PropertyValueList propertyValueList = PropertyValueList.of(this.property1.value(comparable), this.property2.value(comparable2));
            this.putValue(propertyValueList, object);
            return this;
        }

        public PropertyDispatch<V> generate(BiFunction<T1, T2, V> biFunction) {
            this.property1.getPossibleValues().forEach(comparable -> this.property2.getPossibleValues().forEach(comparable2 -> this.select(comparable, comparable2, biFunction.apply(comparable, comparable2))));
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class C3<V, T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>>
    extends PropertyDispatch<V> {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;

        C3(Property<T1> property, Property<T2> property2, Property<T3> property3) {
            this.property1 = property;
            this.property2 = property2;
            this.property3 = property3;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return List.of(this.property1, this.property2, this.property3);
        }

        public C3<V, T1, T2, T3> select(T1 comparable, T2 comparable2, T3 comparable3, V object) {
            PropertyValueList propertyValueList = PropertyValueList.of(this.property1.value(comparable), this.property2.value(comparable2), this.property3.value(comparable3));
            this.putValue(propertyValueList, object);
            return this;
        }

        public PropertyDispatch<V> generate(Function3<T1, T2, T3, V> function3) {
            this.property1.getPossibleValues().forEach(comparable -> this.property2.getPossibleValues().forEach(comparable2 -> this.property3.getPossibleValues().forEach(comparable3 -> this.select(comparable, comparable2, comparable3, function3.apply(comparable, comparable2, comparable3)))));
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class C4<V, T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>>
    extends PropertyDispatch<V> {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;
        private final Property<T4> property4;

        C4(Property<T1> property, Property<T2> property2, Property<T3> property3, Property<T4> property4) {
            this.property1 = property;
            this.property2 = property2;
            this.property3 = property3;
            this.property4 = property4;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return List.of(this.property1, this.property2, this.property3, this.property4);
        }

        public C4<V, T1, T2, T3, T4> select(T1 comparable, T2 comparable2, T3 comparable3, T4 comparable4, V object) {
            PropertyValueList propertyValueList = PropertyValueList.of(this.property1.value(comparable), this.property2.value(comparable2), this.property3.value(comparable3), this.property4.value(comparable4));
            this.putValue(propertyValueList, object);
            return this;
        }

        public PropertyDispatch<V> generate(Function4<T1, T2, T3, T4, V> function4) {
            this.property1.getPossibleValues().forEach(comparable -> this.property2.getPossibleValues().forEach(comparable2 -> this.property3.getPossibleValues().forEach(comparable3 -> this.property4.getPossibleValues().forEach(comparable4 -> this.select(comparable, comparable2, comparable3, comparable4, function4.apply(comparable, comparable2, comparable3, comparable4))))));
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class C5<V, T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>>
    extends PropertyDispatch<V> {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;
        private final Property<T4> property4;
        private final Property<T5> property5;

        C5(Property<T1> property, Property<T2> property2, Property<T3> property3, Property<T4> property4, Property<T5> property5) {
            this.property1 = property;
            this.property2 = property2;
            this.property3 = property3;
            this.property4 = property4;
            this.property5 = property5;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return List.of(this.property1, this.property2, this.property3, this.property4, this.property5);
        }

        public C5<V, T1, T2, T3, T4, T5> select(T1 comparable, T2 comparable2, T3 comparable3, T4 comparable4, T5 comparable5, V object) {
            PropertyValueList propertyValueList = PropertyValueList.of(this.property1.value(comparable), this.property2.value(comparable2), this.property3.value(comparable3), this.property4.value(comparable4), this.property5.value(comparable5));
            this.putValue(propertyValueList, object);
            return this;
        }

        public PropertyDispatch<V> generate(Function5<T1, T2, T3, T4, T5, V> function5) {
            this.property1.getPossibleValues().forEach(comparable -> this.property2.getPossibleValues().forEach(comparable2 -> this.property3.getPossibleValues().forEach(comparable3 -> this.property4.getPossibleValues().forEach(comparable4 -> this.property5.getPossibleValues().forEach(comparable5 -> this.select(comparable, comparable2, comparable3, comparable4, comparable5, function5.apply(comparable, comparable2, comparable3, comparable4, comparable5)))))));
            return this;
        }
    }
}


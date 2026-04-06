/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.attributes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jspecify.annotations.Nullable;

public class AttributeInstance {
    private final Holder<Attribute> attribute;
    private final Map<AttributeModifier.Operation, Map<Identifier, AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
    private final Map<Identifier, AttributeModifier> modifierById = new Object2ObjectArrayMap();
    private final Map<Identifier, AttributeModifier> permanentModifiers = new Object2ObjectArrayMap();
    private double baseValue;
    private boolean dirty = true;
    private double cachedValue;
    private final Consumer<AttributeInstance> onDirty;

    public AttributeInstance(Holder<Attribute> holder, Consumer<AttributeInstance> consumer) {
        this.attribute = holder;
        this.onDirty = consumer;
        this.baseValue = holder.value().getDefaultValue();
    }

    public Holder<Attribute> getAttribute() {
        return this.attribute;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double d) {
        if (d == this.baseValue) {
            return;
        }
        this.baseValue = d;
        this.setDirty();
    }

    @VisibleForTesting
    Map<Identifier, AttributeModifier> getModifiers(AttributeModifier.Operation operation2) {
        return this.modifiersByOperation.computeIfAbsent(operation2, operation -> new Object2ObjectOpenHashMap());
    }

    public Set<AttributeModifier> getModifiers() {
        return ImmutableSet.copyOf(this.modifierById.values());
    }

    public Set<AttributeModifier> getPermanentModifiers() {
        return ImmutableSet.copyOf(this.permanentModifiers.values());
    }

    public @Nullable AttributeModifier getModifier(Identifier identifier) {
        return this.modifierById.get(identifier);
    }

    public boolean hasModifier(Identifier identifier) {
        return this.modifierById.get(identifier) != null;
    }

    private void addModifier(AttributeModifier attributeModifier) {
        AttributeModifier attributeModifier2 = this.modifierById.putIfAbsent(attributeModifier.id(), attributeModifier);
        if (attributeModifier2 != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        }
        this.getModifiers(attributeModifier.operation()).put(attributeModifier.id(), attributeModifier);
        this.setDirty();
    }

    public void addOrUpdateTransientModifier(AttributeModifier attributeModifier) {
        AttributeModifier attributeModifier2 = this.modifierById.put(attributeModifier.id(), attributeModifier);
        if (attributeModifier == attributeModifier2) {
            return;
        }
        this.getModifiers(attributeModifier.operation()).put(attributeModifier.id(), attributeModifier);
        this.setDirty();
    }

    public void addTransientModifier(AttributeModifier attributeModifier) {
        this.addModifier(attributeModifier);
    }

    public void addOrReplacePermanentModifier(AttributeModifier attributeModifier) {
        this.removeModifier(attributeModifier.id());
        this.addModifier(attributeModifier);
        this.permanentModifiers.put(attributeModifier.id(), attributeModifier);
    }

    public void addPermanentModifier(AttributeModifier attributeModifier) {
        this.addModifier(attributeModifier);
        this.permanentModifiers.put(attributeModifier.id(), attributeModifier);
    }

    public void addPermanentModifiers(Collection<AttributeModifier> collection) {
        for (AttributeModifier attributeModifier : collection) {
            this.addPermanentModifier(attributeModifier);
        }
    }

    protected void setDirty() {
        this.dirty = true;
        this.onDirty.accept(this);
    }

    public void removeModifier(AttributeModifier attributeModifier) {
        this.removeModifier(attributeModifier.id());
    }

    public boolean removeModifier(Identifier identifier) {
        AttributeModifier attributeModifier = this.modifierById.remove(identifier);
        if (attributeModifier == null) {
            return false;
        }
        this.getModifiers(attributeModifier.operation()).remove(identifier);
        this.permanentModifiers.remove(identifier);
        this.setDirty();
        return true;
    }

    public void removeModifiers() {
        for (AttributeModifier attributeModifier : this.getModifiers()) {
            this.removeModifier(attributeModifier);
        }
    }

    public double getValue() {
        if (this.dirty) {
            this.cachedValue = this.calculateValue();
            this.dirty = false;
        }
        return this.cachedValue;
    }

    private double calculateValue() {
        double d = this.getBaseValue();
        for (AttributeModifier attributeModifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
            d += attributeModifier.amount();
        }
        double e = d;
        for (AttributeModifier attributeModifier2 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
            e += d * attributeModifier2.amount();
        }
        for (AttributeModifier attributeModifier2 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            e *= 1.0 + attributeModifier2.amount();
        }
        return this.attribute.value().sanitizeValue(e);
    }

    private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation operation) {
        return this.modifiersByOperation.getOrDefault(operation, Map.of()).values();
    }

    public void replaceFrom(AttributeInstance attributeInstance) {
        this.baseValue = attributeInstance.baseValue;
        this.modifierById.clear();
        this.modifierById.putAll(attributeInstance.modifierById);
        this.permanentModifiers.clear();
        this.permanentModifiers.putAll(attributeInstance.permanentModifiers);
        this.modifiersByOperation.clear();
        attributeInstance.modifiersByOperation.forEach((operation, map) -> this.getModifiers((AttributeModifier.Operation)operation).putAll((Map<Identifier, AttributeModifier>)map));
        this.setDirty();
    }

    public Packed pack() {
        return new Packed(this.attribute, this.baseValue, List.copyOf(this.permanentModifiers.values()));
    }

    public void apply(Packed packed) {
        this.baseValue = packed.baseValue;
        for (AttributeModifier attributeModifier : packed.modifiers) {
            this.modifierById.put(attributeModifier.id(), attributeModifier);
            this.getModifiers(attributeModifier.operation()).put(attributeModifier.id(), attributeModifier);
            this.permanentModifiers.put(attributeModifier.id(), attributeModifier);
        }
        this.setDirty();
    }

    public static final class Packed
    extends Record {
        private final Holder<Attribute> attribute;
        final double baseValue;
        final List<AttributeModifier> modifiers;
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("id").forGetter(Packed::attribute), (App)Codec.DOUBLE.fieldOf("base").orElse((Object)0.0).forGetter(Packed::baseValue), (App)AttributeModifier.CODEC.listOf().optionalFieldOf("modifiers", (Object)List.of()).forGetter(Packed::modifiers)).apply((Applicative)instance, Packed::new));
        public static final Codec<List<Packed>> LIST_CODEC = CODEC.listOf();

        public Packed(Holder<Attribute> holder, double d, List<AttributeModifier> list) {
            this.attribute = holder;
            this.baseValue = d;
            this.modifiers = list;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "attribute;baseValue;modifiers", "attribute", "baseValue", "modifiers"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "attribute;baseValue;modifiers", "attribute", "baseValue", "modifiers"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "attribute;baseValue;modifiers", "attribute", "baseValue", "modifiers"}, this, object);
        }

        public Holder<Attribute> attribute() {
            return this.attribute;
        }

        public double baseValue() {
            return this.baseValue;
        }

        public List<AttributeModifier> modifiers() {
            return this.modifiers;
        }
    }
}


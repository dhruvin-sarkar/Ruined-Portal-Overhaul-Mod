/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  java.lang.MatchException
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.apache.commons.lang3.function.TriConsumer
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.function.TriConsumer;
import org.jspecify.annotations.Nullable;

public record ItemAttributeModifiers(List<Entry> modifiers) {
    public static final ItemAttributeModifiers EMPTY = new ItemAttributeModifiers(List.of());
    public static final Codec<ItemAttributeModifiers> CODEC = Entry.CODEC.listOf().xmap(ItemAttributeModifiers::new, ItemAttributeModifiers::modifiers);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers> STREAM_CODEC = StreamCodec.composite(Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), ItemAttributeModifiers::modifiers, ItemAttributeModifiers::new);
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public static Builder builder() {
        return new Builder();
    }

    public ItemAttributeModifiers withModifierAdded(Holder<Attribute> holder, AttributeModifier attributeModifier, EquipmentSlotGroup equipmentSlotGroup) {
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)(this.modifiers.size() + 1));
        for (Entry entry : this.modifiers) {
            if (entry.matches(holder, attributeModifier.id())) continue;
            builder.add((Object)entry);
        }
        builder.add((Object)new Entry(holder, attributeModifier, equipmentSlotGroup));
        return new ItemAttributeModifiers((List<Entry>)builder.build());
    }

    public void forEach(EquipmentSlotGroup equipmentSlotGroup, TriConsumer<Holder<Attribute>, AttributeModifier, Display> triConsumer) {
        for (Entry entry : this.modifiers) {
            if (!entry.slot.equals(equipmentSlotGroup)) continue;
            triConsumer.accept(entry.attribute, (Object)entry.modifier, (Object)entry.display);
        }
    }

    public void forEach(EquipmentSlotGroup equipmentSlotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
        for (Entry entry : this.modifiers) {
            if (!entry.slot.equals(equipmentSlotGroup)) continue;
            biConsumer.accept(entry.attribute, entry.modifier);
        }
    }

    public void forEach(EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
        for (Entry entry : this.modifiers) {
            if (!entry.slot.test(equipmentSlot)) continue;
            biConsumer.accept(entry.attribute, entry.modifier);
        }
    }

    public double compute(Holder<Attribute> holder, double d, EquipmentSlot equipmentSlot) {
        double e = d;
        for (Entry entry : this.modifiers) {
            if (!entry.slot.test(equipmentSlot) || entry.attribute != holder) continue;
            double f = entry.modifier.amount();
            e += (switch (entry.modifier.operation()) {
                default -> throw new MatchException(null, null);
                case AttributeModifier.Operation.ADD_VALUE -> f;
                case AttributeModifier.Operation.ADD_MULTIPLIED_BASE -> f * d;
                case AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL -> f * e;
            });
        }
        return e;
    }

    public static class Builder {
        private final ImmutableList.Builder<Entry> entries = ImmutableList.builder();

        Builder() {
        }

        public Builder add(Holder<Attribute> holder, AttributeModifier attributeModifier, EquipmentSlotGroup equipmentSlotGroup) {
            this.entries.add((Object)new Entry(holder, attributeModifier, equipmentSlotGroup));
            return this;
        }

        public Builder add(Holder<Attribute> holder, AttributeModifier attributeModifier, EquipmentSlotGroup equipmentSlotGroup, Display display) {
            this.entries.add((Object)new Entry(holder, attributeModifier, equipmentSlotGroup, display));
            return this;
        }

        public ItemAttributeModifiers build() {
            return new ItemAttributeModifiers((List<Entry>)this.entries.build());
        }
    }

    public static final class Entry
    extends Record {
        final Holder<Attribute> attribute;
        final AttributeModifier modifier;
        final EquipmentSlotGroup slot;
        final Display display;
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Attribute.CODEC.fieldOf("type").forGetter(Entry::attribute), (App)AttributeModifier.MAP_CODEC.forGetter(Entry::modifier), (App)EquipmentSlotGroup.CODEC.optionalFieldOf("slot", (Object)EquipmentSlotGroup.ANY).forGetter(Entry::slot), (App)Display.CODEC.optionalFieldOf("display", (Object)Display.Default.INSTANCE).forGetter(Entry::display)).apply((Applicative)instance, Entry::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(Attribute.STREAM_CODEC, Entry::attribute, AttributeModifier.STREAM_CODEC, Entry::modifier, EquipmentSlotGroup.STREAM_CODEC, Entry::slot, Display.STREAM_CODEC, Entry::display, Entry::new);

        public Entry(Holder<Attribute> holder, AttributeModifier attributeModifier, EquipmentSlotGroup equipmentSlotGroup) {
            this(holder, attributeModifier, equipmentSlotGroup, Display.attributeModifiers());
        }

        public Entry(Holder<Attribute> holder, AttributeModifier attributeModifier, EquipmentSlotGroup equipmentSlotGroup, Display display) {
            this.attribute = holder;
            this.modifier = attributeModifier;
            this.slot = equipmentSlotGroup;
            this.display = display;
        }

        public boolean matches(Holder<Attribute> holder, Identifier identifier) {
            return holder.equals(this.attribute) && this.modifier.is(identifier);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "attribute;modifier;slot;display", "attribute", "modifier", "slot", "display"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "attribute;modifier;slot;display", "attribute", "modifier", "slot", "display"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "attribute;modifier;slot;display", "attribute", "modifier", "slot", "display"}, this, object);
        }

        public Holder<Attribute> attribute() {
            return this.attribute;
        }

        public AttributeModifier modifier() {
            return this.modifier;
        }

        public EquipmentSlotGroup slot() {
            return this.slot;
        }

        public Display display() {
            return this.display;
        }
    }

    public static interface Display {
        public static final Codec<Display> CODEC = Type.CODEC.dispatch("type", Display::type, type -> type.codec);
        public static final StreamCodec<RegistryFriendlyByteBuf, Display> STREAM_CODEC = Type.STREAM_CODEC.cast().dispatch(Display::type, Type::streamCodec);

        public static Display attributeModifiers() {
            return Default.INSTANCE;
        }

        public static Display hidden() {
            return Hidden.INSTANCE;
        }

        public static Display override(Component component) {
            return new OverrideText(component);
        }

        public Type type();

        public void apply(Consumer<Component> var1, @Nullable Player var2, Holder<Attribute> var3, AttributeModifier var4);

        public record Default() implements Display
        {
            static final Default INSTANCE = new Default();
            static final MapCodec<Default> CODEC = MapCodec.unit((Object)INSTANCE);
            static final StreamCodec<RegistryFriendlyByteBuf, Default> STREAM_CODEC = StreamCodec.unit(INSTANCE);

            @Override
            public Type type() {
                return Type.DEFAULT;
            }

            @Override
            public void apply(Consumer<Component> consumer, @Nullable Player player, Holder<Attribute> holder, AttributeModifier attributeModifier) {
                double d = attributeModifier.amount();
                boolean bl = false;
                if (player != null) {
                    if (attributeModifier.is(Item.BASE_ATTACK_DAMAGE_ID)) {
                        d += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                        bl = true;
                    } else if (attributeModifier.is(Item.BASE_ATTACK_SPEED_ID)) {
                        d += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                        bl = true;
                    }
                }
                double e = attributeModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || attributeModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ? d * 100.0 : (holder.is(Attributes.KNOCKBACK_RESISTANCE) ? d * 10.0 : d);
                if (bl) {
                    consumer.accept(CommonComponents.space().append(Component.translatable("attribute.modifier.equals." + attributeModifier.operation().id(), ATTRIBUTE_MODIFIER_FORMAT.format(e), Component.translatable(holder.value().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
                } else if (d > 0.0) {
                    consumer.accept(Component.translatable("attribute.modifier.plus." + attributeModifier.operation().id(), ATTRIBUTE_MODIFIER_FORMAT.format(e), Component.translatable(holder.value().getDescriptionId())).withStyle(holder.value().getStyle(true)));
                } else if (d < 0.0) {
                    consumer.accept(Component.translatable("attribute.modifier.take." + attributeModifier.operation().id(), ATTRIBUTE_MODIFIER_FORMAT.format(-e), Component.translatable(holder.value().getDescriptionId())).withStyle(holder.value().getStyle(false)));
                }
            }
        }

        public record Hidden() implements Display
        {
            static final Hidden INSTANCE = new Hidden();
            static final MapCodec<Hidden> CODEC = MapCodec.unit((Object)INSTANCE);
            static final StreamCodec<RegistryFriendlyByteBuf, Hidden> STREAM_CODEC = StreamCodec.unit(INSTANCE);

            @Override
            public Type type() {
                return Type.HIDDEN;
            }

            @Override
            public void apply(Consumer<Component> consumer, @Nullable Player player, Holder<Attribute> holder, AttributeModifier attributeModifier) {
            }
        }

        public record OverrideText(Component component) implements Display
        {
            static final MapCodec<OverrideText> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ComponentSerialization.CODEC.fieldOf("value").forGetter(OverrideText::component)).apply((Applicative)instance, OverrideText::new));
            static final StreamCodec<RegistryFriendlyByteBuf, OverrideText> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.STREAM_CODEC, OverrideText::component, OverrideText::new);

            @Override
            public Type type() {
                return Type.OVERRIDE;
            }

            @Override
            public void apply(Consumer<Component> consumer, @Nullable Player player, Holder<Attribute> holder, AttributeModifier attributeModifier) {
                consumer.accept(this.component);
            }
        }

        public static enum Type implements StringRepresentable
        {
            DEFAULT("default", 0, Default.CODEC, Default.STREAM_CODEC),
            HIDDEN("hidden", 1, Hidden.CODEC, Hidden.STREAM_CODEC),
            OVERRIDE("override", 2, OverrideText.CODEC, OverrideText.STREAM_CODEC);

            static final Codec<Type> CODEC;
            private static final IntFunction<Type> BY_ID;
            static final StreamCodec<ByteBuf, Type> STREAM_CODEC;
            private final String name;
            private final int id;
            final MapCodec<? extends Display> codec;
            private final StreamCodec<RegistryFriendlyByteBuf, ? extends Display> streamCodec;

            private Type(String string2, int j, MapCodec<? extends Display> mapCodec, StreamCodec<RegistryFriendlyByteBuf, ? extends Display> streamCodec) {
                this.name = string2;
                this.id = j;
                this.codec = mapCodec;
                this.streamCodec = streamCodec;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }

            private int id() {
                return this.id;
            }

            private StreamCodec<RegistryFriendlyByteBuf, ? extends Display> streamCodec() {
                return this.streamCodec;
            }

            static {
                CODEC = StringRepresentable.fromEnum(Type::values);
                BY_ID = ByIdMap.continuous(Type::id, Type.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
                STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Type::id);
            }
        }
    }
}


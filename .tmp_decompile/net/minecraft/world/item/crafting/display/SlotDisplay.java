/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.crafting.display;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.block.entity.FuelValues;

public interface SlotDisplay {
    public static final Codec<SlotDisplay> CODEC = BuiltInRegistries.SLOT_DISPLAY.byNameCodec().dispatch(SlotDisplay::type, Type::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> STREAM_CODEC = ByteBufCodecs.registry(Registries.SLOT_DISPLAY).dispatch(SlotDisplay::type, Type::streamCodec);

    public <T> Stream<T> resolve(ContextMap var1, DisplayContentsFactory<T> var2);

    public Type<? extends SlotDisplay> type();

    default public boolean isEnabled(FeatureFlagSet featureFlagSet) {
        return true;
    }

    default public List<ItemStack> resolveForStacks(ContextMap contextMap) {
        return this.resolve(contextMap, ItemStackContentsFactory.INSTANCE).toList();
    }

    default public ItemStack resolveForFirstStack(ContextMap contextMap) {
        return this.resolve(contextMap, ItemStackContentsFactory.INSTANCE).findFirst().orElse(ItemStack.EMPTY);
    }

    public static class ItemStackContentsFactory
    implements DisplayContentsFactory.ForStacks<ItemStack> {
        public static final ItemStackContentsFactory INSTANCE = new ItemStackContentsFactory();

        @Override
        public ItemStack forStack(ItemStack itemStack) {
            return itemStack;
        }

        @Override
        public /* synthetic */ Object forStack(ItemStack itemStack) {
            return this.forStack(itemStack);
        }
    }

    public record WithRemainder(SlotDisplay input, SlotDisplay remainder) implements SlotDisplay
    {
        public static final MapCodec<WithRemainder> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CODEC.fieldOf("input").forGetter(WithRemainder::input), (App)CODEC.fieldOf("remainder").forGetter(WithRemainder::remainder)).apply((Applicative)instance, WithRemainder::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, WithRemainder> STREAM_CODEC = StreamCodec.composite(STREAM_CODEC, WithRemainder::input, STREAM_CODEC, WithRemainder::remainder, WithRemainder::new);
        public static final Type<WithRemainder> TYPE = new Type<WithRemainder>(MAP_CODEC, STREAM_CODEC);

        public Type<WithRemainder> type() {
            return TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextMap, DisplayContentsFactory<T> displayContentsFactory) {
            if (displayContentsFactory instanceof DisplayContentsFactory.ForRemainders) {
                DisplayContentsFactory.ForRemainders forRemainders = (DisplayContentsFactory.ForRemainders)displayContentsFactory;
                List list = this.remainder.resolve(contextMap, displayContentsFactory).toList();
                return this.input.resolve(contextMap, displayContentsFactory).map(object -> forRemainders.addRemainder(object, list));
            }
            return this.input.resolve(contextMap, displayContentsFactory);
        }

        @Override
        public boolean isEnabled(FeatureFlagSet featureFlagSet) {
            return this.input.isEnabled(featureFlagSet) && this.remainder.isEnabled(featureFlagSet);
        }
    }

    public record Composite(List<SlotDisplay> contents) implements SlotDisplay
    {
        public static final MapCodec<Composite> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CODEC.listOf().fieldOf("contents").forGetter(Composite::contents)).apply((Applicative)instance, Composite::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Composite> STREAM_CODEC = StreamCodec.composite(STREAM_CODEC.apply(ByteBufCodecs.list()), Composite::contents, Composite::new);
        public static final Type<Composite> TYPE = new Type<Composite>(MAP_CODEC, STREAM_CODEC);

        public Type<Composite> type() {
            return TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextMap, DisplayContentsFactory<T> displayContentsFactory) {
            return this.contents.stream().flatMap(slotDisplay -> slotDisplay.resolve(contextMap, displayContentsFactory));
        }

        @Override
        public boolean isEnabled(FeatureFlagSet featureFlagSet) {
            return this.contents.stream().allMatch(slotDisplay -> slotDisplay.isEnabled(featureFlagSet));
        }
    }

    public record TagSlotDisplay(TagKey<Item> tag) implements SlotDisplay
    {
        public static final MapCodec<TagSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagSlotDisplay::tag)).apply((Applicative)instance, TagSlotDisplay::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, TagSlotDisplay> STREAM_CODEC = StreamCodec.composite(TagKey.streamCodec(Registries.ITEM), TagSlotDisplay::tag, TagSlotDisplay::new);
        public static final Type<TagSlotDisplay> TYPE = new Type<TagSlotDisplay>(MAP_CODEC, STREAM_CODEC);

        public Type<TagSlotDisplay> type() {
            return TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextMap, DisplayContentsFactory<T> displayContentsFactory) {
            if (displayContentsFactory instanceof DisplayContentsFactory.ForStacks) {
                DisplayContentsFactory.ForStacks forStacks = (DisplayContentsFactory.ForStacks)displayContentsFactory;
                HolderLookup.Provider provider = contextMap.getOptional(SlotDisplayContext.REGISTRIES);
                if (provider != null) {
                    return provider.lookupOrThrow(Registries.ITEM).get(this.tag).map(named -> named.stream().map(forStacks::forStack)).stream().flatMap(stream -> stream);
                }
            }
            return Stream.empty();
        }
    }

    public record ItemStackSlotDisplay(ItemStack stack) implements SlotDisplay
    {
        public static final MapCodec<ItemStackSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ItemStack.STRICT_CODEC.fieldOf("item").forGetter(ItemStackSlotDisplay::stack)).apply((Applicative)instance, ItemStackSlotDisplay::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackSlotDisplay> STREAM_CODEC = StreamCodec.composite(ItemStack.STREAM_CODEC, ItemStackSlotDisplay::stack, ItemStackSlotDisplay::new);
        public static final Type<ItemStackSlotDisplay> TYPE = new Type<ItemStackSlotDisplay>(MAP_CODEC, STREAM_CODEC);

        public Type<ItemStackSlotDisplay> type() {
            return TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextMap, DisplayContentsFactory<T> displayContentsFactory) {
            if (displayContentsFactory instanceof DisplayContentsFactory.ForStacks) {
                DisplayContentsFactory.ForStacks forStacks = (DisplayContentsFactory.ForStacks)displayContentsFactory;
                return Stream.of(forStacks.forStack(this.stack));
            }
            return Stream.empty();
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof ItemStackSlotDisplay)) return false;
            ItemStackSlotDisplay itemStackSlotDisplay = (ItemStackSlotDisplay)object;
            if (!ItemStack.matches(this.stack, itemStackSlotDisplay.stack)) return false;
            return true;
        }

        @Override
        public boolean isEnabled(FeatureFlagSet featureFlagSet) {
            return this.stack.getItem().isEnabled(featureFlagSet);
        }
    }

    public record ItemSlotDisplay(Holder<Item> item) implements SlotDisplay
    {
        public static final MapCodec<ItemSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Item.CODEC.fieldOf("item").forGetter(ItemSlotDisplay::item)).apply((Applicative)instance, ItemSlotDisplay::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemSlotDisplay> STREAM_CODEC = StreamCodec.composite(Item.STREAM_CODEC, ItemSlotDisplay::item, ItemSlotDisplay::new);
        public static final Type<ItemSlotDisplay> TYPE = new Type<ItemSlotDisplay>(MAP_CODEC, STREAM_CODEC);

        public ItemSlotDisplay(Item item) {
            this(item.builtInRegistryHolder());
        }

        public Type<ItemSlotDisplay> type() {
            return TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextMap, DisplayContentsFactory<T> displayContentsFactory) {
            if (displayContentsFactory instanceof DisplayContentsFactory.ForStacks) {
                DisplayContentsFactory.ForStacks forStacks = (DisplayContentsFactory.ForStacks)displayContentsFactory;
                return Stream.of(forStacks.forStack(this.item));
            }
            return Stream.empty();
        }

        @Override
        public boolean isEnabled(FeatureFlagSet featureFlagSet) {
            return this.item.value().isEnabled(featureFlagSet);
        }
    }

    public record SmithingTrimDemoSlotDisplay(SlotDisplay base, SlotDisplay material, Holder<TrimPattern> pattern) implements SlotDisplay
    {
        public static final MapCodec<SmithingTrimDemoSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CODEC.fieldOf("base").forGetter(SmithingTrimDemoSlotDisplay::base), (App)CODEC.fieldOf("material").forGetter(SmithingTrimDemoSlotDisplay::material), (App)TrimPattern.CODEC.fieldOf("pattern").forGetter(SmithingTrimDemoSlotDisplay::pattern)).apply((Applicative)instance, SmithingTrimDemoSlotDisplay::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTrimDemoSlotDisplay> STREAM_CODEC = StreamCodec.composite(STREAM_CODEC, SmithingTrimDemoSlotDisplay::base, STREAM_CODEC, SmithingTrimDemoSlotDisplay::material, TrimPattern.STREAM_CODEC, SmithingTrimDemoSlotDisplay::pattern, SmithingTrimDemoSlotDisplay::new);
        public static final Type<SmithingTrimDemoSlotDisplay> TYPE = new Type<SmithingTrimDemoSlotDisplay>(MAP_CODEC, STREAM_CODEC);

        public Type<SmithingTrimDemoSlotDisplay> type() {
            return TYPE;
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextMap, DisplayContentsFactory<T> displayContentsFactory) {
            if (displayContentsFactory instanceof DisplayContentsFactory.ForStacks) {
                DisplayContentsFactory.ForStacks forStacks = (DisplayContentsFactory.ForStacks)displayContentsFactory;
                HolderLookup.Provider provider = contextMap.getOptional(SlotDisplayContext.REGISTRIES);
                if (provider != null) {
                    RandomSource randomSource = RandomSource.create(System.identityHashCode(this));
                    List<ItemStack> list = this.base.resolveForStacks(contextMap);
                    if (list.isEmpty()) {
                        return Stream.empty();
                    }
                    List<ItemStack> list2 = this.material.resolveForStacks(contextMap);
                    if (list2.isEmpty()) {
                        return Stream.empty();
                    }
                    return Stream.generate(() -> {
                        ItemStack itemStack = (ItemStack)Util.getRandom(list, randomSource);
                        ItemStack itemStack2 = (ItemStack)Util.getRandom(list2, randomSource);
                        return SmithingTrimRecipe.applyTrim(provider, itemStack, itemStack2, this.pattern);
                    }).limit(256L).filter(itemStack -> !itemStack.isEmpty()).limit(16L).map(forStacks::forStack);
                }
            }
            return Stream.empty();
        }
    }

    public static class AnyFuel
    implements SlotDisplay {
        public static final AnyFuel INSTANCE = new AnyFuel();
        public static final MapCodec<AnyFuel> MAP_CODEC = MapCodec.unit((Object)INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, AnyFuel> STREAM_CODEC = StreamCodec.unit(INSTANCE);
        public static final Type<AnyFuel> TYPE = new Type<AnyFuel>(MAP_CODEC, STREAM_CODEC);

        private AnyFuel() {
        }

        public Type<AnyFuel> type() {
            return TYPE;
        }

        public String toString() {
            return "<any fuel>";
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextMap, DisplayContentsFactory<T> displayContentsFactory) {
            if (displayContentsFactory instanceof DisplayContentsFactory.ForStacks) {
                DisplayContentsFactory.ForStacks forStacks = (DisplayContentsFactory.ForStacks)displayContentsFactory;
                FuelValues fuelValues = contextMap.getOptional(SlotDisplayContext.FUEL_VALUES);
                if (fuelValues != null) {
                    return fuelValues.fuelItems().stream().map(forStacks::forStack);
                }
            }
            return Stream.empty();
        }
    }

    public static class Empty
    implements SlotDisplay {
        public static final Empty INSTANCE = new Empty();
        public static final MapCodec<Empty> MAP_CODEC = MapCodec.unit((Object)INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, Empty> STREAM_CODEC = StreamCodec.unit(INSTANCE);
        public static final Type<Empty> TYPE = new Type<Empty>(MAP_CODEC, STREAM_CODEC);

        private Empty() {
        }

        public Type<Empty> type() {
            return TYPE;
        }

        public String toString() {
            return "<empty>";
        }

        @Override
        public <T> Stream<T> resolve(ContextMap contextMap, DisplayContentsFactory<T> displayContentsFactory) {
            return Stream.empty();
        }
    }

    public record Type<T extends SlotDisplay>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ResettableOptionWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public final class OptionInstance<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Enum<Boolean> BOOLEAN_VALUES = new Enum(ImmutableList.of((Object)Boolean.TRUE, (Object)Boolean.FALSE), Codec.BOOL);
    public static final CaptionBasedToString<Boolean> BOOLEAN_TO_STRING = (component, boolean_) -> boolean_ != false ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
    private final TooltipSupplier<T> tooltip;
    final Function<T, Component> toString;
    private final ValueSet<T> values;
    private final Codec<T> codec;
    private final T initialValue;
    private final Consumer<T> onValueUpdate;
    final Component caption;
    private T value;

    public static OptionInstance<Boolean> createBoolean(String string, boolean bl, Consumer<Boolean> consumer) {
        return OptionInstance.createBoolean(string, OptionInstance.noTooltip(), bl, consumer);
    }

    public static OptionInstance<Boolean> createBoolean(String string, boolean bl) {
        return OptionInstance.createBoolean(string, OptionInstance.noTooltip(), bl, boolean_ -> {});
    }

    public static OptionInstance<Boolean> createBoolean(String string, TooltipSupplier<Boolean> tooltipSupplier, boolean bl) {
        return OptionInstance.createBoolean(string, tooltipSupplier, bl, boolean_ -> {});
    }

    public static OptionInstance<Boolean> createBoolean(String string, TooltipSupplier<Boolean> tooltipSupplier, boolean bl, Consumer<Boolean> consumer) {
        return OptionInstance.createBoolean(string, tooltipSupplier, BOOLEAN_TO_STRING, bl, consumer);
    }

    public static OptionInstance<Boolean> createBoolean(String string, TooltipSupplier<Boolean> tooltipSupplier, CaptionBasedToString<Boolean> captionBasedToString, boolean bl, Consumer<Boolean> consumer) {
        return new OptionInstance<Boolean>(string, tooltipSupplier, captionBasedToString, BOOLEAN_VALUES, bl, consumer);
    }

    public OptionInstance(String string, TooltipSupplier<T> tooltipSupplier, CaptionBasedToString<T> captionBasedToString, ValueSet<T> valueSet, T object, Consumer<T> consumer) {
        this(string, tooltipSupplier, captionBasedToString, valueSet, valueSet.codec(), object, consumer);
    }

    public OptionInstance(String string, TooltipSupplier<T> tooltipSupplier, CaptionBasedToString<T> captionBasedToString, ValueSet<T> valueSet, Codec<T> codec, T object2, Consumer<T> consumer) {
        this.caption = Component.translatable(string);
        this.tooltip = tooltipSupplier;
        this.toString = object -> captionBasedToString.toString(this.caption, object);
        this.values = valueSet;
        this.codec = codec;
        this.initialValue = object2;
        this.onValueUpdate = consumer;
        this.value = this.initialValue;
    }

    public static <T> TooltipSupplier<T> noTooltip() {
        return object -> null;
    }

    public static <T> TooltipSupplier<T> cachedConstantTooltip(Component component) {
        return object -> Tooltip.create(component);
    }

    public AbstractWidget createButton(Options options) {
        return this.createButton(options, 0, 0, 150);
    }

    public AbstractWidget createButton(Options options, int i, int j, int k) {
        return this.createButton(options, i, j, k, object -> {});
    }

    public AbstractWidget createButton(Options options, int i, int j, int k, Consumer<T> consumer) {
        return this.values.createButton(this.tooltip, options, i, j, k, consumer).apply(this);
    }

    public T get() {
        return this.value;
    }

    public Codec<T> codec() {
        return this.codec;
    }

    public String toString() {
        return this.caption.getString();
    }

    public void set(T object) {
        Object object2 = this.values.validateValue(object).orElseGet(() -> {
            LOGGER.error("Illegal option value {} for {}", object, (Object)this.caption.getString());
            return this.initialValue;
        });
        if (!Minecraft.getInstance().isRunning()) {
            this.value = object2;
            return;
        }
        if (!Objects.equals(this.value, object2)) {
            this.value = object2;
            this.onValueUpdate.accept(this.value);
        }
    }

    public ValueSet<T> values() {
        return this.values;
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface TooltipSupplier<T> {
        public @Nullable Tooltip apply(T var1);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface CaptionBasedToString<T> {
        public Component toString(Component var1, T var2);
    }

    @Environment(value=EnvType.CLIENT)
    public record Enum<T>(List<T> values, Codec<T> codec) implements CycleableValueSet<T>
    {
        @Override
        public Optional<T> validateValue(T object) {
            return this.values.contains(object) ? Optional.of(object) : Optional.empty();
        }

        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(this.values);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface ValueSet<T> {
        public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> var1, Options var2, int var3, int var4, int var5, Consumer<T> var6);

        public Optional<T> validateValue(T var1);

        public Codec<T> codec();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum UnitDouble implements SliderableValueSet<Double>
    {
        INSTANCE;


        @Override
        public Optional<Double> validateValue(Double double_) {
            return double_ >= 0.0 && double_ <= 1.0 ? Optional.of(double_) : Optional.empty();
        }

        @Override
        public double toSliderValue(Double double_) {
            return double_;
        }

        @Override
        public Double fromSliderValue(double d) {
            return d;
        }

        public <R> SliderableValueSet<R> xmap(final DoubleFunction<? extends R> doubleFunction, final ToDoubleFunction<? super R> toDoubleFunction) {
            return new SliderableValueSet<R>(){

                @Override
                public Optional<R> validateValue(R object) {
                    return this.validateValue(toDoubleFunction.applyAsDouble(object)).map(doubleFunction::apply);
                }

                @Override
                public double toSliderValue(R object) {
                    return this.toSliderValue(toDoubleFunction.applyAsDouble(object));
                }

                @Override
                public R fromSliderValue(double d) {
                    return doubleFunction.apply(this.fromSliderValue(d));
                }

                @Override
                public Codec<R> codec() {
                    return this.codec().xmap(doubleFunction::apply, toDoubleFunction::applyAsDouble);
                }
            };
        }

        @Override
        public Codec<Double> codec() {
            return Codec.withAlternative((Codec)Codec.doubleRange((double)0.0, (double)1.0), (Codec)Codec.BOOL, boolean_ -> boolean_ != false ? 1.0 : 0.0);
        }

        @Override
        public /* synthetic */ Object fromSliderValue(double d) {
            return this.fromSliderValue(d);
        }

        @Override
        public /* synthetic */ double toSliderValue(Object object) {
            return this.toSliderValue((Double)object);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record SliderableEnum<T>(List<T> values, Codec<T> codec) implements SliderableValueSet<T>
    {
        @Override
        public double toSliderValue(T object) {
            if (object == this.values.getFirst()) {
                return 0.0;
            }
            if (object == this.values.getLast()) {
                return 1.0;
            }
            return Mth.map((double)this.values.indexOf(object), 0.0, (double)(this.values.size() - 1), 0.0, 1.0);
        }

        @Override
        public Optional<T> next(T object) {
            int i = this.values.indexOf(object);
            int j = Mth.clamp(i + 1, 0, this.values.size() - 1);
            return Optional.of(this.values.get(j));
        }

        @Override
        public Optional<T> previous(T object) {
            int i = this.values.indexOf(object);
            int j = Mth.clamp(i - 1, 0, this.values.size() - 1);
            return Optional.of(this.values.get(j));
        }

        @Override
        public T fromSliderValue(double d) {
            if (d >= 1.0) {
                d = 0.99999f;
            }
            int i = Mth.floor(Mth.map(d, 0.0, 1.0, 0.0, (double)this.values.size()));
            return this.values.get(Mth.clamp(i, 0, this.values.size() - 1));
        }

        @Override
        public Optional<T> validateValue(T object) {
            int i = this.values.indexOf(object);
            return i > -1 ? Optional.of(object) : Optional.empty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record ClampingLazyMaxIntRange(int minInclusive, IntSupplier maxSupplier, int encodableMaxInclusive) implements IntRangeBase,
    SliderableOrCyclableValueSet<Integer>
    {
        @Override
        public Optional<Integer> validateValue(Integer integer) {
            return Optional.of(Mth.clamp(integer, this.minInclusive(), this.maxInclusive()));
        }

        @Override
        public int maxInclusive() {
            return this.maxSupplier.getAsInt();
        }

        @Override
        public Codec<Integer> codec() {
            return Codec.INT.validate(integer -> {
                int i = this.encodableMaxInclusive + 1;
                if (integer.compareTo(this.minInclusive) >= 0 && integer.compareTo(i) <= 0) {
                    return DataResult.success((Object)integer);
                }
                return DataResult.error(() -> "Value " + integer + " outside of range [" + this.minInclusive + ":" + i + "]", (Object)integer);
            });
        }

        @Override
        public boolean createCycleButton() {
            return true;
        }

        @Override
        public CycleButton.ValueListSupplier<Integer> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(IntStream.range(this.minInclusive, this.maxInclusive() + 1).boxed().toList());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record IntRange(int minInclusive, int maxInclusive, boolean applyValueImmediately) implements IntRangeBase
    {
        public IntRange(int i, int j) {
            this(i, j, true);
        }

        @Override
        public Optional<Integer> validateValue(Integer integer) {
            return integer.compareTo(this.minInclusive()) >= 0 && integer.compareTo(this.maxInclusive()) <= 0 ? Optional.of(integer) : Optional.empty();
        }

        @Override
        public Codec<Integer> codec() {
            return Codec.intRange((int)this.minInclusive, (int)(this.maxInclusive + 1));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface IntRangeBase
    extends SliderableValueSet<Integer> {
        public int minInclusive();

        public int maxInclusive();

        @Override
        default public Optional<Integer> next(Integer integer) {
            return Optional.of(integer + 1);
        }

        @Override
        default public Optional<Integer> previous(Integer integer) {
            return Optional.of(integer - 1);
        }

        @Override
        default public double toSliderValue(Integer integer) {
            if (integer.intValue() == this.minInclusive()) {
                return 0.0;
            }
            if (integer.intValue() == this.maxInclusive()) {
                return 1.0;
            }
            return Mth.map((double)integer.intValue() + 0.5, (double)this.minInclusive(), (double)this.maxInclusive() + 1.0, 0.0, 1.0);
        }

        @Override
        default public Integer fromSliderValue(double d) {
            if (d >= 1.0) {
                d = 0.99999f;
            }
            return Mth.floor(Mth.map(d, 0.0, 1.0, (double)this.minInclusive(), (double)this.maxInclusive() + 1.0));
        }

        default public <R> SliderableValueSet<R> xmap(final IntFunction<? extends R> intFunction, final ToIntFunction<? super R> toIntFunction, final boolean bl) {
            return new SliderableValueSet<R>(){

                @Override
                public Optional<R> validateValue(R object) {
                    return this.validateValue(toIntFunction.applyAsInt(object)).map(intFunction::apply);
                }

                @Override
                public double toSliderValue(R object) {
                    return this.toSliderValue(toIntFunction.applyAsInt(object));
                }

                @Override
                public Optional<R> next(R object) {
                    if (!bl) {
                        return Optional.empty();
                    }
                    int i = toIntFunction.applyAsInt(object);
                    return Optional.of(intFunction.apply(this.validateValue(i + 1).orElse(i)));
                }

                @Override
                public Optional<R> previous(R object) {
                    if (!bl) {
                        return Optional.empty();
                    }
                    int i = toIntFunction.applyAsInt(object);
                    return Optional.of(intFunction.apply(this.validateValue(i - 1).orElse(i)));
                }

                @Override
                public R fromSliderValue(double d) {
                    return intFunction.apply(this.fromSliderValue(d));
                }

                @Override
                public Codec<R> codec() {
                    return this.codec().xmap(intFunction::apply, toIntFunction::applyAsInt);
                }
            };
        }

        @Override
        default public /* synthetic */ Object fromSliderValue(double d) {
            return this.fromSliderValue(d);
        }

        @Override
        default public /* synthetic */ Optional previous(Object object) {
            return this.previous((Integer)object);
        }

        @Override
        default public /* synthetic */ Optional next(Object object) {
            return this.next((Integer)object);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class OptionInstanceSliderButton<N>
    extends AbstractOptionSliderButton
    implements ResettableOptionWidget {
        private final OptionInstance<N> instance;
        private final SliderableValueSet<N> values;
        private final TooltipSupplier<N> tooltipSupplier;
        private final Consumer<N> onValueChanged;
        private @Nullable Long delayedApplyAt;
        private final boolean applyValueImmediately;

        OptionInstanceSliderButton(Options options, int i, int j, int k, int l, OptionInstance<N> optionInstance, SliderableValueSet<N> sliderableValueSet, TooltipSupplier<N> tooltipSupplier, Consumer<N> consumer, boolean bl) {
            super(options, i, j, k, l, sliderableValueSet.toSliderValue(optionInstance.get()));
            this.instance = optionInstance;
            this.values = sliderableValueSet;
            this.tooltipSupplier = tooltipSupplier;
            this.onValueChanged = consumer;
            this.applyValueImmediately = bl;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(this.instance.toString.apply(this.values.fromSliderValue(this.value)));
            this.setTooltip(this.tooltipSupplier.apply(this.values.fromSliderValue(this.value)));
        }

        @Override
        protected void applyValue() {
            if (this.applyValueImmediately) {
                this.applyUnsavedValue();
            } else {
                this.delayedApplyAt = Util.getMillis() + 600L;
            }
        }

        public void applyUnsavedValue() {
            N object = this.values.fromSliderValue(this.value);
            if (!Objects.equals(object, this.instance.get())) {
                this.instance.set(object);
                this.onValueChanged.accept(this.instance.get());
            }
        }

        @Override
        public void resetValue() {
            if (this.value != this.values.toSliderValue(this.instance.get())) {
                this.value = this.values.toSliderValue(this.instance.get());
                this.delayedApplyAt = null;
                this.updateMessage();
            }
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            super.renderWidget(guiGraphics, i, j, f);
            if (this.delayedApplyAt != null && Util.getMillis() >= this.delayedApplyAt) {
                this.delayedApplyAt = null;
                this.applyUnsavedValue();
                this.resetValue();
            }
        }

        @Override
        public void onRelease(MouseButtonEvent mouseButtonEvent) {
            super.onRelease(mouseButtonEvent);
            if (this.applyValueImmediately) {
                this.resetValue();
            }
        }

        @Override
        public boolean keyPressed(KeyEvent keyEvent) {
            if (keyEvent.isSelection()) {
                this.canChangeValue = !this.canChangeValue;
                return true;
            }
            if (this.canChangeValue) {
                Optional<N> optional;
                boolean bl = keyEvent.isLeft();
                boolean bl2 = keyEvent.isRight();
                if (bl && (optional = this.values.previous(this.values.fromSliderValue(this.value))).isPresent()) {
                    this.setValue(this.values.toSliderValue(optional.get()));
                    return true;
                }
                if (bl2 && (optional = this.values.next(this.values.fromSliderValue(this.value))).isPresent()) {
                    this.setValue(this.values.toSliderValue(optional.get()));
                    return true;
                }
                if (bl || bl2) {
                    float f = bl ? -1.0f : 1.0f;
                    this.setValue(this.value + (double)(f / (float)(this.width - 8)));
                    return true;
                }
            }
            return false;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record LazyEnum<T>(Supplier<List<T>> values, Function<T, Optional<T>> validateValue, Codec<T> codec) implements CycleableValueSet<T>
    {
        @Override
        public Optional<T> validateValue(T object) {
            return this.validateValue.apply(object);
        }

        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create((Collection)this.values.get());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record AltEnum<T>(List<T> values, List<T> altValues, BooleanSupplier altCondition, CycleableValueSet.ValueSetter<T> valueSetter, Codec<T> codec) implements CycleableValueSet<T>
    {
        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(this.altCondition, this.values, this.altValues);
        }

        @Override
        public Optional<T> validateValue(T object) {
            return (this.altCondition.getAsBoolean() ? this.altValues : this.values).contains(object) ? Optional.of(object) : Optional.empty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface SliderableOrCyclableValueSet<T>
    extends CycleableValueSet<T>,
    SliderableValueSet<T> {
        public boolean createCycleButton();

        @Override
        default public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k, Consumer<T> consumer) {
            if (this.createCycleButton()) {
                return CycleableValueSet.super.createButton(tooltipSupplier, options, i, j, k, consumer);
            }
            return SliderableValueSet.super.createButton(tooltipSupplier, options, i, j, k, consumer);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface CycleableValueSet<T>
    extends ValueSet<T> {
        public CycleButton.ValueListSupplier<T> valueListSupplier();

        default public ValueSetter<T> valueSetter() {
            return OptionInstance::set;
        }

        @Override
        default public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k, Consumer<T> consumer) {
            return optionInstance -> CycleButton.builder(optionInstance.toString, optionInstance::get).withValues(this.valueListSupplier()).withTooltip(tooltipSupplier).create(i, j, k, 20, optionInstance.caption, (cycleButton, object) -> {
                this.valueSetter().set((OptionInstance<Object>)optionInstance, object);
                options.save();
                consumer.accept(object);
            });
        }

        @Environment(value=EnvType.CLIENT)
        public static interface ValueSetter<T> {
            public void set(OptionInstance<T> var1, T var2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface SliderableValueSet<T>
    extends ValueSet<T> {
        public double toSliderValue(T var1);

        default public Optional<T> next(T object) {
            return Optional.empty();
        }

        default public Optional<T> previous(T object) {
            return Optional.empty();
        }

        public T fromSliderValue(double var1);

        default public boolean applyValueImmediately() {
            return true;
        }

        @Override
        default public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k, Consumer<T> consumer) {
            return optionInstance -> new OptionInstanceSliderButton(options, i, j, k, 20, optionInstance, this, tooltipSupplier, consumer, this.applyValueImmediately());
        }
    }
}


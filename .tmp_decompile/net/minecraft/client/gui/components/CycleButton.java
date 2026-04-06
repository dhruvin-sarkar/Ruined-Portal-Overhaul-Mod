/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.ResettableOptionWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CycleButton<T>
extends AbstractButton
implements ResettableOptionWidget {
    public static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = () -> Minecraft.getInstance().hasAltDown();
    private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of((Object)Boolean.TRUE, (Object)Boolean.FALSE);
    private final Supplier<T> defaultValueSupplier;
    private final Component name;
    private int index;
    private T value;
    private final ValueListSupplier<T> values;
    private final Function<T, Component> valueStringifier;
    private final Function<CycleButton<T>, MutableComponent> narrationProvider;
    private final OnValueChange<T> onValueChange;
    private final DisplayState displayState;
    private final OptionInstance.TooltipSupplier<T> tooltipSupplier;
    private final SpriteSupplier<T> spriteSupplier;

    CycleButton(int i, int j, int k, int l, Component component, Component component2, int m, T object, Supplier<T> supplier, ValueListSupplier<T> valueListSupplier, Function<T, Component> function, Function<CycleButton<T>, MutableComponent> function2, OnValueChange<T> onValueChange, OptionInstance.TooltipSupplier<T> tooltipSupplier, DisplayState displayState, SpriteSupplier<T> spriteSupplier) {
        super(i, j, k, l, component);
        this.name = component2;
        this.index = m;
        this.defaultValueSupplier = supplier;
        this.value = object;
        this.values = valueListSupplier;
        this.valueStringifier = function;
        this.narrationProvider = function2;
        this.onValueChange = onValueChange;
        this.displayState = displayState;
        this.tooltipSupplier = tooltipSupplier;
        this.spriteSupplier = spriteSupplier;
        this.updateTooltip();
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        Identifier identifier = this.spriteSupplier.apply(this, this.getValue());
        if (identifier != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        } else {
            this.renderDefaultSprite(guiGraphics);
        }
        if (this.displayState != DisplayState.HIDE) {
            this.renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
        }
    }

    private void updateTooltip() {
        this.setTooltip(this.tooltipSupplier.apply(this.value));
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        if (inputWithModifiers.hasShiftDown()) {
            this.cycleValue(-1);
        } else {
            this.cycleValue(1);
        }
    }

    private void cycleValue(int i) {
        List<T> list = this.values.getSelectedList();
        this.index = Mth.positiveModulo(this.index + i, list.size());
        T object = list.get(this.index);
        this.updateValue(object);
        this.onValueChange.onValueChange(this, object);
    }

    private T getCycledValue(int i) {
        List<T> list = this.values.getSelectedList();
        return list.get(Mth.positiveModulo(this.index + i, list.size()));
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (g > 0.0) {
            this.cycleValue(-1);
        } else if (g < 0.0) {
            this.cycleValue(1);
        }
        return true;
    }

    public void setValue(T object) {
        List<T> list = this.values.getSelectedList();
        int i = list.indexOf(object);
        if (i != -1) {
            this.index = i;
        }
        this.updateValue(object);
    }

    @Override
    public void resetValue() {
        this.setValue(this.defaultValueSupplier.get());
    }

    private void updateValue(T object) {
        Component component = this.createLabelForValue(object);
        this.setMessage(component);
        this.value = object;
        this.updateTooltip();
    }

    private Component createLabelForValue(T object) {
        return this.displayState == DisplayState.VALUE ? this.valueStringifier.apply(object) : this.createFullName(object);
    }

    private MutableComponent createFullName(T object) {
        return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(object));
    }

    public T getValue() {
        return this.value;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.narrationProvider.apply(this);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            T object = this.getCycledValue(1);
            Component component = this.createLabelForValue(object);
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.focused", component));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.hovered", component));
            }
        }
    }

    public MutableComponent createDefaultNarrationMessage() {
        return CycleButton.wrapDefaultNarrationMessage(this.displayState == DisplayState.VALUE ? this.createFullName(this.value) : this.getMessage());
    }

    public static <T> Builder<T> builder(Function<T, Component> function, Supplier<T> supplier) {
        return new Builder<T>(function, supplier);
    }

    public static <T> Builder<T> builder(Function<T, Component> function, T object) {
        return new Builder<Object>(function, () -> object);
    }

    public static Builder<Boolean> booleanBuilder(Component component, Component component2, boolean bl) {
        return new Builder<Boolean>(boolean_ -> boolean_ == Boolean.TRUE ? component : component2, () -> bl).withValues((Collection<Boolean>)BOOLEAN_OPTIONS);
    }

    public static Builder<Boolean> onOffBuilder(boolean bl) {
        return new Builder<Boolean>(boolean_ -> boolean_ == Boolean.TRUE ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF, () -> bl).withValues((Collection<Boolean>)BOOLEAN_OPTIONS);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface ValueListSupplier<T> {
        public List<T> getSelectedList();

        public List<T> getDefaultList();

        public static <T> ValueListSupplier<T> create(Collection<T> collection) {
            ImmutableList list = ImmutableList.copyOf(collection);
            return new ValueListSupplier<T>((List)list){
                final /* synthetic */ List val$copy;
                {
                    this.val$copy = list;
                }

                @Override
                public List<T> getSelectedList() {
                    return this.val$copy;
                }

                @Override
                public List<T> getDefaultList() {
                    return this.val$copy;
                }
            };
        }

        public static <T> ValueListSupplier<T> create(final BooleanSupplier booleanSupplier, List<T> list, List<T> list2) {
            ImmutableList list3 = ImmutableList.copyOf(list);
            ImmutableList list4 = ImmutableList.copyOf(list2);
            return new ValueListSupplier<T>((List)list4, (List)list3){
                final /* synthetic */ List val$altCopy;
                final /* synthetic */ List val$defaultCopy;
                {
                    this.val$altCopy = list;
                    this.val$defaultCopy = list2;
                }

                @Override
                public List<T> getSelectedList() {
                    return booleanSupplier.getAsBoolean() ? this.val$altCopy : this.val$defaultCopy;
                }

                @Override
                public List<T> getDefaultList() {
                    return this.val$defaultCopy;
                }
            };
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface OnValueChange<T> {
        public void onValueChange(CycleButton<T> var1, T var2);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum DisplayState {
        NAME_AND_VALUE,
        VALUE,
        HIDE;

    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface SpriteSupplier<T> {
        public @Nullable Identifier apply(CycleButton<T> var1, T var2);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder<T> {
        private final Supplier<T> defaultValueSupplier;
        private final Function<T, Component> valueStringifier;
        private OptionInstance.TooltipSupplier<T> tooltipSupplier = object -> null;
        private SpriteSupplier<T> spriteSupplier = (cycleButton, object) -> null;
        private Function<CycleButton<T>, MutableComponent> narrationProvider = CycleButton::createDefaultNarrationMessage;
        private ValueListSupplier<T> values = ValueListSupplier.create(ImmutableList.of());
        private DisplayState displayState = DisplayState.NAME_AND_VALUE;

        public Builder(Function<T, Component> function, Supplier<T> supplier) {
            this.valueStringifier = function;
            this.defaultValueSupplier = supplier;
        }

        public Builder<T> withValues(Collection<T> collection) {
            return this.withValues(ValueListSupplier.create(collection));
        }

        @SafeVarargs
        public final Builder<T> withValues(T ... objects) {
            return this.withValues((Collection<T>)ImmutableList.copyOf((Object[])objects));
        }

        public Builder<T> withValues(List<T> list, List<T> list2) {
            return this.withValues(ValueListSupplier.create(DEFAULT_ALT_LIST_SELECTOR, list, list2));
        }

        public Builder<T> withValues(BooleanSupplier booleanSupplier, List<T> list, List<T> list2) {
            return this.withValues(ValueListSupplier.create(booleanSupplier, list, list2));
        }

        public Builder<T> withValues(ValueListSupplier<T> valueListSupplier) {
            this.values = valueListSupplier;
            return this;
        }

        public Builder<T> withTooltip(OptionInstance.TooltipSupplier<T> tooltipSupplier) {
            this.tooltipSupplier = tooltipSupplier;
            return this;
        }

        public Builder<T> withCustomNarration(Function<CycleButton<T>, MutableComponent> function) {
            this.narrationProvider = function;
            return this;
        }

        public Builder<T> withSprite(SpriteSupplier<T> spriteSupplier) {
            this.spriteSupplier = spriteSupplier;
            return this;
        }

        public Builder<T> displayState(DisplayState displayState) {
            this.displayState = displayState;
            return this;
        }

        public Builder<T> displayOnlyValue() {
            return this.displayState(DisplayState.VALUE);
        }

        public CycleButton<T> create(Component component, OnValueChange<T> onValueChange) {
            return this.create(0, 0, 150, 20, component, onValueChange);
        }

        public CycleButton<T> create(int i, int j, int k, int l, Component component) {
            return this.create(i, j, k, l, component, (cycleButton, object) -> {});
        }

        public CycleButton<T> create(int i, int j, int k, int l, Component component, OnValueChange<T> onValueChange) {
            List<T> list = this.values.getDefaultList();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            }
            T object = this.defaultValueSupplier.get();
            int m = list.indexOf(object);
            Component component2 = this.valueStringifier.apply(object);
            Component component3 = this.displayState == DisplayState.VALUE ? component2 : CommonComponents.optionNameValue(component, component2);
            return new CycleButton<T>(i, j, k, l, component3, component, m, object, this.defaultValueSupplier, this.values, this.valueStringifier, this.narrationProvider, onValueChange, this.tooltipSupplier, this.displayState, this.spriteSupplier);
        }
    }
}


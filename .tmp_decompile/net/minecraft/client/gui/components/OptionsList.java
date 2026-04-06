/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ResettableOptionWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OptionsList
extends ContainerObjectSelectionList<AbstractEntry> {
    private static final int BIG_BUTTON_WIDTH = 310;
    private static final int DEFAULT_ITEM_HEIGHT = 25;
    private final OptionsSubScreen screen;

    public OptionsList(Minecraft minecraft, int i, OptionsSubScreen optionsSubScreen) {
        super(minecraft, i, optionsSubScreen.layout.getContentHeight(), optionsSubScreen.layout.getHeaderHeight(), 25);
        this.centerListVertically = false;
        this.screen = optionsSubScreen;
    }

    public void addBig(OptionInstance<?> optionInstance) {
        this.addEntry(Entry.big(this.minecraft.options, optionInstance, this.screen));
    }

    public void addSmall(OptionInstance<?> ... optionInstances) {
        for (int i = 0; i < optionInstances.length; i += 2) {
            OptionInstance<?> optionInstance = i < optionInstances.length - 1 ? optionInstances[i + 1] : null;
            this.addEntry(Entry.small(this.minecraft.options, optionInstances[i], optionInstance, this.screen));
        }
    }

    public void addSmall(List<AbstractWidget> list) {
        for (int i = 0; i < list.size(); i += 2) {
            this.addSmall(list.get(i), i < list.size() - 1 ? list.get(i + 1) : null);
        }
    }

    public void addSmall(AbstractWidget abstractWidget, @Nullable AbstractWidget abstractWidget2) {
        this.addEntry(Entry.small(abstractWidget, abstractWidget2, this.screen));
    }

    public void addSmall(AbstractWidget abstractWidget, OptionInstance<?> optionInstance, @Nullable AbstractWidget abstractWidget2) {
        this.addEntry(Entry.small(abstractWidget, optionInstance, abstractWidget2, (Screen)this.screen));
    }

    public void addHeader(Component component) {
        int i = this.minecraft.font.lineHeight;
        int j = this.children().isEmpty() ? 0 : i * 2;
        this.addEntry(new HeaderEntry(this.screen, component, j), j + i + 4);
    }

    @Override
    public int getRowWidth() {
        return 310;
    }

    public @Nullable AbstractWidget findOption(OptionInstance<?> optionInstance) {
        for (AbstractEntry abstractEntry : this.children()) {
            Entry entry;
            AbstractWidget abstractWidget;
            if (!(abstractEntry instanceof Entry) || (abstractWidget = (entry = (Entry)abstractEntry).findOption(optionInstance)) == null) continue;
            return abstractWidget;
        }
        return null;
    }

    public void applyUnsavedChanges() {
        for (AbstractEntry abstractEntry : this.children()) {
            if (!(abstractEntry instanceof Entry)) continue;
            Entry entry = (Entry)abstractEntry;
            for (OptionInstanceWidget optionInstanceWidget : entry.children) {
                AbstractWidget abstractWidget;
                if (optionInstanceWidget.optionInstance() == null || !((abstractWidget = optionInstanceWidget.widget()) instanceof OptionInstance.OptionInstanceSliderButton)) continue;
                OptionInstance.OptionInstanceSliderButton optionInstanceSliderButton = (OptionInstance.OptionInstanceSliderButton)abstractWidget;
                optionInstanceSliderButton.applyUnsavedValue();
            }
        }
    }

    public void resetOption(OptionInstance<?> optionInstance) {
        for (AbstractEntry abstractEntry : this.children()) {
            if (!(abstractEntry instanceof Entry)) continue;
            Entry entry = (Entry)abstractEntry;
            for (OptionInstanceWidget optionInstanceWidget : entry.children) {
                AbstractWidget abstractWidget;
                if (optionInstanceWidget.optionInstance() != optionInstance || !((abstractWidget = optionInstanceWidget.widget()) instanceof ResettableOptionWidget)) continue;
                ResettableOptionWidget resettableOptionWidget = (ResettableOptionWidget)((Object)abstractWidget);
                resettableOptionWidget.resetValue();
                return;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Entry
    extends AbstractEntry {
        final List<OptionInstanceWidget> children;
        private final Screen screen;
        private static final int X_OFFSET = 160;

        private Entry(List<OptionInstanceWidget> list, Screen screen) {
            this.children = list;
            this.screen = screen;
        }

        public static Entry big(Options options, OptionInstance<?> optionInstance, Screen screen) {
            return new Entry(List.of((Object)((Object)new OptionInstanceWidget(optionInstance.createButton(options, 0, 0, 310), optionInstance))), screen);
        }

        public static Entry small(AbstractWidget abstractWidget, @Nullable AbstractWidget abstractWidget2, Screen screen) {
            if (abstractWidget2 == null) {
                return new Entry(List.of((Object)((Object)new OptionInstanceWidget(abstractWidget))), screen);
            }
            return new Entry(List.of((Object)((Object)new OptionInstanceWidget(abstractWidget)), (Object)((Object)new OptionInstanceWidget(abstractWidget2))), screen);
        }

        public static Entry small(AbstractWidget abstractWidget, OptionInstance<?> optionInstance, @Nullable AbstractWidget abstractWidget2, Screen screen) {
            if (abstractWidget2 == null) {
                return new Entry(List.of((Object)((Object)new OptionInstanceWidget(abstractWidget, optionInstance))), screen);
            }
            return new Entry(List.of((Object)((Object)new OptionInstanceWidget(abstractWidget, optionInstance)), (Object)((Object)new OptionInstanceWidget(abstractWidget2))), screen);
        }

        public static Entry small(Options options, OptionInstance<?> optionInstance, @Nullable OptionInstance<?> optionInstance2, OptionsSubScreen optionsSubScreen) {
            AbstractWidget abstractWidget = optionInstance.createButton(options);
            if (optionInstance2 == null) {
                return new Entry(List.of((Object)((Object)new OptionInstanceWidget(abstractWidget, optionInstance))), optionsSubScreen);
            }
            return new Entry(List.of((Object)((Object)new OptionInstanceWidget(abstractWidget, optionInstance)), (Object)((Object)new OptionInstanceWidget(optionInstance2.createButton(options), optionInstance2))), optionsSubScreen);
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
            int k = 0;
            int l = this.screen.width / 2 - 155;
            for (OptionInstanceWidget optionInstanceWidget : this.children) {
                optionInstanceWidget.widget().setPosition(l + k, this.getContentY());
                optionInstanceWidget.widget().render(guiGraphics, i, j, f);
                k += 160;
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Lists.transform(this.children, OptionInstanceWidget::widget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return Lists.transform(this.children, OptionInstanceWidget::widget);
        }

        public @Nullable AbstractWidget findOption(OptionInstance<?> optionInstance) {
            for (OptionInstanceWidget optionInstanceWidget : this.children) {
                if (optionInstanceWidget.optionInstance != optionInstance) continue;
                return optionInstanceWidget.widget();
            }
            return null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class HeaderEntry
    extends AbstractEntry {
        private final Screen screen;
        private final int paddingTop;
        private final StringWidget widget;

        protected HeaderEntry(Screen screen, Component component, int i) {
            this.screen = screen;
            this.paddingTop = i;
            this.widget = new StringWidget(component, screen.getFont());
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of((Object)this.widget);
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
            this.widget.setPosition(this.screen.width / 2 - 155, this.getContentY() + this.paddingTop);
            this.widget.render(guiGraphics, i, j, f);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of((Object)this.widget);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static abstract class AbstractEntry
    extends ContainerObjectSelectionList.Entry<AbstractEntry> {
        protected AbstractEntry() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class OptionInstanceWidget
    extends Record {
        private final AbstractWidget widget;
        final @Nullable OptionInstance<?> optionInstance;

        public OptionInstanceWidget(AbstractWidget abstractWidget) {
            this(abstractWidget, null);
        }

        public OptionInstanceWidget(AbstractWidget abstractWidget, @Nullable OptionInstance<?> optionInstance) {
            this.widget = abstractWidget;
            this.optionInstance = optionInstance;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{OptionInstanceWidget.class, "widget;optionInstance", "widget", "optionInstance"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{OptionInstanceWidget.class, "widget;optionInstance", "widget", "optionInstance"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{OptionInstanceWidget.class, "widget;optionInstance", "widget", "optionInstance"}, this, object);
        }

        public AbstractWidget widget() {
            return this.widget;
        }

        public @Nullable OptionInstance<?> optionInstance() {
            return this.optionInstance;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components.tabs;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TabNavigationBar
extends AbstractContainerEventHandler
implements Renderable,
NarratableEntry {
    private static final int NO_TAB = -1;
    private static final int MAX_WIDTH = 400;
    private static final int HEIGHT = 24;
    private static final int MARGIN = 14;
    private static final Component USAGE_NARRATION = Component.translatable("narration.tab_navigation.usage");
    private final LinearLayout layout = LinearLayout.horizontal();
    private int width;
    private final TabManager tabManager;
    private final ImmutableList<Tab> tabs;
    private final ImmutableList<TabButton> tabButtons;

    TabNavigationBar(int i, TabManager tabManager, Iterable<Tab> iterable) {
        this.width = i;
        this.tabManager = tabManager;
        this.tabs = ImmutableList.copyOf(iterable);
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (Tab tab : iterable) {
            builder.add((Object)this.layout.addChild(new TabButton(tabManager, tab, 0, 24)));
        }
        this.tabButtons = builder.build();
    }

    public static Builder builder(TabManager tabManager, int i) {
        return new Builder(tabManager, i);
    }

    public void setWidth(int i) {
        this.width = i;
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return d >= (double)this.layout.getX() && e >= (double)this.layout.getY() && d < (double)(this.layout.getX() + this.layout.getWidth()) && e < (double)(this.layout.getY() + this.layout.getHeight());
    }

    @Override
    public void setFocused(boolean bl) {
        super.setFocused(bl);
        if (this.getFocused() != null) {
            this.setFocused(null);
        }
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        TabButton tabButton;
        super.setFocused(guiEventListener);
        if (guiEventListener instanceof TabButton && (tabButton = (TabButton)guiEventListener).isActive()) {
            this.tabManager.setCurrentTab(tabButton.tab(), true);
        }
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        TabButton tabButton;
        if (!this.isFocused() && (tabButton = this.currentTabButton()) != null) {
            return ComponentPath.path(this, ComponentPath.leaf(tabButton));
        }
        if (focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation) {
            return null;
        }
        return super.nextFocusPath(focusNavigationEvent);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.tabButtons;
    }

    public List<Tab> getTabs() {
        return this.tabs;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.tabButtons.stream().map(AbstractWidget::narrationPriority).max(Comparator.naturalOrder()).orElse(NarratableEntry.NarrationPriority.NONE);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        Optional optional = this.tabButtons.stream().filter(AbstractWidget::isHovered).findFirst().or(() -> Optional.ofNullable(this.currentTabButton()));
        optional.ifPresent(tabButton -> {
            this.narrateListElementPosition(narrationElementOutput.nest(), (TabButton)tabButton);
            tabButton.updateNarration(narrationElementOutput);
        });
        if (this.isFocused()) {
            narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }
    }

    protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, TabButton tabButton) {
        int i;
        if (this.tabs.size() > 1 && (i = this.tabButtons.indexOf((Object)tabButton)) != -1) {
            narrationElementOutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.tab", i + 1, this.tabs.size()));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR, 0, this.layout.getY() + this.layout.getHeight() - 2, 0.0f, 0.0f, ((TabButton)this.tabButtons.get(0)).getX(), 2, 32, 2);
        int k = ((TabButton)this.tabButtons.get(this.tabButtons.size() - 1)).getRight();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR, k, this.layout.getY() + this.layout.getHeight() - 2, 0.0f, 0.0f, this.width, 2, 32, 2);
        for (TabButton tabButton : this.tabButtons) {
            tabButton.render(guiGraphics, i, j, f);
        }
    }

    @Override
    public ScreenRectangle getRectangle() {
        return this.layout.getRectangle();
    }

    public void arrangeElements() {
        int i = Math.min(400, this.width) - 28;
        int j = Mth.roundToward(i / this.tabs.size(), 2);
        for (TabButton tabButton : this.tabButtons) {
            tabButton.setWidth(j);
        }
        this.layout.arrangeElements();
        this.layout.setX(Mth.roundToward((this.width - i) / 2, 2));
        this.layout.setY(0);
    }

    public void selectTab(int i, boolean bl) {
        if (this.isFocused()) {
            this.setFocused((GuiEventListener)this.tabButtons.get(i));
        } else if (((TabButton)this.tabButtons.get(i)).isActive()) {
            this.tabManager.setCurrentTab((Tab)this.tabs.get(i), bl);
        }
    }

    public void setTabActiveState(int i, boolean bl) {
        if (i >= 0 && i < this.tabButtons.size()) {
            ((TabButton)this.tabButtons.get((int)i)).active = bl;
        }
    }

    public void setTabTooltip(int i, @Nullable Tooltip tooltip) {
        if (i >= 0 && i < this.tabButtons.size()) {
            ((TabButton)this.tabButtons.get(i)).setTooltip(tooltip);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        int i;
        if (keyEvent.hasControlDownWithQuirk() && (i = this.getNextTabIndex(keyEvent)) != -1) {
            this.selectTab(Mth.clamp(i, 0, this.tabs.size() - 1), true);
            return true;
        }
        return false;
    }

    private int getNextTabIndex(KeyEvent keyEvent) {
        return this.getNextTabIndex(this.currentTabIndex(), keyEvent);
    }

    private int getNextTabIndex(int i, KeyEvent keyEvent) {
        int j = keyEvent.getDigit();
        if (j != -1) {
            return Math.floorMod(j - 1, 10);
        }
        if (keyEvent.isCycleFocus() && i != -1) {
            int k = keyEvent.hasShiftDown() ? i - 1 : i + 1;
            int l = Math.floorMod(k, this.tabs.size());
            if (((TabButton)this.tabButtons.get((int)l)).active) {
                return l;
            }
            return this.getNextTabIndex(l, keyEvent);
        }
        return -1;
    }

    private int currentTabIndex() {
        Tab tab = this.tabManager.getCurrentTab();
        int i = this.tabs.indexOf((Object)tab);
        return i != -1 ? i : -1;
    }

    private @Nullable TabButton currentTabButton() {
        int i = this.currentTabIndex();
        return i != -1 ? (TabButton)this.tabButtons.get(i) : null;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final int width;
        private final TabManager tabManager;
        private final List<Tab> tabs = new ArrayList<Tab>();

        Builder(TabManager tabManager, int i) {
            this.tabManager = tabManager;
            this.width = i;
        }

        public Builder addTabs(Tab ... tabs) {
            Collections.addAll(this.tabs, tabs);
            return this;
        }

        public TabNavigationBar build() {
            return new TabNavigationBar(this.width, this.tabManager, this.tabs);
        }
    }
}


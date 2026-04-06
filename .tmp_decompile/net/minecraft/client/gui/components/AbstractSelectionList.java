/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractSelectionList<E extends Entry<E>>
extends AbstractContainerWidget {
    private static final Identifier MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    private static final int SEPARATOR_HEIGHT = 2;
    protected final Minecraft minecraft;
    protected final int defaultEntryHeight;
    private final List<E> children = new TrackedList();
    protected boolean centerListVertically = true;
    private @Nullable E selected;
    private @Nullable E hovered;

    public AbstractSelectionList(Minecraft minecraft, int i, int j, int k, int l) {
        super(0, k, i, j, CommonComponents.EMPTY);
        this.minecraft = minecraft;
        this.defaultEntryHeight = l;
    }

    public @Nullable E getSelected() {
        return this.selected;
    }

    public void setSelected(@Nullable E entry) {
        this.selected = entry;
        if (entry != null) {
            boolean bl2;
            boolean bl = ((Entry)entry).getContentY() < this.getY();
            boolean bl3 = bl2 = ((Entry)entry).getContentBottom() > this.getBottom();
            if (this.minecraft.getLastInputType().isKeyboard() || bl || bl2) {
                this.scrollToEntry(entry);
            }
        }
    }

    public @Nullable E getFocused() {
        return (E)((Entry)super.getFocused());
    }

    public final List<E> children() {
        return Collections.unmodifiableList(this.children);
    }

    protected void sort(Comparator<E> comparator) {
        this.children.sort(comparator);
        this.repositionEntries();
    }

    protected void swap(int i, int j) {
        Collections.swap(this.children, i, j);
        this.repositionEntries();
        this.scrollToEntry((Entry)this.children.get(j));
    }

    protected void clearEntries() {
        this.children.clear();
        this.selected = null;
    }

    protected void clearEntriesExcept(E entry) {
        this.children.removeIf(entry2 -> entry2 != entry);
        if (this.selected != entry) {
            this.setSelected(null);
        }
    }

    public void replaceEntries(Collection<E> collection) {
        this.clearEntries();
        for (Entry entry : collection) {
            this.addEntry(entry);
        }
    }

    private int getFirstEntryY() {
        return this.getY() + 2;
    }

    public int getNextY() {
        int i = this.getFirstEntryY() - (int)this.scrollAmount();
        for (Entry entry : this.children) {
            i += entry.getHeight();
        }
        return i;
    }

    protected int addEntry(E entry) {
        return this.addEntry(entry, this.defaultEntryHeight);
    }

    protected int addEntry(E entry, int i) {
        ((Entry)entry).setX(this.getRowLeft());
        ((Entry)entry).setWidth(this.getRowWidth());
        ((Entry)entry).setY(this.getNextY());
        ((Entry)entry).setHeight(i);
        this.children.add(entry);
        return this.children.size() - 1;
    }

    protected void addEntryToTop(E entry) {
        this.addEntryToTop(entry, this.defaultEntryHeight);
    }

    protected void addEntryToTop(E entry, int i) {
        double d = (double)this.maxScrollAmount() - this.scrollAmount();
        ((Entry)entry).setHeight(i);
        this.children.addFirst(entry);
        this.repositionEntries();
        this.setScrollAmount((double)this.maxScrollAmount() - d);
    }

    private void repositionEntries() {
        int i = this.getFirstEntryY() - (int)this.scrollAmount();
        for (Entry entry : this.children) {
            entry.setY(i);
            i += entry.getHeight();
            entry.setX(this.getRowLeft());
            entry.setWidth(this.getRowWidth());
        }
    }

    protected void removeEntryFromTop(E entry) {
        double d = (double)this.maxScrollAmount() - this.scrollAmount();
        this.removeEntry(entry);
        this.setScrollAmount((double)this.maxScrollAmount() - d);
    }

    protected int getItemCount() {
        return this.children().size();
    }

    protected boolean entriesCanBeSelected() {
        return true;
    }

    protected final @Nullable E getEntryAtPosition(double d, double e) {
        for (Entry entry : this.children) {
            if (!entry.isMouseOver(d, e)) continue;
            return (E)entry;
        }
        return null;
    }

    public void updateSize(int i, HeaderAndFooterLayout headerAndFooterLayout) {
        this.updateSizeAndPosition(i, headerAndFooterLayout.getContentHeight(), headerAndFooterLayout.getHeaderHeight());
    }

    public void updateSizeAndPosition(int i, int j, int k) {
        this.updateSizeAndPosition(i, j, 0, k);
    }

    public void updateSizeAndPosition(int i, int j, int k, int l) {
        this.setSize(i, j);
        this.setPosition(k, l);
        this.repositionEntries();
        if (this.getSelected() != null) {
            this.scrollToEntry(this.getSelected());
        }
        this.refreshScrollAmount();
    }

    @Override
    protected int contentHeight() {
        int i = 0;
        for (Entry entry : this.children) {
            i += entry.getHeight();
        }
        return i + 4;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        this.hovered = this.isMouseOver(i, j) ? this.getEntryAtPosition(i, j) : null;
        this.renderListBackground(guiGraphics);
        this.enableScissor(guiGraphics);
        this.renderListItems(guiGraphics, i, j, f);
        guiGraphics.disableScissor();
        this.renderListSeparators(guiGraphics);
        this.renderScrollbar(guiGraphics, i, j);
    }

    protected void renderListSeparators(GuiGraphics guiGraphics) {
        Identifier identifier = this.minecraft.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
        Identifier identifier2 = this.minecraft.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY() - 2, 0.0f, 0.0f, this.getWidth(), 2, 32, 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, identifier2, this.getX(), this.getBottom(), 0.0f, 0.0f, this.getWidth(), 2, 32, 2);
    }

    protected void renderListBackground(GuiGraphics guiGraphics) {
        Identifier identifier = this.minecraft.level == null ? MENU_LIST_BACKGROUND : INWORLD_MENU_LIST_BACKGROUND;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), this.getRight(), this.getBottom() + (int)this.scrollAmount(), this.getWidth(), this.getHeight(), 32, 32);
    }

    protected void enableScissor(GuiGraphics guiGraphics) {
        guiGraphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
    }

    protected void scrollToEntry(E entry) {
        int j;
        int i = ((Entry)entry).getY() - this.getY() - 2;
        if (i < 0) {
            this.scroll(i);
        }
        if ((j = this.getBottom() - ((Entry)entry).getY() - ((Entry)entry).getHeight() - 2) < 0) {
            this.scroll(-j);
        }
    }

    protected void centerScrollOn(E entry) {
        int i = 0;
        for (Entry entry2 : this.children) {
            if (entry2 == entry) {
                i += entry2.getHeight() / 2;
                break;
            }
            i += entry2.getHeight();
        }
        this.setScrollAmount((double)i - (double)this.height / 2.0);
    }

    private void scroll(int i) {
        this.setScrollAmount(this.scrollAmount() + (double)i);
    }

    @Override
    public void setScrollAmount(double d) {
        super.setScrollAmount(d);
        this.repositionEntries();
    }

    @Override
    protected double scrollRate() {
        return (double)this.defaultEntryHeight / 2.0;
    }

    @Override
    protected int scrollBarX() {
        return this.getRowRight() + 6 + 2;
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double d, double e) {
        return Optional.ofNullable(this.getEntryAtPosition(d, e));
    }

    @Override
    public void setFocused(boolean bl) {
        super.setFocused(bl);
        if (!bl) {
            this.setFocused(null);
        }
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        GuiEventListener entry = this.getFocused();
        if (entry != guiEventListener && entry instanceof ContainerEventHandler) {
            ContainerEventHandler containerEventHandler = (ContainerEventHandler)entry;
            containerEventHandler.setFocused(null);
        }
        super.setFocused(guiEventListener);
        int i = this.children.indexOf(guiEventListener);
        if (i >= 0) {
            Entry entry2 = (Entry)this.children.get(i);
            this.setSelected(entry2);
        }
    }

    protected @Nullable E nextEntry(ScreenDirection screenDirection) {
        return (E)this.nextEntry(screenDirection, entry -> true);
    }

    protected @Nullable E nextEntry(ScreenDirection screenDirection, Predicate<E> predicate) {
        return this.nextEntry(screenDirection, predicate, this.getSelected());
    }

    protected @Nullable E nextEntry(ScreenDirection screenDirection, Predicate<E> predicate, @Nullable E entry) {
        int i;
        switch (screenDirection) {
            default: {
                throw new MatchException(null, null);
            }
            case RIGHT: 
            case LEFT: {
                int n = 0;
                break;
            }
            case UP: {
                int n = -1;
                break;
            }
            case DOWN: {
                int n = i = 1;
            }
        }
        if (!this.children().isEmpty() && i != 0) {
            int j = entry == null ? (i > 0 ? 0 : this.children().size() - 1) : this.children().indexOf(entry) + i;
            for (int k = j; k >= 0 && k < this.children.size(); k += i) {
                Entry entry2 = (Entry)this.children().get(k);
                if (!predicate.test(entry2)) continue;
                return (E)entry2;
            }
        }
        return null;
    }

    protected void renderListItems(GuiGraphics guiGraphics, int i, int j, float f) {
        for (Entry entry : this.children) {
            if (entry.getY() + entry.getHeight() < this.getY() || entry.getY() > this.getBottom()) continue;
            this.renderItem(guiGraphics, i, j, f, entry);
        }
    }

    protected void renderItem(GuiGraphics guiGraphics, int i, int j, float f, E entry) {
        if (this.entriesCanBeSelected() && this.getSelected() == entry) {
            int k = this.isFocused() ? -1 : -8355712;
            this.renderSelection(guiGraphics, entry, k);
        }
        ((Entry)entry).renderContent(guiGraphics, i, j, Objects.equals(this.hovered, entry), f);
    }

    protected void renderSelection(GuiGraphics guiGraphics, E entry, int i) {
        int j = ((Entry)entry).getX();
        int k = ((Entry)entry).getY();
        int l = j + ((Entry)entry).getWidth();
        int m = k + ((Entry)entry).getHeight();
        guiGraphics.fill(j, k, l, m, i);
        guiGraphics.fill(j + 1, k + 1, l - 1, m - 1, -16777216);
    }

    public int getRowLeft() {
        return this.getX() + this.width / 2 - this.getRowWidth() / 2;
    }

    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth();
    }

    public int getRowTop(int i) {
        return ((Entry)this.children.get(i)).getY();
    }

    public int getRowBottom(int i) {
        Entry entry = (Entry)this.children.get(i);
        return entry.getY() + entry.getHeight();
    }

    public int getRowWidth() {
        return 220;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        if (this.hovered != null) {
            return NarratableEntry.NarrationPriority.HOVERED;
        }
        return NarratableEntry.NarrationPriority.NONE;
    }

    protected void removeEntries(List<E> list) {
        list.forEach(this::removeEntry);
    }

    protected void removeEntry(E entry) {
        boolean bl = this.children.remove(entry);
        if (bl) {
            this.repositionEntries();
            if (entry == this.getSelected()) {
                this.setSelected(null);
            }
        }
    }

    protected @Nullable E getHovered() {
        return this.hovered;
    }

    void bindEntryToSelf(Entry<E> entry) {
        entry.list = this;
    }

    protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, E entry) {
        int i;
        List<E> list = this.children();
        if (list.size() > 1 && (i = list.indexOf(entry)) != -1) {
            narrationElementOutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.list", i + 1, list.size()));
        }
    }

    @Override
    public /* synthetic */ @Nullable GuiEventListener getFocused() {
        return this.getFocused();
    }

    @Environment(value=EnvType.CLIENT)
    class TrackedList
    extends AbstractList<E> {
        private final List<E> delegate = Lists.newArrayList();

        TrackedList() {
        }

        @Override
        public E get(int i) {
            return (Entry)this.delegate.get(i);
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public E set(int i, E entry) {
            Entry entry2 = (Entry)this.delegate.set(i, entry);
            AbstractSelectionList.this.bindEntryToSelf(entry);
            return entry2;
        }

        @Override
        public void add(int i, E entry) {
            this.delegate.add(i, entry);
            AbstractSelectionList.this.bindEntryToSelf(entry);
        }

        @Override
        public E remove(int i) {
            return (Entry)this.delegate.remove(i);
        }

        @Override
        public /* synthetic */ Object remove(int i) {
            return this.remove(i);
        }

        @Override
        public /* synthetic */ void add(int i, Object object) {
            this.add(i, (E)((Entry)object));
        }

        @Override
        public /* synthetic */ Object set(int i, Object object) {
            return this.set(i, (E)((Entry)object));
        }

        @Override
        public /* synthetic */ Object get(int i) {
            return this.get(i);
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static abstract class Entry<E extends Entry<E>>
    implements GuiEventListener,
    LayoutElement {
        public static final int CONTENT_PADDING = 2;
        private int x = 0;
        private int y = 0;
        private int width = 0;
        private int height;
        @Deprecated
        AbstractSelectionList<E> list;

        protected Entry() {
        }

        @Override
        public void setFocused(boolean bl) {
        }

        @Override
        public boolean isFocused() {
            return this.list.getFocused() == this;
        }

        public abstract void renderContent(GuiGraphics var1, int var2, int var3, boolean var4, float var5);

        @Override
        public boolean isMouseOver(double d, double e) {
            return this.getRectangle().containsPoint((int)d, (int)e);
        }

        @Override
        public void setX(int i) {
            this.x = i;
        }

        @Override
        public void setY(int i) {
            this.y = i;
        }

        public void setWidth(int i) {
            this.width = i;
        }

        public void setHeight(int i) {
            this.height = i;
        }

        public int getContentX() {
            return this.getX() + 2;
        }

        public int getContentY() {
            return this.getY() + 2;
        }

        public int getContentHeight() {
            return this.getHeight() - 4;
        }

        public int getContentYMiddle() {
            return this.getContentY() + this.getContentHeight() / 2;
        }

        public int getContentBottom() {
            return this.getContentY() + this.getContentHeight();
        }

        public int getContentWidth() {
            return this.getWidth() - 4;
        }

        public int getContentXMiddle() {
            return this.getContentX() + this.getContentWidth() / 2;
        }

        public int getContentRight() {
            return this.getContentX() + this.getContentWidth();
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return this.height;
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> consumer) {
        }

        @Override
        public ScreenRectangle getRectangle() {
            return LayoutElement.super.getRectangle();
        }
    }
}


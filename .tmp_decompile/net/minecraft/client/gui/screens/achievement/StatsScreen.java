/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.achievement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ItemDisplayWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.LoadingTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class StatsScreen
extends Screen {
    private static final Component TITLE = Component.translatable("gui.stats");
    static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
    static final Identifier HEADER_SPRITE = Identifier.withDefaultNamespace("statistics/header");
    static final Identifier SORT_UP_SPRITE = Identifier.withDefaultNamespace("statistics/sort_up");
    static final Identifier SORT_DOWN_SPRITE = Identifier.withDefaultNamespace("statistics/sort_down");
    private static final Component PENDING_TEXT = Component.translatable("multiplayer.downloadingStats");
    static final Component NO_VALUE_DISPLAY = Component.translatable("stats.none");
    private static final Component GENERAL_BUTTON = Component.translatable("stat.generalButton");
    private static final Component ITEMS_BUTTON = Component.translatable("stat.itemsButton");
    private static final Component MOBS_BUTTON = Component.translatable("stat.mobsButton");
    protected final Screen lastScreen;
    private static final int LIST_WIDTH = 280;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final TabManager tabManager = new TabManager(guiEventListener -> {
        AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
    }, guiEventListener -> this.removeWidget((GuiEventListener)guiEventListener));
    private @Nullable TabNavigationBar tabNavigationBar;
    final StatsCounter stats;
    private boolean isLoading = true;

    public StatsScreen(Screen screen, StatsCounter statsCounter) {
        super(TITLE);
        this.lastScreen = screen;
        this.stats = statsCounter;
    }

    @Override
    protected void init() {
        Component component = PENDING_TEXT;
        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(new LoadingTab(this.getFont(), GENERAL_BUTTON, component), new LoadingTab(this.getFont(), ITEMS_BUTTON, component), new LoadingTab(this.getFont(), MOBS_BUTTON, component)).build();
        this.addRenderableWidget(this.tabNavigationBar);
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
        this.tabNavigationBar.setTabActiveState(0, true);
        this.tabNavigationBar.setTabActiveState(1, false);
        this.tabNavigationBar.setTabActiveState(2, false);
        this.layout.visitWidgets(abstractWidget -> {
            abstractWidget.setTabOrderGroup(1);
            this.addRenderableWidget(abstractWidget);
        });
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
        this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
    }

    public void onStatsUpdated() {
        if (this.isLoading) {
            if (this.tabNavigationBar != null) {
                this.removeWidget(this.tabNavigationBar);
            }
            this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(new StatisticsTab(GENERAL_BUTTON, new GeneralStatisticsList(this.minecraft)), new StatisticsTab(ITEMS_BUTTON, new ItemStatisticsList(this.minecraft)), new StatisticsTab(MOBS_BUTTON, new MobsStatisticsList(this.minecraft))).build();
            this.setFocused(this.tabNavigationBar);
            this.addRenderableWidget(this.tabNavigationBar);
            this.setTabActiveStateAndTooltip(1);
            this.setTabActiveStateAndTooltip(2);
            this.tabNavigationBar.selectTab(0, false);
            this.repositionElements();
            this.isLoading = false;
        }
    }

    /*
     * Unable to fully structure code
     */
    private void setTabActiveStateAndTooltip(int i) {
        if (this.tabNavigationBar == null) {
            return;
        }
        var4_2 = this.tabNavigationBar.getTabs().get(i);
        if (!(var4_2 instanceof StatisticsTab)) ** GOTO lbl-1000
        statisticsTab = (StatisticsTab)var4_2;
        if (!statisticsTab.list.children().isEmpty()) {
            v0 = true;
        } else lbl-1000:
        // 2 sources

        {
            v0 = false;
        }
        bl = v0;
        this.tabNavigationBar.setTabActiveState(i, bl);
        if (bl) {
            this.tabNavigationBar.setTabTooltip(i, null);
        } else {
            this.tabNavigationBar.setTabTooltip(i, Tooltip.create(Component.translatable("gui.stats.none_found")));
        }
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar == null) {
            return;
        }
        this.tabNavigationBar.setWidth(this.width);
        this.tabNavigationBar.arrangeElements();
        int i = this.tabNavigationBar.getRectangle().bottom();
        ScreenRectangle screenRectangle = new ScreenRectangle(0, i, this.width, this.height - this.layout.getFooterHeight() - i);
        this.tabNavigationBar.getTabs().forEach(tab -> tab.visitChildren(abstractWidget -> abstractWidget.setHeight(screenRectangle.height())));
        this.tabManager.setTabArea(screenRectangle);
        this.layout.setHeaderHeight(i);
        this.layout.arrangeElements();
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (this.tabNavigationBar != null && this.tabNavigationBar.keyPressed(keyEvent)) {
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight(), 0.0f, 0.0f, this.width, 2, 32, 2);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.TAB_HEADER_BACKGROUND, 0, 0, 0.0f, 0.0f, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderMenuBackground(guiGraphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    static String getTranslationKey(Stat<Identifier> stat) {
        return "stat." + stat.getValue().toString().replace(':', '.');
    }

    @Environment(value=EnvType.CLIENT)
    class StatisticsTab
    extends GridLayoutTab {
        protected final AbstractSelectionList<?> list;

        public StatisticsTab(Component component, AbstractSelectionList<?> abstractSelectionList) {
            super(component);
            this.layout.addChild(abstractSelectionList, 1, 1);
            this.list = abstractSelectionList;
        }

        @Override
        public void doLayout(ScreenRectangle screenRectangle) {
            this.list.updateSizeAndPosition(StatsScreen.this.width, StatsScreen.this.layout.getContentHeight(), StatsScreen.this.layout.getHeaderHeight());
            super.doLayout(screenRectangle);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class GeneralStatisticsList
    extends ObjectSelectionList<Entry> {
        public GeneralStatisticsList(Minecraft minecraft) {
            super(minecraft, StatsScreen.this.width, StatsScreen.this.layout.getContentHeight(), 33, 14);
            ObjectArrayList objectArrayList = new ObjectArrayList(Stats.CUSTOM.iterator());
            objectArrayList.sort(Comparator.comparing(stat -> I18n.get(StatsScreen.getTranslationKey(stat), new Object[0])));
            for (Stat stat2 : objectArrayList) {
                this.addEntry(new Entry(stat2));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Override
        protected void renderListBackground(GuiGraphics guiGraphics) {
        }

        @Override
        protected void renderListSeparators(GuiGraphics guiGraphics) {
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            private final Stat<Identifier> stat;
            private final Component statDisplay;

            Entry(Stat<Identifier> stat) {
                this.stat = stat;
                this.statDisplay = Component.translatable(StatsScreen.getTranslationKey(stat));
            }

            private String getValueText() {
                return this.stat.format(StatsScreen.this.stats.getValue(this.stat));
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
                int k = this.getContentYMiddle() - ((StatsScreen)StatsScreen.this).font.lineHeight / 2;
                int l = GeneralStatisticsList.this.children().indexOf(this);
                int m = l % 2 == 0 ? -1 : -4539718;
                guiGraphics.drawString(StatsScreen.this.font, this.statDisplay, this.getContentX() + 2, k, m);
                String string = this.getValueText();
                guiGraphics.drawString(StatsScreen.this.font, string, this.getContentRight() - StatsScreen.this.font.width(string) - 4, k, m);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", Component.empty().append(this.statDisplay).append(CommonComponents.SPACE).append(this.getValueText()));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ItemStatisticsList
    extends ContainerObjectSelectionList<Entry> {
        private static final int SLOT_BG_SIZE = 18;
        private static final int SLOT_STAT_HEIGHT = 22;
        private static final int SLOT_BG_Y = 1;
        private static final int SORT_NONE = 0;
        private static final int SORT_DOWN = -1;
        private static final int SORT_UP = 1;
        protected final List<StatType<Block>> blockColumns;
        protected final List<StatType<Item>> itemColumns;
        protected final Comparator<ItemRow> itemStatSorter;
        protected @Nullable StatType<?> sortColumn;
        protected int sortOrder;

        public ItemStatisticsList(Minecraft minecraft) {
            boolean bl;
            super(minecraft, StatsScreen.this.width, StatsScreen.this.layout.getContentHeight(), 33, 22);
            this.itemStatSorter = new ItemRowComparator();
            this.blockColumns = Lists.newArrayList();
            this.blockColumns.add(Stats.BLOCK_MINED);
            this.itemColumns = Lists.newArrayList((Object[])new StatType[]{Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED});
            Set set = Sets.newIdentityHashSet();
            for (Item item : BuiltInRegistries.ITEM) {
                bl = false;
                for (StatType<Item> statType : this.itemColumns) {
                    if (!statType.contains(item) || StatsScreen.this.stats.getValue(statType.get(item)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(item);
            }
            for (Block block : BuiltInRegistries.BLOCK) {
                bl = false;
                for (StatType<FeatureElement> statType : this.blockColumns) {
                    if (!statType.contains(block) || StatsScreen.this.stats.getValue(statType.get(block)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(block.asItem());
            }
            set.remove(Items.AIR);
            if (!set.isEmpty()) {
                this.addEntry(new HeaderEntry());
                for (Item item : set) {
                    this.addEntry(new ItemRow(item));
                }
            }
        }

        @Override
        protected void renderListBackground(GuiGraphics guiGraphics) {
        }

        int getColumnX(int i) {
            return 75 + 40 * i;
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        StatType<?> getColumn(int i) {
            return i < this.blockColumns.size() ? this.blockColumns.get(i) : this.itemColumns.get(i - this.blockColumns.size());
        }

        int getColumnIndex(StatType<?> statType) {
            int i = this.blockColumns.indexOf(statType);
            if (i >= 0) {
                return i;
            }
            int j = this.itemColumns.indexOf(statType);
            if (j >= 0) {
                return j + this.blockColumns.size();
            }
            return -1;
        }

        protected void sortByColumn(StatType<?> statType) {
            if (statType != this.sortColumn) {
                this.sortColumn = statType;
                this.sortOrder = -1;
            } else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            } else {
                this.sortColumn = null;
                this.sortOrder = 0;
            }
            this.sortItems(this.itemStatSorter);
        }

        protected void sortItems(Comparator<ItemRow> comparator) {
            List<ItemRow> list = this.getItemRows();
            list.sort(comparator);
            this.clearEntriesExcept((Entry)this.children().getFirst());
            for (ItemRow itemRow : list) {
                this.addEntry(itemRow);
            }
        }

        private List<ItemRow> getItemRows() {
            ArrayList<ItemRow> list = new ArrayList<ItemRow>();
            this.children().forEach(entry -> {
                if (entry instanceof ItemRow) {
                    ItemRow itemRow = (ItemRow)entry;
                    list.add(itemRow);
                }
            });
            return list;
        }

        @Override
        protected void renderListSeparators(GuiGraphics guiGraphics) {
        }

        @Environment(value=EnvType.CLIENT)
        class ItemRowComparator
        implements Comparator<ItemRow> {
            ItemRowComparator() {
            }

            @Override
            public int compare(ItemRow itemRow, ItemRow itemRow2) {
                int j;
                int i;
                Item item = itemRow.getItem();
                Item item2 = itemRow2.getItem();
                if (ItemStatisticsList.this.sortColumn == null) {
                    i = 0;
                    j = 0;
                } else if (ItemStatisticsList.this.blockColumns.contains(ItemStatisticsList.this.sortColumn)) {
                    StatType<?> statType = ItemStatisticsList.this.sortColumn;
                    i = item instanceof BlockItem ? StatsScreen.this.stats.getValue(statType, ((BlockItem)item).getBlock()) : -1;
                    j = item2 instanceof BlockItem ? StatsScreen.this.stats.getValue(statType, ((BlockItem)item2).getBlock()) : -1;
                } else {
                    StatType<?> statType = ItemStatisticsList.this.sortColumn;
                    i = StatsScreen.this.stats.getValue(statType, item);
                    j = StatsScreen.this.stats.getValue(statType, item2);
                }
                if (i == j) {
                    return ItemStatisticsList.this.sortOrder * Integer.compare(Item.getId(item), Item.getId(item2));
                }
                return ItemStatisticsList.this.sortOrder * Integer.compare(i, j);
            }

            @Override
            public /* synthetic */ int compare(Object object, Object object2) {
                return this.compare((ItemRow)object, (ItemRow)object2);
            }
        }

        @Environment(value=EnvType.CLIENT)
        class HeaderEntry
        extends Entry {
            private static final Identifier BLOCK_MINED_SPRITE = Identifier.withDefaultNamespace("statistics/block_mined");
            private static final Identifier ITEM_BROKEN_SPRITE = Identifier.withDefaultNamespace("statistics/item_broken");
            private static final Identifier ITEM_CRAFTED_SPRITE = Identifier.withDefaultNamespace("statistics/item_crafted");
            private static final Identifier ITEM_USED_SPRITE = Identifier.withDefaultNamespace("statistics/item_used");
            private static final Identifier ITEM_PICKED_UP_SPRITE = Identifier.withDefaultNamespace("statistics/item_picked_up");
            private static final Identifier ITEM_DROPPED_SPRITE = Identifier.withDefaultNamespace("statistics/item_dropped");
            private final StatSortButton blockMined;
            private final StatSortButton itemBroken;
            private final StatSortButton itemCrafted;
            private final StatSortButton itemUsed;
            private final StatSortButton itemPickedUp;
            private final StatSortButton itemDropped;
            private final List<AbstractWidget> children = new ArrayList<AbstractWidget>();

            HeaderEntry() {
                this.blockMined = new StatSortButton(this, 0, BLOCK_MINED_SPRITE);
                this.itemBroken = new StatSortButton(this, 1, ITEM_BROKEN_SPRITE);
                this.itemCrafted = new StatSortButton(this, 2, ITEM_CRAFTED_SPRITE);
                this.itemUsed = new StatSortButton(this, 3, ITEM_USED_SPRITE);
                this.itemPickedUp = new StatSortButton(this, 4, ITEM_PICKED_UP_SPRITE);
                this.itemDropped = new StatSortButton(this, 5, ITEM_DROPPED_SPRITE);
                this.children.addAll(List.of((Object)this.blockMined, (Object)this.itemBroken, (Object)this.itemCrafted, (Object)this.itemUsed, (Object)this.itemPickedUp, (Object)this.itemDropped));
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
                this.blockMined.setPosition(this.getContentX() + ItemStatisticsList.this.getColumnX(0) - 18, this.getContentY() + 1);
                this.blockMined.render(guiGraphics, i, j, f);
                this.itemBroken.setPosition(this.getContentX() + ItemStatisticsList.this.getColumnX(1) - 18, this.getContentY() + 1);
                this.itemBroken.render(guiGraphics, i, j, f);
                this.itemCrafted.setPosition(this.getContentX() + ItemStatisticsList.this.getColumnX(2) - 18, this.getContentY() + 1);
                this.itemCrafted.render(guiGraphics, i, j, f);
                this.itemUsed.setPosition(this.getContentX() + ItemStatisticsList.this.getColumnX(3) - 18, this.getContentY() + 1);
                this.itemUsed.render(guiGraphics, i, j, f);
                this.itemPickedUp.setPosition(this.getContentX() + ItemStatisticsList.this.getColumnX(4) - 18, this.getContentY() + 1);
                this.itemPickedUp.render(guiGraphics, i, j, f);
                this.itemDropped.setPosition(this.getContentX() + ItemStatisticsList.this.getColumnX(5) - 18, this.getContentY() + 1);
                this.itemDropped.render(guiGraphics, i, j, f);
                if (ItemStatisticsList.this.sortColumn != null) {
                    int k = ItemStatisticsList.this.getColumnX(ItemStatisticsList.this.getColumnIndex(ItemStatisticsList.this.sortColumn)) - 36;
                    Identifier identifier = ItemStatisticsList.this.sortOrder == 1 ? SORT_UP_SPRITE : SORT_DOWN_SPRITE;
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getContentX() + k, this.getContentY() + 1, 18, 18);
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return this.children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return this.children;
            }

            @Environment(value=EnvType.CLIENT)
            class StatSortButton
            extends ImageButton {
                private final Identifier sprite;

                StatSortButton(HeaderEntry headerEntry, int i, Identifier identifier) {
                    super(18, 18, new WidgetSprites(HEADER_SPRITE, SLOT_SPRITE), button -> headerEntry.ItemStatisticsList.this.sortByColumn(headerEntry.ItemStatisticsList.this.getColumn(i)), headerEntry.ItemStatisticsList.this.getColumn(i).getDisplayName());
                    this.sprite = identifier;
                    this.setTooltip(Tooltip.create(this.getMessage()));
                }

                @Override
                public void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
                    Identifier identifier = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), this.width, this.height);
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, this.getX(), this.getY(), this.width, this.height);
                }
            }
        }

        @Environment(value=EnvType.CLIENT)
        class ItemRow
        extends Entry {
            private final Item item;
            private final ItemRowWidget itemRowWidget;

            ItemRow(Item item) {
                this.item = item;
                this.itemRowWidget = new ItemRowWidget(item.getDefaultInstance());
            }

            protected Item getItem() {
                return this.item;
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
                int l;
                this.itemRowWidget.setPosition(this.getContentX(), this.getContentY());
                this.itemRowWidget.render(guiGraphics, i, j, f);
                ItemStatisticsList itemStatisticsList = ItemStatisticsList.this;
                int k = itemStatisticsList.children().indexOf(this);
                for (l = 0; l < itemStatisticsList.blockColumns.size(); ++l) {
                    Stat<Block> stat;
                    Item item = this.item;
                    if (item instanceof BlockItem) {
                        BlockItem blockItem = (BlockItem)item;
                        stat = itemStatisticsList.blockColumns.get(l).get(blockItem.getBlock());
                    } else {
                        stat = null;
                    }
                    this.renderStat(guiGraphics, stat, this.getContentX() + ItemStatisticsList.this.getColumnX(l), this.getContentYMiddle() - ((StatsScreen)StatsScreen.this).font.lineHeight / 2, k % 2 == 0);
                }
                for (l = 0; l < itemStatisticsList.itemColumns.size(); ++l) {
                    this.renderStat(guiGraphics, itemStatisticsList.itemColumns.get(l).get(this.item), this.getContentX() + ItemStatisticsList.this.getColumnX(l + itemStatisticsList.blockColumns.size()), this.getContentYMiddle() - ((StatsScreen)StatsScreen.this).font.lineHeight / 2, k % 2 == 0);
                }
            }

            protected void renderStat(GuiGraphics guiGraphics, @Nullable Stat<?> stat, int i, int j, boolean bl) {
                Component component = stat == null ? NO_VALUE_DISPLAY : Component.literal(stat.format(StatsScreen.this.stats.getValue(stat)));
                guiGraphics.drawString(StatsScreen.this.font, component, i - StatsScreen.this.font.width(component), j, bl ? -1 : -4539718);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of((Object)this.itemRowWidget);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of((Object)this.itemRowWidget);
            }

            @Environment(value=EnvType.CLIENT)
            class ItemRowWidget
            extends ItemDisplayWidget {
                ItemRowWidget(ItemStack itemStack) {
                    super(ItemStatisticsList.this.minecraft, 1, 1, 18, 18, itemStack.getHoverName(), itemStack, false, true);
                }

                @Override
                protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, ItemRow.this.getContentX(), ItemRow.this.getContentY(), 18, 18);
                    super.renderWidget(guiGraphics, i, j, f);
                }

                @Override
                protected void renderTooltip(GuiGraphics guiGraphics, int i, int j) {
                    super.renderTooltip(guiGraphics, ItemRow.this.getContentX() + 18, ItemRow.this.getContentY() + 18);
                }
            }
        }

        @Environment(value=EnvType.CLIENT)
        static abstract class Entry
        extends ContainerObjectSelectionList.Entry<Entry> {
            Entry() {
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class MobsStatisticsList
    extends ObjectSelectionList<MobRow> {
        public MobsStatisticsList(Minecraft minecraft) {
            super(minecraft, StatsScreen.this.width, StatsScreen.this.layout.getContentHeight(), 33, ((StatsScreen)StatsScreen.this).font.lineHeight * 4);
            for (EntityType entityType : BuiltInRegistries.ENTITY_TYPE) {
                if (StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType)) <= 0 && StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType)) <= 0) continue;
                this.addEntry(new MobRow(entityType));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Override
        protected void renderListBackground(GuiGraphics guiGraphics) {
        }

        @Override
        protected void renderListSeparators(GuiGraphics guiGraphics) {
        }

        @Environment(value=EnvType.CLIENT)
        class MobRow
        extends ObjectSelectionList.Entry<MobRow> {
            private final Component mobName;
            private final Component kills;
            private final Component killedBy;
            private final boolean hasKills;
            private final boolean wasKilledBy;

            public MobRow(EntityType<?> entityType) {
                this.mobName = entityType.getDescription();
                int i = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType));
                if (i == 0) {
                    this.kills = Component.translatable("stat_type.minecraft.killed.none", this.mobName);
                    this.hasKills = false;
                } else {
                    this.kills = Component.translatable("stat_type.minecraft.killed", i, this.mobName);
                    this.hasKills = true;
                }
                int j = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType));
                if (j == 0) {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by.none", this.mobName);
                    this.wasKilledBy = false;
                } else {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by", this.mobName, j);
                    this.wasKilledBy = true;
                }
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
                guiGraphics.drawString(StatsScreen.this.font, this.mobName, this.getContentX() + 2, this.getContentY() + 1, -1);
                guiGraphics.drawString(StatsScreen.this.font, this.kills, this.getContentX() + 2 + 10, this.getContentY() + 1 + ((StatsScreen)StatsScreen.this).font.lineHeight, this.hasKills ? -4539718 : -8355712);
                guiGraphics.drawString(StatsScreen.this.font, this.killedBy, this.getContentX() + 2 + 10, this.getContentY() + 1 + ((StatsScreen)StatsScreen.this).font.lineHeight * 2, this.wasKilledBy ? -4539718 : -8355712);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.kills, this.killedBy));
            }
        }
    }
}


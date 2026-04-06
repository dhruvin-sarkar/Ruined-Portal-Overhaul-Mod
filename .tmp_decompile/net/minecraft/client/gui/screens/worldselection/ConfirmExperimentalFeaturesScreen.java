/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.booleans.BooleanConsumer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.worldselection;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.flag.FeatureFlags;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ConfirmExperimentalFeaturesScreen
extends Screen {
    private static final Component TITLE = Component.translatable("selectWorld.experimental.title");
    private static final Component MESSAGE = Component.translatable("selectWorld.experimental.message");
    private static final Component DETAILS_BUTTON = Component.translatable("selectWorld.experimental.details");
    private static final int COLUMN_SPACING = 10;
    private static final int DETAILS_BUTTON_WIDTH = 100;
    private final BooleanConsumer callback;
    final Collection<Pack> enabledPacks;
    private final GridLayout layout = new GridLayout().columnSpacing(10).rowSpacing(20);

    public ConfirmExperimentalFeaturesScreen(Collection<Pack> collection, BooleanConsumer booleanConsumer) {
        super(TITLE);
        this.enabledPacks = collection;
        this.callback = booleanConsumer;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE);
    }

    @Override
    protected void init() {
        super.init();
        GridLayout.RowHelper rowHelper = this.layout.createRowHelper(2);
        LayoutSettings layoutSettings = rowHelper.newCellSettings().alignHorizontallyCenter();
        rowHelper.addChild(new StringWidget(this.title, this.font), 2, layoutSettings);
        MultiLineTextWidget multiLineTextWidget = rowHelper.addChild(new MultiLineTextWidget(MESSAGE, this.font).setCentered(true), 2, layoutSettings);
        multiLineTextWidget.setMaxWidth(310);
        rowHelper.addChild(Button.builder(DETAILS_BUTTON, button -> this.minecraft.setScreen(new DetailsScreen())).width(100).build(), 2, layoutSettings);
        rowHelper.addChild(Button.builder(CommonComponents.GUI_PROCEED, button -> this.callback.accept(true)).build());
        rowHelper.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.callback.accept(false)).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.layout.arrangeElements();
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        FrameLayout.alignInRectangle(this.layout, 0, 0, this.width, this.height, 0.5f, 0.5f);
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Environment(value=EnvType.CLIENT)
    class DetailsScreen
    extends Screen {
        private static final Component TITLE = Component.translatable("selectWorld.experimental.details.title");
        final HeaderAndFooterLayout layout;
        private @Nullable PackList list;

        DetailsScreen() {
            super(TITLE);
            this.layout = new HeaderAndFooterLayout(this);
        }

        @Override
        protected void init() {
            this.layout.addTitleHeader(TITLE, this.font);
            this.list = this.layout.addToContents(new PackList(this, this.minecraft, ConfirmExperimentalFeaturesScreen.this.enabledPacks));
            this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).build());
            this.layout.visitWidgets(guiEventListener -> {
                AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
            });
            this.repositionElements();
        }

        @Override
        protected void repositionElements() {
            if (this.list != null) {
                this.list.updateSize(this.width, this.layout);
            }
            this.layout.arrangeElements();
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(ConfirmExperimentalFeaturesScreen.this);
        }

        @Environment(value=EnvType.CLIENT)
        class PackList
        extends ObjectSelectionList<PackListEntry> {
            public PackList(DetailsScreen detailsScreen, Minecraft minecraft, Collection<Pack> collection) {
                super(minecraft, detailsScreen.width, detailsScreen.layout.getContentHeight(), detailsScreen.layout.getHeaderHeight(), (minecraft.font.lineHeight + 2) * 3);
                for (Pack pack : collection) {
                    String string = FeatureFlags.printMissingFlags(FeatureFlags.VANILLA_SET, pack.getRequestedFeatures());
                    if (string.isEmpty()) continue;
                    Component component = ComponentUtils.mergeStyles(pack.getTitle(), Style.EMPTY.withBold(true));
                    MutableComponent component2 = Component.translatable("selectWorld.experimental.details.entry", string);
                    this.addEntry(detailsScreen.new PackListEntry(component, component2, MultiLineLabel.create(detailsScreen.font, (Component)component2, this.getRowWidth())));
                }
            }

            @Override
            public int getRowWidth() {
                return this.width * 3 / 4;
            }
        }

        @Environment(value=EnvType.CLIENT)
        class PackListEntry
        extends ObjectSelectionList.Entry<PackListEntry> {
            private final Component packId;
            private final Component message;
            private final MultiLineLabel splitMessage;

            PackListEntry(Component component, Component component2, MultiLineLabel multiLineLabel) {
                this.packId = component;
                this.message = component2;
                this.splitMessage = multiLineLabel;
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
                ActiveTextCollector activeTextCollector = guiGraphics.textRenderer();
                guiGraphics.drawString(((DetailsScreen)DetailsScreen.this).minecraft.font, this.packId, this.getContentX(), this.getContentY(), -1);
                this.splitMessage.visitLines(TextAlignment.LEFT, this.getContentX(), this.getContentY() + 12, ((DetailsScreen)DetailsScreen.this).font.lineHeight, activeTextCollector);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.packId, this.message));
            }
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  java.lang.MatchException
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  java.lang.runtime.SwitchBootstraps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.net.URI;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.ScreenNarrationCollector;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class Screen
extends AbstractContainerEventHandler
implements Renderable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component USAGE_NARRATION = Component.translatable("narrator.screen.usage");
    public static final Identifier MENU_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_background.png");
    public static final Identifier HEADER_SEPARATOR = Identifier.withDefaultNamespace("textures/gui/header_separator.png");
    public static final Identifier FOOTER_SEPARATOR = Identifier.withDefaultNamespace("textures/gui/footer_separator.png");
    private static final Identifier INWORLD_MENU_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_background.png");
    public static final Identifier INWORLD_HEADER_SEPARATOR = Identifier.withDefaultNamespace("textures/gui/inworld_header_separator.png");
    public static final Identifier INWORLD_FOOTER_SEPARATOR = Identifier.withDefaultNamespace("textures/gui/inworld_footer_separator.png");
    protected static final float FADE_IN_TIME = 2000.0f;
    protected final Component title;
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final List<NarratableEntry> narratables = Lists.newArrayList();
    protected final Minecraft minecraft;
    private boolean initialized;
    public int width;
    public int height;
    private final List<Renderable> renderables = Lists.newArrayList();
    protected final Font font;
    private static final long NARRATE_SUPPRESS_AFTER_INIT_TIME;
    private static final long NARRATE_DELAY_NARRATOR_ENABLED;
    private static final long NARRATE_DELAY_MOUSE_MOVE = 750L;
    private static final long NARRATE_DELAY_MOUSE_ACTION = 200L;
    private static final long NARRATE_DELAY_KEYBOARD_ACTION = 200L;
    private final ScreenNarrationCollector narrationState = new ScreenNarrationCollector();
    private long narrationSuppressTime = Long.MIN_VALUE;
    private long nextNarrationTime = Long.MAX_VALUE;
    protected @Nullable CycleButton<NarratorStatus> narratorButton;
    private @Nullable NarratableEntry lastNarratable;
    protected final Executor screenExecutor;

    protected Screen(Component component) {
        this(Minecraft.getInstance(), Minecraft.getInstance().font, component);
    }

    protected Screen(Minecraft minecraft, Font font, Component component) {
        this.minecraft = minecraft;
        this.font = font;
        this.title = component;
        this.screenExecutor = runnable -> minecraft.execute(() -> {
            if (minecraft.screen == this) {
                runnable.run();
            }
        });
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getNarrationMessage() {
        return this.getTitle();
    }

    public final void renderWithTooltipAndSubtitles(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.nextStratum();
        this.renderBackground(guiGraphics, i, j, f);
        guiGraphics.nextStratum();
        this.render(guiGraphics, i, j, f);
        guiGraphics.renderDeferredElements();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, i, j, f);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        FocusNavigationEvent.ArrowNavigation focusNavigationEvent;
        if (keyEvent.isEscape() && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }
        if (super.keyPressed(keyEvent)) {
            return true;
        }
        switch (keyEvent.key()) {
            case 263: {
                Record record = this.createArrowEvent(ScreenDirection.LEFT);
                break;
            }
            case 262: {
                Record record = this.createArrowEvent(ScreenDirection.RIGHT);
                break;
            }
            case 265: {
                Record record = this.createArrowEvent(ScreenDirection.UP);
                break;
            }
            case 264: {
                Record record = this.createArrowEvent(ScreenDirection.DOWN);
                break;
            }
            case 258: {
                Record record = this.createTabEvent(!keyEvent.hasShiftDown());
                break;
            }
            default: {
                Record record = focusNavigationEvent = null;
            }
        }
        if (focusNavigationEvent != null) {
            ComponentPath componentPath = super.nextFocusPath(focusNavigationEvent);
            if (componentPath == null && focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation) {
                this.clearFocus();
                componentPath = super.nextFocusPath(focusNavigationEvent);
            }
            if (componentPath != null) {
                this.changeFocus(componentPath);
            }
        }
        return false;
    }

    private FocusNavigationEvent.TabNavigation createTabEvent(boolean bl) {
        return new FocusNavigationEvent.TabNavigation(bl);
    }

    private FocusNavigationEvent.ArrowNavigation createArrowEvent(ScreenDirection screenDirection) {
        return new FocusNavigationEvent.ArrowNavigation(screenDirection);
    }

    protected void setInitialFocus() {
        FocusNavigationEvent.TabNavigation tabNavigation;
        ComponentPath componentPath;
        if (this.minecraft.getLastInputType().isKeyboard() && (componentPath = super.nextFocusPath(tabNavigation = new FocusNavigationEvent.TabNavigation(true))) != null) {
            this.changeFocus(componentPath);
        }
    }

    protected void setInitialFocus(GuiEventListener guiEventListener) {
        ComponentPath componentPath = ComponentPath.path(this, guiEventListener.nextFocusPath(new FocusNavigationEvent.InitialFocus()));
        if (componentPath != null) {
            this.changeFocus(componentPath);
        }
    }

    public void clearFocus() {
        ComponentPath componentPath = this.getCurrentFocusPath();
        if (componentPath != null) {
            componentPath.applyFocus(false);
        }
    }

    @VisibleForTesting
    protected void changeFocus(ComponentPath componentPath) {
        this.clearFocus();
        componentPath.applyFocus(true);
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public void onClose() {
        this.minecraft.setScreen(null);
    }

    protected <T extends GuiEventListener & Renderable> T addRenderableWidget(T guiEventListener) {
        this.renderables.add(guiEventListener);
        return this.addWidget(guiEventListener);
    }

    protected <T extends Renderable> T addRenderableOnly(T renderable) {
        this.renderables.add(renderable);
        return renderable;
    }

    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T guiEventListener) {
        this.children.add(guiEventListener);
        this.narratables.add(guiEventListener);
        return guiEventListener;
    }

    protected void removeWidget(GuiEventListener guiEventListener) {
        if (guiEventListener instanceof Renderable) {
            this.renderables.remove((Renderable)((Object)guiEventListener));
        }
        if (guiEventListener instanceof NarratableEntry) {
            this.narratables.remove((NarratableEntry)((Object)guiEventListener));
        }
        if (this.getFocused() == guiEventListener) {
            this.clearFocus();
        }
        this.children.remove(guiEventListener);
    }

    protected void clearWidgets() {
        this.renderables.clear();
        this.children.clear();
        this.narratables.clear();
    }

    public static List<Component> getTooltipFromItem(Minecraft minecraft, ItemStack itemStack) {
        return itemStack.getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    protected void insertText(String string, boolean bl) {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected static void defaultHandleGameClickEvent(ClickEvent clickEvent, Minecraft minecraft, @Nullable Screen screen) {
        LocalPlayer localPlayer = Objects.requireNonNull(minecraft.player, "Player not available");
        ClickEvent clickEvent2 = clickEvent;
        Objects.requireNonNull(clickEvent2);
        ClickEvent clickEvent3 = clickEvent2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClickEvent.RunCommand.class, ClickEvent.ShowDialog.class, ClickEvent.Custom.class}, (Object)clickEvent3, (int)n)) {
            case 0: {
                String string2;
                ClickEvent.RunCommand runCommand = (ClickEvent.RunCommand)clickEvent3;
                try {
                    String string;
                    string2 = string = runCommand.command();
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
                Screen.clickCommandAction(localPlayer, string2, screen);
                return;
            }
            case 1: {
                ClickEvent.ShowDialog showDialog = (ClickEvent.ShowDialog)clickEvent3;
                localPlayer.connection.showDialog(showDialog.dialog(), screen);
                return;
            }
            case 2: {
                ClickEvent.Custom custom = (ClickEvent.Custom)clickEvent3;
                localPlayer.connection.send(new ServerboundCustomClickActionPacket(custom.id(), custom.payload()));
                if (minecraft.screen == screen) return;
                minecraft.setScreen(screen);
                return;
            }
        }
        Screen.defaultHandleClickEvent(clickEvent, minecraft, screen);
    }

    /*
     * Loose catch block
     */
    protected static void defaultHandleClickEvent(ClickEvent clickEvent, Minecraft minecraft, @Nullable Screen screen) {
        block12: {
            boolean bl2;
            ClickEvent clickEvent2 = clickEvent;
            Objects.requireNonNull(clickEvent2);
            ClickEvent clickEvent3 = clickEvent2;
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClickEvent.OpenUrl.class, ClickEvent.OpenFile.class, ClickEvent.SuggestCommand.class, ClickEvent.CopyToClipboard.class}, (Object)clickEvent3, (int)n)) {
                case 0: {
                    URI uRI;
                    ClickEvent.OpenUrl openUrl = (ClickEvent.OpenUrl)clickEvent3;
                    URI uRI2 = uRI = openUrl.uri();
                    Screen.clickUrlAction(minecraft, screen, uRI2);
                    boolean bl2 = false;
                    break;
                }
                case 1: {
                    ClickEvent.OpenFile openFile = (ClickEvent.OpenFile)clickEvent3;
                    Util.getPlatform().openFile(openFile.file());
                    boolean bl2 = true;
                    break;
                }
                case 2: {
                    Object object;
                    ClickEvent.SuggestCommand suggestCommand = (ClickEvent.SuggestCommand)clickEvent3;
                    Object string = object = suggestCommand.command();
                    if (screen != null) {
                        screen.insertText((String)string, true);
                    }
                    boolean bl2 = true;
                    break;
                }
                case 3: {
                    String string;
                    Object object = (ClickEvent.CopyToClipboard)clickEvent3;
                    String string2 = string = ((ClickEvent.CopyToClipboard)object).value();
                    minecraft.keyboardHandler.setClipboard(string2);
                    boolean bl2 = true;
                    break;
                }
                default: {
                    LOGGER.error("Don't know how to handle {}", (Object)clickEvent);
                    boolean bl2 = bl2 = true;
                }
            }
            if (bl2 && minecraft.screen != screen) {
                minecraft.setScreen(screen);
            }
            break block12;
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
        }
    }

    protected static boolean clickUrlAction(Minecraft minecraft, @Nullable Screen screen, URI uRI) {
        if (!minecraft.options.chatLinks().get().booleanValue()) {
            return false;
        }
        if (minecraft.options.chatLinksPrompt().get().booleanValue()) {
            minecraft.setScreen(new ConfirmLinkScreen(bl -> {
                if (bl) {
                    Util.getPlatform().openUri(uRI);
                }
                minecraft.setScreen(screen);
            }, uRI.toString(), false));
        } else {
            Util.getPlatform().openUri(uRI);
        }
        return true;
    }

    protected static void clickCommandAction(LocalPlayer localPlayer, String string, @Nullable Screen screen) {
        localPlayer.connection.sendUnattendedCommand(Commands.trimOptionalPrefix(string), screen);
    }

    public final void init(int i, int j) {
        this.width = i;
        this.height = j;
        if (!this.initialized) {
            this.init();
            this.setInitialFocus();
        } else {
            this.repositionElements();
        }
        this.initialized = true;
        this.triggerImmediateNarration(false);
        if (this.minecraft.getLastInputType().isKeyboard()) {
            this.setNarrationSuppressTime(Long.MAX_VALUE);
        } else {
            this.suppressNarration(NARRATE_SUPPRESS_AFTER_INIT_TIME);
        }
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        this.clearFocus();
        this.init();
        this.setInitialFocus();
    }

    protected void fadeWidgets(float f) {
        for (GuiEventListener guiEventListener : this.children()) {
            if (!(guiEventListener instanceof AbstractWidget)) continue;
            AbstractWidget abstractWidget = (AbstractWidget)guiEventListener;
            abstractWidget.setAlpha(f);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    protected void init() {
    }

    public void tick() {
    }

    public void removed() {
    }

    public void added() {
    }

    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        if (this.isInGameUi()) {
            this.renderTransparentBackground(guiGraphics);
        } else {
            if (this.minecraft.level == null) {
                this.renderPanorama(guiGraphics, f);
            }
            this.renderBlurredBackground(guiGraphics);
            this.renderMenuBackground(guiGraphics);
        }
        this.minecraft.gui.renderDeferredSubtitles();
    }

    protected void renderBlurredBackground(GuiGraphics guiGraphics) {
        float f = this.minecraft.options.getMenuBackgroundBlurriness();
        if (f >= 1.0f) {
            guiGraphics.blurBeforeThisStratum();
        }
    }

    protected void renderPanorama(GuiGraphics guiGraphics, float f) {
        this.minecraft.gameRenderer.getPanorama().render(guiGraphics, this.width, this.height, this.panoramaShouldSpin());
    }

    protected void renderMenuBackground(GuiGraphics guiGraphics) {
        this.renderMenuBackground(guiGraphics, 0, 0, this.width, this.height);
    }

    protected void renderMenuBackground(GuiGraphics guiGraphics, int i, int j, int k, int l) {
        Screen.renderMenuBackgroundTexture(guiGraphics, this.minecraft.level == null ? MENU_BACKGROUND : INWORLD_MENU_BACKGROUND, i, j, 0.0f, 0.0f, k, l);
    }

    public static void renderMenuBackgroundTexture(GuiGraphics guiGraphics, Identifier identifier, int i, int j, float f, float g, int k, int l) {
        int m = 32;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, identifier, i, j, f, g, k, l, 32, 32);
    }

    public void renderTransparentBackground(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
    }

    public boolean isPauseScreen() {
        return true;
    }

    public boolean isInGameUi() {
        return false;
    }

    protected boolean panoramaShouldSpin() {
        return true;
    }

    public boolean isAllowedInPortal() {
        return this.isPauseScreen();
    }

    protected void repositionElements() {
        this.rebuildWidgets();
    }

    public void resize(int i, int j) {
        this.width = i;
        this.height = j;
        this.repositionElements();
    }

    public void fillCrashDetails(CrashReport crashReport) {
        CrashReportCategory crashReportCategory = crashReport.addCategory("Affected screen", 1);
        crashReportCategory.setDetail("Screen name", () -> this.getClass().getCanonicalName());
    }

    protected boolean isValidCharacterForName(String string, int i, int j) {
        int k = string.indexOf(58);
        int l = string.indexOf(47);
        if (i == 58) {
            return (l == -1 || j <= l) && k == -1;
        }
        if (i == 47) {
            return j > k;
        }
        return i == 95 || i == 45 || i >= 97 && i <= 122 || i >= 48 && i <= 57 || i == 46;
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return true;
    }

    public void onFilesDrop(List<Path> list) {
    }

    private void scheduleNarration(long l, boolean bl) {
        this.nextNarrationTime = Util.getMillis() + l;
        if (bl) {
            this.narrationSuppressTime = Long.MIN_VALUE;
        }
    }

    private void suppressNarration(long l) {
        this.setNarrationSuppressTime(Util.getMillis() + l);
    }

    private void setNarrationSuppressTime(long l) {
        this.narrationSuppressTime = l;
    }

    public void afterMouseMove() {
        this.scheduleNarration(750L, false);
    }

    public void afterMouseAction() {
        this.scheduleNarration(200L, true);
    }

    public void afterKeyboardAction() {
        this.scheduleNarration(200L, true);
    }

    private boolean shouldRunNarration() {
        return SharedConstants.DEBUG_UI_NARRATION || this.minecraft.getNarrator().isActive();
    }

    public void handleDelayedNarration() {
        long l;
        if (this.shouldRunNarration() && (l = Util.getMillis()) > this.nextNarrationTime && l > this.narrationSuppressTime) {
            this.runNarration(true);
            this.nextNarrationTime = Long.MAX_VALUE;
        }
    }

    public void triggerImmediateNarration(boolean bl) {
        if (this.shouldRunNarration()) {
            this.runNarration(bl);
        }
    }

    private void runNarration(boolean bl) {
        this.narrationState.update(this::updateNarrationState);
        String string = this.narrationState.collectNarrationText(!bl);
        if (!string.isEmpty()) {
            this.minecraft.getNarrator().saySystemNow(string);
        }
    }

    protected boolean shouldNarrateNavigation() {
        return true;
    }

    protected void updateNarrationState(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getNarrationMessage());
        if (this.shouldNarrateNavigation()) {
            narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }
        this.updateNarratedWidget(narrationElementOutput);
    }

    protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
        List list = this.narratables.stream().flatMap(narratableEntry -> narratableEntry.getNarratables().stream()).filter(NarratableEntry::isActive).sorted(Comparator.comparingInt(TabOrderedElement::getTabOrderGroup)).toList();
        NarratableSearchResult narratableSearchResult = Screen.findNarratableWidget(list, this.lastNarratable);
        if (narratableSearchResult != null) {
            if (narratableSearchResult.priority.isTerminal()) {
                this.lastNarratable = narratableSearchResult.entry;
            }
            if (list.size() > 1) {
                narrationElementOutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.screen", narratableSearchResult.index + 1, list.size()));
                if (narratableSearchResult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                    narrationElementOutput.add(NarratedElementType.USAGE, this.getUsageNarration());
                }
            }
            narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
        }
    }

    protected Component getUsageNarration() {
        return Component.translatable("narration.component_list.usage");
    }

    public static @Nullable NarratableSearchResult findNarratableWidget(List<? extends NarratableEntry> list, @Nullable NarratableEntry narratableEntry) {
        NarratableSearchResult narratableSearchResult = null;
        NarratableSearchResult narratableSearchResult2 = null;
        int j = list.size();
        for (int i = 0; i < j; ++i) {
            NarratableEntry narratableEntry2 = list.get(i);
            NarratableEntry.NarrationPriority narrationPriority = narratableEntry2.narrationPriority();
            if (narrationPriority.isTerminal()) {
                if (narratableEntry2 == narratableEntry) {
                    narratableSearchResult2 = new NarratableSearchResult(narratableEntry2, i, narrationPriority);
                    continue;
                }
                return new NarratableSearchResult(narratableEntry2, i, narrationPriority);
            }
            if (narrationPriority.compareTo(narratableSearchResult != null ? narratableSearchResult.priority : NarratableEntry.NarrationPriority.NONE) <= 0) continue;
            narratableSearchResult = new NarratableSearchResult(narratableEntry2, i, narrationPriority);
        }
        return narratableSearchResult != null ? narratableSearchResult : narratableSearchResult2;
    }

    public void updateNarratorStatus(boolean bl) {
        if (bl) {
            this.scheduleNarration(NARRATE_DELAY_NARRATOR_ENABLED, false);
        }
        if (this.narratorButton != null) {
            this.narratorButton.setValue(this.minecraft.options.narrator().get());
        }
    }

    public Font getFont() {
        return this.font;
    }

    public boolean showsActiveEffects() {
        return false;
    }

    public boolean canInterruptWithAnotherScreen() {
        return this.shouldCloseOnEsc();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(0, 0, this.width, this.height);
    }

    public @Nullable Music getBackgroundMusic() {
        return null;
    }

    static {
        NARRATE_DELAY_NARRATOR_ENABLED = NARRATE_SUPPRESS_AFTER_INIT_TIME = TimeUnit.SECONDS.toMillis(2L);
    }

    @Environment(value=EnvType.CLIENT)
    public static final class NarratableSearchResult
    extends Record {
        final NarratableEntry entry;
        final int index;
        final NarratableEntry.NarrationPriority priority;

        public NarratableSearchResult(NarratableEntry narratableEntry, int i, NarratableEntry.NarrationPriority narrationPriority) {
            this.entry = narratableEntry;
            this.index = i;
            this.priority = narrationPriority;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{NarratableSearchResult.class, "entry;index;priority", "entry", "index", "priority"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{NarratableSearchResult.class, "entry;index;priority", "entry", "index", "priority"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{NarratableSearchResult.class, "entry;index;priority", "entry", "index", "priority"}, this, object);
        }

        public NarratableEntry entry() {
            return this.entry;
        }

        public int index() {
            return this.index;
        }

        public NarratableEntry.NarrationPriority priority() {
            return this.priority;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.SelectableEntry;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldSelectionList
extends ObjectSelectionList<Entry> {
    public static final DateTimeFormatter DATE_FORMAT = Util.localizedDateFormatter(FormatStyle.SHORT);
    static final Identifier ERROR_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("world_list/error_highlighted");
    static final Identifier ERROR_SPRITE = Identifier.withDefaultNamespace("world_list/error");
    static final Identifier MARKED_JOIN_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("world_list/marked_join_highlighted");
    static final Identifier MARKED_JOIN_SPRITE = Identifier.withDefaultNamespace("world_list/marked_join");
    static final Identifier WARNING_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("world_list/warning_highlighted");
    static final Identifier WARNING_SPRITE = Identifier.withDefaultNamespace("world_list/warning");
    static final Identifier JOIN_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("world_list/join_highlighted");
    static final Identifier JOIN_SPRITE = Identifier.withDefaultNamespace("world_list/join");
    static final Logger LOGGER = LogUtils.getLogger();
    static final Component FROM_NEWER_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.fromNewerVersion1").withStyle(ChatFormatting.RED);
    static final Component FROM_NEWER_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.fromNewerVersion2").withStyle(ChatFormatting.RED);
    static final Component SNAPSHOT_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.snapshot1").withStyle(ChatFormatting.GOLD);
    static final Component SNAPSHOT_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.snapshot2").withStyle(ChatFormatting.GOLD);
    static final Component WORLD_LOCKED_TOOLTIP = Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
    static final Component WORLD_REQUIRES_CONVERSION = Component.translatable("selectWorld.conversion.tooltip").withStyle(ChatFormatting.RED);
    static final Component INCOMPATIBLE_VERSION_TOOLTIP = Component.translatable("selectWorld.incompatible.tooltip").withStyle(ChatFormatting.RED);
    static final Component WORLD_EXPERIMENTAL = Component.translatable("selectWorld.experimental");
    private final Screen screen;
    private CompletableFuture<List<LevelSummary>> pendingLevels;
    private @Nullable List<LevelSummary> currentlyDisplayedLevels;
    private final LoadingHeader loadingHeader;
    final EntryType entryType;
    private String filter;
    private boolean hasPolled;
    private final @Nullable Consumer<LevelSummary> onEntrySelect;
    final @Nullable Consumer<WorldListEntry> onEntryInteract;

    WorldSelectionList(Screen screen, Minecraft minecraft, int i, int j, String string, @Nullable WorldSelectionList worldSelectionList, @Nullable Consumer<LevelSummary> consumer, @Nullable Consumer<WorldListEntry> consumer2, EntryType entryType) {
        super(minecraft, i, j, 0, 36);
        this.screen = screen;
        this.loadingHeader = new LoadingHeader(minecraft);
        this.filter = string;
        this.onEntrySelect = consumer;
        this.onEntryInteract = consumer2;
        this.entryType = entryType;
        this.pendingLevels = worldSelectionList != null ? worldSelectionList.pendingLevels : this.loadLevels();
        this.addEntry(this.loadingHeader);
        this.handleNewLevels(this.pollLevelsIgnoreErrors());
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(Entry::close);
        super.clearEntries();
    }

    private @Nullable List<LevelSummary> pollLevelsIgnoreErrors() {
        try {
            List list = this.pendingLevels.getNow(null);
            if (this.entryType == EntryType.UPLOAD_WORLD) {
                if (list != null && !this.hasPolled) {
                    this.hasPolled = true;
                    list = list.stream().filter(LevelSummary::canUpload).toList();
                } else {
                    return null;
                }
            }
            return list;
        }
        catch (CancellationException | CompletionException runtimeException) {
            return null;
        }
    }

    public void reloadWorldList() {
        this.pendingLevels = this.loadLevels();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        List<LevelSummary> list = this.pollLevelsIgnoreErrors();
        if (list != this.currentlyDisplayedLevels) {
            this.handleNewLevels(list);
        }
        super.renderWidget(guiGraphics, i, j, f);
    }

    private void handleNewLevels(@Nullable List<LevelSummary> list) {
        if (list == null) {
            return;
        }
        if (list.isEmpty()) {
            switch (this.entryType.ordinal()) {
                case 0: {
                    CreateWorldScreen.openFresh(this.minecraft, () -> this.minecraft.setScreen(null));
                    break;
                }
                case 1: {
                    this.clearEntries();
                    this.addEntry(new NoWorldsEntry(Component.translatable("mco.upload.select.world.none"), this.screen.getFont()));
                }
            }
        } else {
            this.fillLevels(this.filter, list);
            this.currentlyDisplayedLevels = list;
        }
    }

    public void updateFilter(String string) {
        if (this.currentlyDisplayedLevels != null && !string.equals(this.filter)) {
            this.fillLevels(string, this.currentlyDisplayedLevels);
        }
        this.filter = string;
    }

    private CompletableFuture<List<LevelSummary>> loadLevels() {
        LevelStorageSource.LevelCandidates levelCandidates;
        try {
            levelCandidates = this.minecraft.getLevelSource().findLevelCandidates();
        }
        catch (LevelStorageException levelStorageException) {
            LOGGER.error("Couldn't load level list", (Throwable)levelStorageException);
            this.handleLevelLoadFailure(levelStorageException.getMessageComponent());
            return CompletableFuture.completedFuture(List.of());
        }
        return this.minecraft.getLevelSource().loadLevelSummaries(levelCandidates).exceptionally(throwable -> {
            this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Couldn't load level list"));
            return List.of();
        });
    }

    private void fillLevels(String string, List<LevelSummary> list) {
        ArrayList<WorldListEntry> list2 = new ArrayList<WorldListEntry>();
        Optional<WorldListEntry> optional = this.getSelectedOpt();
        WorldListEntry worldListEntry = null;
        for (LevelSummary levelSummary2 : list.stream().filter(levelSummary -> this.filterAccepts(string.toLowerCase(Locale.ROOT), (LevelSummary)levelSummary)).toList()) {
            WorldListEntry worldListEntry2 = new WorldListEntry(this, levelSummary2);
            if (optional.isPresent() && optional.get().getLevelSummary().getLevelId().equals(worldListEntry2.getLevelSummary().getLevelId())) {
                worldListEntry = worldListEntry2;
            }
            list2.add(worldListEntry2);
        }
        this.removeEntries(this.children().stream().filter(entry -> !list2.contains(entry)).toList());
        list2.forEach(entry -> {
            if (!this.children().contains(entry)) {
                this.addEntry(entry);
            }
        });
        this.setSelected(worldListEntry);
        this.notifyListUpdated();
    }

    private boolean filterAccepts(String string, LevelSummary levelSummary) {
        return levelSummary.getLevelName().toLowerCase(Locale.ROOT).contains(string) || levelSummary.getLevelId().toLowerCase(Locale.ROOT).contains(string);
    }

    private void notifyListUpdated() {
        this.refreshScrollAmount();
        this.screen.triggerImmediateNarration(true);
    }

    private void handleLevelLoadFailure(Component component) {
        this.minecraft.setScreen(new ErrorScreen(Component.translatable("selectWorld.unable_to_load"), component));
    }

    @Override
    public int getRowWidth() {
        return 270;
    }

    @Override
    public void setSelected(@Nullable Entry entry) {
        super.setSelected(entry);
        if (this.onEntrySelect != null) {
            LevelSummary levelSummary;
            if (entry instanceof WorldListEntry) {
                WorldListEntry worldListEntry = (WorldListEntry)entry;
                levelSummary = worldListEntry.summary;
            } else {
                levelSummary = null;
            }
            this.onEntrySelect.accept(levelSummary);
        }
    }

    public Optional<WorldListEntry> getSelectedOpt() {
        Entry entry = (Entry)this.getSelected();
        if (entry instanceof WorldListEntry) {
            WorldListEntry worldListEntry = (WorldListEntry)entry;
            return Optional.of(worldListEntry);
        }
        return Optional.empty();
    }

    public void returnToScreen() {
        this.reloadWorldList();
        this.minecraft.setScreen(this.screen);
    }

    public Screen getScreen() {
        return this.screen;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        if (this.children().contains(this.loadingHeader)) {
            this.loadingHeader.updateNarration(narrationElementOutput);
            return;
        }
        super.updateWidgetNarration(narrationElementOutput);
    }

    @Environment(value=EnvType.CLIENT)
    public static class LoadingHeader
    extends Entry {
        private static final Component LOADING_LABEL = Component.translatable("selectWorld.loading_list");
        private final Minecraft minecraft;

        public LoadingHeader(Minecraft minecraft) {
            this.minecraft = minecraft;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
            int k = (this.minecraft.screen.width - this.minecraft.font.width(LOADING_LABEL)) / 2;
            int l = this.getContentY() + (this.getContentHeight() - this.minecraft.font.lineHeight) / 2;
            guiGraphics.drawString(this.minecraft.font, LOADING_LABEL, k, l, -1);
            String string = LoadingDotsText.get(Util.getMillis());
            int m = (this.minecraft.screen.width - this.minecraft.font.width(string)) / 2;
            int n = l + this.minecraft.font.lineHeight;
            guiGraphics.drawString(this.minecraft.font, string, m, n, -8355712);
        }

        @Override
        public Component getNarration() {
            return LOADING_LABEL;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum EntryType {
        SINGLEPLAYER,
        UPLOAD_WORLD;

    }

    @Environment(value=EnvType.CLIENT)
    public static final class NoWorldsEntry
    extends Entry {
        private final StringWidget stringWidget;

        public NoWorldsEntry(Component component, Font font) {
            this.stringWidget = new StringWidget(component, font);
        }

        @Override
        public Component getNarration() {
            return this.stringWidget.getMessage();
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
            this.stringWidget.setPosition(this.getContentXMiddle() - this.stringWidget.getWidth() / 2, this.getContentYMiddle() - this.stringWidget.getHeight() / 2);
            this.stringWidget.render(guiGraphics, i, j, f);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public final class WorldListEntry
    extends Entry
    implements SelectableEntry {
        private static final int ICON_SIZE = 32;
        private final WorldSelectionList list;
        private final Minecraft minecraft;
        private final Screen screen;
        final LevelSummary summary;
        private final FaviconTexture icon;
        private final StringWidget worldNameText;
        private final StringWidget idAndLastPlayedText;
        private final StringWidget infoText;
        private @Nullable Path iconFile;

        public WorldListEntry(WorldSelectionList worldSelectionList2, LevelSummary levelSummary) {
            this.list = worldSelectionList2;
            this.minecraft = worldSelectionList2.minecraft;
            this.screen = worldSelectionList2.getScreen();
            this.summary = levelSummary;
            this.icon = FaviconTexture.forWorld(this.minecraft.getTextureManager(), levelSummary.getLevelId());
            this.iconFile = levelSummary.getIcon();
            int i = worldSelectionList2.getRowWidth() - this.getTextX() - 2;
            MutableComponent component = Component.literal(levelSummary.getLevelName());
            this.worldNameText = new StringWidget(component, this.minecraft.font);
            this.worldNameText.setMaxWidth(i);
            if (this.minecraft.font.width(component) > i) {
                this.worldNameText.setTooltip(Tooltip.create(component));
            }
            Object string = levelSummary.getLevelId();
            long l = levelSummary.getLastPlayed();
            if (l != -1L) {
                ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault());
                string = (String)string + " (" + DATE_FORMAT.format(zonedDateTime) + ")";
            }
            MutableComponent component2 = Component.literal((String)string).withColor(-8355712);
            this.idAndLastPlayedText = new StringWidget(component2, this.minecraft.font);
            this.idAndLastPlayedText.setMaxWidth(i);
            if (this.minecraft.font.width((String)string) > i) {
                this.idAndLastPlayedText.setTooltip(Tooltip.create(component2));
            }
            Component component3 = ComponentUtils.mergeStyles(levelSummary.getInfo(), Style.EMPTY.withColor(-8355712));
            this.infoText = new StringWidget(component3, this.minecraft.font);
            this.infoText.setMaxWidth(i);
            if (this.minecraft.font.width(component3) > i) {
                this.infoText.setTooltip(Tooltip.create(component3));
            }
            this.validateIconFile();
            this.loadIcon();
        }

        private void validateIconFile() {
            if (this.iconFile == null) {
                return;
            }
            try {
                BasicFileAttributes basicFileAttributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (basicFileAttributes.isSymbolicLink()) {
                    List<ForbiddenSymlinkInfo> list = this.minecraft.directoryValidator().validateSymlink(this.iconFile);
                    if (!list.isEmpty()) {
                        LOGGER.warn("{}", (Object)ContentValidationException.getMessage(this.iconFile, list));
                        this.iconFile = null;
                    } else {
                        basicFileAttributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class, new LinkOption[0]);
                    }
                }
                if (!basicFileAttributes.isRegularFile()) {
                    this.iconFile = null;
                }
            }
            catch (NoSuchFileException noSuchFileException) {
                this.iconFile = null;
            }
            catch (IOException iOException) {
                LOGGER.error("could not validate symlink", (Throwable)iOException);
                this.iconFile = null;
            }
        }

        @Override
        public Component getNarration() {
            MutableComponent component = Component.translatable("narrator.select.world_info", this.summary.getLevelName(), Component.translationArg(new Date(this.summary.getLastPlayed())), this.summary.getInfo());
            if (this.summary.isLocked()) {
                component = CommonComponents.joinForNarration(component, WORLD_LOCKED_TOOLTIP);
            }
            if (this.summary.isExperimental()) {
                component = CommonComponents.joinForNarration(component, WORLD_EXPERIMENTAL);
            }
            return Component.translatable("narrator.select", component);
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
            int k = this.getTextX();
            this.worldNameText.setPosition(k, this.getContentY() + 1);
            this.worldNameText.render(guiGraphics, i, j, f);
            this.idAndLastPlayedText.setPosition(k, this.getContentY() + this.minecraft.font.lineHeight + 3);
            this.idAndLastPlayedText.render(guiGraphics, i, j, f);
            this.infoText.setPosition(k, this.getContentY() + this.minecraft.font.lineHeight + this.minecraft.font.lineHeight + 3);
            this.infoText.render(guiGraphics, i, j, f);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.icon.textureLocation(), this.getContentX(), this.getContentY(), 0.0f, 0.0f, 32, 32, 32, 32);
            if (this.list.entryType == EntryType.SINGLEPLAYER && (this.minecraft.options.touchscreen().get().booleanValue() || bl)) {
                Identifier identifier4;
                guiGraphics.fill(this.getContentX(), this.getContentY(), this.getContentX() + 32, this.getContentY() + 32, -1601138544);
                int l = i - this.getContentX();
                int m = j - this.getContentY();
                boolean bl2 = this.mouseOverIcon(l, m, 32);
                Identifier identifier = bl2 ? JOIN_HIGHLIGHTED_SPRITE : JOIN_SPRITE;
                Identifier identifier2 = bl2 ? WARNING_HIGHLIGHTED_SPRITE : WARNING_SPRITE;
                Identifier identifier3 = bl2 ? ERROR_HIGHLIGHTED_SPRITE : ERROR_SPRITE;
                Identifier identifier5 = identifier4 = bl2 ? MARKED_JOIN_HIGHLIGHTED_SPRITE : MARKED_JOIN_SPRITE;
                if (this.summary instanceof LevelSummary.SymlinkLevelSummary || this.summary instanceof LevelSummary.CorruptedLevelSummary) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier3, this.getContentX(), this.getContentY(), 32, 32);
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier4, this.getContentX(), this.getContentY(), 32, 32);
                    return;
                }
                if (this.summary.isLocked()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier3, this.getContentX(), this.getContentY(), 32, 32);
                    if (bl2) {
                        guiGraphics.setTooltipForNextFrame(this.minecraft.font.split(WORLD_LOCKED_TOOLTIP, 175), i, j);
                    }
                } else if (this.summary.requiresManualConversion()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier3, this.getContentX(), this.getContentY(), 32, 32);
                    if (bl2) {
                        guiGraphics.setTooltipForNextFrame(this.minecraft.font.split(WORLD_REQUIRES_CONVERSION, 175), i, j);
                    }
                } else if (!this.summary.isCompatible()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier3, this.getContentX(), this.getContentY(), 32, 32);
                    if (bl2) {
                        guiGraphics.setTooltipForNextFrame(this.minecraft.font.split(INCOMPATIBLE_VERSION_TOOLTIP, 175), i, j);
                    }
                } else if (this.summary.shouldBackup()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier4, this.getContentX(), this.getContentY(), 32, 32);
                    if (this.summary.isDowngrade()) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier3, this.getContentX(), this.getContentY(), 32, 32);
                        if (bl2) {
                            guiGraphics.setTooltipForNextFrame((List<FormattedCharSequence>)ImmutableList.of((Object)FROM_NEWER_TOOLTIP_1.getVisualOrderText(), (Object)FROM_NEWER_TOOLTIP_2.getVisualOrderText()), i, j);
                        }
                    } else if (!SharedConstants.getCurrentVersion().stable()) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier2, this.getContentX(), this.getContentY(), 32, 32);
                        if (bl2) {
                            guiGraphics.setTooltipForNextFrame((List<FormattedCharSequence>)ImmutableList.of((Object)SNAPSHOT_TOOLTIP_1.getVisualOrderText(), (Object)SNAPSHOT_TOOLTIP_2.getVisualOrderText()), i, j);
                        }
                    }
                    if (bl2) {
                        WorldSelectionList.this.handleCursor(guiGraphics);
                    }
                } else {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getContentX(), this.getContentY(), 32, 32);
                    if (bl2) {
                        WorldSelectionList.this.handleCursor(guiGraphics);
                    }
                }
            }
        }

        private int getTextX() {
            return this.getContentX() + 32 + 3;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
            if (this.canInteract()) {
                int i = (int)mouseButtonEvent.x() - this.getContentX();
                int j = (int)mouseButtonEvent.y() - this.getContentY();
                if (bl || this.mouseOverIcon(i, j, 32) && this.list.entryType == EntryType.SINGLEPLAYER) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    Consumer<WorldListEntry> consumer = this.list.onEntryInteract;
                    if (consumer != null) {
                        consumer.accept(this);
                        return true;
                    }
                }
            }
            return super.mouseClicked(mouseButtonEvent, bl);
        }

        @Override
        public boolean keyPressed(KeyEvent keyEvent) {
            if (keyEvent.isSelection() && this.canInteract()) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                Consumer<WorldListEntry> consumer = this.list.onEntryInteract;
                if (consumer != null) {
                    consumer.accept(this);
                    return true;
                }
            }
            return super.keyPressed(keyEvent);
        }

        public boolean canInteract() {
            return this.summary.primaryActionActive() || this.list.entryType == EntryType.UPLOAD_WORLD;
        }

        public void joinWorld() {
            if (!this.summary.primaryActionActive()) {
                return;
            }
            if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
                this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
                return;
            }
            this.minecraft.createWorldOpenFlows().openWorld(this.summary.getLevelId(), this.list::returnToScreen);
        }

        public void deleteWorld() {
            this.minecraft.setScreen(new ConfirmScreen(bl -> {
                if (bl) {
                    this.minecraft.setScreen(new ProgressScreen(true));
                    this.doDeleteWorld();
                }
                this.list.returnToScreen();
            }, Component.translatable("selectWorld.deleteQuestion"), Component.translatable("selectWorld.deleteWarning", this.summary.getLevelName()), Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
        }

        public void doDeleteWorld() {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            String string = this.summary.getLevelId();
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string);){
                levelStorageAccess.deleteLevel();
            }
            catch (IOException iOException) {
                SystemToast.onWorldDeleteFailure(this.minecraft, string);
                LOGGER.error("Failed to delete world {}", (Object)string, (Object)iOException);
            }
        }

        public void editWorld() {
            EditWorldScreen editWorldScreen;
            LevelStorageSource.LevelStorageAccess levelStorageAccess;
            this.queueLoadScreen();
            String string = this.summary.getLevelId();
            try {
                levelStorageAccess = this.minecraft.getLevelSource().validateAndCreateAccess(string);
            }
            catch (IOException iOException) {
                SystemToast.onWorldAccessFailure(this.minecraft, string);
                LOGGER.error("Failed to access level {}", (Object)string, (Object)iOException);
                this.list.reloadWorldList();
                return;
            }
            catch (ContentValidationException contentValidationException) {
                LOGGER.warn("{}", (Object)contentValidationException.getMessage());
                this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
                return;
            }
            try {
                editWorldScreen = EditWorldScreen.create(this.minecraft, levelStorageAccess, bl -> {
                    levelStorageAccess.safeClose();
                    this.list.returnToScreen();
                });
            }
            catch (IOException | NbtException | ReportedNbtException exception) {
                levelStorageAccess.safeClose();
                SystemToast.onWorldAccessFailure(this.minecraft, string);
                LOGGER.error("Failed to load world data {}", (Object)string, (Object)exception);
                this.list.reloadWorldList();
                return;
            }
            this.minecraft.setScreen(editWorldScreen);
        }

        public void recreateWorld() {
            this.queueLoadScreen();
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().validateAndCreateAccess(this.summary.getLevelId());){
                Pair<LevelSettings, WorldCreationContext> pair = this.minecraft.createWorldOpenFlows().recreateWorldData(levelStorageAccess);
                LevelSettings levelSettings = (LevelSettings)pair.getFirst();
                WorldCreationContext worldCreationContext = (WorldCreationContext)((Object)pair.getSecond());
                Path path = CreateWorldScreen.createTempDataPackDirFromExistingWorld(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR), this.minecraft);
                worldCreationContext.validate();
                if (worldCreationContext.options().isOldCustomizedWorld()) {
                    this.minecraft.setScreen(new ConfirmScreen(bl -> this.minecraft.setScreen(bl ? CreateWorldScreen.createFromExisting(this.minecraft, this.list::returnToScreen, levelSettings, worldCreationContext, path) : this.screen), Component.translatable("selectWorld.recreate.customized.title"), Component.translatable("selectWorld.recreate.customized.text"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
                } else {
                    this.minecraft.setScreen(CreateWorldScreen.createFromExisting(this.minecraft, this.list::returnToScreen, levelSettings, worldCreationContext, path));
                }
            }
            catch (ContentValidationException contentValidationException) {
                LOGGER.warn("{}", (Object)contentValidationException.getMessage());
                this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
            }
            catch (Exception exception) {
                LOGGER.error("Unable to recreate world", (Throwable)exception);
                this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this.screen), Component.translatable("selectWorld.recreate.error.title"), (Component)Component.translatable("selectWorld.recreate.error.text")));
            }
        }

        private void queueLoadScreen() {
            this.minecraft.setScreenAndShow(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));
        }

        private void loadIcon() {
            boolean bl;
            boolean bl2 = bl = this.iconFile != null && Files.isRegularFile(this.iconFile, new LinkOption[0]);
            if (bl) {
                try (InputStream inputStream = Files.newInputStream(this.iconFile, new OpenOption[0]);){
                    this.icon.upload(NativeImage.read(inputStream));
                }
                catch (Throwable throwable) {
                    LOGGER.error("Invalid icon for world {}", (Object)this.summary.getLevelId(), (Object)throwable);
                    this.iconFile = null;
                }
            } else {
                this.icon.clear();
            }
        }

        @Override
        public void close() {
            if (!this.icon.isClosed()) {
                this.icon.close();
            }
        }

        public String getLevelName() {
            return this.summary.getLevelName();
        }

        @Override
        public LevelSummary getLevelSummary() {
            return this.summary;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry
    extends ObjectSelectionList.Entry<Entry>
    implements AutoCloseable {
        @Override
        public void close() {
        }

        public @Nullable LevelSummary getLevelSummary() {
            return null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Minecraft minecraft;
        private final Screen screen;
        private int width;
        private int height;
        private String filter = "";
        private EntryType type = EntryType.SINGLEPLAYER;
        private @Nullable WorldSelectionList oldList = null;
        private @Nullable Consumer<LevelSummary> onEntrySelect = null;
        private @Nullable Consumer<WorldListEntry> onEntryInteract = null;

        public Builder(Minecraft minecraft, Screen screen) {
            this.minecraft = minecraft;
            this.screen = screen;
        }

        public Builder width(int i) {
            this.width = i;
            return this;
        }

        public Builder height(int i) {
            this.height = i;
            return this;
        }

        public Builder filter(String string) {
            this.filter = string;
            return this;
        }

        public Builder oldList(@Nullable WorldSelectionList worldSelectionList) {
            this.oldList = worldSelectionList;
            return this;
        }

        public Builder onEntrySelect(Consumer<LevelSummary> consumer) {
            this.onEntrySelect = consumer;
            return this;
        }

        public Builder onEntryInteract(Consumer<WorldListEntry> consumer) {
            this.onEntryInteract = consumer;
            return this;
        }

        public Builder uploadWorld() {
            this.type = EntryType.UPLOAD_WORLD;
            return this;
        }

        public WorldSelectionList build() {
            return new WorldSelectionList(this.screen, this.minecraft, this.width, this.height, this.filter, this.oldList, this.onEntrySelect, this.onEntryInteract, this.type);
        }
    }
}


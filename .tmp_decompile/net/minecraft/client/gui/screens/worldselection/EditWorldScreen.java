/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.booleans.BooleanConsumer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.apache.commons.io.FileUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.worldselection;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FileUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class EditWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName").withStyle(ChatFormatting.GRAY);
    private static final Component RESET_ICON_BUTTON = Component.translatable("selectWorld.edit.resetIcon");
    private static final Component FOLDER_BUTTON = Component.translatable("selectWorld.edit.openFolder");
    private static final Component BACKUP_BUTTON = Component.translatable("selectWorld.edit.backup");
    private static final Component BACKUP_FOLDER_BUTTON = Component.translatable("selectWorld.edit.backupFolder");
    private static final Component OPTIMIZE_BUTTON = Component.translatable("selectWorld.edit.optimize");
    private static final Component OPTIMIZE_TITLE = Component.translatable("optimizeWorld.confirm.title");
    private static final Component OPTIMIIZE_DESCRIPTION = Component.translatable("optimizeWorld.confirm.description");
    private static final Component OPTIMIIZE_CONFIRMATION = Component.translatable("optimizeWorld.confirm.proceed");
    private static final Component SAVE_BUTTON = Component.translatable("selectWorld.edit.save");
    private static final int DEFAULT_WIDTH = 200;
    private static final int VERTICAL_SPACING = 4;
    private static final int HALF_WIDTH = 98;
    private final LinearLayout layout = LinearLayout.vertical().spacing(5);
    private final BooleanConsumer callback;
    private final LevelStorageSource.LevelStorageAccess levelAccess;
    private final EditBox nameEdit;

    public static EditWorldScreen create(Minecraft minecraft, LevelStorageSource.LevelStorageAccess levelStorageAccess, BooleanConsumer booleanConsumer) throws IOException {
        LevelSummary levelSummary = levelStorageAccess.getSummary(levelStorageAccess.getDataTag());
        return new EditWorldScreen(minecraft, levelStorageAccess, levelSummary.getLevelName(), booleanConsumer);
    }

    private EditWorldScreen(Minecraft minecraft, LevelStorageSource.LevelStorageAccess levelStorageAccess, String string2, BooleanConsumer booleanConsumer) {
        super(Component.translatable("selectWorld.edit.title"));
        this.callback = booleanConsumer;
        this.levelAccess = levelStorageAccess;
        Font font = minecraft.font;
        this.layout.addChild(new SpacerElement(200, 20));
        this.layout.addChild(new StringWidget(NAME_LABEL, font));
        this.nameEdit = this.layout.addChild(new EditBox(font, 200, 20, NAME_LABEL));
        this.nameEdit.setValue(string2);
        LinearLayout linearLayout = LinearLayout.horizontal().spacing(4);
        Button button2 = linearLayout.addChild(Button.builder(SAVE_BUTTON, button -> this.onRename(this.nameEdit.getValue())).width(98).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).width(98).build());
        this.nameEdit.setResponder(string -> {
            button.active = !StringUtil.isBlank(string);
        });
        this.layout.addChild(Button.builder((Component)EditWorldScreen.RESET_ICON_BUTTON, (Button.OnPress)(Button.OnPress)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/components/Button;)V, method_54608(net.minecraft.world.level.storage.LevelStorageSource$LevelStorageAccess net.minecraft.client.gui.components.Button ), (Lnet/minecraft/client/gui/components/Button;)V)((LevelStorageSource.LevelStorageAccess)levelStorageAccess)).width((int)200).build()).active = levelStorageAccess.getIconFile().filter(path -> Files.isRegularFile(path, new LinkOption[0])).isPresent();
        this.layout.addChild(Button.builder(FOLDER_BUTTON, button -> Util.getPlatform().openPath(levelStorageAccess.getLevelPath(LevelResource.ROOT))).width(200).build());
        this.layout.addChild(Button.builder(BACKUP_BUTTON, button -> {
            boolean bl = EditWorldScreen.makeBackupAndShowToast(levelStorageAccess);
            this.callback.accept(!bl);
        }).width(200).build());
        this.layout.addChild(Button.builder(BACKUP_FOLDER_BUTTON, button -> {
            LevelStorageSource levelStorageSource = minecraft.getLevelSource();
            Path path = levelStorageSource.getBackupPath();
            try {
                FileUtil.createDirectoriesSafe(path);
            }
            catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
            Util.getPlatform().openPath(path);
        }).width(200).build());
        this.layout.addChild(Button.builder(OPTIMIZE_BUTTON, button -> minecraft.setScreen(new BackupConfirmScreen(() -> minecraft.setScreen(this), (bl, bl2) -> {
            if (bl) {
                EditWorldScreen.makeBackupAndShowToast(levelStorageAccess);
            }
            minecraft.setScreen(OptimizeWorldScreen.create(minecraft, this.callback, minecraft.getFixerUpper(), levelStorageAccess, bl2));
        }, OPTIMIZE_TITLE, OPTIMIIZE_DESCRIPTION, OPTIMIIZE_CONFIRMATION, true))).width(200).build());
        this.layout.addChild(new SpacerElement(200, 20));
        this.layout.addChild(linearLayout);
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    protected void init() {
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (this.nameEdit.isFocused() && keyEvent.isConfirmation()) {
            this.onRename(this.nameEdit.getValue());
            this.onClose();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    private void onRename(String string) {
        try {
            this.levelAccess.renameLevel(string);
        }
        catch (IOException | NbtException | ReportedNbtException exception) {
            LOGGER.error("Failed to access world '{}'", (Object)this.levelAccess.getLevelId(), (Object)exception);
            SystemToast.onWorldAccessFailure(this.minecraft, this.levelAccess.getLevelId());
        }
        this.callback.accept(true);
    }

    public static boolean makeBackupAndShowToast(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        long l = 0L;
        IOException iOException = null;
        try {
            l = levelStorageAccess.makeWorldBackup();
        }
        catch (IOException iOException2) {
            iOException = iOException2;
        }
        if (iOException != null) {
            MutableComponent component = Component.translatable("selectWorld.edit.backupFailed");
            MutableComponent component2 = Component.literal(iOException.getMessage());
            Minecraft.getInstance().getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.WORLD_BACKUP, component, component2));
            return false;
        }
        MutableComponent component = Component.translatable("selectWorld.edit.backupCreated", levelStorageAccess.getLevelId());
        MutableComponent component2 = Component.translatable("selectWorld.edit.backupSize", Mth.ceil((double)l / 1048576.0));
        Minecraft.getInstance().getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.WORLD_BACKUP, component, component2));
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, -1);
    }

    private static /* synthetic */ void method_54608(LevelStorageSource.LevelStorageAccess levelStorageAccess, Button button) {
        levelStorageAccess.getIconFile().ifPresent(path -> FileUtils.deleteQuietly((File)path.toFile()));
        button.active = false;
    }
}


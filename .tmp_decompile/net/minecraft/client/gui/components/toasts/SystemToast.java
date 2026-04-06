/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SystemToast
implements Toast {
    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/system");
    private static final int MAX_LINE_SIZE = 200;
    private static final int LINE_SPACING = 12;
    private static final int MARGIN = 10;
    private final SystemToastId id;
    private Component title;
    private List<FormattedCharSequence> messageLines;
    private long lastChanged;
    private boolean changed;
    private final int width;
    private boolean forceHide;
    private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;

    public SystemToast(SystemToastId systemToastId, Component component, @Nullable Component component2) {
        this(systemToastId, component, (List<FormattedCharSequence>)SystemToast.nullToEmpty(component2), Math.max(160, 30 + Math.max(Minecraft.getInstance().font.width(component), component2 == null ? 0 : Minecraft.getInstance().font.width(component2))));
    }

    public static SystemToast multiline(Minecraft minecraft, SystemToastId systemToastId, Component component, Component component2) {
        Font font = minecraft.font;
        List<FormattedCharSequence> list = font.split(component2, 200);
        int i = Math.max(200, list.stream().mapToInt(font::width).max().orElse(200));
        return new SystemToast(systemToastId, component, list, i + 30);
    }

    private SystemToast(SystemToastId systemToastId, Component component, List<FormattedCharSequence> list, int i) {
        this.id = systemToastId;
        this.title = component;
        this.messageLines = list;
        this.width = i;
    }

    private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component component) {
        return component == null ? ImmutableList.of() : ImmutableList.of((Object)component.getVisualOrderText());
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return 20 + Math.max(this.messageLines.size(), 1) * 12;
    }

    public void forceHide() {
        this.forceHide = true;
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(ToastManager toastManager, long l) {
        if (this.changed) {
            this.lastChanged = l;
            this.changed = false;
        }
        double d = (double)this.id.displayTime * toastManager.getNotificationDisplayTimeMultiplier();
        long m = l - this.lastChanged;
        this.wantedVisibility = !this.forceHide && (double)m < d ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    @Override
    public void render(GuiGraphics guiGraphics, Font font, long l) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        if (this.messageLines.isEmpty()) {
            guiGraphics.drawString(font, this.title, 18, 12, -256, false);
        } else {
            guiGraphics.drawString(font, this.title, 18, 7, -256, false);
            for (int i = 0; i < this.messageLines.size(); ++i) {
                guiGraphics.drawString(font, this.messageLines.get(i), 18, 18 + i * 12, -1, false);
            }
        }
    }

    public void reset(Component component, @Nullable Component component2) {
        this.title = component;
        this.messageLines = SystemToast.nullToEmpty(component2);
        this.changed = true;
    }

    @Override
    public SystemToastId getToken() {
        return this.id;
    }

    public static void add(ToastManager toastManager, SystemToastId systemToastId, Component component, @Nullable Component component2) {
        toastManager.addToast(new SystemToast(systemToastId, component, component2));
    }

    public static void addOrUpdate(ToastManager toastManager, SystemToastId systemToastId, Component component, @Nullable Component component2) {
        SystemToast systemToast = toastManager.getToast(SystemToast.class, systemToastId);
        if (systemToast == null) {
            SystemToast.add(toastManager, systemToastId, component, component2);
        } else {
            systemToast.reset(component, component2);
        }
    }

    public static void forceHide(ToastManager toastManager, SystemToastId systemToastId) {
        SystemToast systemToast = toastManager.getToast(SystemToast.class, systemToastId);
        if (systemToast != null) {
            systemToast.forceHide();
        }
    }

    public static void onWorldAccessFailure(Minecraft minecraft, String string) {
        SystemToast.add(minecraft.getToastManager(), SystemToastId.WORLD_ACCESS_FAILURE, Component.translatable("selectWorld.access_failure"), Component.literal(string));
    }

    public static void onWorldDeleteFailure(Minecraft minecraft, String string) {
        SystemToast.add(minecraft.getToastManager(), SystemToastId.WORLD_ACCESS_FAILURE, Component.translatable("selectWorld.delete_failure"), Component.literal(string));
    }

    public static void onPackCopyFailure(Minecraft minecraft, String string) {
        SystemToast.add(minecraft.getToastManager(), SystemToastId.PACK_COPY_FAILURE, Component.translatable("pack.copyFailure"), Component.literal(string));
    }

    public static void onFileDropFailure(Minecraft minecraft, int i) {
        SystemToast.add(minecraft.getToastManager(), SystemToastId.FILE_DROP_FAILURE, Component.translatable("gui.fileDropFailure.title"), Component.translatable("gui.fileDropFailure.detail", i));
    }

    public static void onLowDiskSpace(Minecraft minecraft) {
        SystemToast.addOrUpdate(minecraft.getToastManager(), SystemToastId.LOW_DISK_SPACE, Component.translatable("chunk.toast.lowDiskSpace"), Component.translatable("chunk.toast.lowDiskSpace.description"));
    }

    public static void onChunkLoadFailure(Minecraft minecraft, ChunkPos chunkPos) {
        SystemToast.addOrUpdate(minecraft.getToastManager(), SystemToastId.CHUNK_LOAD_FAILURE, Component.translatable("chunk.toast.loadFailure", Component.translationArg(chunkPos)).withStyle(ChatFormatting.RED), Component.translatable("chunk.toast.checkLog"));
    }

    public static void onChunkSaveFailure(Minecraft minecraft, ChunkPos chunkPos) {
        SystemToast.addOrUpdate(minecraft.getToastManager(), SystemToastId.CHUNK_SAVE_FAILURE, Component.translatable("chunk.toast.saveFailure", Component.translationArg(chunkPos)).withStyle(ChatFormatting.RED), Component.translatable("chunk.toast.checkLog"));
    }

    @Override
    public /* synthetic */ Object getToken() {
        return this.getToken();
    }

    @Environment(value=EnvType.CLIENT)
    public static class SystemToastId {
        public static final SystemToastId NARRATOR_TOGGLE = new SystemToastId();
        public static final SystemToastId WORLD_BACKUP = new SystemToastId();
        public static final SystemToastId PACK_LOAD_FAILURE = new SystemToastId();
        public static final SystemToastId WORLD_ACCESS_FAILURE = new SystemToastId();
        public static final SystemToastId PACK_COPY_FAILURE = new SystemToastId();
        public static final SystemToastId FILE_DROP_FAILURE = new SystemToastId();
        public static final SystemToastId PERIODIC_NOTIFICATION = new SystemToastId();
        public static final SystemToastId LOW_DISK_SPACE = new SystemToastId(10000L);
        public static final SystemToastId CHUNK_LOAD_FAILURE = new SystemToastId();
        public static final SystemToastId CHUNK_SAVE_FAILURE = new SystemToastId();
        public static final SystemToastId UNSECURE_SERVER_WARNING = new SystemToastId(10000L);
        final long displayTime;

        public SystemToastId(long l) {
            this.displayTime = l;
        }

        public SystemToastId() {
            this(5000L);
        }
    }
}


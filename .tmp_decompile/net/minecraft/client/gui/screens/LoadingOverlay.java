/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class LoadingOverlay
extends Overlay {
    public static final Identifier MOJANG_STUDIOS_LOGO_LOCATION = Identifier.withDefaultNamespace("textures/gui/title/mojangstudios.png");
    private static final int LOGO_BACKGROUND_COLOR = ARGB.color(255, 239, 50, 61);
    private static final int LOGO_BACKGROUND_COLOR_DARK = ARGB.color(255, 0, 0, 0);
    private static final IntSupplier BRAND_BACKGROUND = () -> Minecraft.getInstance().options.darkMojangStudiosBackground().get() != false ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
    private static final int LOGO_SCALE = 240;
    private static final float LOGO_QUARTER_FLOAT = 60.0f;
    private static final int LOGO_QUARTER = 60;
    private static final int LOGO_HALF = 120;
    private static final float LOGO_OVERLAP = 0.0625f;
    private static final float SMOOTHING = 0.95f;
    public static final long FADE_OUT_TIME = 1000L;
    public static final long FADE_IN_TIME = 500L;
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final boolean fadeIn;
    private float currentProgress;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;

    public LoadingOverlay(Minecraft minecraft, ReloadInstance reloadInstance, Consumer<Optional<Throwable>> consumer, boolean bl) {
        this.minecraft = minecraft;
        this.reload = reloadInstance;
        this.onFinish = consumer;
        this.fadeIn = bl;
    }

    public static void registerTextures(TextureManager textureManager) {
        textureManager.registerAndLoad(MOJANG_STUDIOS_LOGO_LOCATION, new LogoTexture());
    }

    private static int replaceAlpha(int i, int j) {
        return i & 0xFFFFFF | j << 24;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        float o;
        int n;
        float h;
        int k = guiGraphics.guiWidth();
        int l = guiGraphics.guiHeight();
        long m = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = m;
        }
        float g = this.fadeOutStart > -1L ? (float)(m - this.fadeOutStart) / 1000.0f : -1.0f;
        float f2 = h = this.fadeInStart > -1L ? (float)(m - this.fadeInStart) / 500.0f : -1.0f;
        if (g >= 1.0f) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.renderWithTooltipAndSubtitles(guiGraphics, 0, 0, f);
            } else {
                this.minecraft.gui.renderDeferredSubtitles();
            }
            n = Mth.ceil((1.0f - Mth.clamp(g - 1.0f, 0.0f, 1.0f)) * 255.0f);
            guiGraphics.nextStratum();
            guiGraphics.fill(0, 0, k, l, LoadingOverlay.replaceAlpha(BRAND_BACKGROUND.getAsInt(), n));
            o = 1.0f - Mth.clamp(g - 1.0f, 0.0f, 1.0f);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && h < 1.0f) {
                this.minecraft.screen.renderWithTooltipAndSubtitles(guiGraphics, i, j, f);
            } else {
                this.minecraft.gui.renderDeferredSubtitles();
            }
            n = Mth.ceil(Mth.clamp((double)h, 0.15, 1.0) * 255.0);
            guiGraphics.nextStratum();
            guiGraphics.fill(0, 0, k, l, LoadingOverlay.replaceAlpha(BRAND_BACKGROUND.getAsInt(), n));
            o = Mth.clamp(h, 0.0f, 1.0f);
        } else {
            n = BRAND_BACKGROUND.getAsInt();
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.minecraft.getMainRenderTarget().getColorTexture(), n);
            o = 1.0f;
        }
        n = (int)((double)guiGraphics.guiWidth() * 0.5);
        int p = (int)((double)guiGraphics.guiHeight() * 0.5);
        double d = Math.min((double)guiGraphics.guiWidth() * 0.75, (double)guiGraphics.guiHeight()) * 0.25;
        int q = (int)(d * 0.5);
        double e = d * 4.0;
        int r = (int)(e * 0.5);
        int s = ARGB.white(o);
        guiGraphics.blit(RenderPipelines.MOJANG_LOGO, MOJANG_STUDIOS_LOGO_LOCATION, n - r, p - q, -0.0625f, 0.0f, r, (int)d, 120, 60, 120, 120, s);
        guiGraphics.blit(RenderPipelines.MOJANG_LOGO, MOJANG_STUDIOS_LOGO_LOCATION, n, p - q, 0.0625f, 60.0f, r, (int)d, 120, 60, 120, 120, s);
        int t = (int)((double)guiGraphics.guiHeight() * 0.8325);
        float u = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95f + u * 0.050000012f, 0.0f, 1.0f);
        if (g < 1.0f) {
            this.drawProgressBar(guiGraphics, k / 2 - r, t - 5, k / 2 + r, t + 5, 1.0f - Mth.clamp(g, 0.0f, 1.0f));
        }
        if (g >= 2.0f) {
            this.minecraft.setOverlay(null);
        }
    }

    @Override
    public void tick() {
        if (this.fadeOutStart == -1L && this.reload.isDone() && this.isReadyToFadeOut()) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            }
            catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }
            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) {
                Window window = this.minecraft.getWindow();
                this.minecraft.screen.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
            }
        }
    }

    private boolean isReadyToFadeOut() {
        return !this.fadeIn || this.fadeInStart > -1L && Util.getMillis() - this.fadeInStart >= 1000L;
    }

    private void drawProgressBar(GuiGraphics guiGraphics, int i, int j, int k, int l, float f) {
        int m = Mth.ceil((float)(k - i - 2) * this.currentProgress);
        int n = Math.round(f * 255.0f);
        int o = ARGB.color(n, 255, 255, 255);
        guiGraphics.fill(i + 2, j + 2, i + m, l - 2, o);
        guiGraphics.fill(i + 1, j, k - 1, j + 1, o);
        guiGraphics.fill(i + 1, l, k - 1, l - 1, o);
        guiGraphics.fill(i, j, i + 1, l, o);
        guiGraphics.fill(k, j, k - 1, l, o);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    static class LogoTexture
    extends ReloadableTexture {
        public LogoTexture() {
            super(MOJANG_STUDIOS_LOGO_LOCATION);
        }

        @Override
        public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
            ResourceProvider resourceProvider = Minecraft.getInstance().getVanillaPackResources().asProvider();
            try (InputStream inputStream = resourceProvider.open(MOJANG_STUDIOS_LOGO_LOCATION);){
                TextureContents textureContents = new TextureContents(NativeImage.read(inputStream), new TextureMetadataSection(true, true, MipmapStrategy.MEAN, 0.0f));
                return textureContents;
            }
        }
    }
}


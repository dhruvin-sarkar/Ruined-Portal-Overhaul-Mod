/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LevelLoadingScreen
extends Screen {
    private static final Component DOWNLOADING_TERRAIN_TEXT = Component.translatable("multiplayer.downloadingTerrain");
    private static final Component READY_TO_PLAY_TEXT = Component.translatable("narrator.ready_to_play");
    private static final long NARRATION_DELAY_MS = 2000L;
    private static final int PROGRESS_BAR_WIDTH = 200;
    private LevelLoadTracker loadTracker;
    private float smoothedProgress;
    private long lastNarration = -1L;
    private Reason reason;
    private @Nullable TextureAtlasSprite cachedNetherPortalSprite;
    private static final Object2IntMap<ChunkStatus> COLORS = (Object2IntMap)Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> {
        object2IntOpenHashMap.defaultReturnValue(0);
        object2IntOpenHashMap.put((Object)ChunkStatus.EMPTY, 0x545454);
        object2IntOpenHashMap.put((Object)ChunkStatus.STRUCTURE_STARTS, 0x999999);
        object2IntOpenHashMap.put((Object)ChunkStatus.STRUCTURE_REFERENCES, 6250897);
        object2IntOpenHashMap.put((Object)ChunkStatus.BIOMES, 8434258);
        object2IntOpenHashMap.put((Object)ChunkStatus.NOISE, 0xD1D1D1);
        object2IntOpenHashMap.put((Object)ChunkStatus.SURFACE, 7497737);
        object2IntOpenHashMap.put((Object)ChunkStatus.CARVERS, 3159410);
        object2IntOpenHashMap.put((Object)ChunkStatus.FEATURES, 2213376);
        object2IntOpenHashMap.put((Object)ChunkStatus.INITIALIZE_LIGHT, 0xCCCCCC);
        object2IntOpenHashMap.put((Object)ChunkStatus.LIGHT, 16769184);
        object2IntOpenHashMap.put((Object)ChunkStatus.SPAWN, 15884384);
        object2IntOpenHashMap.put((Object)ChunkStatus.FULL, 0xFFFFFF);
    });

    public LevelLoadingScreen(LevelLoadTracker levelLoadTracker, Reason reason) {
        super(GameNarrator.NO_TITLE);
        this.loadTracker = levelLoadTracker;
        this.reason = reason;
    }

    public void update(LevelLoadTracker levelLoadTracker, Reason reason) {
        this.loadTracker = levelLoadTracker;
        this.reason = reason;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean shouldNarrateNavigation() {
        return false;
    }

    @Override
    protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
        if (this.loadTracker.hasProgress()) {
            narrationElementOutput.add(NarratedElementType.TITLE, (Component)Component.translatable("loading.progress", Mth.floor(this.loadTracker.serverProgress() * 100.0f)));
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.smoothedProgress += (this.loadTracker.serverProgress() - this.smoothedProgress) * 0.2f;
        if (this.loadTracker.isLevelReady()) {
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        int o;
        super.render(guiGraphics, i, j, f);
        long l = Util.getMillis();
        if (l - this.lastNarration > 2000L) {
            this.lastNarration = l;
            this.triggerImmediateNarration(true);
        }
        int k = this.width / 2;
        int m = this.height / 2;
        ChunkLoadStatusView chunkLoadStatusView = this.loadTracker.statusView();
        if (chunkLoadStatusView != null) {
            int n = 2;
            LevelLoadingScreen.renderChunks(guiGraphics, k, m, 2, 0, chunkLoadStatusView);
            o = m - chunkLoadStatusView.radius() * 2 - this.font.lineHeight * 3;
        } else {
            o = m - 50;
        }
        guiGraphics.drawCenteredString(this.font, DOWNLOADING_TERRAIN_TEXT, k, o, -1);
        if (this.loadTracker.hasProgress()) {
            this.drawProgressBar(guiGraphics, k - 100, o + this.font.lineHeight + 3, 200, 2, this.smoothedProgress);
        }
    }

    private void drawProgressBar(GuiGraphics guiGraphics, int i, int j, int k, int l, float f) {
        guiGraphics.fill(i, j, i + k, j + l, -16777216);
        guiGraphics.fill(i, j, i + Math.round(f * (float)k), j + l, -16711936);
    }

    public static void renderChunks(GuiGraphics guiGraphics, int i, int j, int k, int l, ChunkLoadStatusView chunkLoadStatusView) {
        int r;
        int m = k + l;
        int n = chunkLoadStatusView.radius() * 2 + 1;
        int o = n * m - l;
        int p = i - o / 2;
        int q = j - o / 2;
        if (Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DebugScreenEntries.VISUALIZE_CHUNKS_ON_SERVER)) {
            r = m / 2 + 1;
            guiGraphics.fill(i - r, j - r, i + r, j + r, -65536);
        }
        for (r = 0; r < n; ++r) {
            for (int s = 0; s < n; ++s) {
                ChunkStatus chunkStatus = chunkLoadStatusView.get(r, s);
                int t = p + r * m;
                int u = q + s * m;
                guiGraphics.fill(t, u, t + k, u + k, ARGB.opaque(COLORS.getInt((Object)chunkStatus)));
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        switch (this.reason.ordinal()) {
            case 2: {
                this.renderPanorama(guiGraphics, f);
                this.renderBlurredBackground(guiGraphics);
                this.renderMenuBackground(guiGraphics);
                break;
            }
            case 0: {
                guiGraphics.blitSprite(RenderPipelines.GUI_OPAQUE_TEXTURED_BACKGROUND, this.getNetherPortalSprite(), 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight());
                break;
            }
            case 1: {
                TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                AbstractTexture abstractTexture = textureManager.getTexture(AbstractEndPortalRenderer.END_SKY_LOCATION);
                AbstractTexture abstractTexture2 = textureManager.getTexture(AbstractEndPortalRenderer.END_PORTAL_LOCATION);
                TextureSetup textureSetup = TextureSetup.doubleTexture(abstractTexture.getTextureView(), abstractTexture.getSampler(), abstractTexture2.getTextureView(), abstractTexture2.getSampler());
                guiGraphics.fill(RenderPipelines.END_PORTAL, textureSetup, 0, 0, this.width, this.height);
            }
        }
    }

    private TextureAtlasSprite getNetherPortalSprite() {
        if (this.cachedNetherPortalSprite != null) {
            return this.cachedNetherPortalSprite;
        }
        this.cachedNetherPortalSprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        return this.cachedNetherPortalSprite;
    }

    @Override
    public void onClose() {
        this.minecraft.getNarrator().saySystemNow(READY_TO_PLAY_TEXT);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Reason {
        NETHER_PORTAL,
        END_PORTAL,
        OTHER;

    }
}


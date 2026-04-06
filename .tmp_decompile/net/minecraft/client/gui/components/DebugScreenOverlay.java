/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.mojang.datafixers.DataFixUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.DataFixUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debugchart.BandwidthDebugChart;
import net.minecraft.client.gui.components.debugchart.FpsDebugChart;
import net.minecraft.client.gui.components.debugchart.PingDebugChart;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;
import net.minecraft.client.gui.components.debugchart.TpsDebugChart;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkLoadStatusView;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugScreenOverlay {
    private static final float CROSSHAIR_SCALE = 0.01f;
    private static final int CROSSHAIR_INDEX_COUNT = 36;
    private static final int MARGIN_RIGHT = 2;
    private static final int MARGIN_LEFT = 2;
    private static final int MARGIN_TOP = 2;
    private final Minecraft minecraft;
    private final Font font;
    private final GpuBuffer crosshairBuffer;
    private final RenderSystem.AutoStorageIndexBuffer crosshairIndicies = RenderSystem.getSequentialBuffer(VertexFormat.Mode.LINES);
    private @Nullable ChunkPos lastPos;
    private @Nullable LevelChunk clientChunk;
    private @Nullable CompletableFuture<LevelChunk> serverChunk;
    private boolean renderProfilerChart;
    private boolean renderFpsCharts;
    private boolean renderNetworkCharts;
    private final LocalSampleLogger frameTimeLogger = new LocalSampleLogger(1);
    private final LocalSampleLogger tickTimeLogger = new LocalSampleLogger(TpsDebugDimensions.values().length);
    private final LocalSampleLogger pingLogger = new LocalSampleLogger(1);
    private final LocalSampleLogger bandwidthLogger = new LocalSampleLogger(1);
    private final Map<RemoteDebugSampleType, LocalSampleLogger> remoteSupportingLoggers = Map.of((Object)((Object)RemoteDebugSampleType.TICK_TIME), (Object)this.tickTimeLogger);
    private final FpsDebugChart fpsChart;
    private final TpsDebugChart tpsChart;
    private final PingDebugChart pingChart;
    private final BandwidthDebugChart bandwidthChart;
    private final ProfilerPieChart profilerPieChart;

    public DebugScreenOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.font = minecraft.font;
        this.fpsChart = new FpsDebugChart(this.font, this.frameTimeLogger);
        this.tpsChart = new TpsDebugChart(this.font, this.tickTimeLogger, () -> Float.valueOf(minecraft.level == null ? 0.0f : minecraft.level.tickRateManager().millisecondsPerTick()));
        this.pingChart = new PingDebugChart(this.font, this.pingLogger);
        this.bandwidthChart = new BandwidthDebugChart(this.font, this.bandwidthLogger);
        this.profilerPieChart = new ProfilerPieChart(this.font);
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize() * 12 * 2);){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
            bufferBuilder.addVertex(0.0f, 0.0f, 0.0f).setColor(-16777216).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(4.0f);
            bufferBuilder.addVertex(1.0f, 0.0f, 0.0f).setColor(-16777216).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(4.0f);
            bufferBuilder.addVertex(0.0f, 0.0f, 0.0f).setColor(-16777216).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(4.0f);
            bufferBuilder.addVertex(0.0f, 1.0f, 0.0f).setColor(-16777216).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(4.0f);
            bufferBuilder.addVertex(0.0f, 0.0f, 0.0f).setColor(-16777216).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(4.0f);
            bufferBuilder.addVertex(0.0f, 0.0f, 1.0f).setColor(-16777216).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(4.0f);
            bufferBuilder.addVertex(0.0f, 0.0f, 0.0f).setColor(-65536).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(2.0f);
            bufferBuilder.addVertex(1.0f, 0.0f, 0.0f).setColor(-65536).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(2.0f);
            bufferBuilder.addVertex(0.0f, 0.0f, 0.0f).setColor(-16711936).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(2.0f);
            bufferBuilder.addVertex(0.0f, 1.0f, 0.0f).setColor(-16711936).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(2.0f);
            bufferBuilder.addVertex(0.0f, 0.0f, 0.0f).setColor(-8421377).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(2.0f);
            bufferBuilder.addVertex(0.0f, 0.0f, 1.0f).setColor(-8421377).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(2.0f);
            try (MeshData meshData = bufferBuilder.buildOrThrow();){
                this.crosshairBuffer = RenderSystem.getDevice().createBuffer(() -> "Crosshair vertex buffer", 32, meshData.vertexBuffer());
            }
        }
    }

    public void clearChunkCache() {
        this.serverChunk = null;
        this.clientChunk = null;
    }

    public void render(GuiGraphics guiGraphics) {
        IntegratedServer integratedServer;
        ArrayList list4;
        ChunkPos chunkPos;
        Options options = this.minecraft.options;
        if (!this.minecraft.isGameLoadFinished() || options.hideGui && this.minecraft.screen == null) {
            return;
        }
        Collection<Identifier> collection = this.minecraft.debugEntries.getCurrentlyEnabled();
        if (collection.isEmpty()) {
            return;
        }
        guiGraphics.nextStratum();
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("debug");
        if (this.minecraft.getCameraEntity() != null && this.minecraft.level != null) {
            BlockPos blockPos = this.minecraft.getCameraEntity().blockPosition();
            chunkPos = new ChunkPos(blockPos);
        } else {
            chunkPos = null;
        }
        if (!Objects.equals(this.lastPos, chunkPos)) {
            this.lastPos = chunkPos;
            this.clearChunkCache();
        }
        final ArrayList<String> list = new ArrayList<String>();
        final ArrayList<String> list2 = new ArrayList<String>();
        final LinkedHashMap map = new LinkedHashMap();
        final ArrayList list3 = new ArrayList();
        DebugScreenDisplayer debugScreenDisplayer = new DebugScreenDisplayer(){

            @Override
            public void addPriorityLine(String string) {
                if (list.size() > list2.size()) {
                    list2.add(string);
                } else {
                    list.add(string);
                }
            }

            @Override
            public void addLine(String string) {
                list3.add(string);
            }

            @Override
            public void addToGroup(Identifier identifier2, Collection<String> collection) {
                map.computeIfAbsent(identifier2, identifier -> new ArrayList()).addAll(collection);
            }

            @Override
            public void addToGroup(Identifier identifier2, String string) {
                map.computeIfAbsent(identifier2, identifier -> new ArrayList()).add(string);
            }
        };
        Level level = this.getLevel();
        for (Identifier identifier : collection) {
            DebugScreenEntry debugScreenEntry = DebugScreenEntries.getEntry(identifier);
            if (debugScreenEntry == null) continue;
            debugScreenEntry.display(debugScreenDisplayer, level, this.getClientChunk(), this.getServerChunk());
        }
        if (!list.isEmpty()) {
            list.add("");
        }
        if (!list2.isEmpty()) {
            list2.add("");
        }
        if (!list3.isEmpty()) {
            int i = (list3.size() + 1) / 2;
            list.addAll(list3.subList(0, i));
            list2.addAll(list3.subList(i, list3.size()));
            list.add("");
            if (i < list3.size()) {
                list2.add("");
            }
        }
        if (!(list4 = new ArrayList(map.values())).isEmpty()) {
            int j = (list4.size() + 1) / 2;
            for (int k = 0; k < list4.size(); ++k) {
                Collection collection2 = (Collection)list4.get(k);
                if (collection2.isEmpty()) continue;
                if (k < j) {
                    list.addAll(collection2);
                    list.add("");
                    continue;
                }
                list2.addAll(collection2);
                list2.add("");
            }
        }
        if (this.minecraft.debugEntries.isOverlayVisible()) {
            list.add("");
            boolean bl = this.minecraft.getSingleplayerServer() != null;
            KeyMapping keyMapping = options.keyDebugModifier;
            String string = keyMapping.getTranslatedKeyMessage().getString();
            String string2 = "[" + (String)(keyMapping.isUnbound() ? "" : string + "+");
            String string3 = string2 + options.keyDebugPofilingChart.getTranslatedKeyMessage().getString() + "]";
            String string4 = string2 + options.keyDebugFpsCharts.getTranslatedKeyMessage().getString() + "]";
            String string5 = string2 + options.keyDebugNetworkCharts.getTranslatedKeyMessage().getString() + "]";
            list.add("Debug charts: " + string3 + " Profiler " + (this.renderProfilerChart ? "visible" : "hidden") + "; " + string4 + " " + (bl ? "FPS + TPS " : "FPS ") + (this.renderFpsCharts ? "visible" : "hidden") + "; " + string5 + " " + (!this.minecraft.isLocalServer() ? "Bandwidth + Ping" : "Ping") + (this.renderNetworkCharts ? " visible" : " hidden"));
            String string6 = string2 + options.keyDebugDebugOptions.getTranslatedKeyMessage().getString() + "]";
            list.add("To edit: press " + string6);
        }
        this.renderLines(guiGraphics, list, true);
        this.renderLines(guiGraphics, list2, false);
        guiGraphics.nextStratum();
        this.profilerPieChart.setBottomOffset(10);
        if (this.showFpsCharts()) {
            int j = guiGraphics.guiWidth();
            int k = j / 2;
            this.fpsChart.drawChart(guiGraphics, 0, this.fpsChart.getWidth(k));
            if (this.tickTimeLogger.size() > 0) {
                int l = this.tpsChart.getWidth(k);
                this.tpsChart.drawChart(guiGraphics, j - l, l);
            }
            this.profilerPieChart.setBottomOffset(this.tpsChart.getFullHeight());
        }
        if (this.showNetworkCharts() && this.minecraft.getConnection() != null) {
            int j = guiGraphics.guiWidth();
            int k = j / 2;
            if (!this.minecraft.isLocalServer()) {
                this.bandwidthChart.drawChart(guiGraphics, 0, this.bandwidthChart.getWidth(k));
            }
            int l = this.pingChart.getWidth(k);
            this.pingChart.drawChart(guiGraphics, j - l, l);
            this.profilerPieChart.setBottomOffset(this.pingChart.getFullHeight());
        }
        if (this.minecraft.debugEntries.isCurrentlyEnabled(DebugScreenEntries.VISUALIZE_CHUNKS_ON_SERVER) && (integratedServer = this.minecraft.getSingleplayerServer()) != null && this.minecraft.player != null) {
            ChunkLoadStatusView chunkLoadStatusView = integratedServer.createChunkLoadStatusView(16 + ChunkLevel.RADIUS_AROUND_FULL_CHUNK);
            chunkLoadStatusView.moveTo(this.minecraft.player.level().dimension(), this.minecraft.player.chunkPosition());
            LevelLoadingScreen.renderChunks(guiGraphics, guiGraphics.guiWidth() / 2, guiGraphics.guiHeight() / 2, 4, 1, chunkLoadStatusView);
        }
        try (Zone zone = profilerFiller.zone("profilerPie");){
            this.profilerPieChart.render(guiGraphics);
        }
        profilerFiller.pop();
    }

    private void renderLines(GuiGraphics guiGraphics, List<String> list, boolean bl) {
        int m;
        int l;
        int k;
        String string;
        int j;
        int i = this.font.lineHeight;
        for (j = 0; j < list.size(); ++j) {
            string = list.get(j);
            if (Strings.isNullOrEmpty((String)string)) continue;
            k = this.font.width(string);
            l = bl ? 2 : guiGraphics.guiWidth() - 2 - k;
            m = 2 + i * j;
            guiGraphics.fill(l - 1, m - 1, l + k + 1, m + i - 1, -1873784752);
        }
        for (j = 0; j < list.size(); ++j) {
            string = list.get(j);
            if (Strings.isNullOrEmpty((String)string)) continue;
            k = this.font.width(string);
            l = bl ? 2 : guiGraphics.guiWidth() - 2 - k;
            m = 2 + i * j;
            guiGraphics.drawString(this.font, string, l, m, -2039584, false);
        }
    }

    private @Nullable ServerLevel getServerLevel() {
        if (this.minecraft.level == null) {
            return null;
        }
        IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
        if (integratedServer != null) {
            return integratedServer.getLevel(this.minecraft.level.dimension());
        }
        return null;
    }

    private @Nullable Level getLevel() {
        if (this.minecraft.level == null) {
            return null;
        }
        return (Level)DataFixUtils.orElse(Optional.ofNullable(this.minecraft.getSingleplayerServer()).flatMap(integratedServer -> Optional.ofNullable(integratedServer.getLevel(this.minecraft.level.dimension()))), (Object)this.minecraft.level);
    }

    private @Nullable LevelChunk getServerChunk() {
        if (this.minecraft.level == null || this.lastPos == null) {
            return null;
        }
        if (this.serverChunk == null) {
            ServerLevel serverLevel = this.getServerLevel();
            if (serverLevel == null) {
                return null;
            }
            this.serverChunk = serverLevel.getChunkSource().getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false).thenApply(chunkResult -> chunkResult.orElse(null));
        }
        return this.serverChunk.getNow(null);
    }

    private @Nullable LevelChunk getClientChunk() {
        if (this.minecraft.level == null || this.lastPos == null) {
            return null;
        }
        if (this.clientChunk == null) {
            this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x, this.lastPos.z);
        }
        return this.clientChunk;
    }

    public boolean showDebugScreen() {
        DebugScreenEntryList debugScreenEntryList = this.minecraft.debugEntries;
        return !(!debugScreenEntryList.isOverlayVisible() && debugScreenEntryList.getCurrentlyEnabled().isEmpty() || this.minecraft.options.hideGui && this.minecraft.screen == null);
    }

    public boolean showProfilerChart() {
        return this.minecraft.debugEntries.isOverlayVisible() && this.renderProfilerChart;
    }

    public boolean showNetworkCharts() {
        return this.minecraft.debugEntries.isOverlayVisible() && this.renderNetworkCharts;
    }

    public boolean showFpsCharts() {
        return this.minecraft.debugEntries.isOverlayVisible() && this.renderFpsCharts;
    }

    public void toggleNetworkCharts() {
        boolean bl = this.renderNetworkCharts = !this.minecraft.debugEntries.isOverlayVisible() || !this.renderNetworkCharts;
        if (this.renderNetworkCharts) {
            this.minecraft.debugEntries.setOverlayVisible(true);
            this.renderFpsCharts = false;
        }
    }

    public void toggleFpsCharts() {
        boolean bl = this.renderFpsCharts = !this.minecraft.debugEntries.isOverlayVisible() || !this.renderFpsCharts;
        if (this.renderFpsCharts) {
            this.minecraft.debugEntries.setOverlayVisible(true);
            this.renderNetworkCharts = false;
        }
    }

    public void toggleProfilerChart() {
        boolean bl = this.renderProfilerChart = !this.minecraft.debugEntries.isOverlayVisible() || !this.renderProfilerChart;
        if (this.renderProfilerChart) {
            this.minecraft.debugEntries.setOverlayVisible(true);
        }
    }

    public void logFrameDuration(long l) {
        this.frameTimeLogger.logSample(l);
    }

    public LocalSampleLogger getTickTimeLogger() {
        return this.tickTimeLogger;
    }

    public LocalSampleLogger getPingLogger() {
        return this.pingLogger;
    }

    public LocalSampleLogger getBandwidthLogger() {
        return this.bandwidthLogger;
    }

    public ProfilerPieChart getProfilerPieChart() {
        return this.profilerPieChart;
    }

    public void logRemoteSample(long[] ls, RemoteDebugSampleType remoteDebugSampleType) {
        LocalSampleLogger localSampleLogger = this.remoteSupportingLoggers.get((Object)remoteDebugSampleType);
        if (localSampleLogger != null) {
            localSampleLogger.logFullSample(ls);
        }
    }

    public void reset() {
        this.tickTimeLogger.reset();
        this.pingLogger.reset();
        this.bandwidthLogger.reset();
    }

    public void render3dCrosshair(Camera camera) {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.translate(0.0f, 0.0f, -1.0f);
        matrix4fStack.rotateX(camera.xRot() * ((float)Math.PI / 180));
        matrix4fStack.rotateY(camera.yRot() * ((float)Math.PI / 180));
        float f = 0.01f * (float)this.minecraft.getWindow().getGuiScale();
        matrix4fStack.scale(-f, f, -f);
        RenderPipeline renderPipeline = RenderPipelines.LINES;
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        GpuTextureView gpuTextureView = renderTarget.getColorTextureView();
        GpuTextureView gpuTextureView2 = renderTarget.getDepthTextureView();
        GpuBuffer gpuBuffer = this.crosshairIndicies.getBuffer(36);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)matrix4fStack, (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "3d crosshair", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setVertexBuffer(0, this.crosshairBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.crosshairIndicies.type());
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.drawIndexed(0, 0, 36, 1);
        }
        matrix4fStack.popMatrix();
    }
}


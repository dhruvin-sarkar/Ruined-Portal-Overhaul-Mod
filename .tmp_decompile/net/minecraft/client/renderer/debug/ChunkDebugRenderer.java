/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.SectionPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChunkDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private final int radius = 12;
    private @Nullable ChunkData data;

    public ChunkDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void emitGizmos(double d, double e, double f, DebugValueAccess debugValueAccess, Frustum frustum, float g) {
        double h = Util.getNanos();
        if (h - this.lastUpdateTime > 3.0E9) {
            this.lastUpdateTime = h;
            IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
            this.data = integratedServer != null ? new ChunkData(this, integratedServer, d, f) : null;
        }
        if (this.data != null) {
            Map map = this.data.serverData.getNow(null);
            double i = this.minecraft.gameRenderer.getMainCamera().position().y * 0.85;
            for (Map.Entry<ChunkPos, String> entry : this.data.clientData.entrySet()) {
                ChunkPos chunkPos = entry.getKey();
                Object string = entry.getValue();
                if (map != null) {
                    string = (String)string + (String)map.get(chunkPos);
                }
                String[] strings = ((String)string).split("\n");
                int j = 0;
                for (String string2 : strings) {
                    Gizmos.billboardText(string2, new Vec3(SectionPos.sectionToBlockCoord(chunkPos.x, 8), i + (double)j, SectionPos.sectionToBlockCoord(chunkPos.z, 8)), TextGizmo.Style.whiteAndCentered().withScale(2.4f)).setAlwaysOnTop();
                    j -= 2;
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    final class ChunkData {
        final Map<ChunkPos, String> clientData;
        final CompletableFuture<Map<ChunkPos, String>> serverData;

        ChunkData(ChunkDebugRenderer chunkDebugRenderer, IntegratedServer integratedServer, double d, double e) {
            ClientLevel clientLevel = chunkDebugRenderer.minecraft.level;
            ResourceKey<Level> resourceKey = clientLevel.dimension();
            int i = SectionPos.posToSectionCoord(d);
            int j = SectionPos.posToSectionCoord(e);
            ImmutableMap.Builder builder = ImmutableMap.builder();
            ClientChunkCache clientChunkCache = clientLevel.getChunkSource();
            for (int k = i - 12; k <= i + 12; ++k) {
                for (int l = j - 12; l <= j + 12; ++l) {
                    ChunkPos chunkPos = new ChunkPos(k, l);
                    Object string = "";
                    LevelChunk levelChunk = clientChunkCache.getChunk(k, l, false);
                    string = (String)string + "Client: ";
                    if (levelChunk == null) {
                        string = (String)string + "0n/a\n";
                    } else {
                        string = (String)string + (levelChunk.isEmpty() ? " E" : "");
                        string = (String)string + "\n";
                    }
                    builder.put((Object)chunkPos, string);
                }
            }
            this.clientData = builder.build();
            this.serverData = integratedServer.submit(() -> {
                ServerLevel serverLevel = integratedServer.getLevel(resourceKey);
                if (serverLevel == null) {
                    return ImmutableMap.of();
                }
                ImmutableMap.Builder builder = ImmutableMap.builder();
                ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
                for (int k = i - 12; k <= i + 12; ++k) {
                    for (int l = j - 12; l <= j + 12; ++l) {
                        ChunkPos chunkPos = new ChunkPos(k, l);
                        builder.put((Object)chunkPos, (Object)("Server: " + serverChunkCache.getChunkDebugData(chunkPos)));
                    }
                }
                return builder.build();
            });
        }
    }
}


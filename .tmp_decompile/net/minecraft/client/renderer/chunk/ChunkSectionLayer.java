/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderPipelines;

@Environment(value=EnvType.CLIENT)
public enum ChunkSectionLayer {
    SOLID(RenderPipelines.SOLID_TERRAIN, 0x400000, false),
    CUTOUT(RenderPipelines.CUTOUT_TERRAIN, 0x400000, false),
    TRANSLUCENT(RenderPipelines.TRANSLUCENT_TERRAIN, 786432, true),
    TRIPWIRE(RenderPipelines.TRIPWIRE_TERRAIN, 1536, true);

    private final RenderPipeline pipeline;
    private final int bufferSize;
    private final boolean sortOnUpload;
    private final String label;

    private ChunkSectionLayer(RenderPipeline renderPipeline, int j, boolean bl) {
        this.pipeline = renderPipeline;
        this.bufferSize = j;
        this.sortOnUpload = bl;
        this.label = this.toString().toLowerCase(Locale.ROOT);
    }

    public RenderPipeline pipeline() {
        return this.pipeline;
    }

    public int bufferSize() {
        return this.bufferSize;
    }

    public String label() {
        return this.label;
    }

    public boolean sortOnUpload() {
        return this.sortOnUpload;
    }
}


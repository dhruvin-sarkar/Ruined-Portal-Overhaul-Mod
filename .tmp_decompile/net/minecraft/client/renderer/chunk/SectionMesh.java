/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.chunk;

import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface SectionMesh
extends AutoCloseable {
    default public boolean isDifferentPointOfView(TranslucencyPointOfView translucencyPointOfView) {
        return false;
    }

    default public boolean hasRenderableLayers() {
        return false;
    }

    default public boolean hasTranslucentGeometry() {
        return false;
    }

    default public boolean isEmpty(ChunkSectionLayer chunkSectionLayer) {
        return true;
    }

    default public List<BlockEntity> getRenderableBlockEntities() {
        return Collections.emptyList();
    }

    public boolean facesCanSeeEachother(Direction var1, Direction var2);

    default public @Nullable SectionBuffers getBuffers(ChunkSectionLayer chunkSectionLayer) {
        return null;
    }

    @Override
    default public void close() {
    }
}


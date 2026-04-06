/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record BlockOutlineRenderState(BlockPos pos, boolean isTranslucent, boolean highContrast, VoxelShape shape, @Nullable VoxelShape collisionShape, @Nullable VoxelShape occlusionShape, @Nullable VoxelShape interactionShape) {
    public BlockOutlineRenderState(BlockPos blockPos, boolean bl, boolean bl2, VoxelShape voxelShape) {
        this(blockPos, bl, bl2, voxelShape, null, null, null);
    }
}


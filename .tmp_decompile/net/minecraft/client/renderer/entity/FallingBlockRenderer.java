/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class FallingBlockRenderer
extends EntityRenderer<FallingBlockEntity, FallingBlockRenderState> {
    public FallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
    }

    @Override
    public boolean shouldRender(FallingBlockEntity fallingBlockEntity, Frustum frustum, double d, double e, double f) {
        if (!super.shouldRender(fallingBlockEntity, frustum, d, e, f)) {
            return false;
        }
        return fallingBlockEntity.getBlockState() != fallingBlockEntity.level().getBlockState(fallingBlockEntity.blockPosition());
    }

    @Override
    public void submit(FallingBlockRenderState fallingBlockRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        BlockState blockState = fallingBlockRenderState.movingBlockRenderState.blockState;
        if (blockState.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);
        submitNodeCollector.submitMovingBlock(poseStack, fallingBlockRenderState.movingBlockRenderState);
        poseStack.popPose();
        super.submit(fallingBlockRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public FallingBlockRenderState createRenderState() {
        return new FallingBlockRenderState();
    }

    @Override
    public void extractRenderState(FallingBlockEntity fallingBlockEntity, FallingBlockRenderState fallingBlockRenderState, float f) {
        super.extractRenderState(fallingBlockEntity, fallingBlockRenderState, f);
        BlockPos blockPos = BlockPos.containing(fallingBlockEntity.getX(), fallingBlockEntity.getBoundingBox().maxY, fallingBlockEntity.getZ());
        fallingBlockRenderState.movingBlockRenderState.randomSeedPos = fallingBlockEntity.getStartPos();
        fallingBlockRenderState.movingBlockRenderState.blockPos = blockPos;
        fallingBlockRenderState.movingBlockRenderState.blockState = fallingBlockEntity.getBlockState();
        fallingBlockRenderState.movingBlockRenderState.biome = fallingBlockEntity.level().getBiome(blockPos);
        fallingBlockRenderState.movingBlockRenderState.level = fallingBlockEntity.level();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.BrushableBlockRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BrushableBlockRenderer
implements BlockEntityRenderer<BrushableBlockEntity, BrushableBlockRenderState> {
    private final ItemModelResolver itemModelResolver;

    public BrushableBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public BrushableBlockRenderState createRenderState() {
        return new BrushableBlockRenderState();
    }

    @Override
    public void extractRenderState(BrushableBlockEntity brushableBlockEntity, BrushableBlockRenderState brushableBlockRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(brushableBlockEntity, brushableBlockRenderState, f, vec3, crumblingOverlay);
        brushableBlockRenderState.hitDirection = brushableBlockEntity.getHitDirection();
        brushableBlockRenderState.dustProgress = brushableBlockEntity.getBlockState().getValue(BlockStateProperties.DUSTED);
        if (brushableBlockEntity.getLevel() != null && brushableBlockEntity.getHitDirection() != null) {
            brushableBlockRenderState.lightCoords = LevelRenderer.getLightColor(LevelRenderer.BrightnessGetter.DEFAULT, brushableBlockEntity.getLevel(), brushableBlockEntity.getBlockState(), brushableBlockEntity.getBlockPos().relative(brushableBlockEntity.getHitDirection()));
        }
        this.itemModelResolver.updateForTopItem(brushableBlockRenderState.itemState, brushableBlockEntity.getItem(), ItemDisplayContext.FIXED, brushableBlockEntity.getLevel(), null, 0);
    }

    @Override
    public void submit(BrushableBlockRenderState brushableBlockRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (brushableBlockRenderState.dustProgress <= 0 || brushableBlockRenderState.hitDirection == null || brushableBlockRenderState.itemState.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.5f, 0.0f);
        float[] fs = this.translations(brushableBlockRenderState.hitDirection, brushableBlockRenderState.dustProgress);
        poseStack.translate(fs[0], fs[1], fs[2]);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(75.0f));
        boolean bl = brushableBlockRenderState.hitDirection == Direction.EAST || brushableBlockRenderState.hitDirection == Direction.WEST;
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((bl ? 90 : 0) + 11));
        poseStack.scale(0.5f, 0.5f, 0.5f);
        brushableBlockRenderState.itemState.submit(poseStack, submitNodeCollector, brushableBlockRenderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private float[] translations(Direction direction, int i) {
        float[] fs = new float[]{0.5f, 0.0f, 0.5f};
        float f = (float)i / 10.0f * 0.75f;
        switch (direction) {
            case EAST: {
                fs[0] = 0.73f + f;
                break;
            }
            case WEST: {
                fs[0] = 0.25f - f;
                break;
            }
            case UP: {
                fs[1] = 0.25f + f;
                break;
            }
            case DOWN: {
                fs[1] = -0.23f - f;
                break;
            }
            case NORTH: {
                fs[2] = 0.25f - f;
                break;
            }
            case SOUTH: {
                fs[2] = 0.73f + f;
            }
        }
        return fs;
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


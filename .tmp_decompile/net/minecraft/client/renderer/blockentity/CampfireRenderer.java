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
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.CampfireRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CampfireRenderer
implements BlockEntityRenderer<CampfireBlockEntity, CampfireRenderState> {
    private static final float SIZE = 0.375f;
    private final ItemModelResolver itemModelResolver;

    public CampfireRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public CampfireRenderState createRenderState() {
        return new CampfireRenderState();
    }

    @Override
    public void extractRenderState(CampfireBlockEntity campfireBlockEntity, CampfireRenderState campfireRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(campfireBlockEntity, campfireRenderState, f, vec3, crumblingOverlay);
        campfireRenderState.facing = campfireBlockEntity.getBlockState().getValue(CampfireBlock.FACING);
        int i = (int)campfireBlockEntity.getBlockPos().asLong();
        campfireRenderState.items = new ArrayList<ItemStackRenderState>();
        for (int j = 0; j < campfireBlockEntity.getItems().size(); ++j) {
            ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(itemStackRenderState, campfireBlockEntity.getItems().get(j), ItemDisplayContext.FIXED, campfireBlockEntity.getLevel(), null, i + j);
            campfireRenderState.items.add(itemStackRenderState);
        }
    }

    @Override
    public void submit(CampfireRenderState campfireRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        Direction direction = campfireRenderState.facing;
        List<ItemStackRenderState> list = campfireRenderState.items;
        for (int i = 0; i < list.size(); ++i) {
            ItemStackRenderState itemStackRenderState = list.get(i);
            if (itemStackRenderState.isEmpty()) continue;
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.44921875f, 0.5f);
            Direction direction2 = Direction.from2DDataValue((i + direction.get2DDataValue()) % 4);
            float f = -direction2.toYRot();
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f));
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
            poseStack.translate(-0.3125f, -0.3125f, 0.0f);
            poseStack.scale(0.375f, 0.375f, 0.375f);
            itemStackRenderState.submit(poseStack, submitNodeCollector, campfireRenderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


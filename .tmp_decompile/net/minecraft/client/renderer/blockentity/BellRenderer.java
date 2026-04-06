/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.bell.BellModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BellRenderer
implements BlockEntityRenderer<BellBlockEntity, BellRenderState> {
    public static final Material BELL_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("bell/bell_body");
    private final MaterialSet materials;
    private final BellModel model;

    public BellRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
        this.model = new BellModel(context.bakeLayer(ModelLayers.BELL));
    }

    @Override
    public BellRenderState createRenderState() {
        return new BellRenderState();
    }

    @Override
    public void extractRenderState(BellBlockEntity bellBlockEntity, BellRenderState bellRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(bellBlockEntity, bellRenderState, f, vec3, crumblingOverlay);
        bellRenderState.ticks = (float)bellBlockEntity.ticks + f;
        bellRenderState.shakeDirection = bellBlockEntity.shaking ? bellBlockEntity.clickDirection : null;
    }

    @Override
    public void submit(BellRenderState bellRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        BellModel.State state = new BellModel.State(bellRenderState.ticks, bellRenderState.shakeDirection);
        this.model.setupAnim(state);
        RenderType renderType = BELL_TEXTURE.renderType(RenderTypes::entitySolid);
        submitNodeCollector.submitModel(this.model, state, poseStack, renderType, bellRenderState.lightCoords, OverlayTexture.NO_OVERLAY, -1, this.materials.get(BELL_TEXTURE), 0, bellRenderState.breakProgress);
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


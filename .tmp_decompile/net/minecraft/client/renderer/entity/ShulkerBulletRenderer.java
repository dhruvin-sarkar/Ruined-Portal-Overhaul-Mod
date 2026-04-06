/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.projectile.ShulkerBulletModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ShulkerBulletRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class ShulkerBulletRenderer
extends EntityRenderer<ShulkerBullet, ShulkerBulletRenderState> {
    private static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/entity/shulker/spark.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucent(TEXTURE_LOCATION);
    private final ShulkerBulletModel model;

    public ShulkerBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ShulkerBulletModel(context.bakeLayer(ModelLayers.SHULKER_BULLET));
    }

    @Override
    protected int getBlockLightLevel(ShulkerBullet shulkerBullet, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void submit(ShulkerBulletRenderState shulkerBulletRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        float f = shulkerBulletRenderState.ageInTicks;
        poseStack.translate(0.0f, 0.15f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(Mth.sin(f * 0.1f) * 180.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.cos(f * 0.1f) * 180.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(Mth.sin(f * 0.15f) * 360.0f));
        poseStack.scale(-0.5f, -0.5f, 0.5f);
        submitNodeCollector.submitModel(this.model, shulkerBulletRenderState, poseStack, this.model.renderType(TEXTURE_LOCATION), shulkerBulletRenderState.lightCoords, OverlayTexture.NO_OVERLAY, shulkerBulletRenderState.outlineColor, null);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        submitNodeCollector.order(1).submitModel(this.model, shulkerBulletRenderState, poseStack, RENDER_TYPE, shulkerBulletRenderState.lightCoords, OverlayTexture.NO_OVERLAY, 0x26FFFFFF, null, shulkerBulletRenderState.outlineColor, null);
        poseStack.popPose();
        super.submit(shulkerBulletRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public ShulkerBulletRenderState createRenderState() {
        return new ShulkerBulletRenderState();
    }

    @Override
    public void extractRenderState(ShulkerBullet shulkerBullet, ShulkerBulletRenderState shulkerBulletRenderState, float f) {
        super.extractRenderState(shulkerBullet, shulkerBulletRenderState, f);
        shulkerBulletRenderState.yRot = shulkerBullet.getYRot(f);
        shulkerBulletRenderState.xRot = shulkerBullet.getXRot(f);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


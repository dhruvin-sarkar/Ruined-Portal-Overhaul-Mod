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
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class DragonFireballRenderer
extends EntityRenderer<DragonFireball, EntityRenderState> {
    private static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon_fireball.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutoutNoCull(TEXTURE_LOCATION);

    public DragonFireballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(DragonFireball dragonFireball, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void submit(EntityRenderState entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);
        poseStack.mulPose((Quaternionfc)cameraRenderState.orientation);
        submitNodeCollector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, vertexConsumer) -> {
            DragonFireballRenderer.vertex(vertexConsumer, pose, entityRenderState.lightCoords, 0.0f, 0, 0, 1);
            DragonFireballRenderer.vertex(vertexConsumer, pose, entityRenderState.lightCoords, 1.0f, 0, 1, 1);
            DragonFireballRenderer.vertex(vertexConsumer, pose, entityRenderState.lightCoords, 1.0f, 1, 1, 0);
            DragonFireballRenderer.vertex(vertexConsumer, pose, entityRenderState.lightCoords, 0.0f, 1, 0, 0);
        });
        poseStack.popPose();
        super.submit(entityRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, int i, float f, int j, int k, int l) {
        vertexConsumer.addVertex(pose, f - 0.5f, (float)j - 0.25f, 0.0f).setColor(-1).setUv(k, l).setOverlay(OverlayTexture.NO_OVERLAY).setLight(i).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}


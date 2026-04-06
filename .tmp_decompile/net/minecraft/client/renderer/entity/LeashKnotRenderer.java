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
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.leash.LeashKnotModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;

@Environment(value=EnvType.CLIENT)
public class LeashKnotRenderer
extends EntityRenderer<LeashFenceKnotEntity, EntityRenderState> {
    private static final Identifier KNOT_LOCATION = Identifier.withDefaultNamespace("textures/entity/lead_knot.png");
    private final LeashKnotModel model;

    public LeashKnotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LeashKnotModel(context.bakeLayer(ModelLayers.LEASH_KNOT));
    }

    @Override
    public void submit(EntityRenderState entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        submitNodeCollector.submitModel(this.model, entityRenderState, poseStack, this.model.renderType(KNOT_LOCATION), entityRenderState.lightCoords, OverlayTexture.NO_OVERLAY, entityRenderState.outlineColor, null);
        poseStack.popPose();
        super.submit(entityRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}


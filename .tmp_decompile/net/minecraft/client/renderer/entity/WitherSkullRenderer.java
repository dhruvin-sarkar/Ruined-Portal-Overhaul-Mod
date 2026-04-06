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
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.WitherSkullRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;

@Environment(value=EnvType.CLIENT)
public class WitherSkullRenderer
extends EntityRenderer<WitherSkull, WitherSkullRenderState> {
    private static final Identifier WITHER_INVULNERABLE_LOCATION = Identifier.withDefaultNamespace("textures/entity/wither/wither_invulnerable.png");
    private static final Identifier WITHER_LOCATION = Identifier.withDefaultNamespace("textures/entity/wither/wither.png");
    private final SkullModel model;

    public WitherSkullRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SkullModel(context.bakeLayer(ModelLayers.WITHER_SKULL));
    }

    public static LayerDefinition createSkullLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    protected int getBlockLightLevel(WitherSkull witherSkull, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void submit(WitherSkullRenderState witherSkullRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        submitNodeCollector.submitModel(this.model, witherSkullRenderState.modelState, poseStack, this.model.renderType(this.getTextureLocation(witherSkullRenderState)), witherSkullRenderState.lightCoords, OverlayTexture.NO_OVERLAY, witherSkullRenderState.outlineColor, null);
        poseStack.popPose();
        super.submit(witherSkullRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    private Identifier getTextureLocation(WitherSkullRenderState witherSkullRenderState) {
        return witherSkullRenderState.isDangerous ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
    }

    @Override
    public WitherSkullRenderState createRenderState() {
        return new WitherSkullRenderState();
    }

    @Override
    public void extractRenderState(WitherSkull witherSkull, WitherSkullRenderState witherSkullRenderState, float f) {
        super.extractRenderState(witherSkull, witherSkullRenderState, f);
        witherSkullRenderState.isDangerous = witherSkull.isDangerous();
        witherSkullRenderState.modelState.animationPos = 0.0f;
        witherSkullRenderState.modelState.yRot = witherSkull.getYRot(f);
        witherSkullRenderState.modelState.xRot = witherSkull.getXRot(f);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


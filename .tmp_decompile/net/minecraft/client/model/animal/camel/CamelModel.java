/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.camel;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.definitions.CamelAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.CamelRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class CamelModel
extends EntityModel<CamelRenderState> {
    private static final float MAX_WALK_ANIMATION_SPEED = 2.0f;
    private static final float WALK_ANIMATION_SCALE_FACTOR = 2.5f;
    public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.45f);
    protected final ModelPart head;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation sitAnimation;
    private final KeyframeAnimation sitPoseAnimation;
    private final KeyframeAnimation standupAnimation;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation dashAnimation;

    public CamelModel(ModelPart modelPart) {
        super(modelPart);
        ModelPart modelPart2 = modelPart.getChild("body");
        this.head = modelPart2.getChild("head");
        this.walkAnimation = CamelAnimation.CAMEL_WALK.bake(modelPart);
        this.sitAnimation = CamelAnimation.CAMEL_SIT.bake(modelPart);
        this.sitPoseAnimation = CamelAnimation.CAMEL_SIT_POSE.bake(modelPart);
        this.standupAnimation = CamelAnimation.CAMEL_STANDUP.bake(modelPart);
        this.idleAnimation = CamelAnimation.CAMEL_IDLE.bake(modelPart);
        this.dashAnimation = CamelAnimation.CAMEL_DASH.bake(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(CamelModel.createBodyMesh(), 128, 128);
    }

    protected static MeshDefinition createBodyMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 25).addBox(-7.5f, -12.0f, -23.5f, 15.0f, 12.0f, 27.0f), PartPose.offset(0.0f, 4.0f, 9.5f));
        partDefinition2.addOrReplaceChild("hump", CubeListBuilder.create().texOffs(74, 0).addBox(-4.5f, -5.0f, -5.5f, 9.0f, 5.0f, 11.0f), PartPose.offset(0.0f, -12.0f, -10.0f));
        partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(122, 0).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 14.0f, 0.0f), PartPose.offset(0.0f, -9.0f, 3.5f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(60, 24).addBox(-3.5f, -7.0f, -15.0f, 7.0f, 8.0f, 19.0f).texOffs(21, 0).addBox(-3.5f, -21.0f, -15.0f, 7.0f, 14.0f, 7.0f).texOffs(50, 0).addBox(-2.5f, -21.0f, -21.0f, 5.0f, 5.0f, 6.0f), PartPose.offset(0.0f, -3.0f, -19.5f));
        partDefinition3.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(45, 0).addBox(-0.5f, 0.5f, -1.0f, 3.0f, 1.0f, 2.0f), PartPose.offset(2.5f, -21.0f, -9.5f));
        partDefinition3.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(67, 0).addBox(-2.5f, 0.5f, -1.0f, 3.0f, 1.0f, 2.0f), PartPose.offset(-2.5f, -21.0f, -9.5f));
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(58, 16).addBox(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), PartPose.offset(4.9f, 1.0f, 9.5f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(94, 16).addBox(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), PartPose.offset(-4.9f, 1.0f, 9.5f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), PartPose.offset(4.9f, 1.0f, -10.5f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), PartPose.offset(-4.9f, 1.0f, -10.5f));
        return meshDefinition;
    }

    @Override
    public void setupAnim(CamelRenderState camelRenderState) {
        super.setupAnim(camelRenderState);
        this.applyHeadRotation(camelRenderState, camelRenderState.yRot, camelRenderState.xRot);
        this.walkAnimation.applyWalk(camelRenderState.walkAnimationPos, camelRenderState.walkAnimationSpeed, 2.0f, 2.5f);
        this.sitAnimation.apply(camelRenderState.sitAnimationState, camelRenderState.ageInTicks);
        this.sitPoseAnimation.apply(camelRenderState.sitPoseAnimationState, camelRenderState.ageInTicks);
        this.standupAnimation.apply(camelRenderState.sitUpAnimationState, camelRenderState.ageInTicks);
        this.idleAnimation.apply(camelRenderState.idleAnimationState, camelRenderState.ageInTicks);
        this.dashAnimation.apply(camelRenderState.dashAnimationState, camelRenderState.ageInTicks);
    }

    private void applyHeadRotation(CamelRenderState camelRenderState, float f, float g) {
        f = Mth.clamp(f, -30.0f, 30.0f);
        g = Mth.clamp(g, -25.0f, 45.0f);
        if (camelRenderState.jumpCooldown > 0.0f) {
            float h = 45.0f * camelRenderState.jumpCooldown / 55.0f;
            g = Mth.clamp(g + h, -25.0f, 70.0f);
        }
        this.head.yRot = f * ((float)Math.PI / 180);
        this.head.xRot = g * ((float)Math.PI / 180);
    }
}


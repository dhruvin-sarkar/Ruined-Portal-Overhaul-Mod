/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.monster.skeleton;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

@Environment(value=EnvType.CLIENT)
public class SkeletonModel<S extends SkeletonRenderState>
extends HumanoidModel<S> {
    public SkeletonModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        SkeletonModel.createDefaultSkeletonMesh(partDefinition);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    protected static void createDefaultSkeletonMesh(PartDefinition partDefinition) {
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-2.0f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(2.0f, 12.0f, 0.0f));
    }

    public static LayerDefinition createSingleModelDualBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(28, 0).addBox(-4.0f, 10.0f, -2.0f, 8.0f, 1.0f, 4.0f).texOffs(16, 48).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.025f)), PartPose.offset(0.0f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 32).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.2f)), PartPose.offset(0.0f, 0.0f, 0.0f)).addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(42, 33).addBox(-1.55f, -2.025f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.5f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(56, 16).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(40, 48).addBox(-1.45f, -2.025f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.5f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(0, 49).addBox(-1.5f, -0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-2.0f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(4, 49).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(2.0f, 12.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(S skeletonRenderState) {
        super.setupAnim(skeletonRenderState);
        if (((SkeletonRenderState)skeletonRenderState).isAggressive && !((SkeletonRenderState)skeletonRenderState).isHoldingBow) {
            float f = ((SkeletonRenderState)skeletonRenderState).attackTime;
            float g = Mth.sin(f * (float)Math.PI);
            float h = Mth.sin((1.0f - (1.0f - f) * (1.0f - f)) * (float)Math.PI);
            this.rightArm.zRot = 0.0f;
            this.leftArm.zRot = 0.0f;
            this.rightArm.yRot = -(0.1f - g * 0.6f);
            this.leftArm.yRot = 0.1f - g * 0.6f;
            this.rightArm.xRot = -1.5707964f;
            this.leftArm.xRot = -1.5707964f;
            this.rightArm.xRot -= g * 1.2f - h * 0.4f;
            this.leftArm.xRot -= g * 1.2f - h * 0.4f;
            AnimationUtils.bobArms(this.rightArm, this.leftArm, ((SkeletonRenderState)skeletonRenderState).ageInTicks);
        }
    }

    @Override
    public void translateToHand(SkeletonRenderState skeletonRenderState, HumanoidArm humanoidArm, PoseStack poseStack) {
        this.root().translateAndRotate(poseStack);
        float f = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        ModelPart modelPart = this.getArm(humanoidArm);
        modelPart.x += f;
        modelPart.translateAndRotate(poseStack);
        modelPart.x -= f;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.golem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class IronGolemModel
extends EntityModel<IronGolemRenderState> {
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public IronGolemModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.rightArm = modelPart.getChild("right_arm");
        this.leftArm = modelPart.getChild("left_arm");
        this.rightLeg = modelPart.getChild("right_leg");
        this.leftLeg = modelPart.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -12.0f, -5.5f, 8.0f, 10.0f, 8.0f).texOffs(24, 0).addBox(-1.0f, -5.0f, -7.5f, 2.0f, 4.0f, 2.0f), PartPose.offset(0.0f, -7.0f, -2.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 40).addBox(-9.0f, -2.0f, -6.0f, 18.0f, 12.0f, 11.0f).texOffs(0, 70).addBox(-4.5f, 10.0f, -3.0f, 9.0f, 5.0f, 6.0f, new CubeDeformation(0.5f)), PartPose.offset(0.0f, -7.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 21).addBox(-13.0f, -2.5f, -3.0f, 4.0f, 30.0f, 6.0f), PartPose.offset(0.0f, -7.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 58).addBox(9.0f, -2.5f, -3.0f, 4.0f, 30.0f, 6.0f), PartPose.offset(0.0f, -7.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(37, 0).addBox(-3.5f, -3.0f, -3.0f, 6.0f, 16.0f, 5.0f), PartPose.offset(-4.0f, 11.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(60, 0).mirror().addBox(-3.5f, -3.0f, -3.0f, 6.0f, 16.0f, 5.0f), PartPose.offset(5.0f, 11.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(IronGolemRenderState ironGolemRenderState) {
        super.setupAnim(ironGolemRenderState);
        float f = ironGolemRenderState.attackTicksRemaining;
        float g = ironGolemRenderState.walkAnimationSpeed;
        float h = ironGolemRenderState.walkAnimationPos;
        if (f > 0.0f) {
            this.rightArm.xRot = -2.0f + 1.5f * Mth.triangleWave(f, 10.0f);
            this.leftArm.xRot = -2.0f + 1.5f * Mth.triangleWave(f, 10.0f);
        } else {
            int i = ironGolemRenderState.offerFlowerTick;
            if (i > 0) {
                this.rightArm.xRot = -0.8f + 0.025f * Mth.triangleWave(i, 70.0f);
                this.leftArm.xRot = 0.0f;
            } else {
                this.rightArm.xRot = (-0.2f + 1.5f * Mth.triangleWave(h, 13.0f)) * g;
                this.leftArm.xRot = (-0.2f - 1.5f * Mth.triangleWave(h, 13.0f)) * g;
            }
        }
        this.head.yRot = ironGolemRenderState.yRot * ((float)Math.PI / 180);
        this.head.xRot = ironGolemRenderState.xRot * ((float)Math.PI / 180);
        this.rightLeg.xRot = -1.5f * Mth.triangleWave(h, 13.0f) * g;
        this.leftLeg.xRot = 1.5f * Mth.triangleWave(h, 13.0f) * g;
        this.rightLeg.yRot = 0.0f;
        this.leftLeg.yRot = 0.0f;
    }

    public ModelPart getFlowerHoldingArm() {
        return this.rightArm;
    }
}


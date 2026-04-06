/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.monster.spider;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class SpiderModel
extends EntityModel<LivingEntityRenderState> {
    private static final String BODY_0 = "body0";
    private static final String BODY_1 = "body1";
    private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
    private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
    private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
    private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightMiddleHindLeg;
    private final ModelPart leftMiddleHindLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public SpiderModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightMiddleHindLeg = modelPart.getChild(RIGHT_MIDDLE_HIND_LEG);
        this.leftMiddleHindLeg = modelPart.getChild(LEFT_MIDDLE_HIND_LEG);
        this.rightMiddleFrontLeg = modelPart.getChild(RIGHT_MIDDLE_FRONT_LEG);
        this.leftMiddleFrontLeg = modelPart.getChild(LEFT_MIDDLE_FRONT_LEG);
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createSpiderBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 15;
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 15.0f, -3.0f));
        partDefinition.addOrReplaceChild(BODY_0, CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f), PartPose.offset(0.0f, 15.0f, 0.0f));
        partDefinition.addOrReplaceChild(BODY_1, CubeListBuilder.create().texOffs(0, 12).addBox(-5.0f, -4.0f, -6.0f, 10.0f, 8.0f, 12.0f), PartPose.offset(0.0f, 15.0f, 9.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(18, 0).addBox(-15.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        float f = 0.7853982f;
        float g = 0.3926991f;
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offsetAndRotation(-4.0f, 15.0f, 2.0f, 0.0f, 0.7853982f, -0.7853982f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder2, PartPose.offsetAndRotation(4.0f, 15.0f, 2.0f, 0.0f, -0.7853982f, 0.7853982f));
        partDefinition.addOrReplaceChild(RIGHT_MIDDLE_HIND_LEG, cubeListBuilder, PartPose.offsetAndRotation(-4.0f, 15.0f, 1.0f, 0.0f, 0.3926991f, -0.58119464f));
        partDefinition.addOrReplaceChild(LEFT_MIDDLE_HIND_LEG, cubeListBuilder2, PartPose.offsetAndRotation(4.0f, 15.0f, 1.0f, 0.0f, -0.3926991f, 0.58119464f));
        partDefinition.addOrReplaceChild(RIGHT_MIDDLE_FRONT_LEG, cubeListBuilder, PartPose.offsetAndRotation(-4.0f, 15.0f, 0.0f, 0.0f, -0.3926991f, -0.58119464f));
        partDefinition.addOrReplaceChild(LEFT_MIDDLE_FRONT_LEG, cubeListBuilder2, PartPose.offsetAndRotation(4.0f, 15.0f, 0.0f, 0.0f, 0.3926991f, 0.58119464f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offsetAndRotation(-4.0f, 15.0f, -1.0f, 0.0f, -0.7853982f, -0.7853982f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offsetAndRotation(4.0f, 15.0f, -1.0f, 0.0f, 0.7853982f, 0.7853982f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState livingEntityRenderState) {
        super.setupAnim(livingEntityRenderState);
        this.head.yRot = livingEntityRenderState.yRot * ((float)Math.PI / 180);
        this.head.xRot = livingEntityRenderState.xRot * ((float)Math.PI / 180);
        float f = livingEntityRenderState.walkAnimationPos * 0.6662f;
        float g = livingEntityRenderState.walkAnimationSpeed;
        float h = -(Mth.cos(f * 2.0f + 0.0f) * 0.4f) * g;
        float i = -(Mth.cos(f * 2.0f + (float)Math.PI) * 0.4f) * g;
        float j = -(Mth.cos(f * 2.0f + 1.5707964f) * 0.4f) * g;
        float k = -(Mth.cos(f * 2.0f + 4.712389f) * 0.4f) * g;
        float l = Math.abs(Mth.sin(f + 0.0f) * 0.4f) * g;
        float m = Math.abs(Mth.sin(f + (float)Math.PI) * 0.4f) * g;
        float n = Math.abs(Mth.sin(f + 1.5707964f) * 0.4f) * g;
        float o = Math.abs(Mth.sin(f + 4.712389f) * 0.4f) * g;
        this.rightHindLeg.yRot += h;
        this.leftHindLeg.yRot -= h;
        this.rightMiddleHindLeg.yRot += i;
        this.leftMiddleHindLeg.yRot -= i;
        this.rightMiddleFrontLeg.yRot += j;
        this.leftMiddleFrontLeg.yRot -= j;
        this.rightFrontLeg.yRot += k;
        this.leftFrontLeg.yRot -= k;
        this.rightHindLeg.zRot += l;
        this.leftHindLeg.zRot -= l;
        this.rightMiddleHindLeg.zRot += m;
        this.leftMiddleHindLeg.zRot -= m;
        this.rightMiddleFrontLeg.zRot += n;
        this.leftMiddleFrontLeg.zRot -= n;
        this.rightFrontLeg.zRot += o;
        this.leftFrontLeg.zRot -= o;
    }
}


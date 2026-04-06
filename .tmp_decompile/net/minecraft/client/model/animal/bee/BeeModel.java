/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.bee;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class BeeModel
extends EntityModel<BeeRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5f);
    private static final String BONE = "bone";
    private static final String STINGER = "stinger";
    private static final String LEFT_ANTENNA = "left_antenna";
    private static final String RIGHT_ANTENNA = "right_antenna";
    private static final String FRONT_LEGS = "front_legs";
    private static final String MIDDLE_LEGS = "middle_legs";
    private static final String BACK_LEGS = "back_legs";
    private final ModelPart bone;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart frontLeg;
    private final ModelPart midLeg;
    private final ModelPart backLeg;
    private final ModelPart stinger;
    private final ModelPart leftAntenna;
    private final ModelPart rightAntenna;
    private float rollAmount;

    public BeeModel(ModelPart modelPart) {
        super(modelPart);
        this.bone = modelPart.getChild(BONE);
        ModelPart modelPart2 = this.bone.getChild("body");
        this.stinger = modelPart2.getChild(STINGER);
        this.leftAntenna = modelPart2.getChild(LEFT_ANTENNA);
        this.rightAntenna = modelPart2.getChild(RIGHT_ANTENNA);
        this.rightWing = this.bone.getChild("right_wing");
        this.leftWing = this.bone.getChild("left_wing");
        this.frontLeg = this.bone.getChild(FRONT_LEGS);
        this.midLeg = this.bone.getChild(MIDDLE_LEGS);
        this.backLeg = this.bone.getChild(BACK_LEGS);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(BONE, CubeListBuilder.create(), PartPose.offset(0.0f, 19.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5f, -4.0f, -5.0f, 7.0f, 7.0f, 10.0f), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(STINGER, CubeListBuilder.create().texOffs(26, 7).addBox(0.0f, -1.0f, 5.0f, 0.0f, 1.0f, 2.0f), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(LEFT_ANTENNA, CubeListBuilder.create().texOffs(2, 0).addBox(1.5f, -2.0f, -3.0f, 1.0f, 2.0f, 3.0f), PartPose.offset(0.0f, -2.0f, -5.0f));
        partDefinition3.addOrReplaceChild(RIGHT_ANTENNA, CubeListBuilder.create().texOffs(2, 3).addBox(-2.5f, -2.0f, -3.0f, 1.0f, 2.0f, 3.0f), PartPose.offset(0.0f, -2.0f, -5.0f));
        CubeDeformation cubeDeformation = new CubeDeformation(0.001f);
        partDefinition2.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 18).addBox(-9.0f, 0.0f, 0.0f, 9.0f, 0.0f, 6.0f, cubeDeformation), PartPose.offsetAndRotation(-1.5f, -4.0f, -3.0f, 0.0f, -0.2618f, 0.0f));
        partDefinition2.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(0.0f, 0.0f, 0.0f, 9.0f, 0.0f, 6.0f, cubeDeformation), PartPose.offsetAndRotation(1.5f, -4.0f, -3.0f, 0.0f, 0.2618f, 0.0f));
        partDefinition2.addOrReplaceChild(FRONT_LEGS, CubeListBuilder.create().addBox(FRONT_LEGS, -5.0f, 0.0f, 0.0f, 7, 2, 0, 26, 1), PartPose.offset(1.5f, 3.0f, -2.0f));
        partDefinition2.addOrReplaceChild(MIDDLE_LEGS, CubeListBuilder.create().addBox(MIDDLE_LEGS, -5.0f, 0.0f, 0.0f, 7, 2, 0, 26, 3), PartPose.offset(1.5f, 3.0f, 0.0f));
        partDefinition2.addOrReplaceChild(BACK_LEGS, CubeListBuilder.create().addBox(BACK_LEGS, -5.0f, 0.0f, 0.0f, 7, 2, 0, 26, 5), PartPose.offset(1.5f, 3.0f, 2.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(BeeRenderState beeRenderState) {
        float f;
        super.setupAnim(beeRenderState);
        this.rollAmount = beeRenderState.rollAmount;
        this.stinger.visible = beeRenderState.hasStinger;
        if (!beeRenderState.isOnGround) {
            f = beeRenderState.ageInTicks * 120.32113f * ((float)Math.PI / 180);
            this.rightWing.yRot = 0.0f;
            this.rightWing.zRot = Mth.cos(f) * (float)Math.PI * 0.15f;
            this.leftWing.xRot = this.rightWing.xRot;
            this.leftWing.yRot = this.rightWing.yRot;
            this.leftWing.zRot = -this.rightWing.zRot;
            this.frontLeg.xRot = 0.7853982f;
            this.midLeg.xRot = 0.7853982f;
            this.backLeg.xRot = 0.7853982f;
        }
        if (!beeRenderState.isAngry && !beeRenderState.isOnGround) {
            f = Mth.cos(beeRenderState.ageInTicks * 0.18f);
            this.bone.xRot = 0.1f + f * (float)Math.PI * 0.025f;
            this.leftAntenna.xRot = f * (float)Math.PI * 0.03f;
            this.rightAntenna.xRot = f * (float)Math.PI * 0.03f;
            this.frontLeg.xRot = -f * (float)Math.PI * 0.1f + 0.3926991f;
            this.backLeg.xRot = -f * (float)Math.PI * 0.05f + 0.7853982f;
            this.bone.y -= Mth.cos(beeRenderState.ageInTicks * 0.18f) * 0.9f;
        }
        if (this.rollAmount > 0.0f) {
            this.bone.xRot = Mth.rotLerpRad(this.rollAmount, this.bone.xRot, 3.0915928f);
        }
    }
}


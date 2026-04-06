/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.object.armorstand;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;

@Environment(value=EnvType.CLIENT)
public class ArmorStandArmorModel
extends HumanoidModel<ArmorStandRenderState> {
    public ArmorStandArmorModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static ArmorModelSet<LayerDefinition> createArmorLayerSet(CubeDeformation cubeDeformation, CubeDeformation cubeDeformation2) {
        return ArmorStandArmorModel.createArmorMeshSet(ArmorStandArmorModel::createBaseMesh, cubeDeformation, cubeDeformation2).map(meshDefinition -> LayerDefinition.create(meshDefinition, 64, 32));
    }

    private static MeshDefinition createBaseMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(cubeDeformation, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.offset(0.0f, 1.0f, 0.0f));
        partDefinition2.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation.extend(0.5f)), PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(-0.1f)), PartPose.offset(-1.9f, 11.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(-0.1f)), PartPose.offset(1.9f, 11.0f, 0.0f));
        return meshDefinition;
    }

    @Override
    public void setupAnim(ArmorStandRenderState armorStandRenderState) {
        super.setupAnim(armorStandRenderState);
        this.head.xRot = (float)Math.PI / 180 * armorStandRenderState.headPose.x();
        this.head.yRot = (float)Math.PI / 180 * armorStandRenderState.headPose.y();
        this.head.zRot = (float)Math.PI / 180 * armorStandRenderState.headPose.z();
        this.body.xRot = (float)Math.PI / 180 * armorStandRenderState.bodyPose.x();
        this.body.yRot = (float)Math.PI / 180 * armorStandRenderState.bodyPose.y();
        this.body.zRot = (float)Math.PI / 180 * armorStandRenderState.bodyPose.z();
        this.leftArm.xRot = (float)Math.PI / 180 * armorStandRenderState.leftArmPose.x();
        this.leftArm.yRot = (float)Math.PI / 180 * armorStandRenderState.leftArmPose.y();
        this.leftArm.zRot = (float)Math.PI / 180 * armorStandRenderState.leftArmPose.z();
        this.rightArm.xRot = (float)Math.PI / 180 * armorStandRenderState.rightArmPose.x();
        this.rightArm.yRot = (float)Math.PI / 180 * armorStandRenderState.rightArmPose.y();
        this.rightArm.zRot = (float)Math.PI / 180 * armorStandRenderState.rightArmPose.z();
        this.leftLeg.xRot = (float)Math.PI / 180 * armorStandRenderState.leftLegPose.x();
        this.leftLeg.yRot = (float)Math.PI / 180 * armorStandRenderState.leftLegPose.y();
        this.leftLeg.zRot = (float)Math.PI / 180 * armorStandRenderState.leftLegPose.z();
        this.rightLeg.xRot = (float)Math.PI / 180 * armorStandRenderState.rightLegPose.x();
        this.rightLeg.yRot = (float)Math.PI / 180 * armorStandRenderState.rightLegPose.y();
        this.rightLeg.zRot = (float)Math.PI / 180 * armorStandRenderState.rightLegPose.z();
    }
}


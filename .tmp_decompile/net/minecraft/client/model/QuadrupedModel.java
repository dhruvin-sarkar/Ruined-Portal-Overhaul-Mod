/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class QuadrupedModel<T extends LivingEntityRenderState>
extends EntityModel<T> {
    protected final ModelPart head;
    protected final ModelPart body;
    protected final ModelPart rightHindLeg;
    protected final ModelPart leftHindLeg;
    protected final ModelPart rightFrontLeg;
    protected final ModelPart leftFrontLeg;

    protected QuadrupedModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.body = modelPart.getChild("body");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static MeshDefinition createBodyMesh(int i, boolean bl, boolean bl2, CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.offset(0.0f, 18 - i, -6.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-5.0f, -10.0f, -7.0f, 10.0f, 16.0f, 8.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 17 - i, 2.0f, 1.5707964f, 0.0f, 0.0f));
        QuadrupedModel.createLegs(partDefinition, bl, bl2, i, cubeDeformation);
        return meshDefinition;
    }

    static void createLegs(PartDefinition partDefinition, boolean bl, boolean bl2, int i, CubeDeformation cubeDeformation) {
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().mirror(bl2).texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, (float)i, 4.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().mirror(bl).texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, (float)i, 4.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-3.0f, 24 - i, 7.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder2, PartPose.offset(3.0f, 24 - i, 7.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-3.0f, 24 - i, -5.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offset(3.0f, 24 - i, -5.0f));
    }

    @Override
    public void setupAnim(T livingEntityRenderState) {
        super.setupAnim(livingEntityRenderState);
        this.head.xRot = ((LivingEntityRenderState)livingEntityRenderState).xRot * ((float)Math.PI / 180);
        this.head.yRot = ((LivingEntityRenderState)livingEntityRenderState).yRot * ((float)Math.PI / 180);
        float f = ((LivingEntityRenderState)livingEntityRenderState).walkAnimationPos;
        float g = ((LivingEntityRenderState)livingEntityRenderState).walkAnimationSpeed;
        this.rightHindLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.leftHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
    }
}


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
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class SnowGolemModel
extends EntityModel<LivingEntityRenderState> {
    private static final String UPPER_BODY = "upper_body";
    private final ModelPart upperBody;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public SnowGolemModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.leftArm = modelPart.getChild("left_arm");
        this.rightArm = modelPart.getChild("right_arm");
        this.upperBody = modelPart.getChild(UPPER_BODY);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = 4.0f;
        CubeDeformation cubeDeformation = new CubeDeformation(-0.5f);
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.offset(0.0f, 4.0f, 0.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(32, 0).addBox(-1.0f, 0.0f, -1.0f, 12.0f, 2.0f, 2.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("left_arm", cubeListBuilder, PartPose.offsetAndRotation(5.0f, 6.0f, 1.0f, 0.0f, 0.0f, 1.0f));
        partDefinition.addOrReplaceChild("right_arm", cubeListBuilder, PartPose.offsetAndRotation(-5.0f, 6.0f, -1.0f, 0.0f, (float)Math.PI, -1.0f));
        partDefinition.addOrReplaceChild(UPPER_BODY, CubeListBuilder.create().texOffs(0, 16).addBox(-5.0f, -10.0f, -5.0f, 10.0f, 10.0f, 10.0f, cubeDeformation), PartPose.offset(0.0f, 13.0f, 0.0f));
        partDefinition.addOrReplaceChild("lower_body", CubeListBuilder.create().texOffs(0, 36).addBox(-6.0f, -12.0f, -6.0f, 12.0f, 12.0f, 12.0f, cubeDeformation), PartPose.offset(0.0f, 24.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState livingEntityRenderState) {
        super.setupAnim(livingEntityRenderState);
        this.head.yRot = livingEntityRenderState.yRot * ((float)Math.PI / 180);
        this.head.xRot = livingEntityRenderState.xRot * ((float)Math.PI / 180);
        this.upperBody.yRot = livingEntityRenderState.yRot * ((float)Math.PI / 180) * 0.25f;
        float f = Mth.sin(this.upperBody.yRot);
        float g = Mth.cos(this.upperBody.yRot);
        this.leftArm.yRot = this.upperBody.yRot;
        this.rightArm.yRot = this.upperBody.yRot + (float)Math.PI;
        this.leftArm.x = g * 5.0f;
        this.leftArm.z = -f * 5.0f;
        this.rightArm.x = -g * 5.0f;
        this.rightArm.z = f * 5.0f;
    }

    public ModelPart getHead() {
        return this.head;
    }
}


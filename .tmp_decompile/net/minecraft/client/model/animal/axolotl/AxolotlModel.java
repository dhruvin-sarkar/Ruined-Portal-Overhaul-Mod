/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.axolotl;

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
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class AxolotlModel
extends EntityModel<AxolotlRenderState> {
    public static final float SWIMMING_LEG_XROT = 1.8849558f;
    public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5f);
    private final ModelPart tail;
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart topGills;
    private final ModelPart leftGills;
    private final ModelPart rightGills;

    public AxolotlModel(ModelPart modelPart) {
        super(modelPart);
        this.body = modelPart.getChild("body");
        this.head = this.body.getChild("head");
        this.rightHindLeg = this.body.getChild("right_hind_leg");
        this.leftHindLeg = this.body.getChild("left_hind_leg");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
        this.topGills = this.head.getChild("top_gills");
        this.leftGills = this.head.getChild("left_gills");
        this.rightGills = this.head.getChild("right_gills");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.0f, -2.0f, -9.0f, 8.0f, 4.0f, 10.0f).texOffs(2, 17).addBox(0.0f, -3.0f, -8.0f, 0.0f, 5.0f, 9.0f), PartPose.offset(0.0f, 20.0f, 5.0f));
        CubeDeformation cubeDeformation = new CubeDeformation(0.001f);
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0f, -3.0f, -5.0f, 8.0f, 5.0f, 5.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f, -9.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(3, 37).addBox(-4.0f, -3.0f, 0.0f, 8.0f, 3.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(0, 40).addBox(-3.0f, -5.0f, 0.0f, 3.0f, 7.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder3 = CubeListBuilder.create().texOffs(11, 40).addBox(0.0f, -5.0f, 0.0f, 3.0f, 7.0f, 0.0f, cubeDeformation);
        partDefinition3.addOrReplaceChild("top_gills", cubeListBuilder, PartPose.offset(0.0f, -3.0f, -1.0f));
        partDefinition3.addOrReplaceChild("left_gills", cubeListBuilder2, PartPose.offset(-4.0f, 0.0f, -1.0f));
        partDefinition3.addOrReplaceChild("right_gills", cubeListBuilder3, PartPose.offset(4.0f, 0.0f, -1.0f));
        CubeListBuilder cubeListBuilder4 = CubeListBuilder.create().texOffs(2, 13).addBox(-1.0f, 0.0f, 0.0f, 3.0f, 5.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder5 = CubeListBuilder.create().texOffs(2, 13).addBox(-2.0f, 0.0f, 0.0f, 3.0f, 5.0f, 0.0f, cubeDeformation);
        partDefinition2.addOrReplaceChild("right_hind_leg", cubeListBuilder5, PartPose.offset(-3.5f, 1.0f, -1.0f));
        partDefinition2.addOrReplaceChild("left_hind_leg", cubeListBuilder4, PartPose.offset(3.5f, 1.0f, -1.0f));
        partDefinition2.addOrReplaceChild("right_front_leg", cubeListBuilder5, PartPose.offset(-3.5f, 1.0f, -8.0f));
        partDefinition2.addOrReplaceChild("left_front_leg", cubeListBuilder4, PartPose.offset(3.5f, 1.0f, -8.0f));
        partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(2, 19).addBox(0.0f, -3.0f, 0.0f, 0.0f, 5.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 1.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(AxolotlRenderState axolotlRenderState) {
        super.setupAnim(axolotlRenderState);
        float f = axolotlRenderState.playingDeadFactor;
        float g = axolotlRenderState.inWaterFactor;
        float h = axolotlRenderState.onGroundFactor;
        float i = axolotlRenderState.movingFactor;
        float j = 1.0f - i;
        float k = 1.0f - Math.min(h, i);
        this.body.yRot += axolotlRenderState.yRot * ((float)Math.PI / 180);
        this.setupSwimmingAnimation(axolotlRenderState.ageInTicks, axolotlRenderState.xRot, Math.min(i, g));
        this.setupWaterHoveringAnimation(axolotlRenderState.ageInTicks, Math.min(j, g));
        this.setupGroundCrawlingAnimation(axolotlRenderState.ageInTicks, Math.min(i, h));
        this.setupLayStillOnGroundAnimation(axolotlRenderState.ageInTicks, Math.min(j, h));
        this.setupPlayDeadAnimation(f);
        this.applyMirrorLegRotations(k);
    }

    private void setupLayStillOnGroundAnimation(float f, float g) {
        if (g <= 1.0E-5f) {
            return;
        }
        float h = f * 0.09f;
        float i = Mth.sin(h);
        float j = Mth.cos(h);
        float k = i * i - 2.0f * i;
        float l = j * j - 3.0f * i;
        this.head.xRot += -0.09f * k * g;
        this.head.zRot += -0.2f * g;
        this.tail.yRot += (-0.1f + 0.1f * k) * g;
        float m = (0.6f + 0.05f * l) * g;
        this.topGills.xRot += m;
        this.leftGills.yRot -= m;
        this.rightGills.yRot += m;
        this.leftHindLeg.xRot += 1.1f * g;
        this.leftHindLeg.yRot += 1.0f * g;
        this.leftFrontLeg.xRot += 0.8f * g;
        this.leftFrontLeg.yRot += 2.3f * g;
        this.leftFrontLeg.zRot -= 0.5f * g;
    }

    private void setupGroundCrawlingAnimation(float f, float g) {
        if (g <= 1.0E-5f) {
            return;
        }
        float h = f * 0.11f;
        float i = Mth.cos(h);
        float j = (i * i - 2.0f * i) / 5.0f;
        float k = 0.7f * i;
        float l = 0.09f * i * g;
        this.head.yRot += l;
        this.tail.yRot += l;
        float m = (0.6f - 0.08f * (i * i + 2.0f * Mth.sin(h))) * g;
        this.topGills.xRot += m;
        this.leftGills.yRot -= m;
        this.rightGills.yRot += m;
        float n = 0.9424779f * g;
        float o = 1.0995574f * g;
        this.leftHindLeg.xRot += n;
        this.leftHindLeg.yRot += (1.5f - j) * g;
        this.leftHindLeg.zRot += -0.1f * g;
        this.leftFrontLeg.xRot += o;
        this.leftFrontLeg.yRot += (1.5707964f - k) * g;
        this.rightHindLeg.xRot += n;
        this.rightHindLeg.yRot += (-1.0f - j) * g;
        this.rightFrontLeg.xRot += o;
        this.rightFrontLeg.yRot += (-1.5707964f - k) * g;
    }

    private void setupWaterHoveringAnimation(float f, float g) {
        if (g <= 1.0E-5f) {
            return;
        }
        float h = f * 0.075f;
        float i = Mth.cos(h);
        float j = Mth.sin(h) * 0.15f;
        float k = (-0.15f + 0.075f * i) * g;
        this.body.xRot += k;
        this.body.y -= j * g;
        this.head.xRot -= k;
        this.topGills.xRot += 0.2f * i * g;
        float l = (-0.3f * i - 0.19f) * g;
        this.leftGills.yRot += l;
        this.rightGills.yRot -= l;
        this.leftHindLeg.xRot += (2.3561945f - i * 0.11f) * g;
        this.leftHindLeg.yRot += 0.47123894f * g;
        this.leftHindLeg.zRot += 1.7278761f * g;
        this.leftFrontLeg.xRot += (0.7853982f - i * 0.2f) * g;
        this.leftFrontLeg.yRot += 2.042035f * g;
        this.tail.yRot += 0.5f * i * g;
    }

    private void setupSwimmingAnimation(float f, float g, float h) {
        if (h <= 1.0E-5f) {
            return;
        }
        float i = f * 0.33f;
        float j = Mth.sin(i);
        float k = Mth.cos(i);
        float l = 0.13f * j;
        this.body.xRot += (g * ((float)Math.PI / 180) + l) * h;
        this.head.xRot -= l * 1.8f * h;
        this.body.y -= 0.45f * k * h;
        this.topGills.xRot += (-0.5f * j - 0.8f) * h;
        float m = (0.3f * j + 0.9f) * h;
        this.leftGills.yRot += m;
        this.rightGills.yRot -= m;
        this.tail.yRot += 0.3f * Mth.cos(i * 0.9f) * h;
        this.leftHindLeg.xRot += 1.8849558f * h;
        this.leftHindLeg.yRot += -0.4f * j * h;
        this.leftHindLeg.zRot += 1.5707964f * h;
        this.leftFrontLeg.xRot += 1.8849558f * h;
        this.leftFrontLeg.yRot += (-0.2f * k - 0.1f) * h;
        this.leftFrontLeg.zRot += 1.5707964f * h;
    }

    private void setupPlayDeadAnimation(float f) {
        if (f <= 1.0E-5f) {
            return;
        }
        this.leftHindLeg.xRot += 1.4137167f * f;
        this.leftHindLeg.yRot += 1.0995574f * f;
        this.leftHindLeg.zRot += 0.7853982f * f;
        this.leftFrontLeg.xRot += 0.7853982f * f;
        this.leftFrontLeg.yRot += 2.042035f * f;
        this.body.xRot += -0.15f * f;
        this.body.zRot += 0.35f * f;
    }

    private void applyMirrorLegRotations(float f) {
        if (f <= 1.0E-5f) {
            return;
        }
        this.rightHindLeg.xRot += this.leftHindLeg.xRot * f;
        ModelPart modelPart = this.rightHindLeg;
        modelPart.yRot = modelPart.yRot + -this.leftHindLeg.yRot * f;
        modelPart = this.rightHindLeg;
        modelPart.zRot = modelPart.zRot + -this.leftHindLeg.zRot * f;
        this.rightFrontLeg.xRot += this.leftFrontLeg.xRot * f;
        modelPart = this.rightFrontLeg;
        modelPart.yRot = modelPart.yRot + -this.leftFrontLeg.yRot * f;
        modelPart = this.rightFrontLeg;
        modelPart.zRot = modelPart.zRot + -this.leftFrontLeg.zRot * f;
    }
}


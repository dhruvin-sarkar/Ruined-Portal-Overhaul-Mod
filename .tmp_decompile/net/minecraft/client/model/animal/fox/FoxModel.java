/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.fox;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class FoxModel
extends EntityModel<FoxRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 8.0f, 3.35f, Set.of((Object)"head"));
    public final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private static final int LEG_SIZE = 6;
    private static final float HEAD_HEIGHT = 16.5f;
    private static final float LEG_POS = 17.5f;
    private float legMotionPos;

    public FoxModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.body = modelPart.getChild("body");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 5).addBox(-3.0f, -2.0f, -5.0f, 8.0f, 6.0f, 6.0f), PartPose.offset(-1.0f, 16.5f, -3.0f));
        partDefinition2.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(8, 1).addBox(-3.0f, -4.0f, -4.0f, 2.0f, 2.0f, 1.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(15, 1).addBox(3.0f, -4.0f, -4.0f, 2.0f, 2.0f, 1.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(6, 18).addBox(-1.0f, 2.01f, -8.0f, 4.0f, 2.0f, 3.0f), PartPose.ZERO);
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(24, 15).addBox(-3.0f, 3.999f, -3.5f, 6.0f, 11.0f, 6.0f), PartPose.offsetAndRotation(0.0f, 16.0f, -6.0f, 1.5707964f, 0.0f, 0.0f));
        CubeDeformation cubeDeformation = new CubeDeformation(0.001f);
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(4, 24).addBox(2.0f, 0.5f, -1.0f, 2.0f, 6.0f, 2.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(13, 24).addBox(2.0f, 0.5f, -1.0f, 2.0f, 6.0f, 2.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder2, PartPose.offset(-5.0f, 17.5f, 7.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(-1.0f, 17.5f, 7.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-5.0f, 17.5f, 0.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(-1.0f, 17.5f, 0.0f));
        partDefinition3.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(30, 0).addBox(2.0f, 0.0f, -1.0f, 4.0f, 9.0f, 5.0f), PartPose.offsetAndRotation(-4.0f, 15.0f, -1.0f, -0.05235988f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 48, 32);
    }

    @Override
    public void setupAnim(FoxRenderState foxRenderState) {
        float i;
        super.setupAnim(foxRenderState);
        float f = foxRenderState.walkAnimationSpeed;
        float g = foxRenderState.walkAnimationPos;
        this.rightHindLeg.xRot = Mth.cos(g * 0.6662f) * 1.4f * f;
        this.leftHindLeg.xRot = Mth.cos(g * 0.6662f + (float)Math.PI) * 1.4f * f;
        this.rightFrontLeg.xRot = Mth.cos(g * 0.6662f + (float)Math.PI) * 1.4f * f;
        this.leftFrontLeg.xRot = Mth.cos(g * 0.6662f) * 1.4f * f;
        this.head.zRot = foxRenderState.headRollAngle;
        this.rightHindLeg.visible = true;
        this.leftHindLeg.visible = true;
        this.rightFrontLeg.visible = true;
        this.leftFrontLeg.visible = true;
        float h = foxRenderState.ageScale;
        if (foxRenderState.isCrouching) {
            this.body.xRot += 0.10471976f;
            i = foxRenderState.crouchAmount;
            this.body.y += i * h;
            this.head.y += i * h;
        } else if (foxRenderState.isSleeping) {
            this.body.zRot = -1.5707964f;
            this.body.y += 5.0f * h;
            this.tail.xRot = -2.6179938f;
            if (foxRenderState.isBaby) {
                this.tail.xRot = -2.1816616f;
                this.body.z += 2.0f;
            }
            this.head.x += 2.0f * h;
            this.head.y += 2.99f * h;
            this.head.yRot = -2.0943952f;
            this.head.zRot = 0.0f;
            this.rightHindLeg.visible = false;
            this.leftHindLeg.visible = false;
            this.rightFrontLeg.visible = false;
            this.leftFrontLeg.visible = false;
        } else if (foxRenderState.isSitting) {
            this.body.xRot = 0.5235988f;
            this.body.y -= 7.0f * h;
            this.body.z += 3.0f * h;
            this.tail.xRot = 0.7853982f;
            this.tail.z -= 1.0f * h;
            this.head.xRot = 0.0f;
            this.head.yRot = 0.0f;
            if (foxRenderState.isBaby) {
                this.head.y -= 1.75f;
                this.head.z -= 0.375f;
            } else {
                this.head.y -= 6.5f;
                this.head.z += 2.75f;
            }
            this.rightHindLeg.xRot = -1.3089969f;
            this.rightHindLeg.y += 4.0f * h;
            this.rightHindLeg.z -= 0.25f * h;
            this.leftHindLeg.xRot = -1.3089969f;
            this.leftHindLeg.y += 4.0f * h;
            this.leftHindLeg.z -= 0.25f * h;
            this.rightFrontLeg.xRot = -0.2617994f;
            this.leftFrontLeg.xRot = -0.2617994f;
        }
        if (!(foxRenderState.isSleeping || foxRenderState.isFaceplanted || foxRenderState.isCrouching)) {
            this.head.xRot = foxRenderState.xRot * ((float)Math.PI / 180);
            this.head.yRot = foxRenderState.yRot * ((float)Math.PI / 180);
        }
        if (foxRenderState.isSleeping) {
            this.head.xRot = 0.0f;
            this.head.yRot = -2.0943952f;
            this.head.zRot = Mth.cos(foxRenderState.ageInTicks * 0.027f) / 22.0f;
        }
        if (foxRenderState.isCrouching) {
            this.body.yRot = i = Mth.cos(foxRenderState.ageInTicks) * 0.01f;
            this.rightHindLeg.zRot = i;
            this.leftHindLeg.zRot = i;
            this.rightFrontLeg.zRot = i / 2.0f;
            this.leftFrontLeg.zRot = i / 2.0f;
        }
        if (foxRenderState.isFaceplanted) {
            i = 0.1f;
            this.legMotionPos += 0.67f;
            this.rightHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f) * 0.1f;
            this.leftHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f + (float)Math.PI) * 0.1f;
            this.rightFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f + (float)Math.PI) * 0.1f;
            this.leftFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f) * 0.1f;
        }
    }
}


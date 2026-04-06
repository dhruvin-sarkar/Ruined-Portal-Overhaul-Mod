/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.panda;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class PandaModel
extends QuadrupedModel<PandaRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 23.0f, 4.8f, 2.7f, 3.0f, 49.0f, Set.of((Object)"head"));

    public PandaModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 6).addBox(-6.5f, -5.0f, -4.0f, 13.0f, 10.0f, 9.0f).texOffs(45, 16).addBox("nose", -3.5f, 0.0f, -6.0f, 7.0f, 5.0f, 2.0f).texOffs(52, 25).addBox("left_ear", 3.5f, -8.0f, -1.0f, 5.0f, 4.0f, 1.0f).texOffs(52, 25).addBox("right_ear", -8.5f, -8.0f, -1.0f, 5.0f, 4.0f, 1.0f), PartPose.offset(0.0f, 11.5f, -17.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 25).addBox(-9.5f, -13.0f, -6.5f, 19.0f, 26.0f, 13.0f), PartPose.offsetAndRotation(0.0f, 10.0f, 0.0f, 1.5707964f, 0.0f, 0.0f));
        int i = 9;
        int j = 6;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(40, 0).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 9.0f, 6.0f);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-5.5f, 15.0f, 9.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(5.5f, 15.0f, 9.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-5.5f, 15.0f, -9.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(5.5f, 15.0f, -9.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(PandaRenderState pandaRenderState) {
        super.setupAnim(pandaRenderState);
        if (pandaRenderState.isUnhappy) {
            this.head.yRot = 0.35f * Mth.sin(0.6f * pandaRenderState.ageInTicks);
            this.head.zRot = 0.35f * Mth.sin(0.6f * pandaRenderState.ageInTicks);
            this.rightFrontLeg.xRot = -0.75f * Mth.sin(0.3f * pandaRenderState.ageInTicks);
            this.leftFrontLeg.xRot = 0.75f * Mth.sin(0.3f * pandaRenderState.ageInTicks);
        } else {
            this.head.zRot = 0.0f;
        }
        if (pandaRenderState.isSneezing) {
            if (pandaRenderState.sneezeTime < 15) {
                this.head.xRot = -0.7853982f * (float)pandaRenderState.sneezeTime / 14.0f;
            } else if (pandaRenderState.sneezeTime < 20) {
                float f = (pandaRenderState.sneezeTime - 15) / 5;
                this.head.xRot = -0.7853982f + 0.7853982f * f;
            }
        }
        if (pandaRenderState.sitAmount > 0.0f) {
            this.body.xRot = Mth.rotLerpRad(pandaRenderState.sitAmount, this.body.xRot, 1.7407963f);
            this.head.xRot = Mth.rotLerpRad(pandaRenderState.sitAmount, this.head.xRot, 1.5707964f);
            this.rightFrontLeg.zRot = -0.27079642f;
            this.leftFrontLeg.zRot = 0.27079642f;
            this.rightHindLeg.zRot = 0.5707964f;
            this.leftHindLeg.zRot = -0.5707964f;
            if (pandaRenderState.isEating) {
                this.head.xRot = 1.5707964f + 0.2f * Mth.sin(pandaRenderState.ageInTicks * 0.6f);
                this.rightFrontLeg.xRot = -0.4f - 0.2f * Mth.sin(pandaRenderState.ageInTicks * 0.6f);
                this.leftFrontLeg.xRot = -0.4f - 0.2f * Mth.sin(pandaRenderState.ageInTicks * 0.6f);
            }
            if (pandaRenderState.isScared) {
                this.head.xRot = 2.1707964f;
                this.rightFrontLeg.xRot = -0.9f;
                this.leftFrontLeg.xRot = -0.9f;
            }
        } else {
            this.rightHindLeg.zRot = 0.0f;
            this.leftHindLeg.zRot = 0.0f;
            this.rightFrontLeg.zRot = 0.0f;
            this.leftFrontLeg.zRot = 0.0f;
        }
        if (pandaRenderState.lieOnBackAmount > 0.0f) {
            this.rightHindLeg.xRot = -0.6f * Mth.sin(pandaRenderState.ageInTicks * 0.15f);
            this.leftHindLeg.xRot = 0.6f * Mth.sin(pandaRenderState.ageInTicks * 0.15f);
            this.rightFrontLeg.xRot = 0.3f * Mth.sin(pandaRenderState.ageInTicks * 0.25f);
            this.leftFrontLeg.xRot = -0.3f * Mth.sin(pandaRenderState.ageInTicks * 0.25f);
            this.head.xRot = Mth.rotLerpRad(pandaRenderState.lieOnBackAmount, this.head.xRot, 1.5707964f);
        }
        if (pandaRenderState.rollAmount > 0.0f) {
            this.head.xRot = Mth.rotLerpRad(pandaRenderState.rollAmount, this.head.xRot, 2.0561945f);
            this.rightHindLeg.xRot = -0.5f * Mth.sin(pandaRenderState.ageInTicks * 0.5f);
            this.leftHindLeg.xRot = 0.5f * Mth.sin(pandaRenderState.ageInTicks * 0.5f);
            this.rightFrontLeg.xRot = 0.5f * Mth.sin(pandaRenderState.ageInTicks * 0.5f);
            this.leftFrontLeg.xRot = -0.5f * Mth.sin(pandaRenderState.ageInTicks * 0.5f);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.monster.dragon;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.DragonFlightHistory;

@Environment(value=EnvType.CLIENT)
public class EnderDragonModel
extends EntityModel<EnderDragonRenderState> {
    private static final int NECK_PART_COUNT = 5;
    private static final int TAIL_PART_COUNT = 12;
    private final ModelPart head;
    private final ModelPart[] neckParts = new ModelPart[5];
    private final ModelPart[] tailParts = new ModelPart[12];
    private final ModelPart jaw;
    private final ModelPart body;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;
    private final ModelPart leftFrontLeg;
    private final ModelPart leftFrontLegTip;
    private final ModelPart leftFrontFoot;
    private final ModelPart leftRearLeg;
    private final ModelPart leftRearLegTip;
    private final ModelPart leftRearFoot;
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;
    private final ModelPart rightFrontLeg;
    private final ModelPart rightFrontLegTip;
    private final ModelPart rightFrontFoot;
    private final ModelPart rightRearLeg;
    private final ModelPart rightRearLegTip;
    private final ModelPart rightRearFoot;

    private static String neckName(int i) {
        return "neck" + i;
    }

    private static String tailName(int i) {
        return "tail" + i;
    }

    public EnderDragonModel(ModelPart modelPart) {
        super(modelPart);
        int i;
        this.head = modelPart.getChild("head");
        this.jaw = this.head.getChild("jaw");
        for (i = 0; i < this.neckParts.length; ++i) {
            this.neckParts[i] = modelPart.getChild(EnderDragonModel.neckName(i));
        }
        for (i = 0; i < this.tailParts.length; ++i) {
            this.tailParts[i] = modelPart.getChild(EnderDragonModel.tailName(i));
        }
        this.body = modelPart.getChild("body");
        this.leftWing = this.body.getChild("left_wing");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.leftFrontLegTip = this.leftFrontLeg.getChild("left_front_leg_tip");
        this.leftFrontFoot = this.leftFrontLegTip.getChild("left_front_foot");
        this.leftRearLeg = this.body.getChild("left_hind_leg");
        this.leftRearLegTip = this.leftRearLeg.getChild("left_hind_leg_tip");
        this.leftRearFoot = this.leftRearLegTip.getChild("left_hind_foot");
        this.rightWing = this.body.getChild("right_wing");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.rightFrontLegTip = this.rightFrontLeg.getChild("right_front_leg_tip");
        this.rightFrontFoot = this.rightFrontLegTip.getChild("right_front_foot");
        this.rightRearLeg = this.body.getChild("right_hind_leg");
        this.rightRearLegTip = this.rightRearLeg.getChild("right_hind_leg_tip");
        this.rightRearFoot = this.rightRearLegTip.getChild("right_hind_foot");
    }

    public static LayerDefinition createBodyLayer() {
        int i;
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = -16.0f;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().addBox("upperlip", -6.0f, -1.0f, -24.0f, 12, 5, 16, 176, 44).addBox("upperhead", -8.0f, -8.0f, -10.0f, 16, 16, 16, 112, 30).mirror().addBox("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).addBox("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0).mirror().addBox("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).addBox("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0), PartPose.offset(0.0f, 20.0f, -62.0f));
        partDefinition2.addOrReplaceChild("jaw", CubeListBuilder.create().addBox("jaw", -6.0f, 0.0f, -16.0f, 12, 4, 16, 176, 65), PartPose.offset(0.0f, 4.0f, -8.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().addBox("box", -5.0f, -5.0f, -5.0f, 10, 10, 10, 192, 104).addBox("scale", -1.0f, -9.0f, -3.0f, 2, 4, 6, 48, 0);
        for (i = 0; i < 5; ++i) {
            partDefinition.addOrReplaceChild(EnderDragonModel.neckName(i), cubeListBuilder, PartPose.offset(0.0f, 20.0f, -12.0f - (float)i * 10.0f));
        }
        for (i = 0; i < 12; ++i) {
            partDefinition.addOrReplaceChild(EnderDragonModel.tailName(i), cubeListBuilder, PartPose.offset(0.0f, 10.0f, 60.0f + (float)i * 10.0f));
        }
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().addBox("body", -12.0f, 1.0f, -16.0f, 24, 24, 64, 0, 0).addBox("scale", -1.0f, -5.0f, -10.0f, 2, 6, 12, 220, 53).addBox("scale", -1.0f, -5.0f, 10.0f, 2, 6, 12, 220, 53).addBox("scale", -1.0f, -5.0f, 30.0f, 2, 6, 12, 220, 53), PartPose.offset(0.0f, 3.0f, 8.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("left_wing", CubeListBuilder.create().mirror().addBox("bone", 0.0f, -4.0f, -4.0f, 56, 8, 8, 112, 88).addBox("skin", 0.0f, 0.0f, 2.0f, 56, 0, 56, -56, 88), PartPose.offset(12.0f, 2.0f, -6.0f));
        partDefinition4.addOrReplaceChild("left_wing_tip", CubeListBuilder.create().mirror().addBox("bone", 0.0f, -2.0f, -2.0f, 56, 4, 4, 112, 136).addBox("skin", 0.0f, 0.0f, 2.0f, 56, 0, 56, -56, 144), PartPose.offset(56.0f, 0.0f, 0.0f));
        PartDefinition partDefinition5 = partDefinition3.addOrReplaceChild("left_front_leg", CubeListBuilder.create().addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, 112, 104), PartPose.offsetAndRotation(12.0f, 17.0f, -6.0f, 1.3f, 0.0f, 0.0f));
        PartDefinition partDefinition6 = partDefinition5.addOrReplaceChild("left_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, 226, 138), PartPose.offsetAndRotation(0.0f, 20.0f, -1.0f, -0.5f, 0.0f, 0.0f));
        partDefinition6.addOrReplaceChild("left_front_foot", CubeListBuilder.create().addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, 144, 104), PartPose.offsetAndRotation(0.0f, 23.0f, 0.0f, 0.75f, 0.0f, 0.0f));
        PartDefinition partDefinition7 = partDefinition3.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, 0, 0), PartPose.offsetAndRotation(16.0f, 13.0f, 34.0f, 1.0f, 0.0f, 0.0f));
        PartDefinition partDefinition8 = partDefinition7.addOrReplaceChild("left_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, 196, 0), PartPose.offsetAndRotation(0.0f, 32.0f, -4.0f, 0.5f, 0.0f, 0.0f));
        partDefinition8.addOrReplaceChild("left_hind_foot", CubeListBuilder.create().addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, 112, 0), PartPose.offsetAndRotation(0.0f, 31.0f, 4.0f, 0.75f, 0.0f, 0.0f));
        PartDefinition partDefinition9 = partDefinition3.addOrReplaceChild("right_wing", CubeListBuilder.create().addBox("bone", -56.0f, -4.0f, -4.0f, 56, 8, 8, 112, 88).addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, -56, 88), PartPose.offset(-12.0f, 2.0f, -6.0f));
        partDefinition9.addOrReplaceChild("right_wing_tip", CubeListBuilder.create().addBox("bone", -56.0f, -2.0f, -2.0f, 56, 4, 4, 112, 136).addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, -56, 144), PartPose.offset(-56.0f, 0.0f, 0.0f));
        PartDefinition partDefinition10 = partDefinition3.addOrReplaceChild("right_front_leg", CubeListBuilder.create().addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, 112, 104), PartPose.offsetAndRotation(-12.0f, 17.0f, -6.0f, 1.3f, 0.0f, 0.0f));
        PartDefinition partDefinition11 = partDefinition10.addOrReplaceChild("right_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, 226, 138), PartPose.offsetAndRotation(0.0f, 20.0f, -1.0f, -0.5f, 0.0f, 0.0f));
        partDefinition11.addOrReplaceChild("right_front_foot", CubeListBuilder.create().addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, 144, 104), PartPose.offsetAndRotation(0.0f, 23.0f, 0.0f, 0.75f, 0.0f, 0.0f));
        PartDefinition partDefinition12 = partDefinition3.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, 0, 0), PartPose.offsetAndRotation(-16.0f, 13.0f, 34.0f, 1.0f, 0.0f, 0.0f));
        PartDefinition partDefinition13 = partDefinition12.addOrReplaceChild("right_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, 196, 0), PartPose.offsetAndRotation(0.0f, 32.0f, -4.0f, 0.5f, 0.0f, 0.0f));
        partDefinition13.addOrReplaceChild("right_hind_foot", CubeListBuilder.create().addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, 112, 0), PartPose.offsetAndRotation(0.0f, 31.0f, 4.0f, 0.75f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 256, 256);
    }

    @Override
    public void setupAnim(EnderDragonRenderState enderDragonRenderState) {
        super.setupAnim(enderDragonRenderState);
        float f = enderDragonRenderState.flapTime * ((float)Math.PI * 2);
        this.jaw.xRot = (Mth.sin(f) + 1.0f) * 0.2f;
        float g = Mth.sin(f - 1.0f) + 1.0f;
        g = (g * g + g * 2.0f) * 0.05f;
        this.root.y = (g - 2.0f) * 16.0f;
        this.root.z = -48.0f;
        this.root.xRot = g * 2.0f * ((float)Math.PI / 180);
        float h = this.neckParts[0].x;
        float i = this.neckParts[0].y;
        float j = this.neckParts[0].z;
        float k = 1.5f;
        DragonFlightHistory.Sample sample = enderDragonRenderState.getHistoricalPos(6);
        float l = Mth.wrapDegrees(enderDragonRenderState.getHistoricalPos(5).yRot() - enderDragonRenderState.getHistoricalPos(10).yRot());
        float m = Mth.wrapDegrees(enderDragonRenderState.getHistoricalPos(5).yRot() + l / 2.0f);
        for (int n = 0; n < 5; ++n) {
            ModelPart modelPart = this.neckParts[n];
            DragonFlightHistory.Sample sample2 = enderDragonRenderState.getHistoricalPos(5 - n);
            float o = Mth.cos((float)n * 0.45f + f) * 0.15f;
            modelPart.yRot = Mth.wrapDegrees(sample2.yRot() - sample.yRot()) * ((float)Math.PI / 180) * 1.5f;
            modelPart.xRot = o + enderDragonRenderState.getHeadPartYOffset(n, sample, sample2) * ((float)Math.PI / 180) * 1.5f * 5.0f;
            modelPart.zRot = -Mth.wrapDegrees(sample2.yRot() - m) * ((float)Math.PI / 180) * 1.5f;
            modelPart.y = i;
            modelPart.z = j;
            modelPart.x = h;
            h -= Mth.sin(modelPart.yRot) * Mth.cos(modelPart.xRot) * 10.0f;
            i += Mth.sin(modelPart.xRot) * 10.0f;
            j -= Mth.cos(modelPart.yRot) * Mth.cos(modelPart.xRot) * 10.0f;
        }
        this.head.y = i;
        this.head.z = j;
        this.head.x = h;
        DragonFlightHistory.Sample sample3 = enderDragonRenderState.getHistoricalPos(0);
        this.head.yRot = Mth.wrapDegrees(sample3.yRot() - sample.yRot()) * ((float)Math.PI / 180);
        this.head.xRot = Mth.wrapDegrees(enderDragonRenderState.getHeadPartYOffset(6, sample, sample3)) * ((float)Math.PI / 180) * 1.5f * 5.0f;
        this.head.zRot = -Mth.wrapDegrees(sample3.yRot() - m) * ((float)Math.PI / 180);
        this.body.zRot = -l * 1.5f * ((float)Math.PI / 180);
        this.leftWing.xRot = 0.125f - Mth.cos(f) * 0.2f;
        this.leftWing.yRot = -0.25f;
        this.leftWing.zRot = -(Mth.sin(f) + 0.125f) * 0.8f;
        this.leftWingTip.zRot = (Mth.sin(f + 2.0f) + 0.5f) * 0.75f;
        this.rightWing.xRot = this.leftWing.xRot;
        this.rightWing.yRot = -this.leftWing.yRot;
        this.rightWing.zRot = -this.leftWing.zRot;
        this.rightWingTip.zRot = -this.leftWingTip.zRot;
        this.poseLimbs(g, this.leftFrontLeg, this.leftFrontLegTip, this.leftFrontFoot, this.leftRearLeg, this.leftRearLegTip, this.leftRearFoot);
        this.poseLimbs(g, this.rightFrontLeg, this.rightFrontLegTip, this.rightFrontFoot, this.rightRearLeg, this.rightRearLegTip, this.rightRearFoot);
        float p = 0.0f;
        i = this.tailParts[0].y;
        j = this.tailParts[0].z;
        h = this.tailParts[0].x;
        sample = enderDragonRenderState.getHistoricalPos(11);
        for (int q = 0; q < 12; ++q) {
            DragonFlightHistory.Sample sample4 = enderDragonRenderState.getHistoricalPos(12 + q);
            ModelPart modelPart2 = this.tailParts[q];
            modelPart2.yRot = (Mth.wrapDegrees(sample4.yRot() - sample.yRot()) * 1.5f + 180.0f) * ((float)Math.PI / 180);
            modelPart2.xRot = (p += Mth.sin((float)q * 0.45f + f) * 0.05f) + (float)(sample4.y() - sample.y()) * ((float)Math.PI / 180) * 1.5f * 5.0f;
            modelPart2.zRot = Mth.wrapDegrees(sample4.yRot() - m) * ((float)Math.PI / 180) * 1.5f;
            modelPart2.y = i;
            modelPart2.z = j;
            modelPart2.x = h;
            i += Mth.sin(modelPart2.xRot) * 10.0f;
            j -= Mth.cos(modelPart2.yRot) * Mth.cos(modelPart2.xRot) * 10.0f;
            h -= Mth.sin(modelPart2.yRot) * Mth.cos(modelPart2.xRot) * 10.0f;
        }
    }

    private void poseLimbs(float f, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, ModelPart modelPart4, ModelPart modelPart5, ModelPart modelPart6) {
        modelPart4.xRot = 1.0f + f * 0.1f;
        modelPart5.xRot = 0.5f + f * 0.1f;
        modelPart6.xRot = 0.75f + f * 0.1f;
        modelPart.xRot = 1.3f + f * 0.1f;
        modelPart2.xRot = -0.5f - f * 0.1f;
        modelPart3.xRot = 0.75f + f * 0.1f;
    }
}


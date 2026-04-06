/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.monster.blaze;

import java.util.Arrays;
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
public class BlazeModel
extends EntityModel<LivingEntityRenderState> {
    private final ModelPart[] upperBodyParts;
    private final ModelPart head;

    public BlazeModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.upperBodyParts = new ModelPart[12];
        Arrays.setAll(this.upperBodyParts, i -> modelPart.getChild(BlazeModel.getPartName(i)));
    }

    private static String getPartName(int i) {
        return "part" + i;
    }

    public static LayerDefinition createBodyLayer() {
        float j;
        float h;
        float g;
        int i;
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        float f = 0.0f;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(0.0f, 0.0f, 0.0f, 2.0f, 8.0f, 2.0f);
        for (i = 0; i < 4; ++i) {
            g = Mth.cos(f) * 9.0f;
            h = -2.0f + Mth.cos((float)(i * 2) * 0.25f);
            j = Mth.sin(f) * 9.0f;
            partDefinition.addOrReplaceChild(BlazeModel.getPartName(i), cubeListBuilder, PartPose.offset(g, h, j));
            f += 1.5707964f;
        }
        f = 0.7853982f;
        for (i = 4; i < 8; ++i) {
            g = Mth.cos(f) * 7.0f;
            h = 2.0f + Mth.cos((float)(i * 2) * 0.25f);
            j = Mth.sin(f) * 7.0f;
            partDefinition.addOrReplaceChild(BlazeModel.getPartName(i), cubeListBuilder, PartPose.offset(g, h, j));
            f += 1.5707964f;
        }
        f = 0.47123894f;
        for (i = 8; i < 12; ++i) {
            g = Mth.cos(f) * 5.0f;
            h = 11.0f + Mth.cos((float)i * 1.5f * 0.5f);
            j = Mth.sin(f) * 5.0f;
            partDefinition.addOrReplaceChild(BlazeModel.getPartName(i), cubeListBuilder, PartPose.offset(g, h, j));
            f += 1.5707964f;
        }
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState livingEntityRenderState) {
        int i;
        super.setupAnim(livingEntityRenderState);
        float f = livingEntityRenderState.ageInTicks * (float)Math.PI * -0.1f;
        for (i = 0; i < 4; ++i) {
            this.upperBodyParts[i].y = -2.0f + Mth.cos(((float)(i * 2) + livingEntityRenderState.ageInTicks) * 0.25f);
            this.upperBodyParts[i].x = Mth.cos(f) * 9.0f;
            this.upperBodyParts[i].z = Mth.sin(f) * 9.0f;
            f += 1.5707964f;
        }
        f = 0.7853982f + livingEntityRenderState.ageInTicks * (float)Math.PI * 0.03f;
        for (i = 4; i < 8; ++i) {
            this.upperBodyParts[i].y = 2.0f + Mth.cos(((float)(i * 2) + livingEntityRenderState.ageInTicks) * 0.25f);
            this.upperBodyParts[i].x = Mth.cos(f) * 7.0f;
            this.upperBodyParts[i].z = Mth.sin(f) * 7.0f;
            f += 1.5707964f;
        }
        f = 0.47123894f + livingEntityRenderState.ageInTicks * (float)Math.PI * -0.05f;
        for (i = 8; i < 12; ++i) {
            this.upperBodyParts[i].y = 11.0f + Mth.cos(((float)i * 1.5f + livingEntityRenderState.ageInTicks) * 0.5f);
            this.upperBodyParts[i].x = Mth.cos(f) * 5.0f;
            this.upperBodyParts[i].z = Mth.sin(f) * 5.0f;
            f += 1.5707964f;
        }
        this.head.yRot = livingEntityRenderState.yRot * ((float)Math.PI / 180);
        this.head.xRot = livingEntityRenderState.xRot * ((float)Math.PI / 180);
    }
}


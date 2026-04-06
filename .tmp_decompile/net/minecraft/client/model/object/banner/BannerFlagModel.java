/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.object.banner;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class BannerFlagModel
extends Model<Float> {
    private final ModelPart flag;

    public BannerFlagModel(ModelPart modelPart) {
        super(modelPart, RenderTypes::entitySolid);
        this.flag = modelPart.getChild("flag");
    }

    public static LayerDefinition createFlagLayer(boolean bl) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0f, 0.0f, -2.0f, 20.0f, 40.0f, 1.0f), PartPose.offset(0.0f, bl ? -44.0f : -20.5f, bl ? 0.0f : 10.5f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(Float float_) {
        super.setupAnim(float_);
        this.flag.xRot = (-0.0125f + 0.01f * Mth.cos((float)Math.PI * 2 * float_.floatValue())) * (float)Math.PI;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.fish;

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
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class TropicalFishSmallModel
extends EntityModel<TropicalFishRenderState> {
    private final ModelPart tail;

    public TropicalFishSmallModel(ModelPart modelPart) {
        super(modelPart);
        this.tail = modelPart.getChild("tail");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 22;
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -1.5f, -3.0f, 2.0f, 3.0f, 6.0f, cubeDeformation), PartPose.offset(0.0f, 22.0f, 0.0f));
        partDefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(22, -6).addBox(0.0f, -1.5f, 0.0f, 0.0f, 3.0f, 6.0f, cubeDeformation), PartPose.offset(0.0f, 22.0f, 3.0f));
        partDefinition.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(2, 16).addBox(-2.0f, -1.0f, 0.0f, 2.0f, 2.0f, 0.0f, cubeDeformation), PartPose.offsetAndRotation(-1.0f, 22.5f, 0.0f, 0.0f, 0.7853982f, 0.0f));
        partDefinition.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(2, 12).addBox(0.0f, -1.0f, 0.0f, 2.0f, 2.0f, 0.0f, cubeDeformation), PartPose.offsetAndRotation(1.0f, 22.5f, 0.0f, 0.0f, -0.7853982f, 0.0f));
        partDefinition.addOrReplaceChild("top_fin", CubeListBuilder.create().texOffs(10, -5).addBox(0.0f, -3.0f, 0.0f, 0.0f, 3.0f, 6.0f, cubeDeformation), PartPose.offset(0.0f, 20.5f, -3.0f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(TropicalFishRenderState tropicalFishRenderState) {
        super.setupAnim(tropicalFishRenderState);
        float f = tropicalFishRenderState.isInWater ? 1.0f : 1.5f;
        this.tail.yRot = -f * 0.45f * Mth.sin(0.6f * tropicalFishRenderState.ageInTicks);
    }
}


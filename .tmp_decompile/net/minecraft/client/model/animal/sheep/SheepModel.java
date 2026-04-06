/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.sheep;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.SheepRenderState;

@Environment(value=EnvType.CLIENT)
public class SheepModel
extends QuadrupedModel<SheepRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(false, 8.0f, 4.0f, 2.0f, 2.0f, 24.0f, Set.of((Object)"head"));

    public SheepModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = QuadrupedModel.createBodyMesh(12, false, true, CubeDeformation.NONE);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -4.0f, -6.0f, 6.0f, 6.0f, 8.0f), PartPose.offset(0.0f, 6.0f, -8.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-4.0f, -10.0f, -7.0f, 8.0f, 16.0f, 6.0f), PartPose.offsetAndRotation(0.0f, 5.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(SheepRenderState sheepRenderState) {
        super.setupAnim(sheepRenderState);
        this.head.y += sheepRenderState.headEatPositionScale * 9.0f * sheepRenderState.ageScale;
        this.head.xRot = sheepRenderState.headEatAngleScale;
    }
}


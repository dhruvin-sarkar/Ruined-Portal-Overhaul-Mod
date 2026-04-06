/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.ghast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;

@Environment(value=EnvType.CLIENT)
public class HappyGhastModel
extends EntityModel<HappyGhastRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.2375f);
    private static final float BODY_SQUEEZE = 0.9375f;
    private final ModelPart[] tentacles = new ModelPart[9];
    private final ModelPart body;

    public HappyGhastModel(ModelPart modelPart) {
        super(modelPart);
        this.body = modelPart.getChild("body");
        for (int i = 0; i < this.tentacles.length; ++i) {
            this.tentacles[i] = this.body.getChild(PartNames.tentacle(i));
        }
    }

    public static LayerDefinition createBodyLayer(boolean bl, CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f, cubeDeformation), PartPose.offset(0.0f, 16.0f, 0.0f));
        if (bl) {
            partDefinition2.addOrReplaceChild("inner_body", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0f, -16.0f, -8.0f, 16.0f, 16.0f, 16.0f, cubeDeformation.extend(-0.5f)), PartPose.offset(0.0f, 8.0f, 0.0f));
        }
        partDefinition2.addOrReplaceChild(PartNames.tentacle(0), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f, cubeDeformation), PartPose.offset(-3.75f, 7.0f, -5.0f));
        partDefinition2.addOrReplaceChild(PartNames.tentacle(1), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f, cubeDeformation), PartPose.offset(1.25f, 7.0f, -5.0f));
        partDefinition2.addOrReplaceChild(PartNames.tentacle(2), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 4.0f, 2.0f, cubeDeformation), PartPose.offset(6.25f, 7.0f, -5.0f));
        partDefinition2.addOrReplaceChild(PartNames.tentacle(3), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f, cubeDeformation), PartPose.offset(-6.25f, 7.0f, 0.0f));
        partDefinition2.addOrReplaceChild(PartNames.tentacle(4), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f, cubeDeformation), PartPose.offset(-1.25f, 7.0f, 0.0f));
        partDefinition2.addOrReplaceChild(PartNames.tentacle(5), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f, cubeDeformation), PartPose.offset(3.75f, 7.0f, 0.0f));
        partDefinition2.addOrReplaceChild(PartNames.tentacle(6), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f, cubeDeformation), PartPose.offset(-3.75f, 7.0f, 5.0f));
        partDefinition2.addOrReplaceChild(PartNames.tentacle(7), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f, cubeDeformation), PartPose.offset(1.25f, 7.0f, 5.0f));
        partDefinition2.addOrReplaceChild(PartNames.tentacle(8), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f, cubeDeformation), PartPose.offset(6.25f, 7.0f, 5.0f));
        return LayerDefinition.create(meshDefinition, 64, 64).apply(MeshTransformer.scaling(4.0f));
    }

    @Override
    public void setupAnim(HappyGhastRenderState happyGhastRenderState) {
        super.setupAnim(happyGhastRenderState);
        if (!happyGhastRenderState.bodyItem.isEmpty()) {
            this.body.xScale = 0.9375f;
            this.body.yScale = 0.9375f;
            this.body.zScale = 0.9375f;
        }
        GhastModel.animateTentacles(happyGhastRenderState, this.tentacles);
    }
}


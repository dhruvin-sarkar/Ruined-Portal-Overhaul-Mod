/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.object.boat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.object.boat.AbstractBoatModel;

@Environment(value=EnvType.CLIENT)
public class RaftModel
extends AbstractBoatModel {
    public RaftModel(ModelPart modelPart) {
        super(modelPart);
    }

    private static void addCommonParts(PartDefinition partDefinition) {
        partDefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0f, -11.0f, -4.0f, 28.0f, 20.0f, 4.0f).texOffs(0, 0).addBox(-14.0f, -9.0f, -8.0f, 28.0f, 16.0f, 4.0f), PartPose.offsetAndRotation(0.0f, -2.1f, 1.0f, 1.5708f, 0.0f, 0.0f));
        int i = 20;
        int j = 7;
        int k = 6;
        float f = -5.0f;
        partDefinition.addOrReplaceChild("left_paddle", CubeListBuilder.create().texOffs(0, 24).addBox(-1.0f, 0.0f, -5.0f, 2.0f, 2.0f, 18.0f).addBox(-1.001f, -3.0f, 8.0f, 1.0f, 6.0f, 7.0f), PartPose.offsetAndRotation(3.0f, -4.0f, 9.0f, 0.0f, 0.0f, 0.19634955f));
        partDefinition.addOrReplaceChild("right_paddle", CubeListBuilder.create().texOffs(40, 24).addBox(-1.0f, 0.0f, -5.0f, 2.0f, 2.0f, 18.0f).addBox(0.001f, -3.0f, 8.0f, 1.0f, 6.0f, 7.0f), PartPose.offsetAndRotation(3.0f, -4.0f, -9.0f, 0.0f, (float)Math.PI, 0.19634955f));
    }

    public static LayerDefinition createRaftModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        RaftModel.addCommonParts(partDefinition);
        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    public static LayerDefinition createChestRaftModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        RaftModel.addCommonParts(partDefinition);
        partDefinition.addOrReplaceChild("chest_bottom", CubeListBuilder.create().texOffs(0, 76).addBox(0.0f, 0.0f, 0.0f, 12.0f, 8.0f, 12.0f), PartPose.offsetAndRotation(-2.0f, -10.1f, -6.0f, 0.0f, -1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild("chest_lid", CubeListBuilder.create().texOffs(0, 59).addBox(0.0f, 0.0f, 0.0f, 12.0f, 4.0f, 12.0f), PartPose.offsetAndRotation(-2.0f, -14.1f, -6.0f, 0.0f, -1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild("chest_lock", CubeListBuilder.create().texOffs(0, 59).addBox(0.0f, 0.0f, 0.0f, 2.0f, 4.0f, 1.0f), PartPose.offsetAndRotation(-1.0f, -11.1f, -1.0f, 0.0f, -1.5707964f, 0.0f));
        return LayerDefinition.create(meshDefinition, 128, 128);
    }
}


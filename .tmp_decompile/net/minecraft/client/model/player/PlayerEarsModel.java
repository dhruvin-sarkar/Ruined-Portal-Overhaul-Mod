/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;

@Environment(value=EnvType.CLIENT)
public class PlayerEarsModel
extends PlayerModel {
    public PlayerEarsModel(ModelPart modelPart) {
        super(modelPart, false);
    }

    public static LayerDefinition createEarsLayer() {
        MeshDefinition meshDefinition = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition partDefinition = meshDefinition.getRoot().clearRecursively();
        PartDefinition partDefinition2 = partDefinition.getChild("head");
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(24, 0).addBox(-3.0f, -6.0f, -1.0f, 6.0f, 6.0f, 1.0f, new CubeDeformation(1.0f));
        partDefinition2.addOrReplaceChild("left_ear", cubeListBuilder, PartPose.offset(-6.0f, -6.0f, 0.0f));
        partDefinition2.addOrReplaceChild("right_ear", cubeListBuilder, PartPose.offset(6.0f, -6.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}


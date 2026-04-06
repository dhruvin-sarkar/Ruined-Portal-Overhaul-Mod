/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.nautilus;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.nautilus.NautilusModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

@Environment(value=EnvType.CLIENT)
public class NautilusSaddleModel
extends NautilusModel {
    private final ModelPart nautilus;
    private final ModelPart shell;

    public NautilusSaddleModel(ModelPart modelPart) {
        super(modelPart);
        this.nautilus = modelPart.getChild("root");
        this.shell = this.nautilus.getChild("shell");
    }

    public static LayerDefinition createSaddleLayer() {
        MeshDefinition meshDefinition = NautilusSaddleModel.createBodyMesh();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, 29.0f, -6.0f));
        partDefinition2.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0f, -10.0f, -7.0f, 14.0f, 10.0f, 16.0f, new CubeDeformation(0.2f)), PartPose.offset(0.0f, -13.0f, 5.0f));
        return LayerDefinition.create(meshDefinition, 128, 128);
    }
}


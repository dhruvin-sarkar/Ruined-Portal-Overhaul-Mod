/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.llama;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Environment(value=EnvType.CLIENT)
public class LlamaSpitModel
extends EntityModel<EntityRenderState> {
    private static final String MAIN = "main";

    public LlamaSpitModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 2;
        partDefinition.addOrReplaceChild(MAIN, CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, -4.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 0.0f, -4.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(2.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 2.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 0.0f, 2.0f, 2.0f, 2.0f, 2.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }
}


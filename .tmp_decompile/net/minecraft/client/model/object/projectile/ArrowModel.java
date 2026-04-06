/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.object.projectile;

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
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class ArrowModel
extends EntityModel<ArrowRenderState> {
    public ArrowModel(ModelPart modelPart) {
        super(modelPart, RenderTypes::entityCutout);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -2.5f, -2.5f, 0.0f, 5.0f, 5.0f), PartPose.offsetAndRotation(-11.0f, 0.0f, 0.0f, 0.7853982f, 0.0f, 0.0f).withScale(0.8f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 0).addBox(-12.0f, -2.0f, 0.0f, 16.0f, 4.0f, 0.0f, CubeDeformation.NONE, 1.0f, 0.8f);
        partDefinition.addOrReplaceChild("cross_1", cubeListBuilder, PartPose.rotation(0.7853982f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("cross_2", cubeListBuilder, PartPose.rotation(2.3561945f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition.transformed(partPose -> partPose.scaled(0.9f)), 32, 32);
    }

    @Override
    public void setupAnim(ArrowRenderState arrowRenderState) {
        super.setupAnim(arrowRenderState);
        if (arrowRenderState.shake > 0.0f) {
            float f = -Mth.sin(arrowRenderState.shake * 3.0f) * arrowRenderState.shake;
            this.root.zRot += f * ((float)Math.PI / 180);
        }
    }
}


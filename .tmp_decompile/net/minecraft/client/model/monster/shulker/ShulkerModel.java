/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.monster.shulker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ShulkerRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class ShulkerModel
extends EntityModel<ShulkerRenderState> {
    public static final String LID = "lid";
    private static final String BASE = "base";
    private final ModelPart lid;
    private final ModelPart head;

    public ShulkerModel(ModelPart modelPart) {
        super(modelPart, RenderTypes::entityCutoutNoCullZOffset);
        this.lid = modelPart.getChild(LID);
        this.head = modelPart.getChild("head");
    }

    private static MeshDefinition createShellMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(LID, CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -16.0f, -8.0f, 16.0f, 12.0f, 16.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
        partDefinition.addOrReplaceChild(BASE, CubeListBuilder.create().texOffs(0, 28).addBox(-8.0f, -8.0f, -8.0f, 16.0f, 8.0f, 16.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
        return meshDefinition;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = ShulkerModel.createShellMesh();
        meshDefinition.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 52).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 6.0f, 6.0f), PartPose.offset(0.0f, 12.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createBoxLayer() {
        MeshDefinition meshDefinition = ShulkerModel.createShellMesh();
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(ShulkerRenderState shulkerRenderState) {
        super.setupAnim(shulkerRenderState);
        float f = (0.5f + shulkerRenderState.peekAmount) * (float)Math.PI;
        float g = -1.0f + Mth.sin(f);
        float h = 0.0f;
        if (f > (float)Math.PI) {
            h = Mth.sin(shulkerRenderState.ageInTicks * 0.1f) * 0.7f;
        }
        this.lid.setPos(0.0f, 16.0f + Mth.sin(f) * 8.0f + h, 0.0f);
        this.lid.yRot = shulkerRenderState.peekAmount > 0.3f ? g * g * g * g * (float)Math.PI * 0.125f : 0.0f;
        this.head.xRot = shulkerRenderState.xRot * ((float)Math.PI / 180);
        this.head.yRot = (shulkerRenderState.yHeadRot - 180.0f - shulkerRenderState.yBodyRot) * ((float)Math.PI / 180);
    }
}


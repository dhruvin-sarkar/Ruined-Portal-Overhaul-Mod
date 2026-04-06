/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.dolphin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.DolphinRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class DolphinModel
extends EntityModel<DolphinRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5f);
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart tailFin;

    public DolphinModel(ModelPart modelPart) {
        super(modelPart);
        this.body = modelPart.getChild("body");
        this.tail = this.body.getChild("tail");
        this.tailFin = this.tail.getChild("tail_fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = 18.0f;
        float g = -8.0f;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(22, 0).addBox(-4.0f, -7.0f, 0.0f, 8.0f, 7.0f, 13.0f), PartPose.offset(0.0f, 22.0f, -5.0f));
        partDefinition2.addOrReplaceChild("back_fin", CubeListBuilder.create().texOffs(51, 0).addBox(-0.5f, 0.0f, 8.0f, 1.0f, 4.0f, 5.0f), PartPose.rotation(1.0471976f, 0.0f, 0.0f));
        partDefinition2.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(48, 20).mirror().addBox(-0.5f, -4.0f, 0.0f, 1.0f, 4.0f, 7.0f), PartPose.offsetAndRotation(2.0f, -2.0f, 4.0f, 1.0471976f, 0.0f, 2.0943952f));
        partDefinition2.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(48, 20).addBox(-0.5f, -4.0f, 0.0f, 1.0f, 4.0f, 7.0f), PartPose.offsetAndRotation(-2.0f, -2.0f, 4.0f, 1.0471976f, 0.0f, -2.0943952f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 19).addBox(-2.0f, -2.5f, 0.0f, 4.0f, 5.0f, 11.0f), PartPose.offsetAndRotation(0.0f, -2.5f, 11.0f, -0.10471976f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild("tail_fin", CubeListBuilder.create().texOffs(19, 20).addBox(-5.0f, -0.5f, 0.0f, 10.0f, 1.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 9.0f));
        PartDefinition partDefinition4 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -3.0f, -3.0f, 8.0f, 7.0f, 6.0f), PartPose.offset(0.0f, -4.0f, -3.0f));
        partDefinition4.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0f, 2.0f, -7.0f, 2.0f, 2.0f, 4.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(DolphinRenderState dolphinRenderState) {
        super.setupAnim(dolphinRenderState);
        this.body.xRot = dolphinRenderState.xRot * ((float)Math.PI / 180);
        this.body.yRot = dolphinRenderState.yRot * ((float)Math.PI / 180);
        if (dolphinRenderState.isMoving) {
            this.body.xRot += -0.05f - 0.05f * Mth.cos(dolphinRenderState.ageInTicks * 0.3f);
            this.tail.xRot = -0.1f * Mth.cos(dolphinRenderState.ageInTicks * 0.3f);
            this.tailFin.xRot = -0.2f * Mth.cos(dolphinRenderState.ageInTicks * 0.3f);
        }
    }
}


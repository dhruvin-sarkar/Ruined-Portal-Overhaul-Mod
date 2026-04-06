/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.effects;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EvokerFangsRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class EvokerFangsModel
extends EntityModel<EvokerFangsRenderState> {
    private static final String BASE = "base";
    private static final String UPPER_JAW = "upper_jaw";
    private static final String LOWER_JAW = "lower_jaw";
    private final ModelPart base;
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;

    public EvokerFangsModel(ModelPart modelPart) {
        super(modelPart);
        this.base = modelPart.getChild(BASE);
        this.upperJaw = this.base.getChild(UPPER_JAW);
        this.lowerJaw = this.base.getChild(LOWER_JAW);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(BASE, CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 10.0f, 12.0f, 10.0f), PartPose.offset(-5.0f, 24.0f, -5.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(40, 0).addBox(0.0f, 0.0f, 0.0f, 4.0f, 14.0f, 8.0f);
        partDefinition2.addOrReplaceChild(UPPER_JAW, cubeListBuilder, PartPose.offsetAndRotation(6.5f, 0.0f, 1.0f, 0.0f, 0.0f, 2.042035f));
        partDefinition2.addOrReplaceChild(LOWER_JAW, cubeListBuilder, PartPose.offsetAndRotation(3.5f, 0.0f, 9.0f, 0.0f, (float)Math.PI, 4.2411504f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(EvokerFangsRenderState evokerFangsRenderState) {
        super.setupAnim(evokerFangsRenderState);
        float f = evokerFangsRenderState.biteProgress;
        float g = Math.min(f * 2.0f, 1.0f);
        g = 1.0f - g * g * g;
        this.upperJaw.zRot = (float)Math.PI - g * 0.35f * (float)Math.PI;
        this.lowerJaw.zRot = (float)Math.PI + g * 0.35f * (float)Math.PI;
        this.base.y -= (f + Mth.sin(f * 2.7f)) * 7.2f;
        float h = 1.0f;
        if (f > 0.9f) {
            h *= (1.0f - f) / 0.1f;
        }
        this.root.y = 24.0f - 20.0f * h;
        this.root.xScale = h;
        this.root.yScale = h;
        this.root.zScale = h;
    }
}


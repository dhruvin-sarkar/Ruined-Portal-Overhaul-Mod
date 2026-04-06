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
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class SpinAttackEffectModel
extends EntityModel<AvatarRenderState> {
    private static final int BOX_COUNT = 2;
    private final ModelPart[] boxes = new ModelPart[2];

    public SpinAttackEffectModel(ModelPart modelPart) {
        super(modelPart);
        for (int i = 0; i < 2; ++i) {
            this.boxes[i] = modelPart.getChild(SpinAttackEffectModel.boxName(i));
        }
    }

    private static String boxName(int i) {
        return "box" + i;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        for (int i = 0; i < 2; ++i) {
            float f = -3.2f + 9.6f * (float)(i + 1);
            float g = 0.75f * (float)(i + 1);
            partDefinition.addOrReplaceChild(SpinAttackEffectModel.boxName(i), CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -16.0f + f, -8.0f, 16.0f, 32.0f, 16.0f), PartPose.ZERO.withScale(g));
        }
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(AvatarRenderState avatarRenderState) {
        super.setupAnim(avatarRenderState);
        for (int i = 0; i < this.boxes.length; ++i) {
            float f = avatarRenderState.ageInTicks * (float)(-(45 + (i + 1) * 5));
            this.boxes[i].yRot = Mth.wrapDegrees(f) * ((float)Math.PI / 180);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.equine;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.DonkeyRenderState;

@Environment(value=EnvType.CLIENT)
public class DonkeyModel
extends AbstractEquineModel<DonkeyRenderState> {
    public static final float DONKEY_SCALE = 0.87f;
    public static final float MULE_SCALE = 0.92f;
    private static final MeshTransformer DONKEY_TRANSFORMER = meshDefinition -> {
        DonkeyModel.modifyMesh(meshDefinition.getRoot());
        return meshDefinition;
    };
    private final ModelPart leftChest;
    private final ModelPart rightChest;

    public DonkeyModel(ModelPart modelPart) {
        super(modelPart);
        this.leftChest = this.body.getChild("left_chest");
        this.rightChest = this.body.getChild("right_chest");
    }

    public static LayerDefinition createBodyLayer(float f) {
        return LayerDefinition.create(AbstractEquineModel.createBodyMesh(CubeDeformation.NONE), 64, 64).apply(DONKEY_TRANSFORMER).apply(MeshTransformer.scaling(f));
    }

    public static LayerDefinition createBabyLayer(float f) {
        return LayerDefinition.create(AbstractEquineModel.createFullScaleBabyMesh(CubeDeformation.NONE), 64, 64).apply(DONKEY_TRANSFORMER).apply(BABY_TRANSFORMER).apply(MeshTransformer.scaling(f));
    }

    public static LayerDefinition createSaddleLayer(float f, boolean bl) {
        return EquineSaddleModel.createFullScaleSaddleLayer(bl).apply(DONKEY_TRANSFORMER).apply(bl ? AbstractEquineModel.BABY_TRANSFORMER : MeshTransformer.IDENTITY).apply(MeshTransformer.scaling(f));
    }

    private static void modifyMesh(PartDefinition partDefinition) {
        PartDefinition partDefinition2 = partDefinition.getChild("body");
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(26, 21).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 8.0f, 3.0f);
        partDefinition2.addOrReplaceChild("left_chest", cubeListBuilder, PartPose.offsetAndRotation(6.0f, -8.0f, 0.0f, 0.0f, -1.5707964f, 0.0f));
        partDefinition2.addOrReplaceChild("right_chest", cubeListBuilder, PartPose.offsetAndRotation(-6.0f, -8.0f, 0.0f, 0.0f, 1.5707964f, 0.0f));
        PartDefinition partDefinition3 = partDefinition.getChild("head_parts").getChild("head");
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(0, 12).addBox(-1.0f, -7.0f, 0.0f, 2.0f, 7.0f, 1.0f);
        partDefinition3.addOrReplaceChild("left_ear", cubeListBuilder2, PartPose.offsetAndRotation(1.25f, -10.0f, 4.0f, 0.2617994f, 0.0f, 0.2617994f));
        partDefinition3.addOrReplaceChild("right_ear", cubeListBuilder2, PartPose.offsetAndRotation(-1.25f, -10.0f, 4.0f, 0.2617994f, 0.0f, -0.2617994f));
    }

    @Override
    public void setupAnim(DonkeyRenderState donkeyRenderState) {
        super.setupAnim(donkeyRenderState);
        this.leftChest.visible = donkeyRenderState.hasChest;
        this.rightChest.visible = donkeyRenderState.hasChest;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.llama;

import java.util.Map;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class LlamaModel
extends EntityModel<LlamaRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = LlamaModel::transformToBaby;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightChest;
    private final ModelPart leftChest;

    public LlamaModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.rightChest = modelPart.getChild("right_chest");
        this.leftChest = modelPart.getChild("left_chest");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -14.0f, -10.0f, 4.0f, 4.0f, 9.0f, cubeDeformation).texOffs(0, 14).addBox("neck", -4.0f, -16.0f, -6.0f, 8.0f, 18.0f, 6.0f, cubeDeformation).texOffs(17, 0).addBox("ear", -4.0f, -19.0f, -4.0f, 3.0f, 3.0f, 2.0f, cubeDeformation).texOffs(17, 0).addBox("ear", 1.0f, -19.0f, -4.0f, 3.0f, 3.0f, 2.0f, cubeDeformation), PartPose.offset(0.0f, 7.0f, -6.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(29, 0).addBox(-6.0f, -10.0f, -7.0f, 12.0f, 18.0f, 10.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 5.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_chest", CubeListBuilder.create().texOffs(45, 28).addBox(-3.0f, 0.0f, 0.0f, 8.0f, 8.0f, 3.0f, cubeDeformation), PartPose.offsetAndRotation(-8.5f, 3.0f, 3.0f, 0.0f, 1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild("left_chest", CubeListBuilder.create().texOffs(45, 41).addBox(-3.0f, 0.0f, 0.0f, 8.0f, 8.0f, 3.0f, cubeDeformation), PartPose.offsetAndRotation(5.5f, 3.0f, 3.0f, 0.0f, 1.5707964f, 0.0f));
        int i = 4;
        int j = 14;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(29, 29).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 14.0f, 4.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-3.5f, 10.0f, 6.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(3.5f, 10.0f, 6.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-3.5f, 10.0f, -5.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(3.5f, 10.0f, -5.0f));
        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    private static MeshDefinition transformToBaby(MeshDefinition meshDefinition) {
        float f = 2.0f;
        float g = 0.7f;
        float h = 1.1f;
        UnaryOperator unaryOperator = partPose -> partPose.translated(0.0f, 21.0f, 3.52f).scaled(0.71428573f, 0.64935064f, 0.7936508f);
        UnaryOperator unaryOperator2 = partPose -> partPose.translated(0.0f, 33.0f, 0.0f).scaled(0.625f, 0.45454544f, 0.45454544f);
        UnaryOperator unaryOperator3 = partPose -> partPose.translated(0.0f, 33.0f, 0.0f).scaled(0.45454544f, 0.41322312f, 0.45454544f);
        MeshDefinition meshDefinition2 = new MeshDefinition();
        for (Map.Entry<String, PartDefinition> entry : meshDefinition.getRoot().getChildren()) {
            String string = entry.getKey();
            PartDefinition partDefinition = entry.getValue();
            UnaryOperator unaryOperator4 = switch (string) {
                case "head" -> unaryOperator;
                case "body" -> unaryOperator2;
                default -> unaryOperator3;
            };
            meshDefinition2.getRoot().addOrReplaceChild(string, partDefinition.transformed(unaryOperator4));
        }
        return meshDefinition2;
    }

    @Override
    public void setupAnim(LlamaRenderState llamaRenderState) {
        super.setupAnim(llamaRenderState);
        this.head.xRot = llamaRenderState.xRot * ((float)Math.PI / 180);
        this.head.yRot = llamaRenderState.yRot * ((float)Math.PI / 180);
        float f = llamaRenderState.walkAnimationSpeed;
        float g = llamaRenderState.walkAnimationPos;
        this.rightHindLeg.xRot = Mth.cos(g * 0.6662f) * 1.4f * f;
        this.leftHindLeg.xRot = Mth.cos(g * 0.6662f + (float)Math.PI) * 1.4f * f;
        this.rightFrontLeg.xRot = Mth.cos(g * 0.6662f + (float)Math.PI) * 1.4f * f;
        this.leftFrontLeg.xRot = Mth.cos(g * 0.6662f) * 1.4f * f;
        this.rightChest.visible = llamaRenderState.hasChest;
        this.leftChest.visible = llamaRenderState.hasChest;
    }
}


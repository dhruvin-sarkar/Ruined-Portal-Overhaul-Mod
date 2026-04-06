/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.chicken;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class ChickenModel
extends EntityModel<ChickenRenderState> {
    public static final String RED_THING = "red_thing";
    public static final float Y_OFFSET = 16.0f;
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(false, 5.0f, 2.0f, 2.0f, 1.99f, 24.0f, Set.of((Object)"head", (Object)"beak", (Object)"red_thing"));
    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightWing;
    private final ModelPart leftWing;

    public ChickenModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.rightLeg = modelPart.getChild("right_leg");
        this.leftLeg = modelPart.getChild("left_leg");
        this.rightWing = modelPart.getChild("right_wing");
        this.leftWing = modelPart.getChild("left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = ChickenModel.createBaseChickenModel();
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    protected static MeshDefinition createBaseChickenModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -6.0f, -2.0f, 4.0f, 6.0f, 3.0f), PartPose.offset(0.0f, 15.0f, -4.0f));
        partDefinition2.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(14, 0).addBox(-2.0f, -4.0f, -4.0f, 4.0f, 2.0f, 2.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild(RED_THING, CubeListBuilder.create().texOffs(14, 4).addBox(-1.0f, -2.0f, -3.0f, 2.0f, 2.0f, 2.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 9).addBox(-3.0f, -4.0f, -3.0f, 6.0f, 8.0f, 6.0f), PartPose.offsetAndRotation(0.0f, 16.0f, 0.0f, 1.5707964f, 0.0f, 0.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(26, 0).addBox(-1.0f, 0.0f, -3.0f, 3.0f, 5.0f, 3.0f);
        partDefinition.addOrReplaceChild("right_leg", cubeListBuilder, PartPose.offset(-2.0f, 19.0f, 1.0f));
        partDefinition.addOrReplaceChild("left_leg", cubeListBuilder, PartPose.offset(1.0f, 19.0f, 1.0f));
        partDefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(24, 13).addBox(0.0f, 0.0f, -3.0f, 1.0f, 4.0f, 6.0f), PartPose.offset(-4.0f, 13.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(24, 13).addBox(-1.0f, 0.0f, -3.0f, 1.0f, 4.0f, 6.0f), PartPose.offset(4.0f, 13.0f, 0.0f));
        return meshDefinition;
    }

    @Override
    public void setupAnim(ChickenRenderState chickenRenderState) {
        super.setupAnim(chickenRenderState);
        float f = (Mth.sin(chickenRenderState.flap) + 1.0f) * chickenRenderState.flapSpeed;
        this.head.xRot = chickenRenderState.xRot * ((float)Math.PI / 180);
        this.head.yRot = chickenRenderState.yRot * ((float)Math.PI / 180);
        float g = chickenRenderState.walkAnimationSpeed;
        float h = chickenRenderState.walkAnimationPos;
        this.rightLeg.xRot = Mth.cos(h * 0.6662f) * 1.4f * g;
        this.leftLeg.xRot = Mth.cos(h * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.rightWing.zRot = f;
        this.leftWing.zRot = -f;
    }
}


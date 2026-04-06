/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.rabbit;

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
import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class RabbitModel
extends EntityModel<RabbitRenderState> {
    private static final float REAR_JUMP_ANGLE = 50.0f;
    private static final float FRONT_JUMP_ANGLE = -40.0f;
    private static final float NEW_SCALE = 0.6f;
    private static final MeshTransformer ADULT_TRANSFORMER = MeshTransformer.scaling(0.6f);
    private static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 22.0f, 2.0f, 2.65f, 2.5f, 36.0f, Set.of((Object)"head", (Object)"left_ear", (Object)"right_ear", (Object)"nose"));
    private static final String LEFT_HAUNCH = "left_haunch";
    private static final String RIGHT_HAUNCH = "right_haunch";
    private final ModelPart leftHaunch;
    private final ModelPart rightHaunch;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart head;

    public RabbitModel(ModelPart modelPart) {
        super(modelPart);
        this.leftHaunch = modelPart.getChild(LEFT_HAUNCH);
        this.rightHaunch = modelPart.getChild(RIGHT_HAUNCH);
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.head = modelPart.getChild("head");
    }

    public static LayerDefinition createBodyLayer(boolean bl) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(LEFT_HAUNCH, CubeListBuilder.create().texOffs(30, 15).addBox(-1.0f, 0.0f, 0.0f, 2.0f, 4.0f, 5.0f), PartPose.offsetAndRotation(3.0f, 17.5f, 3.7f, -0.36651915f, 0.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild(RIGHT_HAUNCH, CubeListBuilder.create().texOffs(16, 15).addBox(-1.0f, 0.0f, 0.0f, 2.0f, 4.0f, 5.0f), PartPose.offsetAndRotation(-3.0f, 17.5f, 3.7f, -0.36651915f, 0.0f, 0.0f));
        partDefinition2.addOrReplaceChild("left_hind_foot", CubeListBuilder.create().texOffs(26, 24).addBox(-1.0f, 5.5f, -3.7f, 2.0f, 1.0f, 7.0f), PartPose.rotation(0.36651915f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild("right_hind_foot", CubeListBuilder.create().texOffs(8, 24).addBox(-1.0f, 5.5f, -3.7f, 2.0f, 1.0f, 7.0f), PartPose.rotation(0.36651915f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -2.0f, -10.0f, 6.0f, 5.0f, 10.0f), PartPose.offsetAndRotation(0.0f, 19.0f, 8.0f, -0.34906584f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(8, 15).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), PartPose.offsetAndRotation(3.0f, 17.0f, -1.0f, -0.19198622f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), PartPose.offsetAndRotation(-3.0f, 17.0f, -1.0f, -0.19198622f, 0.0f, 0.0f));
        PartDefinition partDefinition4 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 0).addBox(-2.5f, -4.0f, -5.0f, 5.0f, 4.0f, 5.0f), PartPose.offset(0.0f, 16.0f, -1.0f));
        partDefinition4.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(52, 0).addBox(-2.5f, -9.0f, -1.0f, 2.0f, 5.0f, 1.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.2617994f, 0.0f));
        partDefinition4.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(58, 0).addBox(0.5f, -9.0f, -1.0f, 2.0f, 5.0f, 1.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.2617994f, 0.0f));
        partDefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(52, 6).addBox(-1.5f, -1.5f, 0.0f, 3.0f, 3.0f, 2.0f), PartPose.offsetAndRotation(0.0f, 20.0f, 7.0f, -0.3490659f, 0.0f, 0.0f));
        partDefinition4.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(32, 9).addBox(-0.5f, -2.5f, -5.5f, 1.0f, 1.0f, 1.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32).apply(bl ? BABY_TRANSFORMER : ADULT_TRANSFORMER);
    }

    @Override
    public void setupAnim(RabbitRenderState rabbitRenderState) {
        super.setupAnim(rabbitRenderState);
        this.head.xRot = rabbitRenderState.xRot * ((float)Math.PI / 180);
        this.head.yRot = rabbitRenderState.yRot * ((float)Math.PI / 180);
        float f = Mth.sin(rabbitRenderState.jumpCompletion * (float)Math.PI);
        this.leftHaunch.xRot += f * 50.0f * ((float)Math.PI / 180);
        this.rightHaunch.xRot += f * 50.0f * ((float)Math.PI / 180);
        this.leftFrontLeg.xRot += f * -40.0f * ((float)Math.PI / 180);
        this.rightFrontLeg.xRot += f * -40.0f * ((float)Math.PI / 180);
    }
}


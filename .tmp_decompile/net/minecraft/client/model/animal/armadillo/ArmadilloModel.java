/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.armadillo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.definitions.ArmadilloAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ArmadilloRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class ArmadilloModel
extends EntityModel<ArmadilloRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.6f);
    private static final float MAX_DOWN_HEAD_ROTATION_EXTENT = 25.0f;
    private static final float MAX_UP_HEAD_ROTATION_EXTENT = 22.5f;
    private static final float MAX_WALK_ANIMATION_SPEED = 16.5f;
    private static final float WALK_ANIMATION_SCALE_FACTOR = 2.5f;
    private static final String HEAD_CUBE = "head_cube";
    private static final String RIGHT_EAR_CUBE = "right_ear_cube";
    private static final String LEFT_EAR_CUBE = "left_ear_cube";
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart cube;
    private final ModelPart head;
    private final ModelPart tail;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation rollOutAnimation;
    private final KeyframeAnimation rollUpAnimation;
    private final KeyframeAnimation peekAnimation;

    public ArmadilloModel(ModelPart modelPart) {
        super(modelPart);
        this.body = modelPart.getChild("body");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.head = this.body.getChild("head");
        this.tail = this.body.getChild("tail");
        this.cube = modelPart.getChild("cube");
        this.walkAnimation = ArmadilloAnimation.ARMADILLO_WALK.bake(modelPart);
        this.rollOutAnimation = ArmadilloAnimation.ARMADILLO_ROLL_OUT.bake(modelPart);
        this.rollUpAnimation = ArmadilloAnimation.ARMADILLO_ROLL_UP.bake(modelPart);
        this.peekAnimation = ArmadilloAnimation.ARMADILLO_PEEK.bake(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0f, -7.0f, -10.0f, 8.0f, 8.0f, 12.0f, new CubeDeformation(0.3f)).texOffs(0, 40).addBox(-4.0f, -7.0f, -10.0f, 8.0f, 8.0f, 12.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 21.0f, 4.0f));
        partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(44, 53).addBox(-0.5f, -0.0865f, 0.0933f, 1.0f, 6.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(0.0f, -3.0f, 1.0f, 0.5061f, 0.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0f, -2.0f, -11.0f));
        partDefinition3.addOrReplaceChild(HEAD_CUBE, CubeListBuilder.create().texOffs(43, 15).addBox(-1.5f, -1.0f, -1.0f, 3.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.3927f, 0.0f, 0.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-1.0f, -1.0f, 0.0f));
        partDefinition4.addOrReplaceChild(RIGHT_EAR_CUBE, CubeListBuilder.create().texOffs(43, 10).addBox(-2.0f, -3.0f, 0.0f, 2.0f, 5.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(-0.5f, 0.0f, -0.6f, 0.1886f, -0.3864f, -0.0718f));
        PartDefinition partDefinition5 = partDefinition3.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(1.0f, -2.0f, 0.0f));
        partDefinition5.addOrReplaceChild(LEFT_EAR_CUBE, CubeListBuilder.create().texOffs(47, 10).addBox(0.0f, -3.0f, 0.0f, 2.0f, 5.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(0.5f, 1.0f, -0.6f, 0.1886f, 0.3864f, 0.0718f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(51, 31).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offset(-2.0f, 21.0f, 4.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(42, 31).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offset(2.0f, 21.0f, 4.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(51, 43).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offset(-2.0f, 21.0f, -4.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(42, 43).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offset(2.0f, 21.0f, -4.0f));
        partDefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, -10.0f, -6.0f, 10.0f, 10.0f, 10.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 24.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(ArmadilloRenderState armadilloRenderState) {
        super.setupAnim(armadilloRenderState);
        if (armadilloRenderState.isHidingInShell) {
            this.body.skipDraw = true;
            this.leftHindLeg.visible = false;
            this.rightHindLeg.visible = false;
            this.tail.visible = false;
            this.cube.visible = true;
        } else {
            this.body.skipDraw = false;
            this.leftHindLeg.visible = true;
            this.rightHindLeg.visible = true;
            this.tail.visible = true;
            this.cube.visible = false;
            this.head.xRot = Mth.clamp(armadilloRenderState.xRot, -22.5f, 25.0f) * ((float)Math.PI / 180);
            this.head.yRot = Mth.clamp(armadilloRenderState.yRot, -32.5f, 32.5f) * ((float)Math.PI / 180);
        }
        this.walkAnimation.applyWalk(armadilloRenderState.walkAnimationPos, armadilloRenderState.walkAnimationSpeed, 16.5f, 2.5f);
        this.rollOutAnimation.apply(armadilloRenderState.rollOutAnimationState, armadilloRenderState.ageInTicks);
        this.rollUpAnimation.apply(armadilloRenderState.rollUpAnimationState, armadilloRenderState.ageInTicks);
        this.peekAnimation.apply(armadilloRenderState.peekAnimationState, armadilloRenderState.ageInTicks);
    }
}


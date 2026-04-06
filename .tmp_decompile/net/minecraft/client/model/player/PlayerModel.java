/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.player;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.HumanoidArm;

@Environment(value=EnvType.CLIENT)
public class PlayerModel
extends HumanoidModel<AvatarRenderState> {
    protected static final String LEFT_SLEEVE = "left_sleeve";
    protected static final String RIGHT_SLEEVE = "right_sleeve";
    protected static final String LEFT_PANTS = "left_pants";
    protected static final String RIGHT_PANTS = "right_pants";
    private final List<ModelPart> bodyParts;
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final boolean slim;

    public PlayerModel(ModelPart modelPart, boolean bl) {
        super(modelPart, RenderTypes::entityTranslucent);
        this.slim = bl;
        this.leftSleeve = this.leftArm.getChild(LEFT_SLEEVE);
        this.rightSleeve = this.rightArm.getChild(RIGHT_SLEEVE);
        this.leftPants = this.leftLeg.getChild(LEFT_PANTS);
        this.rightPants = this.rightLeg.getChild(RIGHT_PANTS);
        this.jacket = this.body.getChild("jacket");
        this.bodyParts = List.of((Object)this.head, (Object)this.body, (Object)this.leftArm, (Object)this.rightArm, (Object)this.leftLeg, (Object)this.rightLeg);
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean bl) {
        PartDefinition partDefinition3;
        PartDefinition partDefinition2;
        MeshDefinition meshDefinition = HumanoidModel.createMesh(cubeDeformation, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = 0.25f;
        if (bl) {
            partDefinition2 = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(5.0f, 2.0f, 0.0f));
            partDefinition3 = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(-5.0f, 2.0f, 0.0f));
            partDefinition2.addOrReplaceChild(LEFT_SLEEVE, CubeListBuilder.create().texOffs(48, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.ZERO);
            partDefinition3.addOrReplaceChild(RIGHT_SLEEVE, CubeListBuilder.create().texOffs(40, 32).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.ZERO);
        } else {
            partDefinition2 = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(5.0f, 2.0f, 0.0f));
            partDefinition3 = partDefinition.getChild("right_arm");
            partDefinition2.addOrReplaceChild(LEFT_SLEEVE, CubeListBuilder.create().texOffs(48, 48).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.ZERO);
            partDefinition3.addOrReplaceChild(RIGHT_SLEEVE, CubeListBuilder.create().texOffs(40, 32).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.ZERO);
        }
        partDefinition2 = partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(1.9f, 12.0f, 0.0f));
        partDefinition3 = partDefinition.getChild("right_leg");
        partDefinition2.addOrReplaceChild(LEFT_PANTS, CubeListBuilder.create().texOffs(0, 48).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.ZERO);
        partDefinition3.addOrReplaceChild(RIGHT_PANTS, CubeListBuilder.create().texOffs(0, 32).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.ZERO);
        PartDefinition partDefinition4 = partDefinition.getChild("body");
        partDefinition4.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.ZERO);
        return meshDefinition;
    }

    public static ArmorModelSet<MeshDefinition> createArmorMeshSet(CubeDeformation cubeDeformation, CubeDeformation cubeDeformation2) {
        return HumanoidModel.createArmorMeshSet(cubeDeformation, cubeDeformation2).map(meshDefinition -> {
            PartDefinition partDefinition = meshDefinition.getRoot();
            PartDefinition partDefinition2 = partDefinition.getChild("left_arm");
            PartDefinition partDefinition3 = partDefinition.getChild("right_arm");
            partDefinition2.addOrReplaceChild(LEFT_SLEEVE, CubeListBuilder.create(), PartPose.ZERO);
            partDefinition3.addOrReplaceChild(RIGHT_SLEEVE, CubeListBuilder.create(), PartPose.ZERO);
            PartDefinition partDefinition4 = partDefinition.getChild("left_leg");
            PartDefinition partDefinition5 = partDefinition.getChild("right_leg");
            partDefinition4.addOrReplaceChild(LEFT_PANTS, CubeListBuilder.create(), PartPose.ZERO);
            partDefinition5.addOrReplaceChild(RIGHT_PANTS, CubeListBuilder.create(), PartPose.ZERO);
            PartDefinition partDefinition6 = partDefinition.getChild("body");
            partDefinition6.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);
            return meshDefinition;
        });
    }

    @Override
    public void setupAnim(AvatarRenderState avatarRenderState) {
        boolean bl;
        this.body.visible = bl = !avatarRenderState.isSpectator;
        this.rightArm.visible = bl;
        this.leftArm.visible = bl;
        this.rightLeg.visible = bl;
        this.leftLeg.visible = bl;
        this.hat.visible = avatarRenderState.showHat;
        this.jacket.visible = avatarRenderState.showJacket;
        this.leftPants.visible = avatarRenderState.showLeftPants;
        this.rightPants.visible = avatarRenderState.showRightPants;
        this.leftSleeve.visible = avatarRenderState.showLeftSleeve;
        this.rightSleeve.visible = avatarRenderState.showRightSleeve;
        super.setupAnim(avatarRenderState);
    }

    @Override
    public void setAllVisible(boolean bl) {
        super.setAllVisible(bl);
        this.leftSleeve.visible = bl;
        this.rightSleeve.visible = bl;
        this.leftPants.visible = bl;
        this.rightPants.visible = bl;
        this.jacket.visible = bl;
    }

    @Override
    public void translateToHand(AvatarRenderState avatarRenderState, HumanoidArm humanoidArm, PoseStack poseStack) {
        this.root().translateAndRotate(poseStack);
        ModelPart modelPart = this.getArm(humanoidArm);
        if (this.slim) {
            float f = 0.5f * (float)(humanoidArm == HumanoidArm.RIGHT ? 1 : -1);
            modelPart.x += f;
            modelPart.translateAndRotate(poseStack);
            modelPart.x -= f;
        } else {
            modelPart.translateAndRotate(poseStack);
        }
    }

    public ModelPart getRandomBodyPart(RandomSource randomSource) {
        return Util.getRandom(this.bodyParts, randomSource);
    }
}


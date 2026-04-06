/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.model.animal.allay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class AllayModel
extends EntityModel<AllayRenderState>
implements ArmedModel<AllayRenderState> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    private static final float FLYING_ANIMATION_X_ROT = 0.7853982f;
    private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = -1.134464f;
    private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = -1.0471976f;

    public AllayModel(ModelPart modelPart) {
        super(modelPart.getChild("root"), RenderTypes::entityTranslucent);
        this.head = this.root.getChild("head");
        this.body = this.root.getChild("body");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.right_wing = this.body.getChild("right_wing");
        this.left_wing = this.body.getChild("left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, 23.5f, 0.0f));
        partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, -5.0f, -2.5f, 5.0f, 5.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -3.99f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5f, 0.0f, -1.0f, 3.0f, 4.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(0, 16).addBox(-1.5f, 0.0f, -1.0f, 3.0f, 5.0f, 2.0f, new CubeDeformation(-0.2f)), PartPose.offset(0.0f, -4.0f, 0.0f));
        partDefinition3.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 0).addBox(-0.75f, -0.5f, -1.0f, 1.0f, 4.0f, 2.0f, new CubeDeformation(-0.01f)), PartPose.offset(-1.75f, 0.5f, 0.0f));
        partDefinition3.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(23, 6).addBox(-0.25f, -0.5f, -1.0f, 1.0f, 4.0f, 2.0f, new CubeDeformation(-0.01f)), PartPose.offset(1.75f, 0.5f, 0.0f));
        partDefinition3.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-0.5f, 0.0f, 0.6f));
        partDefinition3.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(0.5f, 0.0f, 0.6f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(AllayRenderState allayRenderState) {
        float p;
        float o;
        float n;
        super.setupAnim(allayRenderState);
        float f = allayRenderState.walkAnimationSpeed;
        float g = allayRenderState.walkAnimationPos;
        float h = allayRenderState.ageInTicks * 20.0f * ((float)Math.PI / 180) + g;
        float i = Mth.cos(h) * (float)Math.PI * 0.15f + f;
        float j = allayRenderState.ageInTicks * 9.0f * ((float)Math.PI / 180);
        float k = Math.min(f / 0.3f, 1.0f);
        float l = 1.0f - k;
        float m = allayRenderState.holdingAnimationProgress;
        if (allayRenderState.isDancing) {
            n = allayRenderState.ageInTicks * 8.0f * ((float)Math.PI / 180) + f;
            o = Mth.cos(n) * 16.0f * ((float)Math.PI / 180);
            p = allayRenderState.spinningProgress;
            float q = Mth.cos(n) * 14.0f * ((float)Math.PI / 180);
            float r = Mth.cos(n) * 30.0f * ((float)Math.PI / 180);
            this.root.yRot = allayRenderState.isSpinning ? (float)Math.PI * 4 * p : this.root.yRot;
            this.root.zRot = o * (1.0f - p);
            this.head.yRot = r * (1.0f - p);
            this.head.zRot = q * (1.0f - p);
        } else {
            this.head.xRot = allayRenderState.xRot * ((float)Math.PI / 180);
            this.head.yRot = allayRenderState.yRot * ((float)Math.PI / 180);
        }
        this.right_wing.xRot = 0.43633232f * (1.0f - k);
        this.right_wing.yRot = -0.7853982f + i;
        this.left_wing.xRot = 0.43633232f * (1.0f - k);
        this.left_wing.yRot = 0.7853982f - i;
        this.body.xRot = k * 0.7853982f;
        n = m * Mth.lerp(k, -1.0471976f, -1.134464f);
        this.root.y += (float)Math.cos(j) * 0.25f * l;
        this.right_arm.xRot = n;
        this.left_arm.xRot = n;
        o = l * (1.0f - m);
        p = 0.43633232f - Mth.cos(j + 4.712389f) * (float)Math.PI * 0.075f * o;
        this.left_arm.zRot = -p;
        this.right_arm.zRot = p;
        this.right_arm.yRot = 0.27925268f * m;
        this.left_arm.yRot = -0.27925268f * m;
    }

    @Override
    public void translateToHand(AllayRenderState allayRenderState, HumanoidArm humanoidArm, PoseStack poseStack) {
        float f = 1.0f;
        float g = 3.0f;
        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        poseStack.translate(0.0f, 0.0625f, 0.1875f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotation(this.right_arm.xRot));
        poseStack.scale(0.7f, 0.7f, 0.7f);
        poseStack.translate(0.0625f, 0.0f, 0.0f);
    }
}


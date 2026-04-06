/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class HumanoidModel<T extends HumanoidRenderState>
extends EntityModel<T>
implements ArmedModel<T>,
HeadedModel {
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 16.0f, 0.0f, 2.0f, 2.0f, 24.0f, Set.of((Object)"head"));
    public static final float OVERLAY_SCALE = 0.25f;
    public static final float HAT_OVERLAY_SCALE = 0.5f;
    public static final float LEGGINGS_OVERLAY_SCALE = -0.1f;
    private static final float DUCK_WALK_ROTATION = 0.005f;
    private static final float SPYGLASS_ARM_ROT_Y = 0.2617994f;
    private static final float SPYGLASS_ARM_ROT_X = 1.9198622f;
    private static final float SPYGLASS_ARM_CROUCH_ROT_X = 0.2617994f;
    private static final float HIGHEST_SHIELD_BLOCKING_ANGLE = -1.3962634f;
    private static final float LOWEST_SHIELD_BLOCKING_ANGLE = 0.43633232f;
    private static final float HORIZONTAL_SHIELD_MOVEMENT_LIMIT = 0.5235988f;
    public static final float TOOT_HORN_XROT_BASE = 1.4835298f;
    public static final float TOOT_HORN_YROT_BASE = 0.5235988f;
    public final ModelPart head;
    public final ModelPart hat;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;

    public HumanoidModel(ModelPart modelPart) {
        this(modelPart, RenderTypes::entityCutoutNoCull);
    }

    public HumanoidModel(ModelPart modelPart, Function<Identifier, RenderType> function) {
        super(modelPart, function);
        this.head = modelPart.getChild("head");
        this.hat = this.head.getChild("hat");
        this.body = modelPart.getChild("body");
        this.rightArm = modelPart.getChild("right_arm");
        this.leftArm = modelPart.getChild("left_arm");
        this.rightLeg = modelPart.getChild("right_leg");
        this.leftLeg = modelPart.getChild("left_leg");
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, float f) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        partDefinition2.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation.extend(0.5f)), PartPose.ZERO);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(-5.0f, 2.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(5.0f, 2.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(-1.9f, 12.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(1.9f, 12.0f + f, 0.0f));
        return meshDefinition;
    }

    public static ArmorModelSet<MeshDefinition> createArmorMeshSet(CubeDeformation cubeDeformation, CubeDeformation cubeDeformation2) {
        return HumanoidModel.createArmorMeshSet(HumanoidModel::createBaseArmorMesh, cubeDeformation, cubeDeformation2);
    }

    protected static ArmorModelSet<MeshDefinition> createArmorMeshSet(Function<CubeDeformation, MeshDefinition> function, CubeDeformation cubeDeformation, CubeDeformation cubeDeformation2) {
        MeshDefinition meshDefinition = function.apply(cubeDeformation2);
        meshDefinition.getRoot().retainPartsAndChildren(Set.of((Object)"head"));
        MeshDefinition meshDefinition2 = function.apply(cubeDeformation2);
        meshDefinition2.getRoot().retainExactParts(Set.of((Object)"body", (Object)"left_arm", (Object)"right_arm"));
        MeshDefinition meshDefinition3 = function.apply(cubeDeformation);
        meshDefinition3.getRoot().retainExactParts(Set.of((Object)"left_leg", (Object)"right_leg", (Object)"body"));
        MeshDefinition meshDefinition4 = function.apply(cubeDeformation2);
        meshDefinition4.getRoot().retainExactParts(Set.of((Object)"left_leg", (Object)"right_leg"));
        return new ArmorModelSet<MeshDefinition>(meshDefinition, meshDefinition2, meshDefinition3, meshDefinition4);
    }

    private static MeshDefinition createBaseArmorMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(cubeDeformation, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(-0.1f)), PartPose.offset(-1.9f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(-0.1f)), PartPose.offset(1.9f, 12.0f, 0.0f));
        return meshDefinition;
    }

    @Override
    public void setupAnim(T humanoidRenderState) {
        boolean bl2;
        super.setupAnim(humanoidRenderState);
        ArmPose armPose = ((HumanoidRenderState)humanoidRenderState).leftArmPose;
        ArmPose armPose2 = ((HumanoidRenderState)humanoidRenderState).rightArmPose;
        float f = ((HumanoidRenderState)humanoidRenderState).swimAmount;
        boolean bl = ((HumanoidRenderState)humanoidRenderState).isFallFlying;
        this.head.xRot = ((HumanoidRenderState)humanoidRenderState).xRot * ((float)Math.PI / 180);
        this.head.yRot = ((HumanoidRenderState)humanoidRenderState).yRot * ((float)Math.PI / 180);
        if (bl) {
            this.head.xRot = -0.7853982f;
        } else if (f > 0.0f) {
            this.head.xRot = Mth.rotLerpRad(f, this.head.xRot, -0.7853982f);
        }
        float g = ((HumanoidRenderState)humanoidRenderState).walkAnimationPos;
        float h = ((HumanoidRenderState)humanoidRenderState).walkAnimationSpeed;
        this.rightArm.xRot = Mth.cos(g * 0.6662f + (float)Math.PI) * 2.0f * h * 0.5f / ((HumanoidRenderState)humanoidRenderState).speedValue;
        this.leftArm.xRot = Mth.cos(g * 0.6662f) * 2.0f * h * 0.5f / ((HumanoidRenderState)humanoidRenderState).speedValue;
        this.rightLeg.xRot = Mth.cos(g * 0.6662f) * 1.4f * h / ((HumanoidRenderState)humanoidRenderState).speedValue;
        this.leftLeg.xRot = Mth.cos(g * 0.6662f + (float)Math.PI) * 1.4f * h / ((HumanoidRenderState)humanoidRenderState).speedValue;
        this.rightLeg.yRot = 0.005f;
        this.leftLeg.yRot = -0.005f;
        this.rightLeg.zRot = 0.005f;
        this.leftLeg.zRot = -0.005f;
        if (((HumanoidRenderState)humanoidRenderState).isPassenger) {
            this.rightArm.xRot += -0.62831855f;
            this.leftArm.xRot += -0.62831855f;
            this.rightLeg.xRot = -1.4137167f;
            this.rightLeg.yRot = 0.31415927f;
            this.rightLeg.zRot = 0.07853982f;
            this.leftLeg.xRot = -1.4137167f;
            this.leftLeg.yRot = -0.31415927f;
            this.leftLeg.zRot = -0.07853982f;
        }
        boolean bl3 = bl2 = ((HumanoidRenderState)humanoidRenderState).mainArm == HumanoidArm.RIGHT;
        if (((HumanoidRenderState)humanoidRenderState).isUsingItem) {
            boolean bl4 = bl3 = ((HumanoidRenderState)humanoidRenderState).useItemHand == InteractionHand.MAIN_HAND;
            if (bl3 == bl2) {
                this.poseRightArm(humanoidRenderState);
                if (!((HumanoidRenderState)humanoidRenderState).rightArmPose.affectsOffhandPose()) {
                    this.poseLeftArm(humanoidRenderState);
                }
            } else {
                this.poseLeftArm(humanoidRenderState);
                if (!((HumanoidRenderState)humanoidRenderState).leftArmPose.affectsOffhandPose()) {
                    this.poseRightArm(humanoidRenderState);
                }
            }
        } else {
            boolean bl5 = bl3 = bl2 ? armPose.isTwoHanded() : armPose2.isTwoHanded();
            if (bl2 != bl3) {
                this.poseLeftArm(humanoidRenderState);
                if (!((HumanoidRenderState)humanoidRenderState).leftArmPose.affectsOffhandPose()) {
                    this.poseRightArm(humanoidRenderState);
                }
            } else {
                this.poseRightArm(humanoidRenderState);
                if (!((HumanoidRenderState)humanoidRenderState).rightArmPose.affectsOffhandPose()) {
                    this.poseLeftArm(humanoidRenderState);
                }
            }
        }
        this.setupAttackAnimation(humanoidRenderState);
        if (((HumanoidRenderState)humanoidRenderState).isCrouching) {
            this.body.xRot = 0.5f;
            this.rightArm.xRot += 0.4f;
            this.leftArm.xRot += 0.4f;
            this.rightLeg.z += 4.0f;
            this.leftLeg.z += 4.0f;
            this.head.y += 4.2f;
            this.body.y += 3.2f;
            this.leftArm.y += 3.2f;
            this.rightArm.y += 3.2f;
        }
        if (armPose2 != ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.rightArm, ((HumanoidRenderState)humanoidRenderState).ageInTicks, 1.0f);
        }
        if (armPose != ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.leftArm, ((HumanoidRenderState)humanoidRenderState).ageInTicks, -1.0f);
        }
        if (f > 0.0f) {
            float l;
            float k;
            float i = g % 26.0f;
            HumanoidArm humanoidArm = ((HumanoidRenderState)humanoidRenderState).attackArm;
            float j = ((HumanoidRenderState)humanoidRenderState).rightArmPose == ArmPose.SPEAR || humanoidArm == HumanoidArm.RIGHT && ((HumanoidRenderState)humanoidRenderState).attackTime > 0.0f ? 0.0f : f;
            float f2 = k = ((HumanoidRenderState)humanoidRenderState).leftArmPose == ArmPose.SPEAR || humanoidArm == HumanoidArm.LEFT && ((HumanoidRenderState)humanoidRenderState).attackTime > 0.0f ? 0.0f : f;
            if (!((HumanoidRenderState)humanoidRenderState).isUsingItem) {
                if (i < 14.0f) {
                    this.leftArm.xRot = Mth.rotLerpRad(k, this.leftArm.xRot, 0.0f);
                    this.rightArm.xRot = Mth.lerp(j, this.rightArm.xRot, 0.0f);
                    this.leftArm.yRot = Mth.rotLerpRad(k, this.leftArm.yRot, (float)Math.PI);
                    this.rightArm.yRot = Mth.lerp(j, this.rightArm.yRot, (float)Math.PI);
                    this.leftArm.zRot = Mth.rotLerpRad(k, this.leftArm.zRot, (float)Math.PI + 1.8707964f * this.quadraticArmUpdate(i) / this.quadraticArmUpdate(14.0f));
                    this.rightArm.zRot = Mth.lerp(j, this.rightArm.zRot, (float)Math.PI - 1.8707964f * this.quadraticArmUpdate(i) / this.quadraticArmUpdate(14.0f));
                } else if (i >= 14.0f && i < 22.0f) {
                    l = (i - 14.0f) / 8.0f;
                    this.leftArm.xRot = Mth.rotLerpRad(k, this.leftArm.xRot, 1.5707964f * l);
                    this.rightArm.xRot = Mth.lerp(j, this.rightArm.xRot, 1.5707964f * l);
                    this.leftArm.yRot = Mth.rotLerpRad(k, this.leftArm.yRot, (float)Math.PI);
                    this.rightArm.yRot = Mth.lerp(j, this.rightArm.yRot, (float)Math.PI);
                    this.leftArm.zRot = Mth.rotLerpRad(k, this.leftArm.zRot, 5.012389f - 1.8707964f * l);
                    this.rightArm.zRot = Mth.lerp(j, this.rightArm.zRot, 1.2707963f + 1.8707964f * l);
                } else if (i >= 22.0f && i < 26.0f) {
                    l = (i - 22.0f) / 4.0f;
                    this.leftArm.xRot = Mth.rotLerpRad(k, this.leftArm.xRot, 1.5707964f - 1.5707964f * l);
                    this.rightArm.xRot = Mth.lerp(j, this.rightArm.xRot, 1.5707964f - 1.5707964f * l);
                    this.leftArm.yRot = Mth.rotLerpRad(k, this.leftArm.yRot, (float)Math.PI);
                    this.rightArm.yRot = Mth.lerp(j, this.rightArm.yRot, (float)Math.PI);
                    this.leftArm.zRot = Mth.rotLerpRad(k, this.leftArm.zRot, (float)Math.PI);
                    this.rightArm.zRot = Mth.lerp(j, this.rightArm.zRot, (float)Math.PI);
                }
            }
            l = 0.3f;
            float m = 0.33333334f;
            this.leftLeg.xRot = Mth.lerp(f, this.leftLeg.xRot, 0.3f * Mth.cos(g * 0.33333334f + (float)Math.PI));
            this.rightLeg.xRot = Mth.lerp(f, this.rightLeg.xRot, 0.3f * Mth.cos(g * 0.33333334f));
        }
    }

    private void poseRightArm(T humanoidRenderState) {
        switch (((HumanoidRenderState)humanoidRenderState).rightArmPose.ordinal()) {
            case 0: {
                this.rightArm.yRot = 0.0f;
                break;
            }
            case 2: {
                this.poseBlockingArm(this.rightArm, true);
                break;
            }
            case 1: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - 0.31415927f;
                this.rightArm.yRot = 0.0f;
                break;
            }
            case 4: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - (float)Math.PI;
                this.rightArm.yRot = 0.0f;
                break;
            }
            case 10: {
                SpearAnimations.thirdPersonHandUse(this.rightArm, this.head, true, ((ArmedEntityRenderState)humanoidRenderState).getUseItemStackForArm(HumanoidArm.RIGHT), humanoidRenderState);
                break;
            }
            case 3: {
                this.rightArm.yRot = -0.1f + this.head.yRot;
                this.leftArm.yRot = 0.1f + this.head.yRot + 0.4f;
                this.rightArm.xRot = -1.5707964f + this.head.xRot;
                this.leftArm.xRot = -1.5707964f + this.head.xRot;
                break;
            }
            case 5: {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, ((HumanoidRenderState)humanoidRenderState).maxCrossbowChargeDuration, ((HumanoidRenderState)humanoidRenderState).ticksUsingItem, true);
                break;
            }
            case 6: {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
                break;
            }
            case 9: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - 0.62831855f;
                this.rightArm.yRot = 0.0f;
                break;
            }
            case 7: {
                this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622f - (((HumanoidRenderState)humanoidRenderState).isCrouching ? 0.2617994f : 0.0f), -2.4f, 3.3f);
                this.rightArm.yRot = this.head.yRot - 0.2617994f;
                break;
            }
            case 8: {
                this.rightArm.xRot = Mth.clamp(this.head.xRot, -1.2f, 1.2f) - 1.4835298f;
                this.rightArm.yRot = this.head.yRot - 0.5235988f;
            }
        }
    }

    private void poseLeftArm(T humanoidRenderState) {
        switch (((HumanoidRenderState)humanoidRenderState).leftArmPose.ordinal()) {
            case 0: {
                this.leftArm.yRot = 0.0f;
                break;
            }
            case 2: {
                this.poseBlockingArm(this.leftArm, false);
                break;
            }
            case 1: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - 0.31415927f;
                this.leftArm.yRot = 0.0f;
                break;
            }
            case 4: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - (float)Math.PI;
                this.leftArm.yRot = 0.0f;
                break;
            }
            case 10: {
                SpearAnimations.thirdPersonHandUse(this.leftArm, this.head, false, ((ArmedEntityRenderState)humanoidRenderState).getUseItemStackForArm(HumanoidArm.LEFT), humanoidRenderState);
                break;
            }
            case 3: {
                this.rightArm.yRot = -0.1f + this.head.yRot - 0.4f;
                this.leftArm.yRot = 0.1f + this.head.yRot;
                this.rightArm.xRot = -1.5707964f + this.head.xRot;
                this.leftArm.xRot = -1.5707964f + this.head.xRot;
                break;
            }
            case 5: {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, ((HumanoidRenderState)humanoidRenderState).maxCrossbowChargeDuration, ((HumanoidRenderState)humanoidRenderState).ticksUsingItem, false);
                break;
            }
            case 6: {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
                break;
            }
            case 9: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - 0.62831855f;
                this.leftArm.yRot = 0.0f;
                break;
            }
            case 7: {
                this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622f - (((HumanoidRenderState)humanoidRenderState).isCrouching ? 0.2617994f : 0.0f), -2.4f, 3.3f);
                this.leftArm.yRot = this.head.yRot + 0.2617994f;
                break;
            }
            case 8: {
                this.leftArm.xRot = Mth.clamp(this.head.xRot, -1.2f, 1.2f) - 1.4835298f;
                this.leftArm.yRot = this.head.yRot + 0.5235988f;
            }
        }
    }

    private void poseBlockingArm(ModelPart modelPart, boolean bl) {
        modelPart.xRot = modelPart.xRot * 0.5f - 0.9424779f + Mth.clamp(this.head.xRot, -1.3962634f, 0.43633232f);
        modelPart.yRot = (bl ? -30.0f : 30.0f) * ((float)Math.PI / 180) + Mth.clamp(this.head.yRot, -0.5235988f, 0.5235988f);
    }

    protected void setupAttackAnimation(T humanoidRenderState) {
        float f = ((HumanoidRenderState)humanoidRenderState).attackTime;
        if (f <= 0.0f) {
            return;
        }
        this.body.yRot = Mth.sin(Mth.sqrt(f) * ((float)Math.PI * 2)) * 0.2f;
        if (((HumanoidRenderState)humanoidRenderState).attackArm == HumanoidArm.LEFT) {
            this.body.yRot *= -1.0f;
        }
        float g = ((HumanoidRenderState)humanoidRenderState).ageScale;
        this.rightArm.z = Mth.sin(this.body.yRot) * 5.0f * g;
        this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0f * g;
        this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0f * g;
        this.leftArm.x = Mth.cos(this.body.yRot) * 5.0f * g;
        this.rightArm.yRot += this.body.yRot;
        this.leftArm.yRot += this.body.yRot;
        this.leftArm.xRot += this.body.yRot;
        switch (((HumanoidRenderState)humanoidRenderState).swingAnimationType) {
            case WHACK: {
                float h = Ease.outQuart(f);
                float i = Mth.sin(h * (float)Math.PI);
                float j = Mth.sin(f * (float)Math.PI) * -(this.head.xRot - 0.7f) * 0.75f;
                ModelPart modelPart = this.getArm(((HumanoidRenderState)humanoidRenderState).attackArm);
                modelPart.xRot -= i * 1.2f + j;
                modelPart.yRot += this.body.yRot * 2.0f;
                modelPart.zRot += Mth.sin(f * (float)Math.PI) * -0.4f;
                break;
            }
            case NONE: {
                break;
            }
            case STAB: {
                SpearAnimations.thirdPersonAttackHand(this, humanoidRenderState);
            }
        }
    }

    private float quadraticArmUpdate(float f) {
        return -65.0f * f + f * f;
    }

    public void setAllVisible(boolean bl) {
        this.head.visible = bl;
        this.hat.visible = bl;
        this.body.visible = bl;
        this.rightArm.visible = bl;
        this.leftArm.visible = bl;
        this.rightLeg.visible = bl;
        this.leftLeg.visible = bl;
    }

    @Override
    public void translateToHand(HumanoidRenderState humanoidRenderState, HumanoidArm humanoidArm, PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.getArm(humanoidArm).translateAndRotate(poseStack);
    }

    public ModelPart getArm(HumanoidArm humanoidArm) {
        if (humanoidArm == HumanoidArm.LEFT) {
            return this.leftArm;
        }
        return this.rightArm;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ArmPose {
        EMPTY(false, false),
        ITEM(false, false),
        BLOCK(false, false),
        BOW_AND_ARROW(true, true),
        THROW_TRIDENT(false, true),
        CROSSBOW_CHARGE(true, true),
        CROSSBOW_HOLD(true, true),
        SPYGLASS(false, false),
        TOOT_HORN(false, false),
        BRUSH(false, false),
        SPEAR(false, true){

            @Override
            public <S extends ArmedEntityRenderState> void animateUseItem(S armedEntityRenderState, PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack) {
                SpearAnimations.thirdPersonUseItem(armedEntityRenderState, poseStack, f, humanoidArm, itemStack);
            }
        };

        private final boolean twoHanded;
        private final boolean affectsOffhandPose;

        ArmPose(boolean bl, boolean bl2) {
            this.twoHanded = bl;
            this.affectsOffhandPose = bl2;
        }

        public boolean isTwoHanded() {
            return this.twoHanded;
        }

        public boolean affectsOffhandPose() {
            return this.affectsOffhandPose;
        }

        public <S extends ArmedEntityRenderState> void animateUseItem(S armedEntityRenderState, PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack) {
        }
    }
}


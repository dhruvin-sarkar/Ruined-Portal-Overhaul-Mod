/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.model.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.KineticWeapon;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class SpearAnimations {
    static float progress(float f, float g, float h) {
        return Mth.clamp(Mth.inverseLerp(f, g, h), 0.0f, 1.0f);
    }

    public static <T extends HumanoidRenderState> void thirdPersonHandUse(ModelPart modelPart, ModelPart modelPart2, boolean bl, ItemStack itemStack, T humanoidRenderState) {
        int i = bl ? 1 : -1;
        modelPart.yRot = -0.1f * (float)i + modelPart2.yRot;
        modelPart.xRot = -1.5707964f + modelPart2.xRot + 0.8f;
        if (humanoidRenderState.isFallFlying || humanoidRenderState.swimAmount > 0.0f) {
            modelPart.xRot -= 0.9599311f;
        }
        modelPart.yRot = (float)Math.PI / 180 * Math.clamp((float)(57.295776f * modelPart.yRot), (float)-60.0f, (float)60.0f);
        modelPart.xRot = (float)Math.PI / 180 * Math.clamp((float)(57.295776f * modelPart.xRot), (float)-120.0f, (float)30.0f);
        if (humanoidRenderState.ticksUsingItem <= 0.0f || humanoidRenderState.isUsingItem && humanoidRenderState.useItemHand != (bl ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND)) {
            return;
        }
        KineticWeapon kineticWeapon = itemStack.get(DataComponents.KINETIC_WEAPON);
        if (kineticWeapon == null) {
            return;
        }
        UseParams useParams = UseParams.fromKineticWeapon(kineticWeapon, humanoidRenderState.ticksUsingItem);
        modelPart.yRot += (float)(-i) * useParams.swayScaleFast() * ((float)Math.PI / 180) * useParams.swayIntensity() * 1.0f;
        modelPart.zRot += (float)(-i) * useParams.swayScaleSlow() * ((float)Math.PI / 180) * useParams.swayIntensity() * 0.5f;
        modelPart.xRot += (float)Math.PI / 180 * (-40.0f * useParams.raiseProgressStart() + 30.0f * useParams.raiseProgressMiddle() + -20.0f * useParams.raiseProgressEnd() + 20.0f * useParams.lowerProgress() + 10.0f * useParams.raiseBackProgress() + 0.6f * useParams.swayScaleSlow() * useParams.swayIntensity());
    }

    public static <S extends ArmedEntityRenderState> void thirdPersonUseItem(S armedEntityRenderState, PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack) {
        KineticWeapon kineticWeapon = itemStack.get(DataComponents.KINETIC_WEAPON);
        if (kineticWeapon == null || f == 0.0f) {
            return;
        }
        float g = Ease.inQuad(SpearAnimations.progress(armedEntityRenderState.attackTime, 0.05f, 0.2f));
        float h = Ease.inOutExpo(SpearAnimations.progress(armedEntityRenderState.attackTime, 0.4f, 1.0f));
        UseParams useParams = UseParams.fromKineticWeapon(kineticWeapon, f);
        int i = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        float j = 1.0f - Ease.outBack(1.0f - useParams.raiseProgress());
        float k = 0.125f;
        float l = SpearAnimations.hitFeedbackAmount(armedEntityRenderState.ticksSinceKineticHitFeedback);
        poseStack.translate(0.0, (double)(-l) * 0.4, (double)(-kineticWeapon.forwardMovement() * (j - useParams.raiseBackProgress()) + l));
        poseStack.rotateAround((Quaternionfc)Axis.XN.rotationDegrees(70.0f * (useParams.raiseProgress() - useParams.raiseBackProgress()) - 40.0f * (g - h)), 0.0f, -0.03125f, 0.125f);
        poseStack.rotateAround((Quaternionfc)Axis.YP.rotationDegrees((float)(i * 90) * (useParams.raiseProgress() - useParams.swayProgress() + 3.0f * h + g)), 0.0f, 0.0f, 0.125f);
    }

    public static <T extends HumanoidRenderState> void thirdPersonAttackHand(HumanoidModel<T> humanoidModel, T humanoidRenderState) {
        float f = humanoidRenderState.attackTime;
        HumanoidArm humanoidArm = humanoidRenderState.attackArm;
        humanoidModel.rightArm.yRot -= humanoidModel.body.yRot;
        humanoidModel.leftArm.yRot -= humanoidModel.body.yRot;
        humanoidModel.leftArm.xRot -= humanoidModel.body.yRot;
        float g = Ease.inOutSine(SpearAnimations.progress(f, 0.0f, 0.05f));
        float h = Ease.inQuad(SpearAnimations.progress(f, 0.05f, 0.2f));
        float i = Ease.inOutExpo(SpearAnimations.progress(f, 0.4f, 1.0f));
        humanoidModel.getArm((HumanoidArm)humanoidArm).xRot += (90.0f * g - 120.0f * h + 30.0f * i) * ((float)Math.PI / 180);
    }

    public static <S extends ArmedEntityRenderState> void thirdPersonAttackItem(S armedEntityRenderState, PoseStack poseStack) {
        if (armedEntityRenderState.attackTime <= 0.0f) {
            return;
        }
        KineticWeapon kineticWeapon = armedEntityRenderState.getMainHandItemStack().get(DataComponents.KINETIC_WEAPON);
        float f = kineticWeapon != null ? kineticWeapon.forwardMovement() : 0.0f;
        float g = 0.125f;
        float h = armedEntityRenderState.attackTime;
        float i = Ease.inQuad(SpearAnimations.progress(h, 0.05f, 0.2f));
        float j = Ease.inOutExpo(SpearAnimations.progress(h, 0.4f, 1.0f));
        poseStack.rotateAround((Quaternionfc)Axis.XN.rotationDegrees(70.0f * (i - j)), 0.0f, -0.125f, 0.125f);
        poseStack.translate(0.0f, f * (i - j), 0.0f);
    }

    private static float hitFeedbackAmount(float f) {
        return 0.4f * (Ease.outQuart(SpearAnimations.progress(f, 1.0f, 3.0f)) - Ease.inOutSine(SpearAnimations.progress(f, 3.0f, 10.0f)));
    }

    public static void firstPersonUse(float f, PoseStack poseStack, float g, HumanoidArm humanoidArm, ItemStack itemStack) {
        KineticWeapon kineticWeapon = itemStack.get(DataComponents.KINETIC_WEAPON);
        if (kineticWeapon == null) {
            return;
        }
        UseParams useParams = UseParams.fromKineticWeapon(kineticWeapon, g);
        int i = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((double)((float)i * (useParams.raiseProgress() * 0.15f + useParams.raiseProgressEnd() * -0.05f + useParams.swayProgress() * -0.1f + useParams.swayScaleSlow() * 0.005f)), (double)(useParams.raiseProgress() * -0.075f + useParams.raiseProgressMiddle() * 0.075f + useParams.swayScaleFast() * 0.01f), (double)useParams.raiseProgressStart() * 0.05 + (double)useParams.raiseProgressEnd() * -0.05 + (double)(useParams.swayScaleSlow() * 0.005f));
        poseStack.rotateAround((Quaternionfc)Axis.XP.rotationDegrees(-65.0f * Ease.inOutBack(useParams.raiseProgress()) - 35.0f * useParams.lowerProgress() + 100.0f * useParams.raiseBackProgress() + -0.5f * useParams.swayScaleFast()), 0.0f, 0.1f, 0.0f);
        poseStack.rotateAround((Quaternionfc)Axis.YN.rotationDegrees((float)i * (-90.0f * SpearAnimations.progress(useParams.raiseProgress(), 0.5f, 0.55f) + 90.0f * useParams.swayProgress() + 2.0f * useParams.swayScaleSlow())), (float)i * 0.15f, 0.0f, 0.0f);
        poseStack.translate(0.0f, -SpearAnimations.hitFeedbackAmount(f), 0.0f);
    }

    public static void firstPersonAttack(float f, PoseStack poseStack, int i, HumanoidArm humanoidArm) {
        float g = Ease.inOutSine(SpearAnimations.progress(f, 0.0f, 0.05f));
        float h = Ease.outBack(SpearAnimations.progress(f, 0.05f, 0.2f));
        float j = Ease.inOutExpo(SpearAnimations.progress(f, 0.4f, 1.0f));
        poseStack.translate((float)i * 0.1f * (g - h), -0.075f * (g - j), 0.65f * (g - h));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-70.0f * (g - j)));
        poseStack.translate(0.0, 0.0, -0.25 * (double)(j - h));
    }

    @Environment(value=EnvType.CLIENT)
    record UseParams(float raiseProgress, float raiseProgressStart, float raiseProgressMiddle, float raiseProgressEnd, float swayProgress, float lowerProgress, float raiseBackProgress, float swayIntensity, float swayScaleSlow, float swayScaleFast) {
        public static UseParams fromKineticWeapon(KineticWeapon kineticWeapon, float f) {
            int i = kineticWeapon.delayTicks();
            int j = kineticWeapon.dismountConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0) + i;
            int k = j - 20;
            int l = kineticWeapon.knockbackConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0) + i;
            int m = l - 40;
            int n = kineticWeapon.damageConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0) + i;
            float g = SpearAnimations.progress(f, 0.0f, i);
            float h = SpearAnimations.progress(g, 0.0f, 0.5f);
            float o = SpearAnimations.progress(g, 0.5f, 0.8f);
            float p = SpearAnimations.progress(g, 0.8f, 1.0f);
            float q = SpearAnimations.progress(f, k, m);
            float r = Ease.outCubic(Ease.inOutElastic(SpearAnimations.progress(f - 20.0f, m, l)));
            float s = SpearAnimations.progress(f, n - 5, n);
            float t = 2.0f * Ease.outCirc(q) - 2.0f * Ease.inCirc(s);
            float u = Mth.sin(f * 19.0f * ((float)Math.PI / 180)) * t;
            float v = Mth.sin(f * 30.0f * ((float)Math.PI / 180)) * t;
            return new UseParams(g, h, o, p, q, r, s, t, u, v);
        }
    }
}


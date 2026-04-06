/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.UndeadRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.SwingAnimationType;

@Environment(value=EnvType.CLIENT)
public class AnimationUtils {
    public static void animateCrossbowHold(ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, boolean bl) {
        ModelPart modelPart4 = bl ? modelPart : modelPart2;
        ModelPart modelPart5 = bl ? modelPart2 : modelPart;
        modelPart4.yRot = (bl ? -0.3f : 0.3f) + modelPart3.yRot;
        modelPart5.yRot = (bl ? 0.6f : -0.6f) + modelPart3.yRot;
        modelPart4.xRot = -1.5707964f + modelPart3.xRot + 0.1f;
        modelPart5.xRot = -1.5f + modelPart3.xRot;
    }

    public static void animateCrossbowCharge(ModelPart modelPart, ModelPart modelPart2, float f, float g, boolean bl) {
        ModelPart modelPart3 = bl ? modelPart : modelPart2;
        ModelPart modelPart4 = bl ? modelPart2 : modelPart;
        modelPart3.yRot = bl ? -0.8f : 0.8f;
        modelPart4.xRot = modelPart3.xRot = -0.97079635f;
        float h = Mth.clamp(g, 0.0f, f);
        float i = h / f;
        modelPart4.yRot = Mth.lerp(i, 0.4f, 0.85f) * (float)(bl ? 1 : -1);
        modelPart4.xRot = Mth.lerp(i, modelPart4.xRot, -1.5707964f);
    }

    public static void swingWeaponDown(ModelPart modelPart, ModelPart modelPart2, HumanoidArm humanoidArm, float f, float g) {
        float h = Mth.sin(f * (float)Math.PI);
        float i = Mth.sin((1.0f - (1.0f - f) * (1.0f - f)) * (float)Math.PI);
        modelPart.zRot = 0.0f;
        modelPart2.zRot = 0.0f;
        modelPart.yRot = 0.15707964f;
        modelPart2.yRot = -0.15707964f;
        if (humanoidArm == HumanoidArm.RIGHT) {
            modelPart.xRot = -1.8849558f + Mth.cos(g * 0.09f) * 0.15f;
            modelPart2.xRot = -0.0f + Mth.cos(g * 0.19f) * 0.5f;
            modelPart.xRot += h * 2.2f - i * 0.4f;
            modelPart2.xRot += h * 1.2f - i * 0.4f;
        } else {
            modelPart.xRot = -0.0f + Mth.cos(g * 0.19f) * 0.5f;
            modelPart2.xRot = -1.8849558f + Mth.cos(g * 0.09f) * 0.15f;
            modelPart.xRot += h * 1.2f - i * 0.4f;
            modelPart2.xRot += h * 2.2f - i * 0.4f;
        }
        AnimationUtils.bobArms(modelPart, modelPart2, g);
    }

    public static void bobModelPart(ModelPart modelPart, float f, float g) {
        modelPart.zRot += g * (Mth.cos(f * 0.09f) * 0.05f + 0.05f);
        modelPart.xRot += g * (Mth.sin(f * 0.067f) * 0.05f);
    }

    public static void bobArms(ModelPart modelPart, ModelPart modelPart2, float f) {
        AnimationUtils.bobModelPart(modelPart, f, 1.0f);
        AnimationUtils.bobModelPart(modelPart2, f, -1.0f);
    }

    public static <T extends UndeadRenderState> void animateZombieArms(ModelPart modelPart, ModelPart modelPart2, boolean bl, T undeadRenderState) {
        boolean bl2;
        boolean bl3 = bl2 = undeadRenderState.swingAnimationType != SwingAnimationType.STAB;
        if (bl2) {
            float f = undeadRenderState.attackTime;
            float g = (float)(-Math.PI) / (bl ? 1.5f : 2.25f);
            float h = Mth.sin(f * (float)Math.PI);
            float i = Mth.sin((1.0f - (1.0f - f) * (1.0f - f)) * (float)Math.PI);
            modelPart2.zRot = 0.0f;
            modelPart2.yRot = -(0.1f - h * 0.6f);
            modelPart2.xRot = g;
            modelPart2.xRot += h * 1.2f - i * 0.4f;
            modelPart.zRot = 0.0f;
            modelPart.yRot = 0.1f - h * 0.6f;
            modelPart.xRot = g;
            modelPart.xRot += h * 1.2f - i * 0.4f;
        }
        AnimationUtils.bobArms(modelPart2, modelPart, undeadRenderState.ageInTicks);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.panda.PandaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.panda.Panda;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class PandaRenderer
extends AgeableMobRenderer<Panda, PandaRenderState, PandaModel> {
    private static final Map<Panda.Gene, Identifier> TEXTURES = Maps.newEnumMap((Map)Map.of((Object)Panda.Gene.NORMAL, (Object)Identifier.withDefaultNamespace("textures/entity/panda/panda.png"), (Object)Panda.Gene.LAZY, (Object)Identifier.withDefaultNamespace("textures/entity/panda/lazy_panda.png"), (Object)Panda.Gene.WORRIED, (Object)Identifier.withDefaultNamespace("textures/entity/panda/worried_panda.png"), (Object)Panda.Gene.PLAYFUL, (Object)Identifier.withDefaultNamespace("textures/entity/panda/playful_panda.png"), (Object)Panda.Gene.BROWN, (Object)Identifier.withDefaultNamespace("textures/entity/panda/brown_panda.png"), (Object)Panda.Gene.WEAK, (Object)Identifier.withDefaultNamespace("textures/entity/panda/weak_panda.png"), (Object)Panda.Gene.AGGRESSIVE, (Object)Identifier.withDefaultNamespace("textures/entity/panda/aggressive_panda.png")));

    public PandaRenderer(EntityRendererProvider.Context context) {
        super(context, new PandaModel(context.bakeLayer(ModelLayers.PANDA)), new PandaModel(context.bakeLayer(ModelLayers.PANDA_BABY)), 0.9f);
        this.addLayer(new PandaHoldsItemLayer(this));
    }

    @Override
    public Identifier getTextureLocation(PandaRenderState pandaRenderState) {
        return TEXTURES.getOrDefault(pandaRenderState.variant, TEXTURES.get(Panda.Gene.NORMAL));
    }

    @Override
    public PandaRenderState createRenderState() {
        return new PandaRenderState();
    }

    @Override
    public void extractRenderState(Panda panda, PandaRenderState pandaRenderState, float f) {
        super.extractRenderState(panda, pandaRenderState, f);
        HoldingEntityRenderState.extractHoldingEntityRenderState(panda, pandaRenderState, this.itemModelResolver);
        pandaRenderState.variant = panda.getVariant();
        pandaRenderState.isUnhappy = panda.getUnhappyCounter() > 0;
        pandaRenderState.isSneezing = panda.isSneezing();
        pandaRenderState.sneezeTime = panda.getSneezeCounter();
        pandaRenderState.isEating = panda.isEating();
        pandaRenderState.isScared = panda.isScared();
        pandaRenderState.isSitting = panda.isSitting();
        pandaRenderState.sitAmount = panda.getSitAmount(f);
        pandaRenderState.lieOnBackAmount = panda.getLieOnBackAmount(f);
        pandaRenderState.rollAmount = panda.isBaby() ? 0.0f : panda.getRollAmount(f);
        pandaRenderState.rollTime = panda.rollCounter > 0 ? (float)panda.rollCounter + f : 0.0f;
    }

    @Override
    protected void setupRotations(PandaRenderState pandaRenderState, PoseStack poseStack, float f, float g) {
        float q;
        float h;
        super.setupRotations(pandaRenderState, poseStack, f, g);
        if (pandaRenderState.rollTime > 0.0f) {
            float l;
            h = Mth.frac(pandaRenderState.rollTime);
            int i = Mth.floor(pandaRenderState.rollTime);
            int j = i + 1;
            float k = 7.0f;
            float f2 = l = pandaRenderState.isBaby ? 0.3f : 0.8f;
            if ((float)i < 8.0f) {
                float m = 90.0f * (float)i / 7.0f;
                float n = 90.0f * (float)j / 7.0f;
                float o = this.getAngle(m, n, j, h, 8.0f);
                poseStack.translate(0.0f, (l + 0.2f) * (o / 90.0f), 0.0f);
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-o));
            } else if ((float)i < 16.0f) {
                float m = ((float)i - 8.0f) / 7.0f;
                float n = 90.0f + 90.0f * m;
                float p = 90.0f + 90.0f * ((float)j - 8.0f) / 7.0f;
                float o = this.getAngle(n, p, j, h, 16.0f);
                poseStack.translate(0.0f, l + 0.2f + (l - 0.2f) * (o - 90.0f) / 90.0f, 0.0f);
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-o));
            } else if ((float)i < 24.0f) {
                float m = ((float)i - 16.0f) / 7.0f;
                float n = 180.0f + 90.0f * m;
                float p = 180.0f + 90.0f * ((float)j - 16.0f) / 7.0f;
                float o = this.getAngle(n, p, j, h, 24.0f);
                poseStack.translate(0.0f, l + l * (270.0f - o) / 90.0f, 0.0f);
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-o));
            } else if (i < 32) {
                float m = ((float)i - 24.0f) / 7.0f;
                float n = 270.0f + 90.0f * m;
                float p = 270.0f + 90.0f * ((float)j - 24.0f) / 7.0f;
                float o = this.getAngle(n, p, j, h, 32.0f);
                poseStack.translate(0.0f, l * ((360.0f - o) / 90.0f), 0.0f);
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-o));
            }
        }
        if ((h = pandaRenderState.sitAmount) > 0.0f) {
            poseStack.translate(0.0f, 0.8f * h, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.lerp(h, pandaRenderState.xRot, pandaRenderState.xRot + 90.0f)));
            poseStack.translate(0.0f, -1.0f * h, 0.0f);
            if (pandaRenderState.isScared) {
                float q2 = (float)(Math.cos(pandaRenderState.ageInTicks * 1.25f) * Math.PI * (double)0.05f);
                poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(q2));
                if (pandaRenderState.isBaby) {
                    poseStack.translate(0.0f, 0.8f, 0.55f);
                }
            }
        }
        if ((q = pandaRenderState.lieOnBackAmount) > 0.0f) {
            float r = pandaRenderState.isBaby ? 0.5f : 1.3f;
            poseStack.translate(0.0f, r * q, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.lerp(q, pandaRenderState.xRot, pandaRenderState.xRot + 180.0f)));
        }
    }

    private float getAngle(float f, float g, int i, float h, float j) {
        if ((float)i < j) {
            return Mth.lerp(h, f, g);
        }
        return f;
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((PandaRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


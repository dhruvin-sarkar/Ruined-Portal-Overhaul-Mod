/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.feline.CatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.feline.Cat;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class CatRenderer
extends AgeableMobRenderer<Cat, CatRenderState, CatModel> {
    public CatRenderer(EntityRendererProvider.Context context) {
        super(context, new CatModel(context.bakeLayer(ModelLayers.CAT)), new CatModel(context.bakeLayer(ModelLayers.CAT_BABY)), 0.4f);
        this.addLayer(new CatCollarLayer(this, context.getModelSet()));
    }

    @Override
    public Identifier getTextureLocation(CatRenderState catRenderState) {
        return catRenderState.texture;
    }

    @Override
    public CatRenderState createRenderState() {
        return new CatRenderState();
    }

    @Override
    public void extractRenderState(Cat cat, CatRenderState catRenderState, float f) {
        super.extractRenderState(cat, catRenderState, f);
        catRenderState.texture = cat.getVariant().value().assetInfo().texturePath();
        catRenderState.isCrouching = cat.isCrouching();
        catRenderState.isSprinting = cat.isSprinting();
        catRenderState.isSitting = cat.isInSittingPose();
        catRenderState.lieDownAmount = cat.getLieDownAmount(f);
        catRenderState.lieDownAmountTail = cat.getLieDownAmountTail(f);
        catRenderState.relaxStateOneAmount = cat.getRelaxStateOneAmount(f);
        catRenderState.isLyingOnTopOfSleepingPlayer = cat.isLyingOnTopOfSleepingPlayer();
        catRenderState.collarColor = cat.isTame() ? cat.getCollarColor() : null;
    }

    @Override
    protected void setupRotations(CatRenderState catRenderState, PoseStack poseStack, float f, float g) {
        super.setupRotations(catRenderState, poseStack, f, g);
        float h = catRenderState.lieDownAmount;
        if (h > 0.0f) {
            poseStack.translate(0.4f * h, 0.15f * h, 0.1f * h);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(Mth.rotLerp(h, 0.0f, 90.0f)));
            if (catRenderState.isLyingOnTopOfSleepingPlayer) {
                poseStack.translate(0.15f * h, 0.0f, 0.0f);
            }
        }
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((CatRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


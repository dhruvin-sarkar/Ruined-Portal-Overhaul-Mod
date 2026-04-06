/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.animal.wolf.Wolf;

@Environment(value=EnvType.CLIENT)
public class WolfRenderer
extends AgeableMobRenderer<Wolf, WolfRenderState, WolfModel> {
    public WolfRenderer(EntityRendererProvider.Context context) {
        super(context, new WolfModel(context.bakeLayer(ModelLayers.WOLF)), new WolfModel(context.bakeLayer(ModelLayers.WOLF_BABY)), 0.5f);
        this.addLayer(new WolfArmorLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new WolfCollarLayer(this));
    }

    @Override
    protected int getModelTint(WolfRenderState wolfRenderState) {
        float f = wolfRenderState.wetShade;
        if (f == 1.0f) {
            return -1;
        }
        return ARGB.colorFromFloat(1.0f, f, f, f);
    }

    @Override
    public Identifier getTextureLocation(WolfRenderState wolfRenderState) {
        return wolfRenderState.texture;
    }

    @Override
    public WolfRenderState createRenderState() {
        return new WolfRenderState();
    }

    @Override
    public void extractRenderState(Wolf wolf, WolfRenderState wolfRenderState, float f) {
        super.extractRenderState(wolf, wolfRenderState, f);
        wolfRenderState.isAngry = wolf.isAngry();
        wolfRenderState.isSitting = wolf.isInSittingPose();
        wolfRenderState.tailAngle = wolf.getTailAngle();
        wolfRenderState.headRollAngle = wolf.getHeadRollAngle(f);
        wolfRenderState.shakeAnim = wolf.getShakeAnim(f);
        wolfRenderState.texture = wolf.getTexture();
        wolfRenderState.wetShade = wolf.getWetShade(f);
        wolfRenderState.collarColor = wolf.isTame() ? wolf.getCollarColor() : null;
        wolfRenderState.bodyArmorItem = wolf.getBodyArmorItem().copy();
    }

    @Override
    protected /* synthetic */ int getModelTint(LivingEntityRenderState livingEntityRenderState) {
        return this.getModelTint((WolfRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


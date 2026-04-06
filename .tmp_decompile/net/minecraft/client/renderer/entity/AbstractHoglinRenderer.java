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
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.monster.hoglin.HoglinModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractHoglinRenderer<T extends Mob>
extends AgeableMobRenderer<T, HoglinRenderState, HoglinModel> {
    public AbstractHoglinRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, float f) {
        super(context, new HoglinModel(context.bakeLayer(modelLayerLocation)), new HoglinModel(context.bakeLayer(modelLayerLocation2)), f);
    }

    @Override
    public HoglinRenderState createRenderState() {
        return new HoglinRenderState();
    }

    @Override
    public void extractRenderState(T mob, HoglinRenderState hoglinRenderState, float f) {
        super.extractRenderState(mob, hoglinRenderState, f);
        hoglinRenderState.attackAnimationRemainingTicks = ((HoglinBase)mob).getAttackAnimationRemainingTicks();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


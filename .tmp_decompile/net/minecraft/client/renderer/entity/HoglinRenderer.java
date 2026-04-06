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
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHoglinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

@Environment(value=EnvType.CLIENT)
public class HoglinRenderer
extends AbstractHoglinRenderer<Hoglin> {
    private static final Identifier HOGLIN_LOCATION = Identifier.withDefaultNamespace("textures/entity/hoglin/hoglin.png");

    public HoglinRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.HOGLIN, ModelLayers.HOGLIN_BABY, 0.7f);
    }

    @Override
    public Identifier getTextureLocation(HoglinRenderState hoglinRenderState) {
        return HOGLIN_LOCATION;
    }

    @Override
    public void extractRenderState(Hoglin hoglin, HoglinRenderState hoglinRenderState, float f) {
        super.extractRenderState(hoglin, hoglinRenderState, f);
        hoglinRenderState.isConverting = hoglin.isConverting();
    }

    @Override
    protected boolean isShaking(HoglinRenderState hoglinRenderState) {
        return super.isShaking(hoglinRenderState) || hoglinRenderState.isConverting;
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntityRenderState livingEntityRenderState) {
        return this.isShaking((HoglinRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((HoglinRenderState)livingEntityRenderState);
    }
}


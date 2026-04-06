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
import net.minecraft.world.entity.monster.Zoglin;

@Environment(value=EnvType.CLIENT)
public class ZoglinRenderer
extends AbstractHoglinRenderer<Zoglin> {
    private static final Identifier ZOGLIN_LOCATION = Identifier.withDefaultNamespace("textures/entity/hoglin/zoglin.png");

    public ZoglinRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.ZOGLIN, ModelLayers.ZOGLIN_BABY, 0.7f);
    }

    @Override
    public Identifier getTextureLocation(HoglinRenderState hoglinRenderState) {
        return ZOGLIN_LOCATION;
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((HoglinRenderState)livingEntityRenderState);
    }
}


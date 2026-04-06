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
import net.minecraft.client.model.animal.feline.OcelotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.feline.Ocelot;

@Environment(value=EnvType.CLIENT)
public class OcelotRenderer
extends AgeableMobRenderer<Ocelot, FelineRenderState, OcelotModel> {
    private static final Identifier CAT_OCELOT_LOCATION = Identifier.withDefaultNamespace("textures/entity/cat/ocelot.png");

    public OcelotRenderer(EntityRendererProvider.Context context) {
        super(context, new OcelotModel(context.bakeLayer(ModelLayers.OCELOT)), new OcelotModel(context.bakeLayer(ModelLayers.OCELOT_BABY)), 0.4f);
    }

    @Override
    public Identifier getTextureLocation(FelineRenderState felineRenderState) {
        return CAT_OCELOT_LOCATION;
    }

    @Override
    public FelineRenderState createRenderState() {
        return new FelineRenderState();
    }

    @Override
    public void extractRenderState(Ocelot ocelot, FelineRenderState felineRenderState, float f) {
        super.extractRenderState(ocelot, felineRenderState, f);
        felineRenderState.isCrouching = ocelot.isCrouching();
        felineRenderState.isSprinting = ocelot.isSprinting();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((FelineRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


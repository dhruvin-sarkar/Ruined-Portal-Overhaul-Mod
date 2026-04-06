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
import net.minecraft.client.model.animal.goat.GoatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.GoatRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.goat.Goat;

@Environment(value=EnvType.CLIENT)
public class GoatRenderer
extends AgeableMobRenderer<Goat, GoatRenderState, GoatModel> {
    private static final Identifier GOAT_LOCATION = Identifier.withDefaultNamespace("textures/entity/goat/goat.png");

    public GoatRenderer(EntityRendererProvider.Context context) {
        super(context, new GoatModel(context.bakeLayer(ModelLayers.GOAT)), new GoatModel(context.bakeLayer(ModelLayers.GOAT_BABY)), 0.7f);
    }

    @Override
    public Identifier getTextureLocation(GoatRenderState goatRenderState) {
        return GOAT_LOCATION;
    }

    @Override
    public GoatRenderState createRenderState() {
        return new GoatRenderState();
    }

    @Override
    public void extractRenderState(Goat goat, GoatRenderState goatRenderState, float f) {
        super.extractRenderState(goat, goatRenderState, f);
        goatRenderState.hasLeftHorn = goat.hasLeftHorn();
        goatRenderState.hasRightHorn = goat.hasRightHorn();
        goatRenderState.rammingXHeadRot = goat.getRammingXHeadRot();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((GoatRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


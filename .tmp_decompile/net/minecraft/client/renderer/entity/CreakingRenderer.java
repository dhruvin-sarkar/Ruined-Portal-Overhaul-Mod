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
import net.minecraft.client.model.monster.creaking.CreakingModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.creaking.Creaking;

@Environment(value=EnvType.CLIENT)
public class CreakingRenderer<T extends Creaking>
extends MobRenderer<T, CreakingRenderState, CreakingModel> {
    private static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/entity/creaking/creaking.png");
    private static final Identifier EYES_TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/entity/creaking/creaking_eyes.png");

    public CreakingRenderer(EntityRendererProvider.Context context) {
        super(context, new CreakingModel(context.bakeLayer(ModelLayers.CREAKING)), 0.6f);
        this.addLayer(new LivingEntityEmissiveLayer<CreakingRenderState, CreakingModel>(this, creakingRenderState -> EYES_TEXTURE_LOCATION, (creakingRenderState, f) -> creakingRenderState.eyesGlowing ? 1.0f : 0.0f, new CreakingModel(context.bakeLayer(ModelLayers.CREAKING_EYES)), RenderTypes::eyes, true));
    }

    @Override
    public Identifier getTextureLocation(CreakingRenderState creakingRenderState) {
        return TEXTURE_LOCATION;
    }

    @Override
    public CreakingRenderState createRenderState() {
        return new CreakingRenderState();
    }

    @Override
    public void extractRenderState(T creaking, CreakingRenderState creakingRenderState, float f) {
        super.extractRenderState(creaking, creakingRenderState, f);
        creakingRenderState.attackAnimationState.copyFrom(((Creaking)creaking).attackAnimationState);
        creakingRenderState.invulnerabilityAnimationState.copyFrom(((Creaking)creaking).invulnerabilityAnimationState);
        creakingRenderState.deathAnimationState.copyFrom(((Creaking)creaking).deathAnimationState);
        if (((Creaking)creaking).isTearingDown()) {
            creakingRenderState.deathTime = 0.0f;
            creakingRenderState.hasRedOverlay = false;
            creakingRenderState.eyesGlowing = ((Creaking)creaking).hasGlowingEyes();
        } else {
            creakingRenderState.eyesGlowing = ((Creaking)creaking).isActive();
        }
        creakingRenderState.canMove = ((Creaking)creaking).canMove();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((CreakingRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


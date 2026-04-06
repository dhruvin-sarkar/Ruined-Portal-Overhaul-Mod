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
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;

@Environment(value=EnvType.CLIENT)
public class WardenRenderer
extends MobRenderer<Warden, WardenRenderState, WardenModel> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/warden/warden.png");
    private static final Identifier BIOLUMINESCENT_LAYER_TEXTURE = Identifier.withDefaultNamespace("textures/entity/warden/warden_bioluminescent_layer.png");
    private static final Identifier HEART_TEXTURE = Identifier.withDefaultNamespace("textures/entity/warden/warden_heart.png");
    private static final Identifier PULSATING_SPOTS_TEXTURE_1 = Identifier.withDefaultNamespace("textures/entity/warden/warden_pulsating_spots_1.png");
    private static final Identifier PULSATING_SPOTS_TEXTURE_2 = Identifier.withDefaultNamespace("textures/entity/warden/warden_pulsating_spots_2.png");

    public WardenRenderer(EntityRendererProvider.Context context) {
        super(context, new WardenModel(context.bakeLayer(ModelLayers.WARDEN)), 0.9f);
        WardenModel wardenModel = new WardenModel(context.bakeLayer(ModelLayers.WARDEN_BIOLUMINESCENT));
        WardenModel wardenModel2 = new WardenModel(context.bakeLayer(ModelLayers.WARDEN_PULSATING_SPOTS));
        WardenModel wardenModel3 = new WardenModel(context.bakeLayer(ModelLayers.WARDEN_TENDRILS));
        WardenModel wardenModel4 = new WardenModel(context.bakeLayer(ModelLayers.WARDEN_HEART));
        this.addLayer(new LivingEntityEmissiveLayer<WardenRenderState, WardenModel>(this, wardenRenderState -> BIOLUMINESCENT_LAYER_TEXTURE, (wardenRenderState, f) -> 1.0f, wardenModel, RenderTypes::entityTranslucentEmissive, false));
        this.addLayer(new LivingEntityEmissiveLayer<WardenRenderState, WardenModel>(this, wardenRenderState -> PULSATING_SPOTS_TEXTURE_1, (wardenRenderState, f) -> Math.max(0.0f, Mth.cos(f * 0.045f) * 0.25f), wardenModel2, RenderTypes::entityTranslucentEmissive, false));
        this.addLayer(new LivingEntityEmissiveLayer<WardenRenderState, WardenModel>(this, wardenRenderState -> PULSATING_SPOTS_TEXTURE_2, (wardenRenderState, f) -> Math.max(0.0f, Mth.cos(f * 0.045f + (float)Math.PI) * 0.25f), wardenModel2, RenderTypes::entityTranslucentEmissive, false));
        this.addLayer(new LivingEntityEmissiveLayer<WardenRenderState, WardenModel>(this, wardenRenderState -> TEXTURE, (wardenRenderState, f) -> wardenRenderState.tendrilAnimation, wardenModel3, RenderTypes::entityTranslucentEmissive, false));
        this.addLayer(new LivingEntityEmissiveLayer<WardenRenderState, WardenModel>(this, wardenRenderState -> HEART_TEXTURE, (wardenRenderState, f) -> wardenRenderState.heartAnimation, wardenModel4, RenderTypes::entityTranslucentEmissive, false));
    }

    @Override
    public Identifier getTextureLocation(WardenRenderState wardenRenderState) {
        return TEXTURE;
    }

    @Override
    public WardenRenderState createRenderState() {
        return new WardenRenderState();
    }

    @Override
    public void extractRenderState(Warden warden, WardenRenderState wardenRenderState, float f) {
        super.extractRenderState(warden, wardenRenderState, f);
        wardenRenderState.tendrilAnimation = warden.getTendrilAnimation(f);
        wardenRenderState.heartAnimation = warden.getHeartAnimation(f);
        wardenRenderState.roarAnimationState.copyFrom(warden.roarAnimationState);
        wardenRenderState.sniffAnimationState.copyFrom(warden.sniffAnimationState);
        wardenRenderState.emergeAnimationState.copyFrom(warden.emergeAnimationState);
        wardenRenderState.diggingAnimationState.copyFrom(warden.diggingAnimationState);
        wardenRenderState.attackAnimationState.copyFrom(warden.attackAnimationState);
        wardenRenderState.sonicBoomAnimationState.copyFrom(warden.sonicBoomAnimationState);
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((WardenRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


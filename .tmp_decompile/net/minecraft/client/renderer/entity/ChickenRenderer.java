/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.model.animal.chicken.ColdChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;

@Environment(value=EnvType.CLIENT)
public class ChickenRenderer
extends MobRenderer<Chicken, ChickenRenderState, ChickenModel> {
    private final Map<ChickenVariant.ModelType, AdultAndBabyModelPair<ChickenModel>> models;

    public ChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new ChickenModel(context.bakeLayer(ModelLayers.CHICKEN)), 0.3f);
        this.models = ChickenRenderer.bakeModels(context);
    }

    private static Map<ChickenVariant.ModelType, AdultAndBabyModelPair<ChickenModel>> bakeModels(EntityRendererProvider.Context context) {
        return Maps.newEnumMap((Map)Map.of((Object)ChickenVariant.ModelType.NORMAL, new AdultAndBabyModelPair<ChickenModel>(new ChickenModel(context.bakeLayer(ModelLayers.CHICKEN)), new ChickenModel(context.bakeLayer(ModelLayers.CHICKEN_BABY))), (Object)ChickenVariant.ModelType.COLD, new AdultAndBabyModelPair<ColdChickenModel>(new ColdChickenModel(context.bakeLayer(ModelLayers.COLD_CHICKEN)), new ColdChickenModel(context.bakeLayer(ModelLayers.COLD_CHICKEN_BABY)))));
    }

    @Override
    public void submit(ChickenRenderState chickenRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (chickenRenderState.variant == null) {
            return;
        }
        this.model = this.models.get(chickenRenderState.variant.modelAndTexture().model()).getModel(chickenRenderState.isBaby);
        super.submit(chickenRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public Identifier getTextureLocation(ChickenRenderState chickenRenderState) {
        return chickenRenderState.variant == null ? MissingTextureAtlasSprite.getLocation() : chickenRenderState.variant.modelAndTexture().asset().texturePath();
    }

    @Override
    public ChickenRenderState createRenderState() {
        return new ChickenRenderState();
    }

    @Override
    public void extractRenderState(Chicken chicken, ChickenRenderState chickenRenderState, float f) {
        super.extractRenderState(chicken, chickenRenderState, f);
        chickenRenderState.flap = Mth.lerp(f, chicken.oFlap, chicken.flap);
        chickenRenderState.flapSpeed = Mth.lerp(f, chicken.oFlapSpeed, chicken.flapSpeed);
        chickenRenderState.variant = chicken.getVariant().value();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ChickenRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


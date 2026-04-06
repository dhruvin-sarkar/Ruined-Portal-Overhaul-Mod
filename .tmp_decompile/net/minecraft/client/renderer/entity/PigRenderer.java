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
import net.minecraft.client.model.animal.pig.ColdPigModel;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.pig.PigVariant;

@Environment(value=EnvType.CLIENT)
public class PigRenderer
extends MobRenderer<Pig, PigRenderState, PigModel> {
    private final Map<PigVariant.ModelType, AdultAndBabyModelPair<PigModel>> models;

    public PigRenderer(EntityRendererProvider.Context context) {
        super(context, new PigModel(context.bakeLayer(ModelLayers.PIG)), 0.7f);
        this.models = PigRenderer.bakeModels(context);
        this.addLayer(new SimpleEquipmentLayer<PigRenderState, PigModel, PigModel>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.PIG_SADDLE, pigRenderState -> pigRenderState.saddle, new PigModel(context.bakeLayer(ModelLayers.PIG_SADDLE)), new PigModel(context.bakeLayer(ModelLayers.PIG_BABY_SADDLE))));
    }

    private static Map<PigVariant.ModelType, AdultAndBabyModelPair<PigModel>> bakeModels(EntityRendererProvider.Context context) {
        return Maps.newEnumMap((Map)Map.of((Object)PigVariant.ModelType.NORMAL, new AdultAndBabyModelPair<PigModel>(new PigModel(context.bakeLayer(ModelLayers.PIG)), new PigModel(context.bakeLayer(ModelLayers.PIG_BABY))), (Object)PigVariant.ModelType.COLD, new AdultAndBabyModelPair<ColdPigModel>(new ColdPigModel(context.bakeLayer(ModelLayers.COLD_PIG)), new ColdPigModel(context.bakeLayer(ModelLayers.COLD_PIG_BABY)))));
    }

    @Override
    public void submit(PigRenderState pigRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (pigRenderState.variant == null) {
            return;
        }
        this.model = this.models.get(pigRenderState.variant.modelAndTexture().model()).getModel(pigRenderState.isBaby);
        super.submit(pigRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public Identifier getTextureLocation(PigRenderState pigRenderState) {
        return pigRenderState.variant == null ? MissingTextureAtlasSprite.getLocation() : pigRenderState.variant.modelAndTexture().asset().texturePath();
    }

    @Override
    public PigRenderState createRenderState() {
        return new PigRenderState();
    }

    @Override
    public void extractRenderState(Pig pig, PigRenderState pigRenderState, float f) {
        super.extractRenderState(pig, pigRenderState, f);
        pigRenderState.saddle = pig.getItemBySlot(EquipmentSlot.SADDLE).copy();
        pigRenderState.variant = pig.getVariant().value();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((PigRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


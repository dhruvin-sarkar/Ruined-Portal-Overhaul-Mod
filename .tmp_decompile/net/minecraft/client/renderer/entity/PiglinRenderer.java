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
import net.minecraft.client.model.monster.piglin.PiglinModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.CrossbowItem;

@Environment(value=EnvType.CLIENT)
public class PiglinRenderer
extends HumanoidMobRenderer<AbstractPiglin, PiglinRenderState, PiglinModel> {
    private static final Identifier PIGLIN_LOCATION = Identifier.withDefaultNamespace("textures/entity/piglin/piglin.png");
    private static final Identifier PIGLIN_BRUTE_LOCATION = Identifier.withDefaultNamespace("textures/entity/piglin/piglin_brute.png");
    public static final CustomHeadLayer.Transforms PIGLIN_CUSTOM_HEAD_TRANSFORMS = new CustomHeadLayer.Transforms(0.0f, 0.0f, 1.0019531f);

    public PiglinRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, ArmorModelSet<ModelLayerLocation> armorModelSet, ArmorModelSet<ModelLayerLocation> armorModelSet2) {
        super(context, new PiglinModel(context.bakeLayer(modelLayerLocation)), new PiglinModel(context.bakeLayer(modelLayerLocation2)), 0.5f, PIGLIN_CUSTOM_HEAD_TRANSFORMS);
        this.addLayer(new HumanoidArmorLayer<PiglinRenderState, PiglinModel, PiglinModel>(this, ArmorModelSet.bake(armorModelSet, context.getModelSet(), PiglinModel::new), ArmorModelSet.bake(armorModelSet2, context.getModelSet(), PiglinModel::new), context.getEquipmentRenderer()));
    }

    @Override
    public Identifier getTextureLocation(PiglinRenderState piglinRenderState) {
        return piglinRenderState.isBrute ? PIGLIN_BRUTE_LOCATION : PIGLIN_LOCATION;
    }

    @Override
    public PiglinRenderState createRenderState() {
        return new PiglinRenderState();
    }

    @Override
    public void extractRenderState(AbstractPiglin abstractPiglin, PiglinRenderState piglinRenderState, float f) {
        super.extractRenderState(abstractPiglin, piglinRenderState, f);
        piglinRenderState.isBrute = abstractPiglin.getType() == EntityType.PIGLIN_BRUTE;
        piglinRenderState.armPose = abstractPiglin.getArmPose();
        piglinRenderState.maxCrossbowChageDuration = CrossbowItem.getChargeDuration(abstractPiglin.getUseItem(), abstractPiglin);
        piglinRenderState.isConverting = abstractPiglin.isConverting();
    }

    @Override
    protected boolean isShaking(PiglinRenderState piglinRenderState) {
        return super.isShaking(piglinRenderState) || piglinRenderState.isConverting;
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntityRenderState livingEntityRenderState) {
        return this.isShaking((PiglinRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((PiglinRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


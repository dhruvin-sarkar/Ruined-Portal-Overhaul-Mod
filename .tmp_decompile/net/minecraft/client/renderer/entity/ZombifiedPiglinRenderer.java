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
import net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;

@Environment(value=EnvType.CLIENT)
public class ZombifiedPiglinRenderer
extends HumanoidMobRenderer<ZombifiedPiglin, ZombifiedPiglinRenderState, ZombifiedPiglinModel> {
    private static final Identifier ZOMBIFIED_PIGLIN_LOCATION = Identifier.withDefaultNamespace("textures/entity/piglin/zombified_piglin.png");

    public ZombifiedPiglinRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, ArmorModelSet<ModelLayerLocation> armorModelSet, ArmorModelSet<ModelLayerLocation> armorModelSet2) {
        super(context, new ZombifiedPiglinModel(context.bakeLayer(modelLayerLocation)), new ZombifiedPiglinModel(context.bakeLayer(modelLayerLocation2)), 0.5f, PiglinRenderer.PIGLIN_CUSTOM_HEAD_TRANSFORMS);
        this.addLayer(new HumanoidArmorLayer<ZombifiedPiglinRenderState, ZombifiedPiglinModel, ZombifiedPiglinModel>(this, ArmorModelSet.bake(armorModelSet, context.getModelSet(), ZombifiedPiglinModel::new), ArmorModelSet.bake(armorModelSet2, context.getModelSet(), ZombifiedPiglinModel::new), context.getEquipmentRenderer()));
    }

    @Override
    public Identifier getTextureLocation(ZombifiedPiglinRenderState zombifiedPiglinRenderState) {
        return ZOMBIFIED_PIGLIN_LOCATION;
    }

    @Override
    public ZombifiedPiglinRenderState createRenderState() {
        return new ZombifiedPiglinRenderState();
    }

    @Override
    public void extractRenderState(ZombifiedPiglin zombifiedPiglin, ZombifiedPiglinRenderState zombifiedPiglinRenderState, float f) {
        super.extractRenderState(zombifiedPiglin, zombifiedPiglinRenderState, f);
        zombifiedPiglinRenderState.isAggressive = zombifiedPiglin.isAggressive();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ZombifiedPiglinRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


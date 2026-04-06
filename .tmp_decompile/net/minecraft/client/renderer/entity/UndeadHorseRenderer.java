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
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

@Environment(value=EnvType.CLIENT)
public class UndeadHorseRenderer
extends AbstractHorseRenderer<AbstractHorse, EquineRenderState, AbstractEquineModel<EquineRenderState>> {
    private final Identifier texture;

    public UndeadHorseRenderer(EntityRendererProvider.Context context, Type type) {
        super(context, new HorseModel(context.bakeLayer(type.model)), new HorseModel(context.bakeLayer(type.babyModel)));
        this.texture = type.texture;
        this.addLayer(new SimpleEquipmentLayer<EquineRenderState, AbstractEquineModel<EquineRenderState>, HorseModel>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.HORSE_BODY, equineRenderState -> equineRenderState.bodyArmorItem, new HorseModel(context.bakeLayer(ModelLayers.UNDEAD_HORSE_ARMOR)), new HorseModel(context.bakeLayer(ModelLayers.UNDEAD_HORSE_BABY_ARMOR))));
        this.addLayer(new SimpleEquipmentLayer<EquineRenderState, AbstractEquineModel<EquineRenderState>, EquineSaddleModel>(this, context.getEquipmentRenderer(), type.saddleLayer, equineRenderState -> equineRenderState.saddle, new EquineSaddleModel(context.bakeLayer(type.saddleModel)), new EquineSaddleModel(context.bakeLayer(type.babySaddleModel))));
    }

    @Override
    public Identifier getTextureLocation(EquineRenderState equineRenderState) {
        return this.texture;
    }

    @Override
    public EquineRenderState createRenderState() {
        return new EquineRenderState();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((EquineRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        SKELETON(Identifier.withDefaultNamespace("textures/entity/horse/horse_skeleton.png"), ModelLayers.SKELETON_HORSE, ModelLayers.SKELETON_HORSE_BABY, EquipmentClientInfo.LayerType.SKELETON_HORSE_SADDLE, ModelLayers.SKELETON_HORSE_SADDLE, ModelLayers.SKELETON_HORSE_BABY_SADDLE),
        ZOMBIE(Identifier.withDefaultNamespace("textures/entity/horse/horse_zombie.png"), ModelLayers.ZOMBIE_HORSE, ModelLayers.ZOMBIE_HORSE_BABY, EquipmentClientInfo.LayerType.ZOMBIE_HORSE_SADDLE, ModelLayers.ZOMBIE_HORSE_SADDLE, ModelLayers.ZOMBIE_HORSE_BABY_SADDLE);

        final Identifier texture;
        final ModelLayerLocation model;
        final ModelLayerLocation babyModel;
        final EquipmentClientInfo.LayerType saddleLayer;
        final ModelLayerLocation saddleModel;
        final ModelLayerLocation babySaddleModel;

        private Type(Identifier identifier, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, EquipmentClientInfo.LayerType layerType, ModelLayerLocation modelLayerLocation3, ModelLayerLocation modelLayerLocation4) {
            this.texture = identifier;
            this.model = modelLayerLocation;
            this.babyModel = modelLayerLocation2;
            this.saddleLayer = layerType;
            this.saddleModel = modelLayerLocation3;
            this.babySaddleModel = modelLayerLocation4;
        }
    }
}


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
import net.minecraft.client.model.animal.ghast.HappyGhastHarnessModel;
import net.minecraft.client.model.animal.ghast.HappyGhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RopesLayer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.phys.AABB;

@Environment(value=EnvType.CLIENT)
public class HappyGhastRenderer
extends AgeableMobRenderer<HappyGhast, HappyGhastRenderState, HappyGhastModel> {
    private static final Identifier GHAST_LOCATION = Identifier.withDefaultNamespace("textures/entity/ghast/happy_ghast.png");
    private static final Identifier GHAST_BABY_LOCATION = Identifier.withDefaultNamespace("textures/entity/ghast/happy_ghast_baby.png");
    private static final Identifier GHAST_ROPES = Identifier.withDefaultNamespace("textures/entity/ghast/happy_ghast_ropes.png");

    public HappyGhastRenderer(EntityRendererProvider.Context context) {
        super(context, new HappyGhastModel(context.bakeLayer(ModelLayers.HAPPY_GHAST)), new HappyGhastModel(context.bakeLayer(ModelLayers.HAPPY_GHAST_BABY)), 2.0f);
        this.addLayer(new SimpleEquipmentLayer<HappyGhastRenderState, HappyGhastModel, HappyGhastHarnessModel>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.HAPPY_GHAST_BODY, happyGhastRenderState -> happyGhastRenderState.bodyItem, new HappyGhastHarnessModel(context.bakeLayer(ModelLayers.HAPPY_GHAST_HARNESS)), new HappyGhastHarnessModel(context.bakeLayer(ModelLayers.HAPPY_GHAST_BABY_HARNESS))));
        this.addLayer(new RopesLayer<HappyGhastModel>(this, context.getModelSet(), GHAST_ROPES));
    }

    @Override
    public Identifier getTextureLocation(HappyGhastRenderState happyGhastRenderState) {
        if (happyGhastRenderState.isBaby) {
            return GHAST_BABY_LOCATION;
        }
        return GHAST_LOCATION;
    }

    @Override
    public HappyGhastRenderState createRenderState() {
        return new HappyGhastRenderState();
    }

    @Override
    protected AABB getBoundingBoxForCulling(HappyGhast happyGhast) {
        AABB aABB = super.getBoundingBoxForCulling(happyGhast);
        float f = happyGhast.getBbHeight();
        return aABB.setMinY(aABB.minY - (double)(f / 2.0f));
    }

    @Override
    public void extractRenderState(HappyGhast happyGhast, HappyGhastRenderState happyGhastRenderState, float f) {
        super.extractRenderState(happyGhast, happyGhastRenderState, f);
        happyGhastRenderState.bodyItem = happyGhast.getItemBySlot(EquipmentSlot.BODY).copy();
        happyGhastRenderState.isRidden = happyGhast.isVehicle();
        happyGhastRenderState.isLeashHolder = happyGhast.isLeashHolder();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((HappyGhastRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


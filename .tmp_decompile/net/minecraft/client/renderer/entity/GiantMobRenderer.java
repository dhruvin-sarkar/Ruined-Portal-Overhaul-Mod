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
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.GiantZombieModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Giant;

@Environment(value=EnvType.CLIENT)
public class GiantMobRenderer
extends MobRenderer<Giant, ZombieRenderState, HumanoidModel<ZombieRenderState>> {
    private static final Identifier ZOMBIE_LOCATION = Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");

    public GiantMobRenderer(EntityRendererProvider.Context context, float f) {
        super(context, new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT)), 0.5f * f);
        this.addLayer(new ItemInHandLayer<ZombieRenderState, HumanoidModel<ZombieRenderState>>(this));
        this.addLayer(new HumanoidArmorLayer<ZombieRenderState, HumanoidModel<ZombieRenderState>, GiantZombieModel>(this, ArmorModelSet.bake(ModelLayers.GIANT_ARMOR, context.getModelSet(), GiantZombieModel::new), context.getEquipmentRenderer()));
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState zombieRenderState) {
        return ZOMBIE_LOCATION;
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(Giant giant, ZombieRenderState zombieRenderState, float f) {
        super.extractRenderState(giant, zombieRenderState, f);
        HumanoidMobRenderer.extractHumanoidRenderState(giant, zombieRenderState, f, this.itemModelResolver);
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ZombieRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


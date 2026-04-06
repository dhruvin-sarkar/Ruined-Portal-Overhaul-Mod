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
import net.minecraft.client.model.monster.zombie.ZombieVillagerModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieVillagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;

@Environment(value=EnvType.CLIENT)
public class ZombieVillagerRenderer
extends HumanoidMobRenderer<ZombieVillager, ZombieVillagerRenderState, ZombieVillagerModel<ZombieVillagerRenderState>> {
    private static final Identifier ZOMBIE_VILLAGER_LOCATION = Identifier.withDefaultNamespace("textures/entity/zombie_villager/zombie_villager.png");

    public ZombieVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER)), new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_BABY)), 0.5f, VillagerRenderer.CUSTOM_HEAD_TRANSFORMS);
        this.addLayer(new HumanoidArmorLayer<ZombieVillagerRenderState, ZombieVillagerModel<ZombieVillagerRenderState>, ZombieVillagerModel>(this, ArmorModelSet.bake(ModelLayers.ZOMBIE_VILLAGER_ARMOR, context.getModelSet(), ZombieVillagerModel::new), ArmorModelSet.bake(ModelLayers.ZOMBIE_VILLAGER_BABY_ARMOR, context.getModelSet(), ZombieVillagerModel::new), context.getEquipmentRenderer()));
        this.addLayer(new VillagerProfessionLayer(this, context.getResourceManager(), "zombie_villager", new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_NO_HAT)), new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_BABY_NO_HAT))));
    }

    @Override
    public Identifier getTextureLocation(ZombieVillagerRenderState zombieVillagerRenderState) {
        return ZOMBIE_VILLAGER_LOCATION;
    }

    @Override
    public ZombieVillagerRenderState createRenderState() {
        return new ZombieVillagerRenderState();
    }

    @Override
    public void extractRenderState(ZombieVillager zombieVillager, ZombieVillagerRenderState zombieVillagerRenderState, float f) {
        super.extractRenderState(zombieVillager, zombieVillagerRenderState, f);
        zombieVillagerRenderState.isConverting = zombieVillager.isConverting();
        zombieVillagerRenderState.villagerData = zombieVillager.getVillagerData();
        zombieVillagerRenderState.isAggressive = zombieVillager.isAggressive();
    }

    @Override
    protected boolean isShaking(ZombieVillagerRenderState zombieVillagerRenderState) {
        return super.isShaking(zombieVillagerRenderState) || zombieVillagerRenderState.isConverting;
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntityRenderState livingEntityRenderState) {
        return this.isShaking((ZombieVillagerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ZombieVillagerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


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
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;

@Environment(value=EnvType.CLIENT)
public class WanderingTraderRenderer
extends MobRenderer<WanderingTrader, VillagerRenderState, VillagerModel> {
    private static final Identifier VILLAGER_BASE_SKIN = Identifier.withDefaultNamespace("textures/entity/wandering_trader.png");

    public WanderingTraderRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5f);
        this.addLayer(new CustomHeadLayer<VillagerRenderState, VillagerModel>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
        this.addLayer(new CrossedArmsItemLayer<VillagerRenderState, VillagerModel>(this));
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState villagerRenderState) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    @Override
    public void extractRenderState(WanderingTrader wanderingTrader, VillagerRenderState villagerRenderState, float f) {
        super.extractRenderState(wanderingTrader, villagerRenderState, f);
        HoldingEntityRenderState.extractHoldingEntityRenderState(wanderingTrader, villagerRenderState, this.itemModelResolver);
        villagerRenderState.isUnhappy = wanderingTrader.getUnhappyCounter() > 0;
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((VillagerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}


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
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.item.CrossbowItem;

@Environment(value=EnvType.CLIENT)
public abstract class IllagerRenderer<T extends AbstractIllager, S extends IllagerRenderState>
extends MobRenderer<T, S, IllagerModel<S>> {
    protected IllagerRenderer(EntityRendererProvider.Context context, IllagerModel<S> illagerModel, float f) {
        super(context, illagerModel, f);
        this.addLayer(new CustomHeadLayer(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
    }

    @Override
    public void extractRenderState(T abstractIllager, S illagerRenderState, float f) {
        super.extractRenderState(abstractIllager, illagerRenderState, f);
        ArmedEntityRenderState.extractArmedEntityRenderState(abstractIllager, illagerRenderState, this.itemModelResolver, f);
        ((IllagerRenderState)illagerRenderState).isRiding = ((Entity)abstractIllager).isPassenger();
        ((IllagerRenderState)illagerRenderState).mainArm = ((Mob)abstractIllager).getMainArm();
        ((IllagerRenderState)illagerRenderState).armPose = ((AbstractIllager)abstractIllager).getArmPose();
        ((IllagerRenderState)illagerRenderState).maxCrossbowChargeDuration = ((IllagerRenderState)illagerRenderState).armPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE ? CrossbowItem.getChargeDuration(((LivingEntity)abstractIllager).getUseItem(), abstractIllager) : 0;
        ((IllagerRenderState)illagerRenderState).ticksUsingItem = ((LivingEntity)abstractIllager).getTicksUsingItem(f);
        ((IllagerRenderState)illagerRenderState).attackAnim = ((LivingEntity)abstractIllager).getAttackAnim(f);
        ((IllagerRenderState)illagerRenderState).isAggressive = ((Mob)abstractIllager).isAggressive();
    }
}


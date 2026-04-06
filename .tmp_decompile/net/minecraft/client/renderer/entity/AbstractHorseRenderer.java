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
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractHorseRenderer<T extends AbstractHorse, S extends EquineRenderState, M extends EntityModel<? super S>>
extends AgeableMobRenderer<T, S, M> {
    public AbstractHorseRenderer(EntityRendererProvider.Context context, M entityModel, M entityModel2) {
        super(context, entityModel, entityModel2, 0.75f);
    }

    @Override
    public void extractRenderState(T abstractHorse, S equineRenderState, float f) {
        super.extractRenderState(abstractHorse, equineRenderState, f);
        ((EquineRenderState)equineRenderState).saddle = ((LivingEntity)abstractHorse).getItemBySlot(EquipmentSlot.SADDLE).copy();
        ((EquineRenderState)equineRenderState).bodyArmorItem = ((Mob)abstractHorse).getBodyArmorItem().copy();
        ((EquineRenderState)equineRenderState).isRidden = ((Entity)abstractHorse).isVehicle();
        ((EquineRenderState)equineRenderState).eatAnimation = ((AbstractHorse)abstractHorse).getEatAnim(f);
        ((EquineRenderState)equineRenderState).standAnimation = ((AbstractHorse)abstractHorse).getStandAnim(f);
        ((EquineRenderState)equineRenderState).feedingAnimation = ((AbstractHorse)abstractHorse).getMouthAnim(f);
        ((EquineRenderState)equineRenderState).animateTail = ((AbstractHorse)abstractHorse).tailCounter > 0;
    }
}


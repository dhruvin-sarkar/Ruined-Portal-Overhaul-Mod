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
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

@Environment(value=EnvType.CLIENT)
public abstract class MobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
extends LivingEntityRenderer<T, S, M> {
    public MobRenderer(EntityRendererProvider.Context context, M entityModel, float f) {
        super(context, entityModel, f);
    }

    @Override
    protected boolean shouldShowName(T mob, double d) {
        return super.shouldShowName(mob, d) && (((LivingEntity)mob).shouldShowName() || ((Entity)mob).hasCustomName() && mob == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    protected float getShadowRadius(S livingEntityRenderState) {
        return super.getShadowRadius(livingEntityRenderState) * ((LivingEntityRenderState)livingEntityRenderState).ageScale;
    }

    protected static boolean checkMagicName(Entity entity, String string) {
        Component component = entity.getCustomName();
        return component != null && string.equals(component.getString());
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((S)((LivingEntityRenderState)entityRenderState));
    }
}


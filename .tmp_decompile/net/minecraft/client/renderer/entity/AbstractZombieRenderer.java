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
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.SwingAnimation;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractZombieRenderer<T extends Zombie, S extends ZombieRenderState, M extends ZombieModel<S>>
extends HumanoidMobRenderer<T, S, M> {
    private static final Identifier ZOMBIE_LOCATION = Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");

    protected AbstractZombieRenderer(EntityRendererProvider.Context context, M zombieModel, M zombieModel2, ArmorModelSet<M> armorModelSet, ArmorModelSet<M> armorModelSet2) {
        super(context, zombieModel, zombieModel2, 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, armorModelSet, armorModelSet2, context.getEquipmentRenderer()));
    }

    @Override
    public Identifier getTextureLocation(S zombieRenderState) {
        return ZOMBIE_LOCATION;
    }

    @Override
    public void extractRenderState(T zombie, S zombieRenderState, float f) {
        super.extractRenderState(zombie, zombieRenderState, f);
        ((ZombieRenderState)zombieRenderState).isAggressive = ((Mob)zombie).isAggressive();
        ((ZombieRenderState)zombieRenderState).isConverting = ((Zombie)zombie).isUnderWaterConverting();
    }

    @Override
    protected boolean isShaking(S zombieRenderState) {
        return super.isShaking(zombieRenderState) || ((ZombieRenderState)zombieRenderState).isConverting;
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(T zombie, HumanoidArm humanoidArm) {
        SwingAnimation swingAnimation = ((LivingEntity)zombie).getItemHeldByArm(humanoidArm.getOpposite()).get(DataComponents.SWING_ANIMATION);
        if (swingAnimation != null && swingAnimation.type() == SwingAnimationType.STAB) {
            return HumanoidModel.ArmPose.SPEAR;
        }
        return super.getArmPose(zombie, humanoidArm);
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntityRenderState livingEntityRenderState) {
        return this.isShaking((S)((ZombieRenderState)livingEntityRenderState));
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((S)((ZombieRenderState)livingEntityRenderState));
    }
}


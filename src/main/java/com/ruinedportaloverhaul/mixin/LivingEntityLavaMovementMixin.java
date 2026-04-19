package com.ruinedportaloverhaul.mixin;

import com.ruinedportaloverhaul.block.entity.NetherConduitPowerTracker;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
abstract class LivingEntityLavaMovementMixin {
    @ModifyConstant(
        method = "travelFlying(Lnet/minecraft/world/phys/Vec3;FFF)V",
        constant = @Constant(doubleValue = 0.5D)
    )
    private double ruinedportaloverhaul$softenFlyingLavaDrag(double original) {
        return this.ruinedportaloverhaul$lavaDrag(original);
    }

    @ModifyConstant(
        method = "travelInLava(Lnet/minecraft/world/phys/Vec3;DZD)V",
        constant = @Constant(doubleValue = 0.5D)
    )
    private double ruinedportaloverhaul$softenLavaDrag(double original) {
        return this.ruinedportaloverhaul$lavaDrag(original);
    }

    @ModifyConstant(
        method = "travelInLava(Lnet/minecraft/world/phys/Vec3;DZD)V",
        constant = @Constant(floatValue = 0.02F)
    )
    private float ruinedportaloverhaul$increaseLavaAcceleration(float original) {
        int level = NetherConduitPowerTracker.lavaBoostLevel((LivingEntity) (Object) this);
        if (level < 0) {
            return original;
        }
        return level >= 2 ? 0.08F : 0.04F;
    }

    private double ruinedportaloverhaul$lavaDrag(double original) {
        int level = NetherConduitPowerTracker.lavaBoostLevel((LivingEntity) (Object) this);
        if (level < 0) {
            return original;
        }
        return level >= 2 ? 0.91D : 0.72D;
    }
}

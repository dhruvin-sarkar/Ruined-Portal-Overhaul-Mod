package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NetherCrystalEntity extends EndCrystal implements GeoEntity {
    private static final RawAnimation PULSE = RawAnimation.begin().thenLoop("misc.pulse");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public NetherCrystalEntity(EntityType<? extends NetherCrystalEntity> entityType, Level level) {
        super(entityType, level);
    }

    public NetherCrystalEntity(Level level, double x, double y, double z) {
        this(ModEntities.NETHER_CRYSTAL, level);
        this.setPos(x, y, z);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.NETHER_CRYSTAL);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Fix: the ritual crystal still used a static vanilla renderer, so it could not be resource-pack animated like the rest of the endgame objects. This controller moves its pulse/rotation into GeckoLib data.
        controllers.add(new AnimationController<>("Crystal", 0, state -> state.setAndContinue(PULSE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}

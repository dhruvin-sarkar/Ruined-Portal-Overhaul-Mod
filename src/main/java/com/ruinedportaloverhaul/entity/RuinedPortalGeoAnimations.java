package com.ruinedportaloverhaul.entity;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;

public final class RuinedPortalGeoAnimations {
    public static final String ACTION_CONTROLLER = "Actions";
    public static final String ATTACK_SWING = "swing";
    public static final String ATTACK_SHOOT = "shoot";
    public static final String ATTACK_CAST = "cast";
    public static final String ATTACK_SLAM = "slam";
    public static final String ATTACK_FLYING = "flying";

    private RuinedPortalGeoAnimations() {
    }

    public static <T extends GeoAnimatable> AnimationController<T> walkIdleController() {
        return DefaultAnimations.genericWalkIdleController();
    }

    public static <T extends GeoAnimatable> AnimationController<T> flyIdleController() {
        return DefaultAnimations.genericFlyIdleController();
    }

    public static <T extends LivingEntity & GeoAnimatable> AnimationController<T> deathController() {
        return DefaultAnimations.genericDeathController();
    }

    public static <T extends GeoAnimatable> AnimationController<T> actionController() {
        return DefaultAnimations.<T>triggerOnlyController()
            .triggerableAnim(ATTACK_SWING, DefaultAnimations.ATTACK_SWING)
            .triggerableAnim(ATTACK_SHOOT, DefaultAnimations.ATTACK_SHOOT)
            .triggerableAnim(ATTACK_CAST, DefaultAnimations.ATTACK_CAST)
            .triggerableAnim(ATTACK_SLAM, DefaultAnimations.ATTACK_SLAM)
            .triggerableAnim(ATTACK_FLYING, DefaultAnimations.ATTACK_FLYING_ATTACK);
    }
}

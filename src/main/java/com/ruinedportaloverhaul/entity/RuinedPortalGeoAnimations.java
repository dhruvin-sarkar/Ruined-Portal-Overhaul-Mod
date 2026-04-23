package com.ruinedportaloverhaul.entity;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;

public final class RuinedPortalGeoAnimations {
    public static final String ACTION_CONTROLLER = "Actions";
    public static final String ATTACK_SWING = "swing";
    public static final String ATTACK_SHOOT = "shoot";
    public static final String ATTACK_CAST = "cast";
    public static final String ATTACK_SLAM = "slam";
    public static final String ATTACK_FLYING = "flying";
    public static final String ATTACK_ROAR = "roar";
    private static final RawAnimation RAVAGER_ROAR = RawAnimation.begin().thenPlay("action.roar");

    private RuinedPortalGeoAnimations() {
    }

    public static <T extends GeoAnimatable> AnimationController<T> walkIdleController() {
        return DefaultAnimations.genericWalkIdleController();
    }

    public static <T extends GeoAnimatable> AnimationController<T> flyIdleController() {
        // Fix: GeckoLib's stock fly-idle helper falls back to `misc.idle`, but the Vex asset intentionally defines `misc.idle.flying`. This controller keeps flying movement on `move.fly` and true hover idle on `misc.idle.flying`.
        return new AnimationController<>("Fly/Idle", state -> state.setAndContinue(state.isMoving() ? DefaultAnimations.FLY : DefaultAnimations.IDLE_FLYING));
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
            .triggerableAnim(ATTACK_FLYING, DefaultAnimations.ATTACK_FLYING_ATTACK)
            .triggerableAnim(ATTACK_ROAR, RAVAGER_ROAR);
    }
}

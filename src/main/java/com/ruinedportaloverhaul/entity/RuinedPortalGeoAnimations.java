package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.world.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.state.KeyFrameEvent;
import software.bernie.geckolib.cache.animation.keyframeevent.ParticleKeyframeData;
import software.bernie.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import software.bernie.geckolib.constant.DataTickets;
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
        return withEncounterKeyframes(DefaultAnimations.<T>triggerOnlyController()
            .triggerableAnim(ATTACK_SWING, DefaultAnimations.ATTACK_SWING)
            .triggerableAnim(ATTACK_SHOOT, DefaultAnimations.ATTACK_SHOOT)
            .triggerableAnim(ATTACK_CAST, DefaultAnimations.ATTACK_CAST)
            .triggerableAnim(ATTACK_SLAM, DefaultAnimations.ATTACK_SLAM)
            .triggerableAnim(ATTACK_FLYING, DefaultAnimations.ATTACK_FLYING_ATTACK)
            .triggerableAnim(ATTACK_ROAR, RAVAGER_ROAR));
    }

    public static <T extends GeoAnimatable> AnimationController<T> withEncounterKeyframes(AnimationController<T> controller) {
        return controller
            .setSoundKeyframeHandler(RuinedPortalGeoAnimations::handleSoundKeyframe)
            .setParticleKeyframeHandler(RuinedPortalGeoAnimations::handleParticleKeyframe);
    }

    private static <T extends GeoAnimatable> void handleSoundKeyframe(KeyFrameEvent<T, SoundKeyframeData> event) {
        Level level = levelOf(event.animatable());
        Vec3 position = positionOf(event);
        if (level == null || position == null || !level.isClientSide()) {
            return;
        }

        String[] segments = event.keyframeData().getSound().split("\\|");
        BuiltInRegistries.SOUND_EVENT.get(Identifier.read(segments[0]).getOrThrow()).ifPresent(sound -> {
            float volume = segments.length > 1 ? parseFloatOrDefault(segments[1], 1.0f) : 1.0f;
            float pitch = segments.length > 2 ? parseFloatOrDefault(segments[2], 1.0f) : 1.0f;
            SoundSource source = event.animatable() instanceof BlockEntity ? SoundSource.BLOCKS : SoundSource.HOSTILE;
            level.playLocalSound(position.x, position.y, position.z, sound.value(), source, volume, pitch, false);
        });
    }

    private static <T extends GeoAnimatable> void handleParticleKeyframe(KeyFrameEvent<T, ParticleKeyframeData> event) {
        Level level = levelOf(event.animatable());
        Vec3 position = positionOf(event);
        SimpleParticleType particle = particleOf(event.keyframeData().getEffect());
        if (level == null || position == null || particle == null || !level.isClientSide()) {
            return;
        }

        ParticleBurst burst = ParticleBurst.fromScript(event.keyframeData().script());
        Vec3 center = position.add(0.0, burst.yOffset, 0.0);
        for (int i = 0; i < burst.count; i++) {
            double x = center.x + (level.random.nextDouble() - 0.5) * burst.spread;
            double y = center.y + (level.random.nextDouble() - 0.5) * burst.verticalSpread;
            double z = center.z + (level.random.nextDouble() - 0.5) * burst.spread;
            level.addParticle(particle, x, y, z, 0.0, burst.speed, 0.0);
        }
    }

    private static <T extends GeoAnimatable> Vec3 positionOf(KeyFrameEvent<T, ?> event) {
        Vec3 position = event.renderState().getGeckolibData(DataTickets.POSITION);
        if (position != null) {
            return position;
        }

        BlockPos blockPos = event.renderState().getGeckolibData(DataTickets.BLOCKPOS);
        if (blockPos != null) {
            return Vec3.atCenterOf(blockPos);
        }

        return switch (event.animatable()) {
            case Entity entity -> entity.position();
            case BlockEntity blockEntity -> Vec3.atCenterOf(blockEntity.getBlockPos());
            default -> null;
        };
    }

    private static Level levelOf(GeoAnimatable animatable) {
        return switch (animatable) {
            case Entity entity -> entity.level();
            case BlockEntity blockEntity -> blockEntity.getLevel();
            default -> null;
        };
    }

    private static SimpleParticleType particleOf(String id) {
        Identifier netherEmberId = BuiltInRegistries.PARTICLE_TYPE.getKey(ModParticles.NETHER_EMBER);
        Identifier corruptionRuneId = BuiltInRegistries.PARTICLE_TYPE.getKey(ModParticles.CORRUPTION_RUNE);
        if (netherEmberId.toString().equals(id)) {
            return ModParticles.NETHER_EMBER;
        }
        if (corruptionRuneId.toString().equals(id)) {
            return ModParticles.CORRUPTION_RUNE;
        }
        return null;
    }

    private static float parseFloatOrDefault(String rawValue, float fallback) {
        try {
            return Float.parseFloat(rawValue);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private record ParticleBurst(int count, double yOffset, double spread, double verticalSpread, double speed) {
        private static ParticleBurst fromScript(String script) {
            return switch (script) {
                case "axe_impact" -> new ParticleBurst(8, 1.05, 0.55, 0.35, 0.015);
                case "ravager_roar" -> new ParticleBurst(18, 1.65, 1.25, 0.7, 0.025);
                case "evoker_cast" -> new ParticleBurst(12, 1.35, 0.85, 0.45, 0.0);
                case "conduit_pulse" -> new ParticleBurst(14, 0.5, 0.8, 0.8, 0.012);
                default -> new ParticleBurst(6, 1.0, 0.5, 0.4, 0.01);
            };
        }
    }
}

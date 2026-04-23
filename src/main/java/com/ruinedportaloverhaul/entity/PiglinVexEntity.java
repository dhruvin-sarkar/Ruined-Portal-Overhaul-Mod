package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiglinVexEntity extends Vex implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PiglinVexEntity(EntityType<? extends PiglinVexEntity> entityType, Level level) {
        super(entityType, level);
        this.setLimitedLife(70 * 20);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Vex.createAttributes()
            .add(Attributes.MAX_HEALTH, 28.0)
            .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        return PiglinDifficultyScaler.applySpawnScaling(this, level, result);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
            RuinedPortalGeoAnimations.flyIdleController(),
            RuinedPortalGeoAnimations.deathController(),
            RuinedPortalGeoAnimations.actionController()
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        // Fix: Piglin Vex dives previously used only vanilla hit feedback, so successful strikes now trigger the flying attack animation declared in its GeckoLib assets.
        boolean damaged = super.doHurtTarget(serverLevel, target);
        if (damaged) {
            this.triggerAnim(RuinedPortalGeoAnimations.ACTION_CONTROLLER, RuinedPortalGeoAnimations.ATTACK_FLYING);
        }
        return damaged;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_PIGLIN_VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_PIGLIN_VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_PIGLIN_VEX_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        // Fix: the custom piglin vex sound set used inherited volume, so its smaller body now has a deliberate quieter mix level.
        return 0.75f;
    }

    @Override
    public float getVoicePitch() {
        return 1.3f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.12f;
    }
}

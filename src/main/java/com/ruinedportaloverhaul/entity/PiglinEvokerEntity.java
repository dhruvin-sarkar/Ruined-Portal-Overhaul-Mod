package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiglinEvokerEntity extends Evoker implements GeoEntity, TextureVariantMob {
    private static final int FANG_COOLDOWN_TICKS = 160;
    private static final int VEX_COOLDOWN_TICKS = 220;
    private static final int TEXTURE_VARIANT_COUNT = 2;
    private static final EntityDataAccessor<Integer> TEXTURE_VARIANT = SynchedEntityData.defineId(PiglinEvokerEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int fangCooldown;
    private int vexCooldown;
    private boolean summonedDesperationVex;

    public PiglinEvokerEntity(EntityType<? extends PiglinEvokerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Evoker.createAttributes()
            .add(Attributes.MAX_HEALTH, 70.0)
            .add(Attributes.MOVEMENT_SPEED, 0.27);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Fix: evokers previously had no synced visual variant, so GeckoLib could only ever render the base texture. The builder now registers a stable variant slot for client rendering.
        super.defineSynchedData(builder);
        builder.define(TEXTURE_VARIANT, 0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        // Fix: evoker spawn setup previously handled only stats, leaving every caster visually identical. The UUID now seeds a deterministic variant before any client render occurs.
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.getEntityData().set(TEXTURE_VARIANT, TextureVariantHelper.selectVariant(this.getUUID(), TEXTURE_VARIANT_COUNT));
        return PiglinDifficultyScaler.applySpawnScaling(this, level, result);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
            RuinedPortalGeoAnimations.walkIdleController(),
            RuinedPortalGeoAnimations.deathController(),
            RuinedPortalGeoAnimations.actionController()
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);
        if (this.fangCooldown > 0) {
            this.fangCooldown--;
        }
        if (this.vexCooldown > 0) {
            this.vexCooldown--;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        if (!this.summonedDesperationVex && this.getHealth() <= this.getMaxHealth() * 0.5f) {
            summonPiglinVexes(serverLevel, 2);
            this.summonedDesperationVex = true;
        }

        double distance = this.distanceToSqr(target);
        if (distance <= 196.0 && this.fangCooldown <= 0) {
            castMagmaEruption(serverLevel, target.position());
            target.igniteForSeconds(4.0f);
            this.fangCooldown = FANG_COOLDOWN_TICKS;
        }

        if (distance <= 256.0 && this.vexCooldown <= 0) {
            summonPiglinVexes(serverLevel, 4);
            this.vexCooldown = VEX_COOLDOWN_TICKS;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        // Fix: the evoker's save data previously tracked only spell-state flags, so its chosen appearance was lost across reloads. The current variant is now stored with the rest of the entity state.
        super.addAdditionalSaveData(valueOutput);
        TextureVariantHelper.writeVariant(valueOutput, this.getTextureVariant(), TEXTURE_VARIANT_COUNT);
        valueOutput.putBoolean("SummonedDesperationVex", this.summonedDesperationVex);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        // Fix: restored evokers used to forget which texture they were using because only combat flags were reloaded. The saved variant now repopulates synced data before AI resumes.
        super.readAdditionalSaveData(valueInput);
        this.getEntityData().set(TEXTURE_VARIANT, TextureVariantHelper.readVariant(valueInput, this.getUUID(), TEXTURE_VARIANT_COUNT));
        this.summonedDesperationVex = valueInput.getBooleanOr("SummonedDesperationVex", false);
    }

    private void castMagmaEruption(ServerLevel serverLevel, Vec3 center) {
        float radius = 3.0f;
        this.triggerAnim(RuinedPortalGeoAnimations.ACTION_CONTROLLER, RuinedPortalGeoAnimations.ATTACK_CAST);
        serverLevel.playSound(null, this.blockPosition(), ModSounds.ENTITY_PIGLIN_EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.1f, 0.55f);
        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians((360.0 / 10.0) * i);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            EvokerFangs fangs = new EvokerFangs(serverLevel, x, center.y, z, (float) angle, 0, this);
            serverLevel.addFreshEntity(fangs);
        }
    }

    private void summonPiglinVexes(ServerLevel serverLevel, int count) {
        RandomSource random = this.getRandom();
        for (int i = 0; i < count; i++) {
            BlockPos spawnPos = this.blockPosition().offset(
                (int) Math.round((random.nextDouble() - 0.5) * 3.0),
                1,
                (int) Math.round((random.nextDouble() - 0.5) * 3.0)
            );
            PiglinVexEntity spawnedVex = ModEntities.PIGLIN_VEX.spawn(
                serverLevel,
                vex -> {
                    vex.setOwner(this);
                    vex.setBoundOrigin(this.blockPosition());
                    LivingEntity target = this.getTarget();
                    if (target != null) {
                        vex.setTarget(target);
                    }
                },
                spawnPos,
                EntitySpawnReason.MOB_SUMMONED,
                true,
                false
            );
            if (spawnedVex != null) {
                serverLevel.playSound(null, spawnPos, ModSounds.ENTITY_PIGLIN_EVOKER_CAST_SPELL, SoundSource.HOSTILE, 0.85f, 0.95f);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_PIGLIN_EVOKER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_PIGLIN_EVOKER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_PIGLIN_EVOKER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        // Fix: the custom piglin sound set used vanilla evoker volume defaults, so the final-wave caster now has an explicit raid mix level.
        return 1.0f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.08f;
    }

    @Override
    public int getTextureVariant() {
        return this.getEntityData().get(TEXTURE_VARIANT);
    }

    @Override
    public int getTextureVariantCount() {
        return TEXTURE_VARIANT_COUNT;
    }
}

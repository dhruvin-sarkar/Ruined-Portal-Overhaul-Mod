package com.ruinedportaloverhaul.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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

public class PiglinEvokerEntity extends Evoker {
    private static final int FANG_COOLDOWN_TICKS = 160;
    private static final int VEX_COOLDOWN_TICKS = 220;

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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        return PiglinDifficultyScaler.applyHardHealth(this, level, result);
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
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("SummonedDesperationVex", this.summonedDesperationVex);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.summonedDesperationVex = valueInput.getBooleanOr("SummonedDesperationVex", false);
    }

    private void castMagmaEruption(ServerLevel serverLevel, Vec3 center) {
        float radius = 3.0f;
        serverLevel.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.1f, 0.55f);
        serverLevel.playSound(null, this.blockPosition(), SoundEvents.LAVA_POP, SoundSource.HOSTILE, 0.8f, 0.70f);
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
                serverLevel.playSound(null, spawnPos, SoundEvents.PIGLIN_ANGRY, SoundSource.HOSTILE, 0.85f, 0.95f);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_DEATH;
    }
}

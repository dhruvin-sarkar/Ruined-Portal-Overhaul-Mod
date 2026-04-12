package com.ruinedportaloverhaul.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PiglinEvokerEntity extends Evoker {
    private static final int FANG_COOLDOWN_TICKS = 240;
    private static final int VEX_COOLDOWN_TICKS = 300;

    private int fangCooldown;
    private int vexCooldown;

    public PiglinEvokerEntity(EntityType<? extends PiglinEvokerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Evoker.createAttributes()
            .add(Attributes.MAX_HEALTH, 45.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25);
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

        double distance = this.distanceToSqr(target);
        if (distance <= 196.0 && this.fangCooldown <= 0) {
            castMagmaEruption(serverLevel, target.position());
            target.igniteForSeconds(4.0f);
            this.fangCooldown = FANG_COOLDOWN_TICKS;
        }

        if (distance <= 256.0 && this.vexCooldown <= 0) {
            summonPiglinVexes(serverLevel);
            this.vexCooldown = VEX_COOLDOWN_TICKS;
        }
    }

    private void castMagmaEruption(ServerLevel serverLevel, Vec3 center) {
        float radius = 2.5f;
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians((360.0 / 8.0) * i);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            EvokerFangs fangs = new EvokerFangs(serverLevel, x, center.y, z, (float) angle, 0, this);
            serverLevel.addFreshEntity(fangs);
        }
    }

    private void summonPiglinVexes(ServerLevel serverLevel) {
        RandomSource random = this.getRandom();
        for (int i = 0; i < 3; i++) {
            BlockPos spawnPos = this.blockPosition().offset(
                (int) Math.round((random.nextDouble() - 0.5) * 3.0),
                1,
                (int) Math.round((random.nextDouble() - 0.5) * 3.0)
            );
            ModEntities.PIGLIN_PILLAGER_VEX.spawn(
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
        }
    }
}

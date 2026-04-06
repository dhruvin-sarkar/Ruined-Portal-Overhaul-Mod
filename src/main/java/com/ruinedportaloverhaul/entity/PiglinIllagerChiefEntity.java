package com.ruinedportaloverhaul.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PiglinIllagerChiefEntity extends PiglinIllagerBruteEntity {
    private static final String REINFORCEMENTS_SPAWNED_TAG = "ReinforcementsSpawned";

    private boolean reinforcementsSpawned;

    public PiglinIllagerChiefEntity(EntityType<? extends PiglinIllagerChiefEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.35f)
            .add(Attributes.MAX_HEALTH, 100.0)
            .add(Attributes.ATTACK_DAMAGE, 12.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean(REINFORCEMENTS_SPAWNED_TAG, this.reinforcementsSpawned);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.reinforcementsSpawned = valueInput.getBooleanOr(REINFORCEMENTS_SPAWNED_TAG, false);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float damageAmount) {
        boolean damaged = super.hurtServer(serverLevel, damageSource, damageAmount);

        if (damaged && this.isAlive() && !this.reinforcementsSpawned && this.getHealth() <= this.getMaxHealth() * 0.5f) {
            this.reinforcementsSpawned = true;
            this.spawnReinforcements(serverLevel);
        }

        return damaged;
    }

    private void spawnReinforcements(ServerLevel serverLevel) {
        LivingEntity currentTarget = this.getTarget();

        this.spawnReinforcement(serverLevel, 2, 0, currentTarget);
        this.spawnReinforcement(serverLevel, -2, 0, currentTarget);
    }

    private void spawnReinforcement(ServerLevel serverLevel, int offsetX, int offsetZ, LivingEntity target) {
        ModEntities.PIGLIN_ILLAGER_RANGED.spawn(
            serverLevel,
            ranged -> {
                if (target != null) {
                    ranged.setTarget(target);
                }
            },
            this.blockPosition().offset(offsetX, 0, offsetZ),
            EntitySpawnReason.MOB_SUMMONED,
            true,
            false
        );
    }
}

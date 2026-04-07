package com.ruinedportaloverhaul.entity;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class PiglinIllagerShamanEntity extends PiglinIllagerEntity {
    private static final int DEBUFF_COOLDOWN_TICKS = 100;
    private static final int HEAL_COOLDOWN_TICKS = 160;
    private static final double DEBUFF_RANGE = 10.0;
    private static final double HEAL_RANGE = 8.0;

    private int debuffCooldown;
    private int healCooldown;

    public PiglinIllagerShamanEntity(EntityType<? extends PiglinIllagerShamanEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.23f)
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.ATTACK_DAMAGE, 4.0)
            .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeAllGoals(goal -> true);
        this.targetSelector.removeAllGoals(goal -> true);

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0f));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public boolean canUseNonMeleeWeapon(ItemStack itemStack) {
        return false;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BLAZE_ROD));
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);

        if (this.debuffCooldown > 0) {
            this.debuffCooldown--;
        }
        if (this.healCooldown > 0) {
            this.healCooldown--;
        }

        if (this.debuffCooldown <= 0) {
            this.tryDebuffNearbyPlayer(serverLevel);
        }
        if (this.healCooldown <= 0) {
            this.tryHealNearbyAlly(serverLevel);
        }
    }

    private void tryDebuffNearbyPlayer(ServerLevel serverLevel) {
        Player nearest = serverLevel.getNearestPlayer(this, DEBUFF_RANGE);
        if (nearest == null || !this.hasLineOfSight(nearest)) {
            return;
        }

        nearest.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), this);
        this.debuffCooldown = DEBUFF_COOLDOWN_TICKS;
        this.spawnCastingParticles(serverLevel);
    }

    private void tryHealNearbyAlly(ServerLevel serverLevel) {
        AABB searchBox = this.getBoundingBox().inflate(HEAL_RANGE);
        List<PiglinIllagerEntity> allies = serverLevel.getEntitiesOfClass(
            PiglinIllagerEntity.class,
            searchBox,
            ally -> ally != this && ally.isAlive() && ally.getHealth() < ally.getMaxHealth()
        );

        if (allies.isEmpty()) {
            return;
        }

        PiglinIllagerEntity target = allies.getFirst();
        float lowestHealth = target.getHealth() / target.getMaxHealth();
        for (int i = 1; i < allies.size(); i++) {
            PiglinIllagerEntity candidate = allies.get(i);
            float ratio = candidate.getHealth() / candidate.getMaxHealth();
            if (ratio < lowestHealth) {
                lowestHealth = ratio;
                target = candidate;
            }
        }

        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0), this);
        this.healCooldown = HEAL_COOLDOWN_TICKS;
        this.spawnCastingParticles(serverLevel);
    }

    private void spawnCastingParticles(ServerLevel serverLevel) {
        for (int i = 0; i < 8; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.8;
            double offsetY = this.random.nextDouble() * 1.5 + 0.3;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.8;
            serverLevel.sendParticles(
                ParticleTypes.WITCH,
                this.getX() + offsetX,
                this.getY() + offsetY,
                this.getZ() + offsetZ,
                1, 0.0, 0.02, 0.0, 0.0
            );
        }
    }
}

package com.ruinedportaloverhaul.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import com.ruinedportaloverhaul.sound.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiglinPillagerEntity extends Pillager implements GeoEntity {
    private static final float ARROW_DAMAGE = 9.5f;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PiglinPillagerEntity(EntityType<? extends PiglinPillagerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pillager.createAttributes()
            .add(Attributes.MAX_HEALTH, 44.0)
            .add(Attributes.MOVEMENT_SPEED, 0.34)
            .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.08, false));
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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        return PiglinDifficultyScaler.applyHardHealth(this, level, result);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        float variant = randomSource.nextFloat();
        if (variant < 0.55f) {
            this.setItemSlot(EquipmentSlot.MAINHAND, createCrossbow(randomSource));
            return;
        }
        if (variant < 0.75f) {
            this.setItemSlot(EquipmentSlot.MAINHAND, createMeleeWeapon(randomSource, true));
            return;
        }
        if (variant < 0.90f) {
            this.setItemSlot(EquipmentSlot.MAINHAND, createMeleeWeapon(randomSource, false));
            return;
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, createCrossbow(randomSource));
        this.setItemSlot(EquipmentSlot.OFFHAND, createMeleeWeapon(randomSource, randomSource.nextBoolean()));
    }

    private ItemStack createCrossbow(RandomSource randomSource) {
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        float roll = randomSource.nextFloat();
        Holder.Reference<Enchantment> quickCharge = this.level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.QUICK_CHARGE);
        crossbow.enchant(quickCharge, 3);
        if (roll < 0.35f) {
            Holder.Reference<Enchantment> piercing = this.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.PIERCING);
            crossbow.enchant(piercing, 2);
        } else if (roll < 0.55f) {
            Holder.Reference<Enchantment> multishot = this.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.MULTISHOT);
            crossbow.enchant(multishot, 1);
        }
        return crossbow;
    }

    private ItemStack createMeleeWeapon(RandomSource randomSource, boolean axePreferred) {
        ItemStack weapon = new ItemStack(axePreferred ? Items.GOLDEN_AXE : Items.GOLDEN_SWORD);
        Holder.Reference<Enchantment> sharpness = this.level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.SHARPNESS);
        weapon.enchant(sharpness, 2 + randomSource.nextInt(3));
        if (randomSource.nextFloat() < 0.35f) {
            Holder.Reference<Enchantment> fireAspect = this.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FIRE_ASPECT);
            weapon.enchant(fireAspect, 1);
        }
        return weapon;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack weapon = this.getMainHandItem();
        ItemStack ammo = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, ammo, distanceFactor, weapon);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        arrow.setBaseDamage(ARROW_DAMAGE);
        arrow.shoot(dx, dy, dz, 1.6f, 0.9f);
        serverLevel.addFreshEntity(arrow);
        this.triggerAnim(RuinedPortalGeoAnimations.ACTION_CONTROLLER, RuinedPortalGeoAnimations.ATTACK_SHOOT);
        this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.playSound(SoundEvents.PIGLIN_ANGRY, 0.72f, 0.75f + this.getRandom().nextFloat() * 0.25f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_PIGLIN_PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_PIGLIN_PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_PIGLIN_PILLAGER_DEATH;
    }
}

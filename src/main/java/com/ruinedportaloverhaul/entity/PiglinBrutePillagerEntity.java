package com.ruinedportaloverhaul.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.ruinedportaloverhaul.sound.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiglinBrutePillagerEntity extends Pillager implements GeoEntity, TextureVariantMob {
    private static final float ARROW_DAMAGE = 11.0f;
    private static final int TEXTURE_VARIANT_COUNT = 3;
    private static final EntityDataAccessor<Integer> TEXTURE_VARIANT = SynchedEntityData.defineId(PiglinBrutePillagerEntity.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PiglinBrutePillagerEntity(EntityType<? extends PiglinBrutePillagerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Pillager.createAttributes()
            .add(Attributes.MAX_HEALTH, 88.0)
            .add(Attributes.MOVEMENT_SPEED, 0.29)
            .add(Attributes.ATTACK_DAMAGE, 20.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Fix: brute pillagers previously had no client-visible variant state, so their GeckoLib model could never swap textures. A synced variant slot now carries the selected appearance.
        super.defineSynchedData(builder);
        builder.define(TEXTURE_VARIANT, 0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        // Fix: brute pillager spawning used to randomize only equipment, leaving visuals identical. The UUID now deterministically seeds a persistent texture variant during initialization.
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.getEntityData().set(TEXTURE_VARIANT, TextureVariantHelper.selectVariant(this.getUUID(), TEXTURE_VARIANT_COUNT));
        return PiglinDifficultyScaler.applySpawnScaling(this, level, result);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        // Fix: reloads used to discard brute texture selection because the variant never entered save data. The synced variant is now written with the rest of the mob state.
        super.addAdditionalSaveData(valueOutput);
        TextureVariantHelper.writeVariant(valueOutput, this.getTextureVariant(), TEXTURE_VARIANT_COUNT);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        // Fix: restored brutes defaulted back to variant zero because no saved appearance was reapplied. The saved variant now repopulates the synced entity data on load.
        super.readAdditionalSaveData(valueInput);
        this.getEntityData().set(TEXTURE_VARIANT, TextureVariantHelper.readVariant(valueInput, this.getUUID(), TEXTURE_VARIANT_COUNT));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new CloseRangeMeleeGoal());
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

    private boolean isTargetWithinMeleeFallbackRange() {
        LivingEntity target = this.getTarget();
        return target != null && this.distanceToSqr(target) <= 9.0;
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        // Fix: brute melee fallback and axe loadouts previously skipped their GeckoLib swing trigger, so successful close-range hits now animate like the ranged shot path.
        boolean damaged = super.doHurtTarget(serverLevel, target);
        if (damaged) {
            this.triggerAnim(RuinedPortalGeoAnimations.ACTION_CONTROLLER, RuinedPortalGeoAnimations.ATTACK_SWING);
        }
        return damaged;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        float roll = randomSource.nextFloat();
        if (roll < 0.70f) {
            this.setItemSlot(EquipmentSlot.MAINHAND, createMeleeWeapon(randomSource, Items.GOLDEN_AXE));
        } else if (roll < 0.88f) {
            this.setItemSlot(EquipmentSlot.MAINHAND, createMeleeWeapon(randomSource, Items.GOLDEN_SWORD));
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, createCrossbow(randomSource));
            this.setItemSlot(EquipmentSlot.OFFHAND, createMeleeWeapon(randomSource, randomSource.nextBoolean() ? Items.GOLDEN_AXE : Items.GOLDEN_SWORD));
        }
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
    }

    private ItemStack createCrossbow(RandomSource randomSource) {
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        crossbow.enchant(enchantment(Enchantments.MULTISHOT), 1);
        crossbow.enchant(enchantment(Enchantments.QUICK_CHARGE), 2);
        if (randomSource.nextBoolean()) {
            crossbow.enchant(enchantment(Enchantments.PIERCING), 1);
        }
        return crossbow;
    }

    private ItemStack createMeleeWeapon(RandomSource randomSource, net.minecraft.world.item.Item item) {
        ItemStack weapon = new ItemStack(item);
        weapon.enchant(enchantment(Enchantments.SHARPNESS), 4 + randomSource.nextInt(2));
        if (randomSource.nextFloat() < 0.34f) {
            weapon.enchant(enchantment(Enchantments.KNOCKBACK), 1);
        }
        return weapon;
    }

    private Holder.Reference<Enchantment> enchantment(ResourceKey<Enchantment> key) {
        return this.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        // Fix: brute ranged attack audio previously bypassed the mod sound registry. The shot and battle-cry layers now stay replaceable through mod-owned sound ids.
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack weapon = this.getMainHandItem();
        if (!weapon.is(Items.CROSSBOW)) {
            return;
        }
        ItemStack ammo = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, ammo, distanceFactor, weapon);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        arrow.setBaseDamage(ARROW_DAMAGE);
        arrow.shoot(dx, dy, dz, 1.5f, 1.0f);
        serverLevel.addFreshEntity(arrow);
        this.triggerAnim(RuinedPortalGeoAnimations.ACTION_CONTROLLER, RuinedPortalGeoAnimations.ATTACK_SHOOT);
        this.playSound(ModSounds.ENTITY_PIGLIN_BRUTE_PILLAGER_ATTACK, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.playSound(ModSounds.ENTITY_PIGLIN_BRUTE_PILLAGER_AMBIENT, 0.75f, 0.75f + this.getRandom().nextFloat() * 0.25f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_PIGLIN_BRUTE_PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_PIGLIN_BRUTE_PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_PIGLIN_BRUTE_PILLAGER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        // Fix: the custom piglin sound set used vanilla pillager volume defaults, so the brute hybrid now has an explicit heavier raid mix level.
        return 1.05f;
    }

    @Override
    public float getVoicePitch() {
        return 0.85f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.08f;
    }

    @Override
    public int getTextureVariant() {
        return this.getEntityData().get(TEXTURE_VARIANT);
    }

    @Override
    public int getTextureVariantCount() {
        return TEXTURE_VARIANT_COUNT;
    }

    private final class CloseRangeMeleeGoal extends MeleeAttackGoal {
        private CloseRangeMeleeGoal() {
            super(PiglinBrutePillagerEntity.this, 1.0, false);
        }

        @Override
        public boolean canUse() {
            return (!PiglinBrutePillagerEntity.this.getMainHandItem().is(Items.CROSSBOW)
                || PiglinBrutePillagerEntity.this.isTargetWithinMeleeFallbackRange())
                && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return (!PiglinBrutePillagerEntity.this.getMainHandItem().is(Items.CROSSBOW)
                || PiglinBrutePillagerEntity.this.isTargetWithinMeleeFallbackRange())
                && super.canContinueToUse();
        }
    }
}

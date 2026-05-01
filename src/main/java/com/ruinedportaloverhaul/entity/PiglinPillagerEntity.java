package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.sound.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
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
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiglinPillagerEntity extends Pillager implements GeoEntity, TextureVariantMob {
    private static final float ARROW_DAMAGE = 9.5f;
    private static final double KITE_START_RANGE_SQUARED = 7.0 * 7.0;
    private static final double KITE_STOP_RANGE_SQUARED = 10.0 * 10.0;
    private static final int TEXTURE_VARIANT_COUNT = 3;
    private static final EntityDataAccessor<Integer> TEXTURE_VARIANT = SynchedEntityData.defineId(PiglinPillagerEntity.class, EntityDataSerializers.INT);
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Fix: the pillager variants were previously only implied by asset names, so clients had no synced variant to render. The shared data value now keeps the chosen texture variant stable across networking.
        super.defineSynchedData(builder);
        builder.define(TEXTURE_VARIANT, 0);
    }

    @Override
    protected void registerGoals() {
        // Fix: water/lava escape was inherited implicitly, so the custom raid mob now pins an explicit float goal at the requested top priority.
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new KiteCloseTargetGoal());
        this.goalSelector.addGoal(3, new MeleeFallbackGoal());
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
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        // Fix: pillagers can spawn with sword or axe fallback loadouts, but only crossbow shots triggered GeckoLib before. Successful melee hits now play the swing action too.
        boolean damaged = super.doHurtTarget(serverLevel, target);
        if (damaged) {
            this.triggerAnim(RuinedPortalGeoAnimations.ACTION_CONTROLLER, RuinedPortalGeoAnimations.ATTACK_SWING);
        }
        return damaged;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        // Fix: every pillager used the same texture because spawn initialization never picked a persistent visual variant. The UUID now seeds the synced variant before the first render packet is sent.
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.getEntityData().set(TEXTURE_VARIANT, TextureVariantHelper.selectVariant(this.getUUID(), TEXTURE_VARIANT_COUNT));
        return PiglinDifficultyScaler.applySpawnScaling(this, level, result);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        // Fix: visual variants were previously transient, so save/load cycles could collapse mobs back to the default appearance. The chosen variant is now written alongside the rest of the entity state.
        super.addAdditionalSaveData(valueOutput);
        TextureVariantHelper.writeVariant(valueOutput, this.getTextureVariant(), TEXTURE_VARIANT_COUNT);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        // Fix: loaded mobs used to rely on whatever default texture the client saw first. Save data now restores the exact variant and resyncs it to the client-facing data accessor.
        super.readAdditionalSaveData(valueInput);
        this.getEntityData().set(TEXTURE_VARIANT, TextureVariantHelper.readVariant(valueInput, this.getUUID(), TEXTURE_VARIANT_COUNT));
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
        return RuinedPortalSwingAnimations.withHumanoidAttackTiming(weapon);
    }

    private boolean isHoldingCrossbow() {
        return this.getMainHandItem().is(Items.CROSSBOW);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        // Fix: ranged attack audio previously mixed raw vanilla sounds into an otherwise custom sound set. The shot cue now stays inside the mod registry so packs can replace encounter audio consistently.
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
        arrow.shoot(dx, dy, dz, 1.6f, 0.9f);
        serverLevel.addFreshEntity(arrow);
        this.triggerAnim(RuinedPortalGeoAnimations.ACTION_CONTROLLER, RuinedPortalGeoAnimations.ATTACK_SHOOT);
        this.playSound(ModSounds.ENTITY_PIGLIN_PILLAGER_ATTACK, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.playSound(ModSounds.ENTITY_PIGLIN_PILLAGER_AMBIENT, 0.72f, 0.75f + this.getRandom().nextFloat() * 0.25f);
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

    @Override
    protected float getSoundVolume() {
        // Fix: the custom piglin sound set used vanilla pillager volume defaults, so the raid mix now has an explicit baseline for resource-packable mob audio.
        return 1.0f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.10f;
    }

    @Override
    public int getTextureVariant() {
        return this.getEntityData().get(TEXTURE_VARIANT);
    }

    @Override
    public int getTextureVariantCount() {
        return TEXTURE_VARIANT_COUNT;
    }

    private final class KiteCloseTargetGoal extends Goal {
        private KiteCloseTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = PiglinPillagerEntity.this.getTarget();
            return PiglinPillagerEntity.this.isHoldingCrossbow()
                && target != null
                && target.isAlive()
                && PiglinPillagerEntity.this.distanceToSqr(target) <= KITE_START_RANGE_SQUARED;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = PiglinPillagerEntity.this.getTarget();
            return PiglinPillagerEntity.this.isHoldingCrossbow()
                && target != null
                && target.isAlive()
                && PiglinPillagerEntity.this.distanceToSqr(target) < KITE_STOP_RANGE_SQUARED;
        }

        @Override
        public void tick() {
            LivingEntity target = PiglinPillagerEntity.this.getTarget();
            if (target == null) {
                return;
            }

            Vec3 away = PiglinPillagerEntity.this.position().subtract(target.position());
            if (away.lengthSqr() < 0.001) {
                away = PiglinPillagerEntity.this.getLookAngle().scale(-1.0);
            }
            Vec3 retreat = PiglinPillagerEntity.this.position().add(away.normalize().scale(4.0));
            PiglinPillagerEntity.this.getNavigation().moveTo(retreat.x, retreat.y, retreat.z, 1.18);
        }
    }

    private final class MeleeFallbackGoal extends MeleeAttackGoal {
        private MeleeFallbackGoal() {
            super(PiglinPillagerEntity.this, 1.08, false);
        }

        @Override
        public boolean canUse() {
            return !PiglinPillagerEntity.this.isHoldingCrossbow() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !PiglinPillagerEntity.this.isHoldingCrossbow() && super.canContinueToUse();
        }
    }
}

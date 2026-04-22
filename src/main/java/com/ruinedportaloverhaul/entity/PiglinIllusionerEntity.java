package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.sound.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiglinIllusionerEntity extends Illusioner implements GeoEntity, TextureVariantMob {
    private static final float ARROW_DAMAGE = 8.0f;
    private static final int TEXTURE_VARIANT_COUNT = 2;
    private static final EntityDataAccessor<Integer> TEXTURE_VARIANT = SynchedEntityData.defineId(PiglinIllusionerEntity.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PiglinIllusionerEntity(EntityType<? extends PiglinIllusionerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Illusioner.createAttributes()
            .add(Attributes.MAX_HEALTH, 54.0)
            .add(Attributes.MOVEMENT_SPEED, 0.31);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Fix: illusioner variants were never synchronized to the client, so all summoned copies looked the same. A synced variant field now carries the chosen texture index.
        super.defineSynchedData(builder);
        builder.define(TEXTURE_VARIANT, 0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        // Fix: illusioner spawns previously stopped after stat scaling, leaving no persistent visual identity. The UUID now seeds a deterministic texture variant on spawn.
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.getEntityData().set(TEXTURE_VARIANT, TextureVariantHelper.selectVariant(this.getUUID(), TEXTURE_VARIANT_COUNT));
        return PiglinDifficultyScaler.applyHardHealth(this, level, result);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        // Fix: illusioner appearance previously reset after save/load because the selected variant was never written. The active variant now persists in save data.
        super.addAdditionalSaveData(valueOutput);
        TextureVariantHelper.writeVariant(valueOutput, this.getTextureVariant(), TEXTURE_VARIANT_COUNT);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        // Fix: restored illusioners defaulted to the first texture because no variant was reapplied. The saved variant now repopulates synced data when the entity loads.
        super.readAdditionalSaveData(valueInput);
        this.getEntityData().set(TEXTURE_VARIANT, TextureVariantHelper.readVariant(valueInput, this.getUUID(), TEXTURE_VARIANT_COUNT));
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
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        ItemStack bow = new ItemStack(Items.BOW);
        Holder.Reference<Enchantment> flame = this.level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.FLAME);
        bow.enchant(flame, 1);
        Holder.Reference<Enchantment> power = this.level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.POWER);
        bow.enchant(power, 3);
        if (randomSource.nextBoolean()) {
            Holder.Reference<Enchantment> punch = this.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.PUNCH);
            bow.enchant(punch, 1);
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, bow);
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
        arrow.igniteForSeconds(4.0f);
        arrow.shoot(dx, dy, dz, 1.6f, 0.9f);
        serverLevel.addFreshEntity(arrow);
        this.triggerAnim(RuinedPortalGeoAnimations.ACTION_CONTROLLER, RuinedPortalGeoAnimations.ATTACK_SHOOT);
        serverLevel.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.55f, 1.45f);
        serverLevel.playSound(null, this.blockPosition(), SoundEvents.PIGLIN_ANGRY, SoundSource.HOSTILE, 0.45f, 1.10f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_PIGLIN_ILLUSIONER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_PIGLIN_ILLUSIONER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_PIGLIN_ILLUSIONER_DEATH;
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

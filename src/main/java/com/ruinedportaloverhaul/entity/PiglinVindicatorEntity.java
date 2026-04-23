package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.sound.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.illager.Vindicator;
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

public class PiglinVindicatorEntity extends Vindicator implements GeoEntity, TextureVariantMob {
    private static final int TEXTURE_VARIANT_COUNT = 3;
    private static final EntityDataAccessor<Integer> TEXTURE_VARIANT = SynchedEntityData.defineId(PiglinVindicatorEntity.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PiglinVindicatorEntity(EntityType<? extends PiglinVindicatorEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Vindicator.createAttributes()
            .add(Attributes.MAX_HEALTH, 58.0)
            .add(Attributes.MOVEMENT_SPEED, 0.37)
            .add(Attributes.ATTACK_DAMAGE, 16.5);
    }

    @Override
    protected void registerGoals() {
        // Fix: the custom vindicator used only inherited swim behavior; an explicit priority-1 float goal keeps fluid escape ahead of melee pressure.
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Fix: the vindicator visuals previously had no synced variant state, which left GeckoLib stuck on a single texture. The entity now advertises a stable variant index to the client.
        super.defineSynchedData(builder);
        builder.define(TEXTURE_VARIANT, 0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        // Fix: spawn initialization used to stop at stats and gear, so every vindicator rendered identically. The UUID now seeds a deterministic visual variant before the mob enters play.
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.getEntityData().set(TEXTURE_VARIANT, TextureVariantHelper.selectVariant(this.getUUID(), TEXTURE_VARIANT_COUNT));
        return PiglinDifficultyScaler.applySpawnScaling(this, level, result);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        // Fix: save data previously omitted the chosen texture variant, causing reloads to lose visual diversity. The current variant now persists with the entity.
        super.addAdditionalSaveData(valueOutput);
        TextureVariantHelper.writeVariant(valueOutput, this.getTextureVariant(), TEXTURE_VARIANT_COUNT);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        // Fix: loaded vindicators used to fall back to the default appearance because no variant was restored. The saved variant is now read back into synced data on load.
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
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        // Fix: successful vindicator melee hits previously dealt damage without starting the GeckoLib swing animation, so landed attacks now trigger the matching action clip.
        boolean damaged = super.doHurtTarget(serverLevel, target);
        if (damaged) {
            this.triggerAnim(RuinedPortalGeoAnimations.ACTION_CONTROLLER, RuinedPortalGeoAnimations.ATTACK_SWING);
        }
        return damaged;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        ItemStack weapon = new ItemStack(randomSource.nextFloat() < 0.74f ? Items.GOLDEN_AXE : Items.GOLDEN_SWORD);
        Holder.Reference<Enchantment> sharpness = this.level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(Enchantments.SHARPNESS);
        weapon.enchant(sharpness, 3 + randomSource.nextInt(3));
        if (randomSource.nextFloat() < 0.34f) {
            Holder.Reference<Enchantment> fireAspect = this.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FIRE_ASPECT);
            weapon.enchant(fireAspect, 1);
        }
        if (randomSource.nextFloat() < 0.22f) {
            Holder.Reference<Enchantment> knockback = this.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.KNOCKBACK);
            weapon.enchant(knockback, 1);
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, weapon);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_PIGLIN_VINDICATOR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_PIGLIN_VINDICATOR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_PIGLIN_VINDICATOR_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        // Fix: the custom piglin sound set used vanilla vindicator volume defaults, so the raid mix now has an explicit baseline for resource-packable mob audio.
        return 1.0f;
    }

    @Override
    public float getVoicePitch() {
        return 0.95f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.10f;
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

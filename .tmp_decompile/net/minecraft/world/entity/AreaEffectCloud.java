/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class AreaEffectCloud
extends Entity
implements TraceableEntity {
    private static final int TIME_BETWEEN_APPLICATIONS = 5;
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
    private static final float MAX_RADIUS = 32.0f;
    private static final int DEFAULT_AGE = 0;
    private static final int DEFAULT_DURATION_ON_USE = 0;
    private static final float DEFAULT_RADIUS_ON_USE = 0.0f;
    private static final float DEFAULT_RADIUS_PER_TICK = 0.0f;
    private static final float DEFAULT_POTION_DURATION_SCALE = 1.0f;
    private static final float MINIMAL_RADIUS = 0.5f;
    private static final float DEFAULT_RADIUS = 3.0f;
    public static final float DEFAULT_WIDTH = 6.0f;
    public static final float HEIGHT = 0.5f;
    public static final int INFINITE_DURATION = -1;
    public static final int DEFAULT_LINGERING_DURATION = 600;
    private static final int DEFAULT_WAIT_TIME = 20;
    private static final int DEFAULT_REAPPLICATION_DELAY = 20;
    private static final ColorParticleOption DEFAULT_PARTICLE = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1);
    private @Nullable ParticleOptions customParticle;
    private PotionContents potionContents = PotionContents.EMPTY;
    private float potionDurationScale = 1.0f;
    private final Map<Entity, Integer> victims = Maps.newHashMap();
    private int duration = -1;
    private int waitTime = 20;
    private int reapplicationDelay = 20;
    private int durationOnUse = 0;
    private float radiusOnUse = 0.0f;
    private float radiusPerTick = 0.0f;
    private @Nullable EntityReference<LivingEntity> owner;

    public AreaEffectCloud(EntityType<? extends AreaEffectCloud> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public AreaEffectCloud(Level level, double d, double e, double f) {
        this((EntityType<? extends AreaEffectCloud>)EntityType.AREA_EFFECT_CLOUD, level);
        this.setPos(d, e, f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, Float.valueOf(3.0f));
        builder.define(DATA_WAITING, false);
        builder.define(DATA_PARTICLE, DEFAULT_PARTICLE);
    }

    public void setRadius(float f) {
        if (!this.level().isClientSide()) {
            this.getEntityData().set(DATA_RADIUS, Float.valueOf(Mth.clamp(f, 0.0f, 32.0f)));
        }
    }

    @Override
    public void refreshDimensions() {
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        super.refreshDimensions();
        this.setPos(d, e, f);
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS).floatValue();
    }

    public void setPotionContents(PotionContents potionContents) {
        this.potionContents = potionContents;
        this.updateParticle();
    }

    public void setCustomParticle(@Nullable ParticleOptions particleOptions) {
        this.customParticle = particleOptions;
        this.updateParticle();
    }

    public void setPotionDurationScale(float f) {
        this.potionDurationScale = f;
    }

    private void updateParticle() {
        if (this.customParticle != null) {
            this.entityData.set(DATA_PARTICLE, this.customParticle);
        } else {
            int i = ARGB.opaque(this.potionContents.getColor());
            this.entityData.set(DATA_PARTICLE, ColorParticleOption.create(DEFAULT_PARTICLE.getType(), i));
        }
    }

    public void addEffect(MobEffectInstance mobEffectInstance) {
        this.setPotionContents(this.potionContents.withEffectAdded(mobEffectInstance));
    }

    public ParticleOptions getParticle() {
        return this.getEntityData().get(DATA_PARTICLE);
    }

    protected void setWaiting(boolean bl) {
        this.getEntityData().set(DATA_WAITING, bl);
    }

    public boolean isWaiting() {
        return this.getEntityData().get(DATA_WAITING);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int i) {
        this.duration = i;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.serverTick(serverLevel);
        } else {
            this.clientTick();
        }
    }

    private void clientTick() {
        float g;
        int i;
        boolean bl = this.isWaiting();
        float f = this.getRadius();
        if (bl && this.random.nextBoolean()) {
            return;
        }
        ParticleOptions particleOptions = this.getParticle();
        if (bl) {
            i = 2;
            g = 0.2f;
        } else {
            i = Mth.ceil((float)Math.PI * f * f);
            g = f;
        }
        for (int j = 0; j < i; ++j) {
            float h = this.random.nextFloat() * ((float)Math.PI * 2);
            float k = Mth.sqrt(this.random.nextFloat()) * g;
            double d = this.getX() + (double)(Mth.cos(h) * k);
            double e = this.getY();
            double l = this.getZ() + (double)(Mth.sin(h) * k);
            if (particleOptions.getType() == ParticleTypes.ENTITY_EFFECT) {
                if (bl && this.random.nextBoolean()) {
                    this.level().addAlwaysVisibleParticle(DEFAULT_PARTICLE, d, e, l, 0.0, 0.0, 0.0);
                    continue;
                }
                this.level().addAlwaysVisibleParticle(particleOptions, d, e, l, 0.0, 0.0, 0.0);
                continue;
            }
            if (bl) {
                this.level().addAlwaysVisibleParticle(particleOptions, d, e, l, 0.0, 0.0, 0.0);
                continue;
            }
            this.level().addAlwaysVisibleParticle(particleOptions, d, e, l, (0.5 - this.random.nextDouble()) * 0.15, 0.01f, (0.5 - this.random.nextDouble()) * 0.15);
        }
    }

    private void serverTick(ServerLevel serverLevel) {
        boolean bl2;
        if (this.duration != -1 && this.tickCount - this.waitTime >= this.duration) {
            this.discard();
            return;
        }
        boolean bl = this.isWaiting();
        boolean bl3 = bl2 = this.tickCount < this.waitTime;
        if (bl != bl2) {
            this.setWaiting(bl2);
        }
        if (bl2) {
            return;
        }
        float f = this.getRadius();
        if (this.radiusPerTick != 0.0f) {
            if ((f += this.radiusPerTick) < 0.5f) {
                this.discard();
                return;
            }
            this.setRadius(f);
        }
        if (this.tickCount % 5 == 0) {
            this.victims.entrySet().removeIf(entry -> this.tickCount >= (Integer)entry.getValue());
            if (!this.potionContents.hasEffects()) {
                this.victims.clear();
            } else {
                ArrayList list = new ArrayList();
                this.potionContents.forEachEffect(list::add, this.potionDurationScale);
                List<LivingEntity> list2 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
                if (!list2.isEmpty()) {
                    for (LivingEntity livingEntity : list2) {
                        double e;
                        double d;
                        double g;
                        if (this.victims.containsKey(livingEntity) || !livingEntity.isAffectedByPotions()) continue;
                        if (list.stream().noneMatch(livingEntity::canBeAffected) || !((g = (d = livingEntity.getX() - this.getX()) * d + (e = livingEntity.getZ() - this.getZ()) * e) <= (double)(f * f))) continue;
                        this.victims.put(livingEntity, this.tickCount + this.reapplicationDelay);
                        for (MobEffectInstance mobEffectInstance : list) {
                            if (mobEffectInstance.getEffect().value().isInstantenous()) {
                                mobEffectInstance.getEffect().value().applyInstantenousEffect(serverLevel, this, this.getOwner(), livingEntity, mobEffectInstance.getAmplifier(), 0.5);
                                continue;
                            }
                            livingEntity.addEffect(new MobEffectInstance(mobEffectInstance), this);
                        }
                        if (this.radiusOnUse != 0.0f) {
                            if ((f += this.radiusOnUse) < 0.5f) {
                                this.discard();
                                return;
                            }
                            this.setRadius(f);
                        }
                        if (this.durationOnUse == 0 || this.duration == -1) continue;
                        this.duration += this.durationOnUse;
                        if (this.duration > 0) continue;
                        this.discard();
                        return;
                    }
                }
            }
        }
    }

    public float getRadiusOnUse() {
        return this.radiusOnUse;
    }

    public void setRadiusOnUse(float f) {
        this.radiusOnUse = f;
    }

    public float getRadiusPerTick() {
        return this.radiusPerTick;
    }

    public void setRadiusPerTick(float f) {
        this.radiusPerTick = f;
    }

    public int getDurationOnUse() {
        return this.durationOnUse;
    }

    public void setDurationOnUse(int i) {
        this.durationOnUse = i;
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public void setWaitTime(int i) {
        this.waitTime = i;
    }

    public void setOwner(@Nullable LivingEntity livingEntity) {
        this.owner = EntityReference.of(livingEntity);
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return EntityReference.getLivingEntity(this.owner, this.level());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.tickCount = valueInput.getIntOr("Age", 0);
        this.duration = valueInput.getIntOr("Duration", -1);
        this.waitTime = valueInput.getIntOr("WaitTime", 20);
        this.reapplicationDelay = valueInput.getIntOr("ReapplicationDelay", 20);
        this.durationOnUse = valueInput.getIntOr("DurationOnUse", 0);
        this.radiusOnUse = valueInput.getFloatOr("RadiusOnUse", 0.0f);
        this.radiusPerTick = valueInput.getFloatOr("RadiusPerTick", 0.0f);
        this.setRadius(valueInput.getFloatOr("Radius", 3.0f));
        this.owner = EntityReference.read(valueInput, "Owner");
        this.setCustomParticle(valueInput.read("custom_particle", ParticleTypes.CODEC).orElse(null));
        this.setPotionContents(valueInput.read("potion_contents", PotionContents.CODEC).orElse(PotionContents.EMPTY));
        this.potionDurationScale = valueInput.getFloatOr("potion_duration_scale", 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putInt("Age", this.tickCount);
        valueOutput.putInt("Duration", this.duration);
        valueOutput.putInt("WaitTime", this.waitTime);
        valueOutput.putInt("ReapplicationDelay", this.reapplicationDelay);
        valueOutput.putInt("DurationOnUse", this.durationOnUse);
        valueOutput.putFloat("RadiusOnUse", this.radiusOnUse);
        valueOutput.putFloat("RadiusPerTick", this.radiusPerTick);
        valueOutput.putFloat("Radius", this.getRadius());
        valueOutput.storeNullable("custom_particle", ParticleTypes.CODEC, this.customParticle);
        EntityReference.store(this.owner, valueOutput, "Owner");
        if (!this.potionContents.equals(PotionContents.EMPTY)) {
            valueOutput.store("potion_contents", PotionContents.CODEC, this.potionContents);
        }
        if (this.potionDurationScale != 1.0f) {
            valueOutput.putFloat("potion_duration_scale", this.potionDurationScale);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_RADIUS.equals(entityDataAccessor)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0f, 0.5f);
    }

    @Override
    public final boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return false;
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.POTION_CONTENTS) {
            return AreaEffectCloud.castComponentValue(dataComponentType, this.potionContents);
        }
        if (dataComponentType == DataComponents.POTION_DURATION_SCALE) {
            return AreaEffectCloud.castComponentValue(dataComponentType, Float.valueOf(this.potionDurationScale));
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.POTION_CONTENTS);
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.POTION_DURATION_SCALE);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T object) {
        if (dataComponentType == DataComponents.POTION_CONTENTS) {
            this.setPotionContents(AreaEffectCloud.castComponentValue(DataComponents.POTION_CONTENTS, object));
            return true;
        }
        if (dataComponentType == DataComponents.POTION_DURATION_SCALE) {
            this.setPotionDurationScale(AreaEffectCloud.castComponentValue(DataComponents.POTION_DURATION_SCALE, object).floatValue());
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, object);
    }

    @Override
    public /* synthetic */ @Nullable Entity getOwner() {
        return this.getOwner();
    }
}


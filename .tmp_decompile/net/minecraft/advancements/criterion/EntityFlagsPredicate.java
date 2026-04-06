/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record EntityFlagsPredicate(Optional<Boolean> isOnGround, Optional<Boolean> isOnFire, Optional<Boolean> isCrouching, Optional<Boolean> isSprinting, Optional<Boolean> isSwimming, Optional<Boolean> isFlying, Optional<Boolean> isBaby, Optional<Boolean> isInWater, Optional<Boolean> isFallFlying) {
    public static final Codec<EntityFlagsPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("is_on_ground").forGetter(EntityFlagsPredicate::isOnGround), (App)Codec.BOOL.optionalFieldOf("is_on_fire").forGetter(EntityFlagsPredicate::isOnFire), (App)Codec.BOOL.optionalFieldOf("is_sneaking").forGetter(EntityFlagsPredicate::isCrouching), (App)Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(EntityFlagsPredicate::isSprinting), (App)Codec.BOOL.optionalFieldOf("is_swimming").forGetter(EntityFlagsPredicate::isSwimming), (App)Codec.BOOL.optionalFieldOf("is_flying").forGetter(EntityFlagsPredicate::isFlying), (App)Codec.BOOL.optionalFieldOf("is_baby").forGetter(EntityFlagsPredicate::isBaby), (App)Codec.BOOL.optionalFieldOf("is_in_water").forGetter(EntityFlagsPredicate::isInWater), (App)Codec.BOOL.optionalFieldOf("is_fall_flying").forGetter(EntityFlagsPredicate::isFallFlying)).apply((Applicative)instance, EntityFlagsPredicate::new));

    /*
     * Unable to fully structure code
     */
    public boolean matches(Entity entity) {
        block11: {
            if (this.isOnGround.isPresent() && entity.onGround() != this.isOnGround.get().booleanValue()) {
                return false;
            }
            if (this.isOnFire.isPresent() && entity.isOnFire() != this.isOnFire.get().booleanValue()) {
                return false;
            }
            if (this.isCrouching.isPresent() && entity.isCrouching() != this.isCrouching.get().booleanValue()) {
                return false;
            }
            if (this.isSprinting.isPresent() && entity.isSprinting() != this.isSprinting.get().booleanValue()) {
                return false;
            }
            if (this.isSwimming.isPresent() && entity.isSwimming() != this.isSwimming.get().booleanValue()) {
                return false;
            }
            if (!this.isFlying.isPresent()) break block11;
            if (!(entity instanceof LivingEntity)) ** GOTO lbl-1000
            livingEntity = (LivingEntity)entity;
            if (livingEntity.isFallFlying()) ** GOTO lbl-1000
            if (livingEntity instanceof Player) {
                player = (Player)livingEntity;
                ** if (!player.getAbilities().flying) goto lbl-1000
            }
            ** GOTO lbl-1000
lbl-1000:
            // 2 sources

            {
                v0 = true;
                ** GOTO lbl22
            }
lbl-1000:
            // 3 sources

            {
                v0 = bl = false;
            }
lbl22:
            // 2 sources

            if (bl != this.isFlying.get()) {
                return false;
            }
        }
        if (this.isInWater.isPresent() && entity.isInWater() != this.isInWater.get().booleanValue()) {
            return false;
        }
        if (this.isFallFlying.isPresent() && entity instanceof LivingEntity && (livingEntity2 = (LivingEntity)entity).isFallFlying() != this.isFallFlying.get().booleanValue()) {
            return false;
        }
        return this.isBaby.isPresent() == false || entity instanceof LivingEntity == false || (livingEntity2 = (LivingEntity)entity).isBaby() == this.isBaby.get().booleanValue();
    }

    public static class Builder {
        private Optional<Boolean> isOnGround = Optional.empty();
        private Optional<Boolean> isOnFire = Optional.empty();
        private Optional<Boolean> isCrouching = Optional.empty();
        private Optional<Boolean> isSprinting = Optional.empty();
        private Optional<Boolean> isSwimming = Optional.empty();
        private Optional<Boolean> isFlying = Optional.empty();
        private Optional<Boolean> isBaby = Optional.empty();
        private Optional<Boolean> isInWater = Optional.empty();
        private Optional<Boolean> isFallFlying = Optional.empty();

        public static Builder flags() {
            return new Builder();
        }

        public Builder setOnGround(Boolean boolean_) {
            this.isOnGround = Optional.of(boolean_);
            return this;
        }

        public Builder setOnFire(Boolean boolean_) {
            this.isOnFire = Optional.of(boolean_);
            return this;
        }

        public Builder setCrouching(Boolean boolean_) {
            this.isCrouching = Optional.of(boolean_);
            return this;
        }

        public Builder setSprinting(Boolean boolean_) {
            this.isSprinting = Optional.of(boolean_);
            return this;
        }

        public Builder setSwimming(Boolean boolean_) {
            this.isSwimming = Optional.of(boolean_);
            return this;
        }

        public Builder setIsFlying(Boolean boolean_) {
            this.isFlying = Optional.of(boolean_);
            return this;
        }

        public Builder setIsBaby(Boolean boolean_) {
            this.isBaby = Optional.of(boolean_);
            return this;
        }

        public Builder setIsInWater(Boolean boolean_) {
            this.isInWater = Optional.of(boolean_);
            return this;
        }

        public Builder setIsFallFlying(Boolean boolean_) {
            this.isFallFlying = Optional.of(boolean_);
            return this;
        }

        public EntityFlagsPredicate build() {
            return new EntityFlagsPredicate(this.isOnGround, this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isFlying, this.isBaby, this.isInWater, this.isFallFlying);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public record KineticWeapon(int contactCooldownTicks, int delayTicks, Optional<Condition> dismountConditions, Optional<Condition> knockbackConditions, Optional<Condition> damageConditions, float forwardMovement, float damageMultiplier, Optional<Holder<SoundEvent>> sound, Optional<Holder<SoundEvent>> hitSound) {
    public static final int HIT_FEEDBACK_TICKS = 10;
    public static final Codec<KineticWeapon> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("contact_cooldown_ticks", (Object)10).forGetter(KineticWeapon::contactCooldownTicks), (App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("delay_ticks", (Object)0).forGetter(KineticWeapon::delayTicks), (App)Condition.CODEC.optionalFieldOf("dismount_conditions").forGetter(KineticWeapon::dismountConditions), (App)Condition.CODEC.optionalFieldOf("knockback_conditions").forGetter(KineticWeapon::knockbackConditions), (App)Condition.CODEC.optionalFieldOf("damage_conditions").forGetter(KineticWeapon::damageConditions), (App)Codec.FLOAT.optionalFieldOf("forward_movement", (Object)Float.valueOf(0.0f)).forGetter(KineticWeapon::forwardMovement), (App)Codec.FLOAT.optionalFieldOf("damage_multiplier", (Object)Float.valueOf(1.0f)).forGetter(KineticWeapon::damageMultiplier), (App)SoundEvent.CODEC.optionalFieldOf("sound").forGetter(KineticWeapon::sound), (App)SoundEvent.CODEC.optionalFieldOf("hit_sound").forGetter(KineticWeapon::hitSound)).apply((Applicative)instance, KineticWeapon::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KineticWeapon> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, KineticWeapon::contactCooldownTicks, ByteBufCodecs.VAR_INT, KineticWeapon::delayTicks, Condition.STREAM_CODEC.apply(ByteBufCodecs::optional), KineticWeapon::dismountConditions, Condition.STREAM_CODEC.apply(ByteBufCodecs::optional), KineticWeapon::knockbackConditions, Condition.STREAM_CODEC.apply(ByteBufCodecs::optional), KineticWeapon::damageConditions, ByteBufCodecs.FLOAT, KineticWeapon::forwardMovement, ByteBufCodecs.FLOAT, KineticWeapon::damageMultiplier, SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional), KineticWeapon::sound, SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional), KineticWeapon::hitSound, KineticWeapon::new);

    public static Vec3 getMotion(Entity entity) {
        if (!(entity instanceof Player) && entity.isPassenger()) {
            entity = entity.getRootVehicle();
        }
        return entity.getKnownSpeed().scale(20.0);
    }

    public void makeSound(Entity entity) {
        this.sound.ifPresent(holder -> entity.level().playSound(entity, entity.getX(), entity.getY(), entity.getZ(), (Holder<SoundEvent>)holder, entity.getSoundSource(), 1.0f, 1.0f));
    }

    public void makeLocalHitSound(Entity entity) {
        this.hitSound.ifPresent(holder -> entity.level().playLocalSound(entity, (SoundEvent)((Object)((Object)holder.value())), entity.getSoundSource(), 1.0f, 1.0f));
    }

    public int computeDamageUseDuration() {
        return this.delayTicks + this.damageConditions.map(Condition::maxDurationTicks).orElse(0);
    }

    public void damageEntities(ItemStack itemStack, int i, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        int j = itemStack.getUseDuration(livingEntity) - i;
        if (j < this.delayTicks) {
            return;
        }
        j -= this.delayTicks;
        Vec3 vec3 = livingEntity.getLookAngle();
        double d = vec3.dot(KineticWeapon.getMotion(livingEntity));
        float f = livingEntity instanceof Player ? 1.0f : 0.2f;
        AttackRange attackRange = livingEntity.entityAttackRange();
        double e = livingEntity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        boolean bl = false;
        for (EntityHitResult entityHitResult : (Collection)ProjectileUtil.getHitEntitiesAlong(livingEntity, attackRange, entity -> PiercingWeapon.canHitEntity(livingEntity, entity), ClipContext.Block.COLLIDER).map(blockHitResult -> List.of(), collection -> collection)) {
            boolean bl5;
            boolean bl2;
            Entity entity2 = entityHitResult.getEntity();
            if (entity2 instanceof EnderDragonPart) {
                EnderDragonPart enderDragonPart = (EnderDragonPart)entity2;
                entity2 = enderDragonPart.parentMob;
            }
            if (bl2 = livingEntity.wasRecentlyStabbed(entity2, this.contactCooldownTicks)) continue;
            livingEntity.rememberStabbedEntity(entity2);
            double g = vec3.dot(KineticWeapon.getMotion(entity2));
            double h = Math.max(0.0, d - g);
            boolean bl3 = this.dismountConditions.isPresent() && this.dismountConditions.get().test(j, d, h, f);
            boolean bl4 = this.knockbackConditions.isPresent() && this.knockbackConditions.get().test(j, d, h, f);
            boolean bl6 = bl5 = this.damageConditions.isPresent() && this.damageConditions.get().test(j, d, h, f);
            if (!bl3 && !bl4 && !bl5) continue;
            float k = (float)e + (float)Mth.floor(h * (double)this.damageMultiplier);
            bl |= livingEntity.stabAttack(equipmentSlot, entity2, k, bl5, bl4, bl3);
        }
        if (bl) {
            livingEntity.level().broadcastEntityEvent(livingEntity, (byte)2);
            if (livingEntity instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                CriteriaTriggers.SPEAR_MOBS_TRIGGER.trigger(serverPlayer, livingEntity.stabbedEntities(entity -> entity instanceof LivingEntity));
            }
        }
    }

    public record Condition(int maxDurationTicks, float minSpeed, float minRelativeSpeed) {
        public static final Codec<Condition> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_duration_ticks").forGetter(Condition::maxDurationTicks), (App)Codec.FLOAT.optionalFieldOf("min_speed", (Object)Float.valueOf(0.0f)).forGetter(Condition::minSpeed), (App)Codec.FLOAT.optionalFieldOf("min_relative_speed", (Object)Float.valueOf(0.0f)).forGetter(Condition::minRelativeSpeed)).apply((Applicative)instance, Condition::new));
        public static final StreamCodec<ByteBuf, Condition> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Condition::maxDurationTicks, ByteBufCodecs.FLOAT, Condition::minSpeed, ByteBufCodecs.FLOAT, Condition::minRelativeSpeed, Condition::new);

        public boolean test(int i, double d, double e, double f) {
            return i <= this.maxDurationTicks && d >= (double)this.minSpeed * f && e >= (double)this.minRelativeSpeed * f;
        }

        public static Optional<Condition> ofAttackerSpeed(int i, float f) {
            return Optional.of(new Condition(i, f, 0.0f));
        }

        public static Optional<Condition> ofRelativeSpeed(int i, float f) {
            return Optional.of(new Condition(i, 0.0f, f));
        }
    }
}


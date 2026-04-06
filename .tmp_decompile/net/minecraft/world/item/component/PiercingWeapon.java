/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;

public record PiercingWeapon(boolean dealsKnockback, boolean dismounts, Optional<Holder<SoundEvent>> sound, Optional<Holder<SoundEvent>> hitSound) {
    public static final Codec<PiercingWeapon> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("deals_knockback", (Object)true).forGetter(PiercingWeapon::dealsKnockback), (App)Codec.BOOL.optionalFieldOf("dismounts", (Object)false).forGetter(PiercingWeapon::dismounts), (App)SoundEvent.CODEC.optionalFieldOf("sound").forGetter(PiercingWeapon::sound), (App)SoundEvent.CODEC.optionalFieldOf("hit_sound").forGetter(PiercingWeapon::hitSound)).apply((Applicative)instance, PiercingWeapon::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PiercingWeapon> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, PiercingWeapon::dealsKnockback, ByteBufCodecs.BOOL, PiercingWeapon::dismounts, SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional), PiercingWeapon::sound, SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional), PiercingWeapon::hitSound, PiercingWeapon::new);

    public void makeSound(Entity entity) {
        this.sound.ifPresent(holder -> entity.level().playSound(entity, entity.getX(), entity.getY(), entity.getZ(), (Holder<SoundEvent>)holder, entity.getSoundSource(), 1.0f, 1.0f));
    }

    public void makeHitSound(Entity entity) {
        this.hitSound.ifPresent(holder -> entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), (Holder<SoundEvent>)holder, entity.getSoundSource(), 1.0f, 1.0f));
    }

    public static boolean canHitEntity(Entity entity, Entity entity2) {
        if (entity2.isInvulnerable() || !entity2.isAlive()) {
            return false;
        }
        if (entity2 instanceof Interaction) {
            return true;
        }
        if (!entity2.canBeHitByProjectile()) {
            return false;
        }
        if (entity2 instanceof Player) {
            Player player2;
            Player player = (Player)entity2;
            if (entity instanceof Player && !(player2 = (Player)entity).canHarmPlayer(player)) {
                return false;
            }
        }
        return !entity.isPassengerOfSameVehicle(entity2);
    }

    public void attack(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        float f = (float)livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        AttackRange attackRange = livingEntity.entityAttackRange();
        boolean bl = false;
        for (EntityHitResult entityHitResult : (Collection)ProjectileUtil.getHitEntitiesAlong(livingEntity, attackRange, entity -> PiercingWeapon.canHitEntity(livingEntity, entity), ClipContext.Block.COLLIDER).map(blockHitResult -> List.of(), collection -> collection)) {
            bl |= livingEntity.stabAttack(equipmentSlot, entityHitResult.getEntity(), f, true, this.dealsKnockback, this.dismounts);
        }
        livingEntity.onAttack();
        livingEntity.lungeForwardMaybe();
        if (bl) {
            this.makeHitSound(livingEntity);
        }
        this.makeSound(livingEntity);
        livingEntity.swing(InteractionHand.MAIN_HAND, false);
    }
}


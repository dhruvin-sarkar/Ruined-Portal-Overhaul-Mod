/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MaceItem
extends Item {
    private static final int DEFAULT_ATTACK_DAMAGE = 5;
    private static final float DEFAULT_ATTACK_SPEED = -3.4f;
    public static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5f;
    private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0f;
    public static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 3.5f;
    private static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.7f;

    public MaceItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0f, 2, false);
    }

    @Override
    public void hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (MaceItem.canSmashAttack(livingEntity2)) {
            ServerPlayer serverPlayer;
            ServerLevel serverLevel = (ServerLevel)livingEntity2.level();
            livingEntity2.setDeltaMovement(livingEntity2.getDeltaMovement().with(Direction.Axis.Y, 0.01f));
            if (livingEntity2 instanceof ServerPlayer) {
                serverPlayer = (ServerPlayer)livingEntity2;
                serverPlayer.currentImpulseImpactPos = this.calculateImpactPosition(serverPlayer);
                serverPlayer.setIgnoreFallDamageFromCurrentImpulse(true);
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }
            if (livingEntity.onGround()) {
                if (livingEntity2 instanceof ServerPlayer) {
                    serverPlayer = (ServerPlayer)livingEntity2;
                    serverPlayer.setSpawnExtraParticlesOnFall(true);
                }
                SoundEvent soundEvent = livingEntity2.fallDistance > 5.0 ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
                serverLevel.playSound(null, livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ(), soundEvent, livingEntity2.getSoundSource(), 1.0f, 1.0f);
            } else {
                serverLevel.playSound(null, livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ(), SoundEvents.MACE_SMASH_AIR, livingEntity2.getSoundSource(), 1.0f, 1.0f);
            }
            MaceItem.knockback(serverLevel, livingEntity2, livingEntity);
        }
    }

    private Vec3 calculateImpactPosition(ServerPlayer serverPlayer) {
        if (serverPlayer.isIgnoringFallDamageFromCurrentImpulse() && serverPlayer.currentImpulseImpactPos != null && serverPlayer.currentImpulseImpactPos.y <= serverPlayer.position().y) {
            return serverPlayer.currentImpulseImpactPos;
        }
        return serverPlayer.position();
    }

    @Override
    public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (MaceItem.canSmashAttack(livingEntity2)) {
            livingEntity2.resetFallDistance();
        }
    }

    @Override
    public float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
        Entity entity2 = damageSource.getDirectEntity();
        if (!(entity2 instanceof LivingEntity)) {
            return 0.0f;
        }
        LivingEntity livingEntity = (LivingEntity)entity2;
        if (!MaceItem.canSmashAttack(livingEntity)) {
            return 0.0f;
        }
        double d = 3.0;
        double e = 8.0;
        double g = livingEntity.fallDistance;
        double h = g <= 3.0 ? 4.0 * g : (g <= 8.0 ? 12.0 + 2.0 * (g - 3.0) : 22.0 + g - 8.0);
        Level level = livingEntity.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return (float)(h + (double)EnchantmentHelper.modifyFallBasedDamage(serverLevel, livingEntity.getWeaponItem(), entity, damageSource, 0.0f) * g);
        }
        return (float)h;
    }

    private static void knockback(Level level, Entity entity, Entity entity2) {
        level.levelEvent(2013, entity2.getOnPos(), 750);
        level.getEntitiesOfClass(LivingEntity.class, entity2.getBoundingBox().inflate(3.5), MaceItem.knockbackPredicate(entity, entity2)).forEach(livingEntity -> {
            Vec3 vec3 = livingEntity.position().subtract(entity2.position());
            double d = MaceItem.getKnockbackPower(entity, livingEntity, vec3);
            Vec3 vec32 = vec3.normalize().scale(d);
            if (d > 0.0) {
                livingEntity.push(vec32.x, 0.7f, vec32.z);
                if (livingEntity instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                }
            }
        });
    }

    private static Predicate<LivingEntity> knockbackPredicate(Entity entity, Entity entity2) {
        return arg_0 -> MaceItem.method_58661(entity, entity2, arg_0);
    }

    private static double getKnockbackPower(Entity entity, LivingEntity livingEntity, Vec3 vec3) {
        return (3.5 - vec3.length()) * (double)0.7f * (double)(entity.fallDistance > 5.0 ? 2 : 1) * (1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }

    public static boolean canSmashAttack(LivingEntity livingEntity) {
        return livingEntity.fallDistance > 1.5 && !livingEntity.isFallFlying();
    }

    @Override
    public @Nullable DamageSource getItemDamageSource(LivingEntity livingEntity) {
        if (MaceItem.canSmashAttack(livingEntity)) {
            return livingEntity.damageSources().mace(livingEntity);
        }
        return super.getItemDamageSource(livingEntity);
    }

    /*
     * Unable to fully structure code
     */
    private static /* synthetic */ boolean method_58661(Entity entity, Entity entity2, LivingEntity livingEntity) {
        bl = livingEntity.isSpectator() == false;
        bl2 = livingEntity != entity && livingEntity != entity2;
        v0 = bl3 = entity.isAlliedTo(livingEntity) == false;
        if (!(livingEntity instanceof TamableAnimal)) ** GOTO lbl-1000
        tamableAnimal = (TamableAnimal)livingEntity;
        if (!(entity2 instanceof LivingEntity)) ** GOTO lbl-1000
        livingEntity2 = (LivingEntity)entity2;
        if (tamableAnimal.isTame() && tamableAnimal.isOwnedBy(livingEntity2)) {
            v1 = true;
        } else lbl-1000:
        // 3 sources

        {
            v1 = false;
        }
        bl4 = v1 == false;
        bl5 = livingEntity instanceof ArmorStand == false || (armorStand = (ArmorStand)livingEntity).isMarker() == false;
        bl6 = entity2.distanceToSqr(livingEntity) <= Math.pow(3.5, 2.0);
        bl7 = (livingEntity instanceof Player != false && (player = (Player)livingEntity).isCreative() != false && player.getAbilities().flying != false) == false;
        return bl != false && bl2 != false && bl3 != false && bl4 != false && bl5 != false && bl6 != false && bl7 != false;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.Set;
import java.util.UUID;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Scoreboard;

public enum ConversionType {
    SINGLE(true){

        @Override
        void convert(Mob mob, Mob mob2, ConversionParams conversionParams) {
            Entity entity3;
            Entity entity = mob.getFirstPassenger();
            mob2.copyPosition(mob);
            mob2.setDeltaMovement(mob.getDeltaMovement());
            if (entity != null) {
                entity.stopRiding();
                entity.boardingCooldown = 0;
                for (Entity entity2 : mob2.getPassengers()) {
                    entity2.stopRiding();
                    entity2.remove(Entity.RemovalReason.DISCARDED);
                }
                entity.startRiding(mob2);
            }
            if ((entity3 = mob.getVehicle()) != null) {
                mob.stopRiding();
                mob2.startRiding(entity3, false, false);
            }
            if (conversionParams.keepEquipment()) {
                for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
                    ItemStack itemStack = mob.getItemBySlot(equipmentSlot);
                    if (itemStack.isEmpty()) continue;
                    mob2.setItemSlot(equipmentSlot, itemStack.copyAndClear());
                    mob2.setDropChance(equipmentSlot, mob.getDropChances().byEquipment(equipmentSlot));
                }
            }
            mob2.fallDistance = mob.fallDistance;
            mob2.setSharedFlag(7, mob.isFallFlying());
            mob2.lastHurtByPlayerMemoryTime = mob.lastHurtByPlayerMemoryTime;
            mob2.hurtTime = mob.hurtTime;
            mob2.yBodyRot = mob.yBodyRot;
            mob2.setOnGround(mob.onGround());
            mob.getSleepingPos().ifPresent(mob2::setSleepingPos);
            Entity entity4 = mob.getLeashHolder();
            if (entity4 != null) {
                mob2.setLeashedTo(entity4, true);
            }
            this.convertCommon(mob, mob2, conversionParams);
        }
    }
    ,
    SPLIT_ON_DEATH(false){

        @Override
        void convert(Mob mob, Mob mob2, ConversionParams conversionParams) {
            Entity entity2;
            Entity entity = mob.getFirstPassenger();
            if (entity != null) {
                entity.stopRiding();
            }
            if ((entity2 = mob.getLeashHolder()) != null) {
                mob.dropLeash();
            }
            this.convertCommon(mob, mob2, conversionParams);
        }
    };

    private static final Set<DataComponentType<?>> COMPONENTS_TO_COPY;
    private final boolean discardAfterConversion;

    ConversionType(boolean bl) {
        this.discardAfterConversion = bl;
    }

    public boolean shouldDiscardAfterConversion() {
        return this.discardAfterConversion;
    }

    abstract void convert(Mob var1, Mob var2, ConversionParams var3);

    void convertCommon(Mob mob, Mob mob2, ConversionParams conversionParams) {
        Zombie zombie;
        mob2.setAbsorptionAmount(mob.getAbsorptionAmount());
        for (MobEffectInstance mobEffectInstance : mob.getActiveEffects()) {
            mob2.addEffect(new MobEffectInstance(mobEffectInstance));
        }
        if (mob.isBaby()) {
            mob2.setBaby(true);
        }
        if (mob instanceof AgeableMob) {
            AgeableMob ageableMob = (AgeableMob)mob;
            if (mob2 instanceof AgeableMob) {
                AgeableMob ageableMob2 = (AgeableMob)mob2;
                ageableMob2.setAge(ageableMob.getAge());
                ageableMob2.forcedAge = ageableMob.forcedAge;
                ageableMob2.forcedAgeTimer = ageableMob.forcedAgeTimer;
            }
        }
        Brain<UUID> brain = mob.getBrain();
        Brain<?> brain2 = mob2.getBrain();
        if (brain.checkMemory(MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED) && brain.hasMemoryValue(MemoryModuleType.ANGRY_AT)) {
            brain2.setMemory(MemoryModuleType.ANGRY_AT, brain.getMemory(MemoryModuleType.ANGRY_AT));
        }
        if (conversionParams.preserveCanPickUpLoot()) {
            mob2.setCanPickUpLoot(mob.canPickUpLoot());
        }
        mob2.setLeftHanded(mob.isLeftHanded());
        mob2.setNoAi(mob.isNoAi());
        if (mob.isPersistenceRequired()) {
            mob2.setPersistenceRequired();
        }
        mob2.setCustomNameVisible(mob.isCustomNameVisible());
        mob2.setSharedFlagOnFire(mob.isOnFire());
        mob2.setInvulnerable(mob.isInvulnerable());
        mob2.setNoGravity(mob.isNoGravity());
        mob2.setPortalCooldown(mob.getPortalCooldown());
        mob2.setSilent(mob.isSilent());
        mob.getTags().forEach(mob2::addTag);
        for (DataComponentType<?> dataComponentType : COMPONENTS_TO_COPY) {
            ConversionType.copyComponent(mob, mob2, dataComponentType);
        }
        if (conversionParams.team() != null) {
            Scoreboard scoreboard = mob2.level().getScoreboard();
            scoreboard.addPlayerToTeam(mob2.getStringUUID(), conversionParams.team());
            if (mob.getTeam() != null && mob.getTeam() == conversionParams.team()) {
                scoreboard.removePlayerFromTeam(mob.getStringUUID(), mob.getTeam());
            }
        }
        if (mob instanceof Zombie && (zombie = (Zombie)mob).canBreakDoors() && mob2 instanceof Zombie) {
            Zombie zombie2 = (Zombie)mob2;
            zombie2.setCanBreakDoors(true);
        }
    }

    private static <T> void copyComponent(Mob mob, Mob mob2, DataComponentType<T> dataComponentType) {
        T object = mob.get(dataComponentType);
        if (object != null) {
            mob2.setComponent(dataComponentType, object);
        }
    }

    static {
        COMPONENTS_TO_COPY = Set.of(DataComponents.CUSTOM_NAME, DataComponents.CUSTOM_DATA);
    }
}


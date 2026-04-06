/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class GiveGiftToHero
extends Behavior<Villager> {
    private static final int THROW_GIFT_AT_DISTANCE = 5;
    private static final int MIN_TIME_BETWEEN_GIFTS = 600;
    private static final int MAX_TIME_BETWEEN_GIFTS = 6600;
    private static final int TIME_TO_DELAY_FOR_HEAD_TO_FINISH_TURNING = 20;
    private static final Map<ResourceKey<VillagerProfession>, ResourceKey<LootTable>> GIFTS = ImmutableMap.builder().put(VillagerProfession.ARMORER, BuiltInLootTables.ARMORER_GIFT).put(VillagerProfession.BUTCHER, BuiltInLootTables.BUTCHER_GIFT).put(VillagerProfession.CARTOGRAPHER, BuiltInLootTables.CARTOGRAPHER_GIFT).put(VillagerProfession.CLERIC, BuiltInLootTables.CLERIC_GIFT).put(VillagerProfession.FARMER, BuiltInLootTables.FARMER_GIFT).put(VillagerProfession.FISHERMAN, BuiltInLootTables.FISHERMAN_GIFT).put(VillagerProfession.FLETCHER, BuiltInLootTables.FLETCHER_GIFT).put(VillagerProfession.LEATHERWORKER, BuiltInLootTables.LEATHERWORKER_GIFT).put(VillagerProfession.LIBRARIAN, BuiltInLootTables.LIBRARIAN_GIFT).put(VillagerProfession.MASON, BuiltInLootTables.MASON_GIFT).put(VillagerProfession.SHEPHERD, BuiltInLootTables.SHEPHERD_GIFT).put(VillagerProfession.TOOLSMITH, BuiltInLootTables.TOOLSMITH_GIFT).put(VillagerProfession.WEAPONSMITH, BuiltInLootTables.WEAPONSMITH_GIFT).build();
    private static final float SPEED_MODIFIER = 0.5f;
    private int timeUntilNextGift = 600;
    private boolean giftGivenDuringThisRun;
    private long timeSinceStart;

    public GiveGiftToHero(int i) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.INTERACTION_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.NEAREST_VISIBLE_PLAYER, (Object)((Object)MemoryStatus.VALUE_PRESENT)), i);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        if (!this.isHeroVisible(villager)) {
            return false;
        }
        if (this.timeUntilNextGift > 0) {
            --this.timeUntilNextGift;
            return false;
        }
        return true;
    }

    @Override
    protected void start(ServerLevel serverLevel, Villager villager, long l) {
        this.giftGivenDuringThisRun = false;
        this.timeSinceStart = l;
        Player player = this.getNearestTargetableHero(villager).get();
        villager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, player);
        BehaviorUtils.lookAtEntity(villager, player);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
        return this.isHeroVisible(villager) && !this.giftGivenDuringThisRun;
    }

    @Override
    protected void tick(ServerLevel serverLevel, Villager villager, long l) {
        Player player = this.getNearestTargetableHero(villager).get();
        BehaviorUtils.lookAtEntity(villager, player);
        if (this.isWithinThrowingDistance(villager, player)) {
            if (l - this.timeSinceStart > 20L) {
                this.throwGift(serverLevel, villager, player);
                this.giftGivenDuringThisRun = true;
            }
        } else {
            BehaviorUtils.setWalkAndLookTargetMemories((LivingEntity)villager, player, 0.5f, 5);
        }
    }

    @Override
    protected void stop(ServerLevel serverLevel, Villager villager, long l) {
        this.timeUntilNextGift = GiveGiftToHero.calculateTimeUntilNextGift(serverLevel);
        villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    private void throwGift(ServerLevel serverLevel2, Villager villager, LivingEntity livingEntity) {
        villager.dropFromGiftLootTable(serverLevel2, GiveGiftToHero.getLootTableToThrow(villager), (serverLevel, itemStack) -> BehaviorUtils.throwItem(villager, itemStack, livingEntity.position()));
    }

    private static ResourceKey<LootTable> getLootTableToThrow(Villager villager) {
        if (villager.isBaby()) {
            return BuiltInLootTables.BABY_VILLAGER_GIFT;
        }
        Optional<ResourceKey<VillagerProfession>> optional = villager.getVillagerData().profession().unwrapKey();
        if (optional.isEmpty()) {
            return BuiltInLootTables.UNEMPLOYED_GIFT;
        }
        return GIFTS.getOrDefault(optional.get(), BuiltInLootTables.UNEMPLOYED_GIFT);
    }

    private boolean isHeroVisible(Villager villager) {
        return this.getNearestTargetableHero(villager).isPresent();
    }

    private Optional<Player> getNearestTargetableHero(Villager villager) {
        return villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).filter(this::isHero);
    }

    private boolean isHero(Player player) {
        return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE);
    }

    private boolean isWithinThrowingDistance(Villager villager, Player player) {
        BlockPos blockPos = player.blockPosition();
        BlockPos blockPos2 = villager.blockPosition();
        return blockPos2.closerThan(blockPos, 5.0);
    }

    private static int calculateTimeUntilNextGift(ServerLevel serverLevel) {
        return 600 + serverLevel.random.nextInt(6001);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}


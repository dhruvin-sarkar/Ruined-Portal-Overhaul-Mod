/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface RandomizableContainer
extends Container {
    public static final String LOOT_TABLE_TAG = "LootTable";
    public static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";

    public @Nullable ResourceKey<LootTable> getLootTable();

    public void setLootTable(@Nullable ResourceKey<LootTable> var1);

    default public void setLootTable(ResourceKey<LootTable> resourceKey, long l) {
        this.setLootTable(resourceKey);
        this.setLootTableSeed(l);
    }

    public long getLootTableSeed();

    public void setLootTableSeed(long var1);

    public BlockPos getBlockPos();

    public @Nullable Level getLevel();

    public static void setBlockEntityLootTable(BlockGetter blockGetter, RandomSource randomSource, BlockPos blockPos, ResourceKey<LootTable> resourceKey) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof RandomizableContainer) {
            RandomizableContainer randomizableContainer = (RandomizableContainer)((Object)blockEntity);
            randomizableContainer.setLootTable(resourceKey, randomSource.nextLong());
        }
    }

    default public boolean tryLoadLootTable(ValueInput valueInput) {
        ResourceKey resourceKey = valueInput.read(LOOT_TABLE_TAG, LootTable.KEY_CODEC).orElse(null);
        this.setLootTable(resourceKey);
        this.setLootTableSeed(valueInput.getLongOr(LOOT_TABLE_SEED_TAG, 0L));
        return resourceKey != null;
    }

    default public boolean trySaveLootTable(ValueOutput valueOutput) {
        ResourceKey<LootTable> resourceKey = this.getLootTable();
        if (resourceKey == null) {
            return false;
        }
        valueOutput.store(LOOT_TABLE_TAG, LootTable.KEY_CODEC, resourceKey);
        long l = this.getLootTableSeed();
        if (l != 0L) {
            valueOutput.putLong(LOOT_TABLE_SEED_TAG, l);
        }
        return true;
    }

    default public void unpackLootTable(@Nullable Player player) {
        Level level = this.getLevel();
        BlockPos blockPos = this.getBlockPos();
        ResourceKey<LootTable> resourceKey = this.getLootTable();
        if (resourceKey != null && level != null && level.getServer() != null) {
            LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(resourceKey);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, resourceKey);
            }
            this.setLootTable(null);
            LootParams.Builder builder = new LootParams.Builder((ServerLevel)level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos));
            if (player != null) {
                builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }
            lootTable.fill(this, builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
        }
    }
}


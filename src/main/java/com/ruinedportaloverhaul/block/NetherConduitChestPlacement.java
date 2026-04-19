package com.ruinedportaloverhaul.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

public final class NetherConduitChestPlacement {
    private static final long BOSS_CHEST_CHOICE_SALT = 0x6E7CE0F9A5D1B33DL;
    private static final long DEEP_CHEST_CHOICE_SALT = 0x1F2D3C4B5A697887L;

    private NetherConduitChestPlacement() {
    }

    public static boolean useBossChest(BlockPos portalOrigin) {
        return RandomSource.create(portalOrigin.asLong() ^ BOSS_CHEST_CHOICE_SALT).nextBoolean();
    }

    public static BlockPos pickDeepChest(BlockPos portalOrigin, List<BlockPos> deepChests) {
        if (deepChests.isEmpty()) {
            return null;
        }
        RandomSource random = RandomSource.create(portalOrigin.asLong() ^ DEEP_CHEST_CHOICE_SALT);
        return deepChests.get(random.nextInt(deepChests.size()));
    }

    public static void addNetherConduit(RandomizableContainerBlockEntity chest) {
        int slot = Math.max(0, chest.getContainerSize() / 2);
        chest.setItem(slot, new ItemStack(ModBlocks.NETHER_CONDUIT_ITEM));
        chest.setChanged();
    }
}

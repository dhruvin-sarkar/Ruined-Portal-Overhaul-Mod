package com.ruinedportaloverhaul.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

public final class NetherConduitChestPlacement {
    private static final long DEEP_CHEST_CHOICE_SALT = 0x1F2D3C4B5A697887L;

    private NetherConduitChestPlacement() {
    }

    public static BlockPos pickDeepChest(BlockPos portalOrigin, List<BlockPos> deepChests) {
        // Fix: the guaranteed conduit used to be split between generated deep chests and the later post-raid boss chest, leaving some structures with no conduit during exploration. The selector now always targets a generated deep chest so each dungeon contains exactly one direct-insert conduit before raid completion.
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

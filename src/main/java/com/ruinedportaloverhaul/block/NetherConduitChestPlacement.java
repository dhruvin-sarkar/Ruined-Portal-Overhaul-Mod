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
        // Fix: the guaranteed conduit used to overwrite the center loot slot after the chest loot table unpacked. It now prefers the nearest empty slot so the direct-insert reward does not silently delete generated treasure.
        int slot = findBestEmptySlot(chest);
        chest.setItem(slot, new ItemStack(ModBlocks.NETHER_CONDUIT_ITEM));
        chest.setChanged();
    }

    private static int findBestEmptySlot(RandomizableContainerBlockEntity chest) {
        int size = chest.getContainerSize();
        if (size <= 0) {
            return 0;
        }
        int center = size / 2;
        if (chest.getItem(center).isEmpty()) {
            return center;
        }
        for (int offset = 1; offset < size; offset++) {
            int right = center + offset;
            if (right < size && chest.getItem(right).isEmpty()) {
                return right;
            }
            int left = center - offset;
            if (left >= 0 && chest.getItem(left).isEmpty()) {
                return left;
            }
        }
        return center;
    }
}

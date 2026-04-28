package com.ruinedportaloverhaul.structure;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class StructureBlockPalette {
    private StructureBlockPalette() {
    }

    public static BlockState netherWall(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.80f) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (roll < 0.95f) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        return Blocks.GRAVEL.defaultBlockState();
    }

    public static BlockState netherBrick(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.65f) {
            return Blocks.NETHER_BRICKS.defaultBlockState();
        }
        if (roll < 0.85f) {
            return Blocks.CRACKED_NETHER_BRICKS.defaultBlockState();
        }
        return Blocks.CHISELED_NETHER_BRICKS.defaultBlockState();
    }

    public static BlockState deepWall(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.50f) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        if (roll < 0.80f) {
            return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        }
        if (roll < 0.95f) {
            return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        }
        return Blocks.GILDED_BLACKSTONE.defaultBlockState();
    }

    public static BlockState ritualFloor(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.60f) {
            return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        }
        if (roll < 0.85f) {
            return Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        }
        return Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
    }
}

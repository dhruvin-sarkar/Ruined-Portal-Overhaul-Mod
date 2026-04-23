package com.ruinedportaloverhaul.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public enum PortalDungeonVariant {
    CRIMSON_THRONE(0, "crimson_throne", 0),
    SUNKEN_SANCTUM(1, "sunken_sanctum", -4),
    BASALT_CITADEL(2, "basalt_citadel", 0);

    private static final PortalDungeonVariant[] VALUES = values();
    private static final long SELECTION_MULTIPLIER = 6364136223846793005L;

    private final int id;
    private final String serializedName;
    private final int centerYOffset;

    PortalDungeonVariant(int id, String serializedName, int centerYOffset) {
        this.id = id;
        this.serializedName = serializedName;
        this.centerYOffset = centerYOffset;
    }

    public int id() {
        return this.id;
    }

    public String serializedName() {
        return this.serializedName;
    }

    public int centerYOffset() {
        return this.centerYOffset;
    }

    public static PortalDungeonVariant byId(int id) {
        return VALUES[Math.floorMod(id, VALUES.length)];
    }

    public static PortalDungeonVariant selectForChunk(ChunkPos chunkPos) {
        long mixed = chunkPos.toLong() * SELECTION_MULTIPLIER;
        return byId((int) Math.floorMod(mixed, VALUES.length));
    }

    public static PortalDungeonVariant selectForOrigin(BlockPos origin) {
        return selectForChunk(new ChunkPos(origin));
    }
}

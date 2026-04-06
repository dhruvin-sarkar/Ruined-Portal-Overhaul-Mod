/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectListIterator
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.BitStorage;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.slf4j.Logger;

public class Heightmap {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Predicate<BlockState> NOT_AIR = blockState -> !blockState.isAir();
    static final Predicate<BlockState> MATERIAL_MOTION_BLOCKING = BlockBehaviour.BlockStateBase::blocksMotion;
    private final BitStorage data;
    private final Predicate<BlockState> isOpaque;
    private final ChunkAccess chunk;

    public Heightmap(ChunkAccess chunkAccess, Types types) {
        this.isOpaque = types.isOpaque();
        this.chunk = chunkAccess;
        int i = Mth.ceillog2(chunkAccess.getHeight() + 1);
        this.data = new SimpleBitStorage(i, 256);
    }

    public static void primeHeightmaps(ChunkAccess chunkAccess, Set<Types> set) {
        if (set.isEmpty()) {
            return;
        }
        int i = set.size();
        ObjectArrayList objectList = new ObjectArrayList(i);
        ObjectListIterator objectListIterator = objectList.iterator();
        int j = chunkAccess.getHighestSectionPosition() + 16;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int k = 0; k < 16; ++k) {
            block1: for (int l = 0; l < 16; ++l) {
                for (Types types : set) {
                    objectList.add((Object)chunkAccess.getOrCreateHeightmapUnprimed(types));
                }
                for (int m = j - 1; m >= chunkAccess.getMinY(); --m) {
                    mutableBlockPos.set(k, m, l);
                    BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
                    if (blockState.is(Blocks.AIR)) continue;
                    while (objectListIterator.hasNext()) {
                        Heightmap heightmap = (Heightmap)objectListIterator.next();
                        if (!heightmap.isOpaque.test(blockState)) continue;
                        heightmap.setHeight(k, l, m + 1);
                        objectListIterator.remove();
                    }
                    if (objectList.isEmpty()) continue block1;
                    objectListIterator.back(i);
                }
            }
        }
    }

    public boolean update(int i, int j, int k, BlockState blockState) {
        int l = this.getFirstAvailable(i, k);
        if (j <= l - 2) {
            return false;
        }
        if (this.isOpaque.test(blockState)) {
            if (j >= l) {
                this.setHeight(i, k, j + 1);
                return true;
            }
        } else if (l - 1 == j) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int m = j - 1; m >= this.chunk.getMinY(); --m) {
                mutableBlockPos.set(i, m, k);
                if (!this.isOpaque.test(this.chunk.getBlockState(mutableBlockPos))) continue;
                this.setHeight(i, k, m + 1);
                return true;
            }
            this.setHeight(i, k, this.chunk.getMinY());
            return true;
        }
        return false;
    }

    public int getFirstAvailable(int i, int j) {
        return this.getFirstAvailable(Heightmap.getIndex(i, j));
    }

    public int getHighestTaken(int i, int j) {
        return this.getFirstAvailable(Heightmap.getIndex(i, j)) - 1;
    }

    private int getFirstAvailable(int i) {
        return this.data.get(i) + this.chunk.getMinY();
    }

    private void setHeight(int i, int j, int k) {
        this.data.set(Heightmap.getIndex(i, j), k - this.chunk.getMinY());
    }

    public void setRawData(ChunkAccess chunkAccess, Types types, long[] ls) {
        long[] ms = this.data.getRaw();
        if (ms.length == ls.length) {
            System.arraycopy(ls, 0, ms, 0, ls.length);
            return;
        }
        LOGGER.warn("Ignoring heightmap data for chunk {}, size does not match; expected: {}, got: {}", new Object[]{chunkAccess.getPos(), ms.length, ls.length});
        Heightmap.primeHeightmaps(chunkAccess, EnumSet.of(types));
    }

    public long[] getRawData() {
        return this.data.getRaw();
    }

    private static int getIndex(int i, int j) {
        return i + j * 16;
    }

    public static enum Types implements StringRepresentable
    {
        WORLD_SURFACE_WG(0, "WORLD_SURFACE_WG", Usage.WORLDGEN, NOT_AIR),
        WORLD_SURFACE(1, "WORLD_SURFACE", Usage.CLIENT, NOT_AIR),
        OCEAN_FLOOR_WG(2, "OCEAN_FLOOR_WG", Usage.WORLDGEN, MATERIAL_MOTION_BLOCKING),
        OCEAN_FLOOR(3, "OCEAN_FLOOR", Usage.LIVE_WORLD, MATERIAL_MOTION_BLOCKING),
        MOTION_BLOCKING(4, "MOTION_BLOCKING", Usage.CLIENT, blockState -> blockState.blocksMotion() || !blockState.getFluidState().isEmpty()),
        MOTION_BLOCKING_NO_LEAVES(5, "MOTION_BLOCKING_NO_LEAVES", Usage.CLIENT, blockState -> (blockState.blocksMotion() || !blockState.getFluidState().isEmpty()) && !(blockState.getBlock() instanceof LeavesBlock));

        public static final Codec<Types> CODEC;
        private static final IntFunction<Types> BY_ID;
        public static final StreamCodec<ByteBuf, Types> STREAM_CODEC;
        private final int id;
        private final String serializationKey;
        private final Usage usage;
        private final Predicate<BlockState> isOpaque;

        private Types(int j, String string2, Usage usage, Predicate<BlockState> predicate) {
            this.id = j;
            this.serializationKey = string2;
            this.usage = usage;
            this.isOpaque = predicate;
        }

        public String getSerializationKey() {
            return this.serializationKey;
        }

        public boolean sendToClient() {
            return this.usage == Usage.CLIENT;
        }

        public boolean keepAfterWorldgen() {
            return this.usage != Usage.WORLDGEN;
        }

        public Predicate<BlockState> isOpaque() {
            return this.isOpaque;
        }

        @Override
        public String getSerializedName() {
            return this.serializationKey;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Types::values);
            BY_ID = ByIdMap.continuous(types -> types.id, Types.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, types -> types.id);
        }
    }

    public static enum Usage {
        WORLDGEN,
        LIVE_WORLD,
        CLIENT;

    }
}


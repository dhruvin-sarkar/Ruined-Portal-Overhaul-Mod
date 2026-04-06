/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  java.lang.MatchException
 *  org.apache.commons.lang3.Validate
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jetbrains.annotations.Unmodifiable
 */
package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Unmodifiable;

@Unmodifiable
public class BlockPos
extends Vec3i {
    public static final Codec<BlockPos> CODEC = Codec.INT_STREAM.comapFlatMap(intStream -> Util.fixedSize(intStream, 3).map(is -> new BlockPos(is[0], is[1], is[2])), blockPos -> IntStream.of(blockPos.getX(), blockPos.getY(), blockPos.getZ())).stable();
    public static final StreamCodec<ByteBuf, BlockPos> STREAM_CODEC = new StreamCodec<ByteBuf, BlockPos>(){

        @Override
        public BlockPos decode(ByteBuf byteBuf) {
            return FriendlyByteBuf.readBlockPos(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, BlockPos blockPos) {
            FriendlyByteBuf.writeBlockPos(byteBuf, blockPos);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (BlockPos)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    public static final int PACKED_HORIZONTAL_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
    public static final int PACKED_Y_LENGTH = 64 - 2 * PACKED_HORIZONTAL_LENGTH;
    private static final long PACKED_X_MASK = (1L << PACKED_HORIZONTAL_LENGTH) - 1L;
    private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
    private static final long PACKED_Z_MASK = (1L << PACKED_HORIZONTAL_LENGTH) - 1L;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = PACKED_Y_LENGTH;
    private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_HORIZONTAL_LENGTH;
    public static final int MAX_HORIZONTAL_COORDINATE = (1 << PACKED_HORIZONTAL_LENGTH) / 2 - 1;

    public BlockPos(int i, int j, int k) {
        super(i, j, k);
    }

    public BlockPos(Vec3i vec3i) {
        this(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static long offset(long l, Direction direction) {
        return BlockPos.offset(l, direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public static long offset(long l, int i, int j, int k) {
        return BlockPos.asLong(BlockPos.getX(l) + i, BlockPos.getY(l) + j, BlockPos.getZ(l) + k);
    }

    public static int getX(long l) {
        return (int)(l << 64 - X_OFFSET - PACKED_HORIZONTAL_LENGTH >> 64 - PACKED_HORIZONTAL_LENGTH);
    }

    public static int getY(long l) {
        return (int)(l << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
    }

    public static int getZ(long l) {
        return (int)(l << 64 - Z_OFFSET - PACKED_HORIZONTAL_LENGTH >> 64 - PACKED_HORIZONTAL_LENGTH);
    }

    public static BlockPos of(long l) {
        return new BlockPos(BlockPos.getX(l), BlockPos.getY(l), BlockPos.getZ(l));
    }

    public static BlockPos containing(double d, double e, double f) {
        return new BlockPos(Mth.floor(d), Mth.floor(e), Mth.floor(f));
    }

    public static BlockPos containing(Position position) {
        return BlockPos.containing(position.x(), position.y(), position.z());
    }

    public static BlockPos min(BlockPos blockPos, BlockPos blockPos2) {
        return new BlockPos(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()));
    }

    public static BlockPos max(BlockPos blockPos, BlockPos blockPos2) {
        return new BlockPos(Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
    }

    public long asLong() {
        return BlockPos.asLong(this.getX(), this.getY(), this.getZ());
    }

    public static long asLong(int i, int j, int k) {
        long l = 0L;
        l |= ((long)i & PACKED_X_MASK) << X_OFFSET;
        l |= ((long)j & PACKED_Y_MASK) << 0;
        return l |= ((long)k & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long l) {
        return l & 0xFFFFFFFFFFFFFFF0L;
    }

    @Override
    public BlockPos offset(int i, int j, int k) {
        if (i == 0 && j == 0 && k == 0) {
            return this;
        }
        return new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
    }

    public Vec3 getCenter() {
        return Vec3.atCenterOf(this);
    }

    public Vec3 getBottomCenter() {
        return Vec3.atBottomCenterOf(this);
    }

    @Override
    public BlockPos offset(Vec3i vec3i) {
        return this.offset(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    @Override
    public BlockPos subtract(Vec3i vec3i) {
        return this.offset(-vec3i.getX(), -vec3i.getY(), -vec3i.getZ());
    }

    @Override
    public BlockPos multiply(int i) {
        if (i == 1) {
            return this;
        }
        if (i == 0) {
            return ZERO;
        }
        return new BlockPos(this.getX() * i, this.getY() * i, this.getZ() * i);
    }

    @Override
    public BlockPos above() {
        return this.relative(Direction.UP);
    }

    @Override
    public BlockPos above(int i) {
        return this.relative(Direction.UP, i);
    }

    @Override
    public BlockPos below() {
        return this.relative(Direction.DOWN);
    }

    @Override
    public BlockPos below(int i) {
        return this.relative(Direction.DOWN, i);
    }

    @Override
    public BlockPos north() {
        return this.relative(Direction.NORTH);
    }

    @Override
    public BlockPos north(int i) {
        return this.relative(Direction.NORTH, i);
    }

    @Override
    public BlockPos south() {
        return this.relative(Direction.SOUTH);
    }

    @Override
    public BlockPos south(int i) {
        return this.relative(Direction.SOUTH, i);
    }

    @Override
    public BlockPos west() {
        return this.relative(Direction.WEST);
    }

    @Override
    public BlockPos west(int i) {
        return this.relative(Direction.WEST, i);
    }

    @Override
    public BlockPos east() {
        return this.relative(Direction.EAST);
    }

    @Override
    public BlockPos east(int i) {
        return this.relative(Direction.EAST, i);
    }

    @Override
    public BlockPos relative(Direction direction) {
        return new BlockPos(this.getX() + direction.getStepX(), this.getY() + direction.getStepY(), this.getZ() + direction.getStepZ());
    }

    @Override
    public BlockPos relative(Direction direction, int i) {
        if (i == 0) {
            return this;
        }
        return new BlockPos(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
    }

    @Override
    public BlockPos relative(Direction.Axis axis, int i) {
        if (i == 0) {
            return this;
        }
        int j = axis == Direction.Axis.X ? i : 0;
        int k = axis == Direction.Axis.Y ? i : 0;
        int l = axis == Direction.Axis.Z ? i : 0;
        return new BlockPos(this.getX() + j, this.getY() + k, this.getZ() + l);
    }

    public BlockPos rotate(Rotation rotation) {
        return switch (rotation) {
            default -> throw new MatchException(null, null);
            case Rotation.CLOCKWISE_90 -> new BlockPos(-this.getZ(), this.getY(), this.getX());
            case Rotation.CLOCKWISE_180 -> new BlockPos(-this.getX(), this.getY(), -this.getZ());
            case Rotation.COUNTERCLOCKWISE_90 -> new BlockPos(this.getZ(), this.getY(), -this.getX());
            case Rotation.NONE -> this;
        };
    }

    @Override
    public BlockPos cross(Vec3i vec3i) {
        return new BlockPos(this.getY() * vec3i.getZ() - this.getZ() * vec3i.getY(), this.getZ() * vec3i.getX() - this.getX() * vec3i.getZ(), this.getX() * vec3i.getY() - this.getY() * vec3i.getX());
    }

    public BlockPos atY(int i) {
        return new BlockPos(this.getX(), i, this.getZ());
    }

    public BlockPos immutable() {
        return this;
    }

    public MutableBlockPos mutable() {
        return new MutableBlockPos(this.getX(), this.getY(), this.getZ());
    }

    public Vec3 clampLocationWithin(Vec3 vec3) {
        return new Vec3(Mth.clamp(vec3.x, (double)((float)this.getX() + 1.0E-5f), (double)this.getX() + 1.0 - (double)1.0E-5f), Mth.clamp(vec3.y, (double)((float)this.getY() + 1.0E-5f), (double)this.getY() + 1.0 - (double)1.0E-5f), Mth.clamp(vec3.z, (double)((float)this.getZ() + 1.0E-5f), (double)this.getZ() + 1.0 - (double)1.0E-5f));
    }

    public static Iterable<BlockPos> randomInCube(RandomSource randomSource, int i, BlockPos blockPos, int j) {
        return BlockPos.randomBetweenClosed(randomSource, i, blockPos.getX() - j, blockPos.getY() - j, blockPos.getZ() - j, blockPos.getX() + j, blockPos.getY() + j, blockPos.getZ() + j);
    }

    @Deprecated
    public static Stream<BlockPos> squareOutSouthEast(BlockPos blockPos) {
        return Stream.of(blockPos, blockPos.south(), blockPos.east(), blockPos.south().east());
    }

    public static Iterable<BlockPos> randomBetweenClosed(final RandomSource randomSource, final int i, final int j, final int k, final int l, int m, int n, int o) {
        final int p = m - j + 1;
        final int q = n - k + 1;
        final int r = o - l + 1;
        return () -> new AbstractIterator<BlockPos>(){
            final MutableBlockPos nextPos = new MutableBlockPos();
            int counter = i;

            protected BlockPos computeNext() {
                if (this.counter <= 0) {
                    return (BlockPos)this.endOfData();
                }
                MutableBlockPos blockPos = this.nextPos.set(j + randomSource.nextInt(p), k + randomSource.nextInt(q), l + randomSource.nextInt(r));
                --this.counter;
                return blockPos;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<BlockPos> withinManhattan(BlockPos blockPos, final int i, final int j, final int k) {
        final int l = i + j + k;
        final int m = blockPos.getX();
        final int n = blockPos.getY();
        final int o = blockPos.getZ();
        return () -> new AbstractIterator<BlockPos>(){
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int currentDepth;
            private int maxX;
            private int maxY;
            private int x;
            private int y;
            private boolean zMirror;

            protected BlockPos computeNext() {
                if (this.zMirror) {
                    this.zMirror = false;
                    this.cursor.setZ(o - (this.cursor.getZ() - o));
                    return this.cursor;
                }
                MutableBlockPos blockPos = null;
                while (blockPos == null) {
                    if (this.y > this.maxY) {
                        ++this.x;
                        if (this.x > this.maxX) {
                            ++this.currentDepth;
                            if (this.currentDepth > l) {
                                return (BlockPos)this.endOfData();
                            }
                            this.maxX = Math.min(i, this.currentDepth);
                            this.x = -this.maxX;
                        }
                        this.maxY = Math.min(j, this.currentDepth - Math.abs(this.x));
                        this.y = -this.maxY;
                    }
                    int i2 = this.x;
                    int j2 = this.y;
                    int k2 = this.currentDepth - Math.abs(i2) - Math.abs(j2);
                    if (k2 <= k) {
                        this.zMirror = k2 != 0;
                        blockPos = this.cursor.set(m + i2, n + j2, o + k2);
                    }
                    ++this.y;
                }
                return blockPos;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Optional<BlockPos> findClosestMatch(BlockPos blockPos, int i, int j, Predicate<BlockPos> predicate) {
        for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, i, j, i)) {
            if (!predicate.test(blockPos2)) continue;
            return Optional.of(blockPos2);
        }
        return Optional.empty();
    }

    public static Stream<BlockPos> withinManhattanStream(BlockPos blockPos, int i, int j, int k) {
        return StreamSupport.stream(BlockPos.withinManhattan(blockPos, i, j, k).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(AABB aABB) {
        BlockPos blockPos = BlockPos.containing(aABB.minX, aABB.minY, aABB.minZ);
        BlockPos blockPos2 = BlockPos.containing(aABB.maxX, aABB.maxY, aABB.maxZ);
        return BlockPos.betweenClosed(blockPos, blockPos2);
    }

    public static Iterable<BlockPos> betweenClosed(BlockPos blockPos, BlockPos blockPos2) {
        return BlockPos.betweenClosed(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()), Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(BlockPos blockPos, BlockPos blockPos2) {
        return StreamSupport.stream(BlockPos.betweenClosed(blockPos, blockPos2).spliterator(), false);
    }

    public static Stream<BlockPos> betweenClosedStream(BoundingBox boundingBox) {
        return BlockPos.betweenClosedStream(Math.min(boundingBox.minX(), boundingBox.maxX()), Math.min(boundingBox.minY(), boundingBox.maxY()), Math.min(boundingBox.minZ(), boundingBox.maxZ()), Math.max(boundingBox.minX(), boundingBox.maxX()), Math.max(boundingBox.minY(), boundingBox.maxY()), Math.max(boundingBox.minZ(), boundingBox.maxZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(AABB aABB) {
        return BlockPos.betweenClosedStream(Mth.floor(aABB.minX), Mth.floor(aABB.minY), Mth.floor(aABB.minZ), Mth.floor(aABB.maxX), Mth.floor(aABB.maxY), Mth.floor(aABB.maxZ));
    }

    public static Stream<BlockPos> betweenClosedStream(int i, int j, int k, int l, int m, int n) {
        return StreamSupport.stream(BlockPos.betweenClosed(i, j, k, l, m, n).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(final int i, final int j, final int k, int l, int m, int n) {
        final int o = l - i + 1;
        final int p = m - j + 1;
        int q = n - k + 1;
        final int r = o * p * q;
        return () -> new AbstractIterator<BlockPos>(){
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int index;

            protected BlockPos computeNext() {
                if (this.index == r) {
                    return (BlockPos)this.endOfData();
                }
                int i2 = this.index % o;
                int j2 = this.index / o;
                int k2 = j2 % p;
                int l = j2 / p;
                ++this.index;
                return this.cursor.set(i + i2, j + k2, k + l);
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<MutableBlockPos> spiralAround(final BlockPos blockPos, final int i, final Direction direction, final Direction direction2) {
        Validate.validState((direction.getAxis() != direction2.getAxis() ? 1 : 0) != 0, (String)"The two directions cannot be on the same axis", (Object[])new Object[0]);
        return () -> new AbstractIterator<MutableBlockPos>(){
            private final Direction[] directions;
            private final MutableBlockPos cursor;
            private final int legs;
            private int leg;
            private int legSize;
            private int legIndex;
            private int lastX;
            private int lastY;
            private int lastZ;
            {
                this.directions = new Direction[]{direction, direction2, direction.getOpposite(), direction2.getOpposite()};
                this.cursor = blockPos.mutable().move(direction2);
                this.legs = 4 * i;
                this.leg = -1;
                this.lastX = this.cursor.getX();
                this.lastY = this.cursor.getY();
                this.lastZ = this.cursor.getZ();
            }

            protected MutableBlockPos computeNext() {
                this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
                this.lastX = this.cursor.getX();
                this.lastY = this.cursor.getY();
                this.lastZ = this.cursor.getZ();
                if (this.legIndex >= this.legSize) {
                    if (this.leg >= this.legs) {
                        return (MutableBlockPos)this.endOfData();
                    }
                    ++this.leg;
                    this.legIndex = 0;
                    this.legSize = this.leg / 2 + 1;
                }
                ++this.legIndex;
                return this.cursor;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static int breadthFirstTraversal(BlockPos blockPos2, int i, int j, BiConsumer<BlockPos, Consumer<BlockPos>> biConsumer, Function<BlockPos, TraversalNodeStatus> function) {
        ArrayDeque<Pair> queue = new ArrayDeque<Pair>();
        LongOpenHashSet longSet = new LongOpenHashSet();
        queue.add(Pair.of((Object)blockPos2, (Object)0));
        int k = 0;
        while (!queue.isEmpty()) {
            TraversalNodeStatus traversalNodeStatus;
            Pair pair = (Pair)queue.poll();
            BlockPos blockPos22 = (BlockPos)pair.getLeft();
            int l = (Integer)pair.getRight();
            long m = blockPos22.asLong();
            if (!longSet.add(m) || (traversalNodeStatus = function.apply(blockPos22)) == TraversalNodeStatus.SKIP) continue;
            if (traversalNodeStatus == TraversalNodeStatus.STOP) break;
            if (++k >= j) {
                return k;
            }
            if (l >= i) continue;
            biConsumer.accept(blockPos22, blockPos -> queue.add(Pair.of((Object)blockPos, (Object)(l + 1))));
        }
        return k;
    }

    public static Iterable<BlockPos> betweenCornersInDirection(AABB aABB, Vec3 vec3) {
        Vec3 vec32 = aABB.getMinPosition();
        int i = Mth.floor(vec32.x());
        int j = Mth.floor(vec32.y());
        int k = Mth.floor(vec32.z());
        Vec3 vec33 = aABB.getMaxPosition();
        int l = Mth.floor(vec33.x());
        int m = Mth.floor(vec33.y());
        int n = Mth.floor(vec33.z());
        return BlockPos.betweenCornersInDirection(i, j, k, l, m, n, vec3);
    }

    public static Iterable<BlockPos> betweenCornersInDirection(BlockPos blockPos, BlockPos blockPos2, Vec3 vec3) {
        return BlockPos.betweenCornersInDirection(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), vec3);
    }

    public static Iterable<BlockPos> betweenCornersInDirection(int i, int j, int k, int l, int m, int n, Vec3 vec3) {
        int o = Math.min(i, l);
        int p = Math.min(j, m);
        int q = Math.min(k, n);
        int r = Math.max(i, l);
        int s = Math.max(j, m);
        int t = Math.max(k, n);
        int u = r - o;
        int v = s - p;
        int w = t - q;
        final int x = vec3.x >= 0.0 ? o : r;
        final int y = vec3.y >= 0.0 ? p : s;
        final int z = vec3.z >= 0.0 ? q : t;
        ImmutableList<Direction.Axis> list = Direction.axisStepOrder(vec3);
        Direction.Axis axis = (Direction.Axis)list.get(0);
        Direction.Axis axis2 = (Direction.Axis)list.get(1);
        Direction.Axis axis3 = (Direction.Axis)list.get(2);
        final Direction direction = vec3.get(axis) >= 0.0 ? axis.getPositive() : axis.getNegative();
        final Direction direction2 = vec3.get(axis2) >= 0.0 ? axis2.getPositive() : axis2.getNegative();
        final Direction direction3 = vec3.get(axis3) >= 0.0 ? axis3.getPositive() : axis3.getNegative();
        final int aa = axis.choose(u, v, w);
        final int ab = axis2.choose(u, v, w);
        final int ac = axis3.choose(u, v, w);
        return () -> new AbstractIterator<BlockPos>(){
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int firstIndex;
            private int secondIndex;
            private int thirdIndex;
            private boolean end;
            private final int firstDirX = direction.getStepX();
            private final int firstDirY = direction.getStepY();
            private final int firstDirZ = direction.getStepZ();
            private final int secondDirX = direction2.getStepX();
            private final int secondDirY = direction2.getStepY();
            private final int secondDirZ = direction2.getStepZ();
            private final int thirdDirX = direction3.getStepX();
            private final int thirdDirY = direction3.getStepY();
            private final int thirdDirZ = direction3.getStepZ();

            protected BlockPos computeNext() {
                if (this.end) {
                    return (BlockPos)this.endOfData();
                }
                this.cursor.set(x + this.firstDirX * this.firstIndex + this.secondDirX * this.secondIndex + this.thirdDirX * this.thirdIndex, y + this.firstDirY * this.firstIndex + this.secondDirY * this.secondIndex + this.thirdDirY * this.thirdIndex, z + this.firstDirZ * this.firstIndex + this.secondDirZ * this.secondIndex + this.thirdDirZ * this.thirdIndex);
                if (this.thirdIndex < ac) {
                    ++this.thirdIndex;
                } else if (this.secondIndex < ab) {
                    ++this.secondIndex;
                    this.thirdIndex = 0;
                } else if (this.firstIndex < aa) {
                    ++this.firstIndex;
                    this.thirdIndex = 0;
                    this.secondIndex = 0;
                } else {
                    this.end = true;
                }
                return this.cursor;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    @Override
    public /* synthetic */ Vec3i cross(Vec3i vec3i) {
        return this.cross(vec3i);
    }

    @Override
    public /* synthetic */ Vec3i relative(Direction.Axis axis, int i) {
        return this.relative(axis, i);
    }

    @Override
    public /* synthetic */ Vec3i relative(Direction direction, int i) {
        return this.relative(direction, i);
    }

    @Override
    public /* synthetic */ Vec3i relative(Direction direction) {
        return this.relative(direction);
    }

    @Override
    public /* synthetic */ Vec3i east(int i) {
        return this.east(i);
    }

    @Override
    public /* synthetic */ Vec3i east() {
        return this.east();
    }

    @Override
    public /* synthetic */ Vec3i west(int i) {
        return this.west(i);
    }

    @Override
    public /* synthetic */ Vec3i west() {
        return this.west();
    }

    @Override
    public /* synthetic */ Vec3i south(int i) {
        return this.south(i);
    }

    @Override
    public /* synthetic */ Vec3i south() {
        return this.south();
    }

    @Override
    public /* synthetic */ Vec3i north(int i) {
        return this.north(i);
    }

    @Override
    public /* synthetic */ Vec3i north() {
        return this.north();
    }

    @Override
    public /* synthetic */ Vec3i below(int i) {
        return this.below(i);
    }

    @Override
    public /* synthetic */ Vec3i below() {
        return this.below();
    }

    @Override
    public /* synthetic */ Vec3i above(int i) {
        return this.above(i);
    }

    @Override
    public /* synthetic */ Vec3i above() {
        return this.above();
    }

    @Override
    public /* synthetic */ Vec3i multiply(int i) {
        return this.multiply(i);
    }

    @Override
    public /* synthetic */ Vec3i subtract(Vec3i vec3i) {
        return this.subtract(vec3i);
    }

    @Override
    public /* synthetic */ Vec3i offset(Vec3i vec3i) {
        return this.offset(vec3i);
    }

    @Override
    public /* synthetic */ Vec3i offset(int i, int j, int k) {
        return this.offset(i, j, k);
    }

    public static class MutableBlockPos
    extends BlockPos {
        public MutableBlockPos() {
            this(0, 0, 0);
        }

        public MutableBlockPos(int i, int j, int k) {
            super(i, j, k);
        }

        public MutableBlockPos(double d, double e, double f) {
            this(Mth.floor(d), Mth.floor(e), Mth.floor(f));
        }

        @Override
        public BlockPos offset(int i, int j, int k) {
            return super.offset(i, j, k).immutable();
        }

        @Override
        public BlockPos multiply(int i) {
            return super.multiply(i).immutable();
        }

        @Override
        public BlockPos relative(Direction direction, int i) {
            return super.relative(direction, i).immutable();
        }

        @Override
        public BlockPos relative(Direction.Axis axis, int i) {
            return super.relative(axis, i).immutable();
        }

        @Override
        public BlockPos rotate(Rotation rotation) {
            return super.rotate(rotation).immutable();
        }

        public MutableBlockPos set(int i, int j, int k) {
            this.setX(i);
            this.setY(j);
            this.setZ(k);
            return this;
        }

        public MutableBlockPos set(double d, double e, double f) {
            return this.set(Mth.floor(d), Mth.floor(e), Mth.floor(f));
        }

        public MutableBlockPos set(Vec3i vec3i) {
            return this.set(vec3i.getX(), vec3i.getY(), vec3i.getZ());
        }

        public MutableBlockPos set(long l) {
            return this.set(MutableBlockPos.getX(l), MutableBlockPos.getY(l), MutableBlockPos.getZ(l));
        }

        public MutableBlockPos set(AxisCycle axisCycle, int i, int j, int k) {
            return this.set(axisCycle.cycle(i, j, k, Direction.Axis.X), axisCycle.cycle(i, j, k, Direction.Axis.Y), axisCycle.cycle(i, j, k, Direction.Axis.Z));
        }

        public MutableBlockPos setWithOffset(Vec3i vec3i, Direction direction) {
            return this.set(vec3i.getX() + direction.getStepX(), vec3i.getY() + direction.getStepY(), vec3i.getZ() + direction.getStepZ());
        }

        public MutableBlockPos setWithOffset(Vec3i vec3i, int i, int j, int k) {
            return this.set(vec3i.getX() + i, vec3i.getY() + j, vec3i.getZ() + k);
        }

        public MutableBlockPos setWithOffset(Vec3i vec3i, Vec3i vec3i2) {
            return this.set(vec3i.getX() + vec3i2.getX(), vec3i.getY() + vec3i2.getY(), vec3i.getZ() + vec3i2.getZ());
        }

        public MutableBlockPos move(Direction direction) {
            return this.move(direction, 1);
        }

        public MutableBlockPos move(Direction direction, int i) {
            return this.set(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
        }

        public MutableBlockPos move(int i, int j, int k) {
            return this.set(this.getX() + i, this.getY() + j, this.getZ() + k);
        }

        public MutableBlockPos move(Vec3i vec3i) {
            return this.set(this.getX() + vec3i.getX(), this.getY() + vec3i.getY(), this.getZ() + vec3i.getZ());
        }

        public MutableBlockPos clamp(Direction.Axis axis, int i, int j) {
            return switch (axis) {
                default -> throw new MatchException(null, null);
                case Direction.Axis.X -> this.set(Mth.clamp(this.getX(), i, j), this.getY(), this.getZ());
                case Direction.Axis.Y -> this.set(this.getX(), Mth.clamp(this.getY(), i, j), this.getZ());
                case Direction.Axis.Z -> this.set(this.getX(), this.getY(), Mth.clamp(this.getZ(), i, j));
            };
        }

        @Override
        public MutableBlockPos setX(int i) {
            super.setX(i);
            return this;
        }

        @Override
        public MutableBlockPos setY(int i) {
            super.setY(i);
            return this;
        }

        @Override
        public MutableBlockPos setZ(int i) {
            super.setZ(i);
            return this;
        }

        @Override
        public BlockPos immutable() {
            return new BlockPos(this);
        }

        @Override
        public /* synthetic */ Vec3i cross(Vec3i vec3i) {
            return super.cross(vec3i);
        }

        @Override
        public /* synthetic */ Vec3i relative(Direction.Axis axis, int i) {
            return this.relative(axis, i);
        }

        @Override
        public /* synthetic */ Vec3i relative(Direction direction, int i) {
            return this.relative(direction, i);
        }

        @Override
        public /* synthetic */ Vec3i relative(Direction direction) {
            return super.relative(direction);
        }

        @Override
        public /* synthetic */ Vec3i east(int i) {
            return super.east(i);
        }

        @Override
        public /* synthetic */ Vec3i east() {
            return super.east();
        }

        @Override
        public /* synthetic */ Vec3i west(int i) {
            return super.west(i);
        }

        @Override
        public /* synthetic */ Vec3i west() {
            return super.west();
        }

        @Override
        public /* synthetic */ Vec3i south(int i) {
            return super.south(i);
        }

        @Override
        public /* synthetic */ Vec3i south() {
            return super.south();
        }

        @Override
        public /* synthetic */ Vec3i north(int i) {
            return super.north(i);
        }

        @Override
        public /* synthetic */ Vec3i north() {
            return super.north();
        }

        @Override
        public /* synthetic */ Vec3i below(int i) {
            return super.below(i);
        }

        @Override
        public /* synthetic */ Vec3i below() {
            return super.below();
        }

        @Override
        public /* synthetic */ Vec3i above(int i) {
            return super.above(i);
        }

        @Override
        public /* synthetic */ Vec3i above() {
            return super.above();
        }

        @Override
        public /* synthetic */ Vec3i multiply(int i) {
            return this.multiply(i);
        }

        @Override
        public /* synthetic */ Vec3i subtract(Vec3i vec3i) {
            return super.subtract(vec3i);
        }

        @Override
        public /* synthetic */ Vec3i offset(Vec3i vec3i) {
            return super.offset(vec3i);
        }

        @Override
        public /* synthetic */ Vec3i offset(int i, int j, int k) {
            return this.offset(i, j, k);
        }

        @Override
        public /* synthetic */ Vec3i setZ(int i) {
            return this.setZ(i);
        }

        @Override
        public /* synthetic */ Vec3i setY(int i) {
            return this.setY(i);
        }

        @Override
        public /* synthetic */ Vec3i setX(int i) {
            return this.setX(i);
        }
    }

    public static enum TraversalNodeStatus {
        ACCEPT,
        SKIP,
        STOP;

    }
}


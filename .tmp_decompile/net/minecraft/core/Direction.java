/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Iterators
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  io.netty.buffer.ByteBuf
 *  java.lang.MatchException
 *  org.jetbrains.annotations.Contract
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public enum Direction implements StringRepresentable
{
    DOWN(0, 1, -1, "down", AxisDirection.NEGATIVE, Axis.Y, new Vec3i(0, -1, 0)),
    UP(1, 0, -1, "up", AxisDirection.POSITIVE, Axis.Y, new Vec3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", AxisDirection.NEGATIVE, Axis.Z, new Vec3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", AxisDirection.POSITIVE, Axis.Z, new Vec3i(0, 0, 1)),
    WEST(4, 5, 1, "west", AxisDirection.NEGATIVE, Axis.X, new Vec3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", AxisDirection.POSITIVE, Axis.X, new Vec3i(1, 0, 0));

    public static final StringRepresentable.EnumCodec<Direction> CODEC;
    public static final Codec<Direction> VERTICAL_CODEC;
    public static final IntFunction<Direction> BY_ID;
    public static final StreamCodec<ByteBuf, Direction> STREAM_CODEC;
    @Deprecated
    public static final Codec<Direction> LEGACY_ID_CODEC;
    @Deprecated
    public static final Codec<Direction> LEGACY_ID_CODEC_2D;
    private static final ImmutableList<Axis> YXZ_AXIS_ORDER;
    private static final ImmutableList<Axis> YZX_AXIS_ORDER;
    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;
    private final String name;
    private final Axis axis;
    private final AxisDirection axisDirection;
    private final Vec3i normal;
    private final Vec3 normalVec3;
    private final Vector3fc normalVec3f;
    private static final Direction[] VALUES;
    private static final Direction[] BY_3D_DATA;
    private static final Direction[] BY_2D_DATA;

    private Direction(int j, int k, int l, String string2, AxisDirection axisDirection, Axis axis, Vec3i vec3i) {
        this.data3d = j;
        this.data2d = l;
        this.oppositeIndex = k;
        this.name = string2;
        this.axis = axis;
        this.axisDirection = axisDirection;
        this.normal = vec3i;
        this.normalVec3 = Vec3.atLowerCornerOf(vec3i);
        this.normalVec3f = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
    }

    public static Direction[] orderedByNearest(Entity entity) {
        Direction direction3;
        float f = entity.getViewXRot(1.0f) * ((float)Math.PI / 180);
        float g = -entity.getViewYRot(1.0f) * ((float)Math.PI / 180);
        float h = Mth.sin(f);
        float i = Mth.cos(f);
        float j = Mth.sin(g);
        float k = Mth.cos(g);
        boolean bl = j > 0.0f;
        boolean bl2 = h < 0.0f;
        boolean bl3 = k > 0.0f;
        float l = bl ? j : -j;
        float m = bl2 ? -h : h;
        float n = bl3 ? k : -k;
        float o = l * i;
        float p = n * i;
        Direction direction = bl ? EAST : WEST;
        Direction direction2 = bl2 ? UP : DOWN;
        Direction direction4 = direction3 = bl3 ? SOUTH : NORTH;
        if (l > n) {
            if (m > o) {
                return Direction.makeDirectionArray(direction2, direction, direction3);
            }
            if (p > m) {
                return Direction.makeDirectionArray(direction, direction3, direction2);
            }
            return Direction.makeDirectionArray(direction, direction2, direction3);
        }
        if (m > p) {
            return Direction.makeDirectionArray(direction2, direction3, direction);
        }
        if (o > m) {
            return Direction.makeDirectionArray(direction3, direction, direction2);
        }
        return Direction.makeDirectionArray(direction3, direction2, direction);
    }

    private static Direction[] makeDirectionArray(Direction direction, Direction direction2, Direction direction3) {
        return new Direction[]{direction, direction2, direction3, direction3.getOpposite(), direction2.getOpposite(), direction.getOpposite()};
    }

    public static Direction rotate(Matrix4fc matrix4fc, Direction direction) {
        Vector3f vector3f = matrix4fc.transformDirection(direction.normalVec3f, new Vector3f());
        return Direction.getApproximateNearest(vector3f.x(), vector3f.y(), vector3f.z());
    }

    public static Collection<Direction> allShuffled(RandomSource randomSource) {
        return Util.shuffledCopy(Direction.values(), randomSource);
    }

    public static Stream<Direction> stream() {
        return Stream.of(VALUES);
    }

    public static float getYRot(Direction direction) {
        return switch (direction.ordinal()) {
            case 2 -> 180.0f;
            case 3 -> 0.0f;
            case 4 -> 90.0f;
            case 5 -> -90.0f;
            default -> throw new IllegalStateException("No y-Rot for vertical axis: " + String.valueOf(direction));
        };
    }

    public Quaternionf getRotation() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> new Quaternionf().rotationX((float)Math.PI);
            case 1 -> new Quaternionf();
            case 2 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, (float)Math.PI);
            case 3 -> new Quaternionf().rotationX(1.5707964f);
            case 4 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, 1.5707964f);
            case 5 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, -1.5707964f);
        };
    }

    public int get3DDataValue() {
        return this.data3d;
    }

    public int get2DDataValue() {
        return this.data2d;
    }

    public AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public static Direction getFacingAxis(Entity entity, Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (EAST.isFacingAngle(entity.getViewYRot(1.0f))) {
                    yield EAST;
                }
                yield WEST;
            }
            case 2 -> {
                if (SOUTH.isFacingAngle(entity.getViewYRot(1.0f))) {
                    yield SOUTH;
                }
                yield NORTH;
            }
            case 1 -> entity.getViewXRot(1.0f) < 0.0f ? UP : DOWN;
        };
    }

    public Direction getOpposite() {
        return Direction.from3DDataValue(this.oppositeIndex);
    }

    public Direction getClockWise(Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this == WEST || this == EAST) {
                    yield this;
                }
                yield this.getClockWiseX();
            }
            case 1 -> {
                if (this == UP || this == DOWN) {
                    yield this;
                }
                yield this.getClockWise();
            }
            case 2 -> this == NORTH || this == SOUTH ? this : this.getClockWiseZ();
        };
    }

    public Direction getCounterClockWise(Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this == WEST || this == EAST) {
                    yield this;
                }
                yield this.getCounterClockWiseX();
            }
            case 1 -> {
                if (this == UP || this == DOWN) {
                    yield this;
                }
                yield this.getCounterClockWise();
            }
            case 2 -> this == NORTH || this == SOUTH ? this : this.getCounterClockWiseZ();
        };
    }

    public Direction getClockWise() {
        return switch (this.ordinal()) {
            case 2 -> EAST;
            case 5 -> SOUTH;
            case 3 -> WEST;
            case 4 -> NORTH;
            default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getClockWiseX() {
        return switch (this.ordinal()) {
            case 1 -> NORTH;
            case 2 -> DOWN;
            case 0 -> SOUTH;
            case 3 -> UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getCounterClockWiseX() {
        return switch (this.ordinal()) {
            case 1 -> SOUTH;
            case 3 -> DOWN;
            case 0 -> NORTH;
            case 2 -> UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getClockWiseZ() {
        return switch (this.ordinal()) {
            case 1 -> EAST;
            case 5 -> DOWN;
            case 0 -> WEST;
            case 4 -> UP;
            default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction getCounterClockWiseZ() {
        return switch (this.ordinal()) {
            case 1 -> WEST;
            case 4 -> DOWN;
            case 0 -> EAST;
            case 5 -> UP;
            default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + String.valueOf(this));
        };
    }

    public Direction getCounterClockWise() {
        return switch (this.ordinal()) {
            case 2 -> WEST;
            case 5 -> NORTH;
            case 3 -> EAST;
            case 4 -> SOUTH;
            default -> throw new IllegalStateException("Unable to get CCW facing of " + String.valueOf(this));
        };
    }

    public int getStepX() {
        return this.normal.getX();
    }

    public int getStepY() {
        return this.normal.getY();
    }

    public int getStepZ() {
        return this.normal.getZ();
    }

    public Vector3f step() {
        return new Vector3f(this.normalVec3f);
    }

    public String getName() {
        return this.name;
    }

    public Axis getAxis() {
        return this.axis;
    }

    public static @Nullable Direction byName(String string) {
        return CODEC.byName(string);
    }

    public static Direction from3DDataValue(int i) {
        return BY_3D_DATA[Mth.abs(i % BY_3D_DATA.length)];
    }

    public static Direction from2DDataValue(int i) {
        return BY_2D_DATA[Mth.abs(i % BY_2D_DATA.length)];
    }

    public static Direction fromYRot(double d) {
        return Direction.from2DDataValue(Mth.floor(d / 90.0 + 0.5) & 3);
    }

    public static Direction fromAxisAndDirection(Axis axis, AxisDirection axisDirection) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (axisDirection == AxisDirection.POSITIVE) {
                    yield EAST;
                }
                yield WEST;
            }
            case 1 -> {
                if (axisDirection == AxisDirection.POSITIVE) {
                    yield UP;
                }
                yield DOWN;
            }
            case 2 -> axisDirection == AxisDirection.POSITIVE ? SOUTH : NORTH;
        };
    }

    public float toYRot() {
        return (this.data2d & 3) * 90;
    }

    public static Direction getRandom(RandomSource randomSource) {
        return Util.getRandom(VALUES, randomSource);
    }

    public static Direction getApproximateNearest(double d, double e, double f) {
        return Direction.getApproximateNearest((float)d, (float)e, (float)f);
    }

    public static Direction getApproximateNearest(float f, float g, float h) {
        Direction direction = NORTH;
        float i = Float.MIN_VALUE;
        for (Direction direction2 : VALUES) {
            float j = f * (float)direction2.normal.getX() + g * (float)direction2.normal.getY() + h * (float)direction2.normal.getZ();
            if (!(j > i)) continue;
            i = j;
            direction = direction2;
        }
        return direction;
    }

    public static Direction getApproximateNearest(Vec3 vec3) {
        return Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z);
    }

    @Contract(value="_,_,_,!null->!null;_,_,_,_->_")
    public static @Nullable Direction getNearest(int i, int j, int k, @Nullable Direction direction) {
        int l = Math.abs(i);
        int m = Math.abs(j);
        int n = Math.abs(k);
        if (l > n && l > m) {
            return i < 0 ? WEST : EAST;
        }
        if (n > l && n > m) {
            return k < 0 ? NORTH : SOUTH;
        }
        if (m > l && m > n) {
            return j < 0 ? DOWN : UP;
        }
        return direction;
    }

    @Contract(value="_,!null->!null;_,_->_")
    public static @Nullable Direction getNearest(Vec3i vec3i, @Nullable Direction direction) {
        return Direction.getNearest(vec3i.getX(), vec3i.getY(), vec3i.getZ(), direction);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    private static DataResult<Direction> verifyVertical(Direction direction) {
        return direction.getAxis().isVertical() ? DataResult.success((Object)direction) : DataResult.error(() -> "Expected a vertical direction");
    }

    public static Direction get(AxisDirection axisDirection, Axis axis) {
        for (Direction direction : VALUES) {
            if (direction.getAxisDirection() != axisDirection || direction.getAxis() != axis) continue;
            return direction;
        }
        throw new IllegalArgumentException("No such direction: " + String.valueOf((Object)axisDirection) + " " + String.valueOf(axis));
    }

    public static ImmutableList<Axis> axisStepOrder(Vec3 vec3) {
        if (Math.abs(vec3.x) < Math.abs(vec3.z)) {
            return YZX_AXIS_ORDER;
        }
        return YXZ_AXIS_ORDER;
    }

    public Vec3i getUnitVec3i() {
        return this.normal;
    }

    public Vec3 getUnitVec3() {
        return this.normalVec3;
    }

    public Vector3fc getUnitVec3f() {
        return this.normalVec3f;
    }

    public boolean isFacingAngle(float f) {
        float g = f * ((float)Math.PI / 180);
        float h = -Mth.sin(g);
        float i = Mth.cos(g);
        return (float)this.normal.getX() * h + (float)this.normal.getZ() * i > 0.0f;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Direction::values);
        VERTICAL_CODEC = CODEC.validate(Direction::verifyVertical);
        BY_ID = ByIdMap.continuous(Direction::get3DDataValue, Direction.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Direction::get3DDataValue);
        LEGACY_ID_CODEC = Codec.BYTE.xmap(Direction::from3DDataValue, direction -> (byte)direction.get3DDataValue());
        LEGACY_ID_CODEC_2D = Codec.BYTE.xmap(Direction::from2DDataValue, direction -> (byte)direction.get2DDataValue());
        YXZ_AXIS_ORDER = ImmutableList.of((Object)Axis.Y, (Object)Axis.X, (Object)Axis.Z);
        YZX_AXIS_ORDER = ImmutableList.of((Object)Axis.Y, (Object)Axis.Z, (Object)Axis.X);
        VALUES = Direction.values();
        BY_3D_DATA = (Direction[])Arrays.stream(VALUES).sorted(Comparator.comparingInt(direction -> direction.data3d)).toArray(Direction[]::new);
        BY_2D_DATA = (Direction[])Arrays.stream(VALUES).filter(direction -> direction.getAxis().isHorizontal()).sorted(Comparator.comparingInt(direction -> direction.data2d)).toArray(Direction[]::new);
    }

    public static enum Axis implements StringRepresentable,
    Predicate<Direction>
    {
        X("x"){

            @Override
            public int choose(int i, int j, int k) {
                return i;
            }

            @Override
            public boolean choose(boolean bl, boolean bl2, boolean bl3) {
                return bl;
            }

            @Override
            public double choose(double d, double e, double f) {
                return d;
            }

            @Override
            public Direction getPositive() {
                return EAST;
            }

            @Override
            public Direction getNegative() {
                return WEST;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        }
        ,
        Y("y"){

            @Override
            public int choose(int i, int j, int k) {
                return j;
            }

            @Override
            public double choose(double d, double e, double f) {
                return e;
            }

            @Override
            public boolean choose(boolean bl, boolean bl2, boolean bl3) {
                return bl2;
            }

            @Override
            public Direction getPositive() {
                return UP;
            }

            @Override
            public Direction getNegative() {
                return DOWN;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        }
        ,
        Z("z"){

            @Override
            public int choose(int i, int j, int k) {
                return k;
            }

            @Override
            public double choose(double d, double e, double f) {
                return f;
            }

            @Override
            public boolean choose(boolean bl, boolean bl2, boolean bl3) {
                return bl3;
            }

            @Override
            public Direction getPositive() {
                return SOUTH;
            }

            @Override
            public Direction getNegative() {
                return NORTH;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        };

        public static final Axis[] VALUES;
        public static final StringRepresentable.EnumCodec<Axis> CODEC;
        private final String name;

        Axis(String string2) {
            this.name = string2;
        }

        public static @Nullable Axis byName(String string) {
            return CODEC.byName(string);
        }

        public String getName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == Y;
        }

        public boolean isHorizontal() {
            return this == X || this == Z;
        }

        public abstract Direction getPositive();

        public abstract Direction getNegative();

        public Direction[] getDirections() {
            return new Direction[]{this.getPositive(), this.getNegative()};
        }

        public String toString() {
            return this.name;
        }

        public static Axis getRandom(RandomSource randomSource) {
            return Util.getRandom(VALUES, randomSource);
        }

        @Override
        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.getAxis() == this;
        }

        public Plane getPlane() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2 -> Plane.HORIZONTAL;
                case 1 -> Plane.VERTICAL;
            };
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public abstract int choose(int var1, int var2, int var3);

        public abstract double choose(double var1, double var3, double var5);

        public abstract boolean choose(boolean var1, boolean var2, boolean var3);

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Direction)object);
        }

        static {
            VALUES = Axis.values();
            CODEC = StringRepresentable.fromEnum(Axis::values);
        }
    }

    public static enum AxisDirection {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int step;
        private final String name;

        private AxisDirection(int j, String string2) {
            this.step = j;
            this.name = string2;
        }

        public int getStep() {
            return this.step;
        }

        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }

        public AxisDirection opposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }
    }

    public static enum Plane implements Iterable<Direction>,
    Predicate<Direction>
    {
        HORIZONTAL(new Direction[]{NORTH, EAST, SOUTH, WEST}, new Axis[]{Axis.X, Axis.Z}),
        VERTICAL(new Direction[]{UP, DOWN}, new Axis[]{Axis.Y});

        private final Direction[] faces;
        private final Axis[] axis;

        private Plane(Direction[] directions, Axis[] axiss) {
            this.faces = directions;
            this.axis = axiss;
        }

        public Direction getRandomDirection(RandomSource randomSource) {
            return Util.getRandom(this.faces, randomSource);
        }

        public Axis getRandomAxis(RandomSource randomSource) {
            return Util.getRandom(this.axis, randomSource);
        }

        @Override
        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.getAxis().getPlane() == this;
        }

        @Override
        public Iterator<Direction> iterator() {
            return Iterators.forArray((Object[])this.faces);
        }

        public Stream<Direction> stream() {
            return Arrays.stream(this.faces);
        }

        public List<Direction> shuffledCopy(RandomSource randomSource) {
            return Util.shuffledCopy(this.faces, randomSource);
        }

        public int length() {
            return this.faces.length;
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Direction)object);
        }
    }
}


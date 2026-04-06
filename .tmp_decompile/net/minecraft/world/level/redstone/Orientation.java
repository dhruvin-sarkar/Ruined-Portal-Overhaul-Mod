/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.level.redstone;

import com.google.common.annotations.VisibleForTesting;
import io.netty.buffer.ByteBuf;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

public class Orientation {
    public static final StreamCodec<ByteBuf, Orientation> STREAM_CODEC = ByteBufCodecs.idMapper(Orientation::fromIndex, Orientation::getIndex);
    private static final Orientation[] ORIENTATIONS = Util.make(() -> {
        Orientation[] orientations = new Orientation[48];
        Orientation.generateContext(new Orientation(Direction.UP, Direction.NORTH, SideBias.LEFT), orientations);
        return orientations;
    });
    private final Direction up;
    private final Direction front;
    private final Direction side;
    private final SideBias sideBias;
    private final int index;
    private final List<Direction> neighbors;
    private final List<Direction> horizontalNeighbors;
    private final List<Direction> verticalNeighbors;
    private final Map<Direction, Orientation> withFront = new EnumMap<Direction, Orientation>(Direction.class);
    private final Map<Direction, Orientation> withUp = new EnumMap<Direction, Orientation>(Direction.class);
    private final Map<SideBias, Orientation> withSideBias = new EnumMap<SideBias, Orientation>(SideBias.class);

    private Orientation(Direction direction2, Direction direction22, SideBias sideBias) {
        this.up = direction2;
        this.front = direction22;
        this.sideBias = sideBias;
        this.index = Orientation.generateIndex(direction2, direction22, sideBias);
        Vec3i vec3i = direction22.getUnitVec3i().cross(direction2.getUnitVec3i());
        Direction direction3 = Direction.getNearest(vec3i, null);
        Objects.requireNonNull(direction3);
        this.side = this.sideBias == SideBias.RIGHT ? direction3 : direction3.getOpposite();
        this.neighbors = List.of((Object)this.front.getOpposite(), (Object)this.front, (Object)this.side, (Object)this.side.getOpposite(), (Object)this.up.getOpposite(), (Object)this.up);
        this.horizontalNeighbors = this.neighbors.stream().filter(direction -> direction.getAxis() != this.up.getAxis()).toList();
        this.verticalNeighbors = this.neighbors.stream().filter(direction -> direction.getAxis() == this.up.getAxis()).toList();
    }

    public static Orientation of(Direction direction, Direction direction2, SideBias sideBias) {
        return ORIENTATIONS[Orientation.generateIndex(direction, direction2, sideBias)];
    }

    public Orientation withUp(Direction direction) {
        return this.withUp.get(direction);
    }

    public Orientation withFront(Direction direction) {
        return this.withFront.get(direction);
    }

    public Orientation withFrontPreserveUp(Direction direction) {
        if (direction.getAxis() == this.up.getAxis()) {
            return this;
        }
        return this.withFront.get(direction);
    }

    public Orientation withFrontAdjustSideBias(Direction direction) {
        Orientation orientation = this.withFront(direction);
        if (this.front == orientation.side) {
            return orientation.withMirror();
        }
        return orientation;
    }

    public Orientation withSideBias(SideBias sideBias) {
        return this.withSideBias.get((Object)sideBias);
    }

    public Orientation withMirror() {
        return this.withSideBias(this.sideBias.getOpposite());
    }

    public Direction getFront() {
        return this.front;
    }

    public Direction getUp() {
        return this.up;
    }

    public Direction getSide() {
        return this.side;
    }

    public SideBias getSideBias() {
        return this.sideBias;
    }

    public List<Direction> getDirections() {
        return this.neighbors;
    }

    public List<Direction> getHorizontalDirections() {
        return this.horizontalNeighbors;
    }

    public List<Direction> getVerticalDirections() {
        return this.verticalNeighbors;
    }

    public String toString() {
        return "[up=" + String.valueOf(this.up) + ",front=" + String.valueOf(this.front) + ",sideBias=" + String.valueOf((Object)this.sideBias) + "]";
    }

    public int getIndex() {
        return this.index;
    }

    public static Orientation fromIndex(int i) {
        return ORIENTATIONS[i];
    }

    public static Orientation random(RandomSource randomSource) {
        return Util.getRandom(ORIENTATIONS, randomSource);
    }

    private static Orientation generateContext(Orientation orientation, Orientation[] orientations) {
        Direction direction2;
        if (orientations[orientation.getIndex()] != null) {
            return orientations[orientation.getIndex()];
        }
        orientations[orientation.getIndex()] = orientation;
        for (SideBias sideBias : SideBias.values()) {
            orientation.withSideBias.put(sideBias, Orientation.generateContext(new Orientation(orientation.up, orientation.front, sideBias), orientations));
        }
        for (Enum enum_ : Direction.values()) {
            direction2 = orientation.up;
            if (enum_ == orientation.up) {
                direction2 = orientation.front.getOpposite();
            }
            if (enum_ == orientation.up.getOpposite()) {
                direction2 = orientation.front;
            }
            orientation.withFront.put((Direction)enum_, Orientation.generateContext(new Orientation(direction2, (Direction)enum_, orientation.sideBias), orientations));
        }
        for (Enum enum_ : Direction.values()) {
            direction2 = orientation.front;
            if (enum_ == orientation.front) {
                direction2 = orientation.up.getOpposite();
            }
            if (enum_ == orientation.front.getOpposite()) {
                direction2 = orientation.up;
            }
            orientation.withUp.put((Direction)enum_, Orientation.generateContext(new Orientation((Direction)enum_, direction2, orientation.sideBias), orientations));
        }
        return orientation;
    }

    @VisibleForTesting
    protected static int generateIndex(Direction direction, Direction direction2, SideBias sideBias) {
        if (direction.getAxis() == direction2.getAxis()) {
            throw new IllegalStateException("Up-vector and front-vector can not be on the same axis");
        }
        int i = direction.getAxis() == Direction.Axis.Y ? (direction2.getAxis() == Direction.Axis.X ? 1 : 0) : (direction2.getAxis() == Direction.Axis.Y ? 1 : 0);
        int j = i << 1 | direction2.getAxisDirection().ordinal();
        return ((direction.ordinal() << 2) + j << 1) + sideBias.ordinal();
    }

    public static enum SideBias {
        LEFT("left"),
        RIGHT("right");

        private final String name;

        private SideBias(String string2) {
            this.name = string2;
        }

        public SideBias getOpposite() {
            return this == LEFT ? RIGHT : LEFT;
        }

        public String toString() {
            return this.name;
        }
    }
}


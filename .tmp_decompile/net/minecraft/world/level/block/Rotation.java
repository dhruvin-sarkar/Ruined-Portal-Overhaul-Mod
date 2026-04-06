/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  java.lang.MatchException
 */
package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;

public enum Rotation implements StringRepresentable
{
    NONE(0, "none", OctahedralGroup.IDENTITY),
    CLOCKWISE_90(1, "clockwise_90", OctahedralGroup.ROT_90_Y_NEG),
    CLOCKWISE_180(2, "180", OctahedralGroup.ROT_180_FACE_XZ),
    COUNTERCLOCKWISE_90(3, "counterclockwise_90", OctahedralGroup.ROT_90_Y_POS);

    public static final IntFunction<Rotation> BY_ID;
    public static final Codec<Rotation> CODEC;
    public static final StreamCodec<ByteBuf, Rotation> STREAM_CODEC;
    @Deprecated
    public static final Codec<Rotation> LEGACY_CODEC;
    private final int index;
    private final String id;
    private final OctahedralGroup rotation;

    private Rotation(int j, String string2, OctahedralGroup octahedralGroup) {
        this.index = j;
        this.id = string2;
        this.rotation = octahedralGroup;
    }

    public Rotation getRotated(Rotation rotation) {
        return switch (rotation.ordinal()) {
            case 2 -> {
                switch (this.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield CLOCKWISE_180;
                    }
                    case 1: {
                        yield COUNTERCLOCKWISE_90;
                    }
                    case 2: {
                        yield NONE;
                    }
                    case 3: 
                }
                yield CLOCKWISE_90;
            }
            case 3 -> {
                switch (this.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield COUNTERCLOCKWISE_90;
                    }
                    case 1: {
                        yield NONE;
                    }
                    case 2: {
                        yield CLOCKWISE_90;
                    }
                    case 3: 
                }
                yield CLOCKWISE_180;
            }
            case 1 -> {
                switch (this.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield CLOCKWISE_90;
                    }
                    case 1: {
                        yield CLOCKWISE_180;
                    }
                    case 2: {
                        yield COUNTERCLOCKWISE_90;
                    }
                    case 3: 
                }
                yield NONE;
            }
            default -> this;
        };
    }

    public OctahedralGroup rotation() {
        return this.rotation;
    }

    public Direction rotate(Direction direction) {
        if (direction.getAxis() == Direction.Axis.Y) {
            return direction;
        }
        return switch (this.ordinal()) {
            case 2 -> direction.getOpposite();
            case 3 -> direction.getCounterClockWise();
            case 1 -> direction.getClockWise();
            default -> direction;
        };
    }

    public int rotate(int i, int j) {
        return switch (this.ordinal()) {
            case 2 -> (i + j / 2) % j;
            case 3 -> (i + j * 3 / 4) % j;
            case 1 -> (i + j / 4) % j;
            default -> i;
        };
    }

    public static Rotation getRandom(RandomSource randomSource) {
        return Util.getRandom(Rotation.values(), randomSource);
    }

    public static List<Rotation> getShuffled(RandomSource randomSource) {
        return Util.shuffledCopy(Rotation.values(), randomSource);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    private int getIndex() {
        return this.index;
    }

    static {
        BY_ID = ByIdMap.continuous(Rotation::getIndex, Rotation.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        CODEC = StringRepresentable.fromEnum(Rotation::values);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Rotation::getIndex);
        LEGACY_CODEC = ExtraCodecs.legacyEnum(Rotation::valueOf);
    }
}


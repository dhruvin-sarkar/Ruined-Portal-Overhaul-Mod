/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  java.lang.MatchException
 */
package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum HumanoidArm implements StringRepresentable
{
    LEFT(0, "left", "options.mainHand.left"),
    RIGHT(1, "right", "options.mainHand.right");

    public static final Codec<HumanoidArm> CODEC;
    private static final IntFunction<HumanoidArm> BY_ID;
    public static final StreamCodec<ByteBuf, HumanoidArm> STREAM_CODEC;
    private final int id;
    private final String name;
    private final Component caption;

    private HumanoidArm(int j, String string2, String string3) {
        this.id = j;
        this.name = string2;
        this.caption = Component.translatable(string3);
    }

    public HumanoidArm getOpposite() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> RIGHT;
            case 1 -> LEFT;
        };
    }

    public Component caption() {
        return this.caption;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromEnum(HumanoidArm::values);
        BY_ID = ByIdMap.continuous(humanoidArm -> humanoidArm.id, HumanoidArm.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, humanoidArm -> humanoidArm.id);
    }
}


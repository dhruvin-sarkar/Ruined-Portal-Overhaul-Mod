/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;

public class VarInt {
    public static final int MAX_VARINT_SIZE = 5;
    private static final int DATA_BITS_MASK = 127;
    private static final int CONTINUATION_BIT_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    public static int getByteSize(int i) {
        for (int j = 1; j < 5; ++j) {
            if ((i & -1 << j * 7) != 0) continue;
            return j;
        }
        return 5;
    }

    public static boolean hasContinuationBit(byte b) {
        return (b & 0x80) == 128;
    }

    public static int read(ByteBuf byteBuf) {
        byte b;
        int i = 0;
        int j = 0;
        do {
            b = byteBuf.readByte();
            i |= (b & 0x7F) << j++ * 7;
            if (j <= 5) continue;
            throw new RuntimeException("VarInt too big");
        } while (VarInt.hasContinuationBit(b));
        return i;
    }

    public static ByteBuf write(ByteBuf byteBuf, int i) {
        while (true) {
            if ((i & 0xFFFFFF80) == 0) {
                byteBuf.writeByte(i);
                return byteBuf;
            }
            byteBuf.writeByte(i & 0x7F | 0x80);
            i >>>= 7;
        }
    }
}


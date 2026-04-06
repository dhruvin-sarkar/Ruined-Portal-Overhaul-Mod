/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufUtil
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.nio.charset.StandardCharsets;
import net.minecraft.network.VarInt;

public class Utf8String {
    public static String read(ByteBuf byteBuf, int i) {
        int j = ByteBufUtil.utf8MaxBytes((int)i);
        int k = VarInt.read(byteBuf);
        if (k > j) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + k + " > " + j + ")");
        }
        if (k < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        int l = byteBuf.readableBytes();
        if (k > l) {
            throw new DecoderException("Not enough bytes in buffer, expected " + k + ", but got " + l);
        }
        String string = byteBuf.toString(byteBuf.readerIndex(), k, StandardCharsets.UTF_8);
        byteBuf.readerIndex(byteBuf.readerIndex() + k);
        if (string.length() > i) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + string.length() + " > " + i + ")");
        }
        return string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void write(ByteBuf byteBuf, CharSequence charSequence, int i) {
        if (charSequence.length() > i) {
            throw new EncoderException("String too big (was " + charSequence.length() + " characters, max " + i + ")");
        }
        int j = ByteBufUtil.utf8MaxBytes((CharSequence)charSequence);
        ByteBuf byteBuf2 = byteBuf.alloc().buffer(j);
        try {
            int k = ByteBufUtil.writeUtf8((ByteBuf)byteBuf2, (CharSequence)charSequence);
            int l = ByteBufUtil.utf8MaxBytes((int)i);
            if (k > l) {
                throw new EncoderException("String too big (was " + k + " bytes encoded, max " + l + ")");
            }
            VarInt.write(byteBuf, k);
            byteBuf.writeBytes(byteBuf2);
        }
        finally {
            byteBuf2.release();
        }
    }
}


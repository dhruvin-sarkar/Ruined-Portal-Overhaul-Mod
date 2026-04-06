/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufUtil
 *  io.netty.util.ReferenceCounted
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCounted;

public record HiddenByteBuf(ByteBuf contents) implements ReferenceCounted
{
    public HiddenByteBuf(ByteBuf byteBuf) {
        this.contents = ByteBufUtil.ensureAccessible((ByteBuf)byteBuf);
    }

    public static Object pack(Object object) {
        if (object instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf)object;
            return new HiddenByteBuf(byteBuf);
        }
        return object;
    }

    public static Object unpack(Object object) {
        if (object instanceof HiddenByteBuf) {
            HiddenByteBuf hiddenByteBuf = (HiddenByteBuf)((Object)object);
            return ByteBufUtil.ensureAccessible((ByteBuf)hiddenByteBuf.contents);
        }
        return object;
    }

    public int refCnt() {
        return this.contents.refCnt();
    }

    public HiddenByteBuf retain() {
        this.contents.retain();
        return this;
    }

    public HiddenByteBuf retain(int i) {
        this.contents.retain(i);
        return this;
    }

    public HiddenByteBuf touch() {
        this.contents.touch();
        return this;
    }

    public HiddenByteBuf touch(Object object) {
        this.contents.touch(object);
        return this;
    }

    public boolean release() {
        return this.contents.release();
    }

    public boolean release(int i) {
        return this.contents.release(i);
    }

    public /* synthetic */ ReferenceCounted touch(Object object) {
        return this.touch(object);
    }

    public /* synthetic */ ReferenceCounted touch() {
        return this.touch();
    }

    public /* synthetic */ ReferenceCounted retain(int i) {
        return this.retain(i);
    }

    public /* synthetic */ ReferenceCounted retain() {
        return this.retain();
    }
}


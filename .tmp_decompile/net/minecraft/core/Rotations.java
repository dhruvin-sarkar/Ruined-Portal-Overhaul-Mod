/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;

public final class Rotations
extends Record {
    final float x;
    final float y;
    final float z;
    public static final Codec<Rotations> CODEC = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 3).map(list -> new Rotations(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue(), ((Float)list.get(2)).floatValue())), rotations -> List.of((Object)Float.valueOf(rotations.x()), (Object)Float.valueOf(rotations.y()), (Object)Float.valueOf(rotations.z())));
    public static final StreamCodec<ByteBuf, Rotations> STREAM_CODEC = new StreamCodec<ByteBuf, Rotations>(){

        @Override
        public Rotations decode(ByteBuf byteBuf) {
            return new Rotations(byteBuf.readFloat(), byteBuf.readFloat(), byteBuf.readFloat());
        }

        @Override
        public void encode(ByteBuf byteBuf, Rotations rotations) {
            byteBuf.writeFloat(rotations.x);
            byteBuf.writeFloat(rotations.y);
            byteBuf.writeFloat(rotations.z);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Rotations)((Object)object2));
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };

    public Rotations(float f, float g, float h) {
        f = Float.isInfinite(f) || Float.isNaN(f) ? 0.0f : f % 360.0f;
        g = Float.isInfinite(g) || Float.isNaN(g) ? 0.0f : g % 360.0f;
        h = Float.isInfinite(h) || Float.isNaN(h) ? 0.0f : h % 360.0f;
        this.x = f;
        this.y = g;
        this.z = h;
    }

    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{Rotations.class, "x;y;z", "x", "y", "z"}, this);
    }

    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Rotations.class, "x;y;z", "x", "y", "z"}, this);
    }

    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Rotations.class, "x;y;z", "x", "y", "z"}, this, object);
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }
}


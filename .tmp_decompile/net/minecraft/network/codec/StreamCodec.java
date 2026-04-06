/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.util.Function10
 *  com.mojang.datafixers.util.Function11
 *  com.mojang.datafixers.util.Function12
 *  com.mojang.datafixers.util.Function3
 *  com.mojang.datafixers.util.Function4
 *  com.mojang.datafixers.util.Function5
 *  com.mojang.datafixers.util.Function6
 *  com.mojang.datafixers.util.Function7
 *  com.mojang.datafixers.util.Function8
 *  com.mojang.datafixers.util.Function9
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;
import io.netty.buffer.ByteBuf;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.codec.StreamMemberEncoder;

public interface StreamCodec<B, V>
extends StreamDecoder<B, V>,
StreamEncoder<B, V> {
    public static <B, V> StreamCodec<B, V> of(final StreamEncoder<B, V> streamEncoder, final StreamDecoder<B, V> streamDecoder) {
        return new StreamCodec<B, V>(){

            @Override
            public V decode(B object) {
                return streamDecoder.decode(object);
            }

            @Override
            public void encode(B object, V object2) {
                streamEncoder.encode(object, object2);
            }
        };
    }

    public static <B, V> StreamCodec<B, V> ofMember(final StreamMemberEncoder<B, V> streamMemberEncoder, final StreamDecoder<B, V> streamDecoder) {
        return new StreamCodec<B, V>(){

            @Override
            public V decode(B object) {
                return streamDecoder.decode(object);
            }

            @Override
            public void encode(B object, V object2) {
                streamMemberEncoder.encode(object2, object);
            }
        };
    }

    public static <B, V> StreamCodec<B, V> unit(final V object) {
        return new StreamCodec<B, V>(){

            @Override
            public V decode(B object2) {
                return object;
            }

            @Override
            public void encode(B object3, V object2) {
                if (!object2.equals(object)) {
                    throw new IllegalStateException("Can't encode '" + String.valueOf(object2) + "', expected '" + String.valueOf(object) + "'");
                }
            }
        };
    }

    default public <O> StreamCodec<B, O> apply(CodecOperation<B, V, O> codecOperation) {
        return codecOperation.apply(this);
    }

    default public <O> StreamCodec<B, O> map(final Function<? super V, ? extends O> function, final Function<? super O, ? extends V> function2) {
        return new StreamCodec<B, O>(){

            @Override
            public O decode(B object) {
                return function.apply(StreamCodec.this.decode(object));
            }

            @Override
            public void encode(B object, O object2) {
                StreamCodec.this.encode(object, function2.apply(object2));
            }
        };
    }

    default public <O extends ByteBuf> StreamCodec<O, V> mapStream(final Function<O, ? extends B> function) {
        return new StreamCodec<O, V>(){

            @Override
            public V decode(O byteBuf) {
                Object object = function.apply(byteBuf);
                return StreamCodec.this.decode(object);
            }

            @Override
            public void encode(O byteBuf, V object) {
                Object object2 = function.apply(byteBuf);
                StreamCodec.this.encode(object2, object);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((O)((ByteBuf)object), (V)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((O)((ByteBuf)object));
            }
        };
    }

    default public <U> StreamCodec<B, U> dispatch(final Function<? super U, ? extends V> function, final Function<? super V, ? extends StreamCodec<? super B, ? extends U>> function2) {
        return new StreamCodec<B, U>(){

            @Override
            public U decode(B object) {
                Object object2 = StreamCodec.this.decode(object);
                StreamCodec streamCodec = (StreamCodec)function2.apply(object2);
                return streamCodec.decode(object);
            }

            @Override
            public void encode(B object, U object2) {
                Object object3 = function.apply(object2);
                StreamCodec streamCodec = (StreamCodec)function2.apply(object3);
                StreamCodec.this.encode(object, object3);
                streamCodec.encode(object, object2);
            }
        };
    }

    public static <B, C, T1> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final Function<T1, C> function2) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                return function2.apply(object2);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final BiFunction<T1, T2, C> biFunction) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                return biFunction.apply(object2, object3);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final Function3<T1, T2, T3, C> function32) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                Object object4 = streamCodec3.decode(object);
                return function32.apply(object2, object3, object4);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final Function4<T1, T2, T3, T4, C> function42) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                Object object4 = streamCodec3.decode(object);
                Object object5 = streamCodec4.decode(object);
                return function42.apply(object2, object3, object4, object5);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
                streamCodec4.encode(object, function4.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final StreamCodec<? super B, T5> streamCodec5, final Function<C, T5> function5, final Function5<T1, T2, T3, T4, T5, C> function52) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                Object object4 = streamCodec3.decode(object);
                Object object5 = streamCodec4.decode(object);
                Object object6 = streamCodec5.decode(object);
                return function52.apply(object2, object3, object4, object5, object6);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
                streamCodec4.encode(object, function4.apply(object2));
                streamCodec5.encode(object, function5.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final StreamCodec<? super B, T5> streamCodec5, final Function<C, T5> function5, final StreamCodec<? super B, T6> streamCodec6, final Function<C, T6> function6, final Function6<T1, T2, T3, T4, T5, T6, C> function62) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                Object object4 = streamCodec3.decode(object);
                Object object5 = streamCodec4.decode(object);
                Object object6 = streamCodec5.decode(object);
                Object object7 = streamCodec6.decode(object);
                return function62.apply(object2, object3, object4, object5, object6, object7);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
                streamCodec4.encode(object, function4.apply(object2));
                streamCodec5.encode(object, function5.apply(object2));
                streamCodec6.encode(object, function6.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final StreamCodec<? super B, T5> streamCodec5, final Function<C, T5> function5, final StreamCodec<? super B, T6> streamCodec6, final Function<C, T6> function6, final StreamCodec<? super B, T7> streamCodec7, final Function<C, T7> function7, final Function7<T1, T2, T3, T4, T5, T6, T7, C> function72) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                Object object4 = streamCodec3.decode(object);
                Object object5 = streamCodec4.decode(object);
                Object object6 = streamCodec5.decode(object);
                Object object7 = streamCodec6.decode(object);
                Object object8 = streamCodec7.decode(object);
                return function72.apply(object2, object3, object4, object5, object6, object7, object8);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
                streamCodec4.encode(object, function4.apply(object2));
                streamCodec5.encode(object, function5.apply(object2));
                streamCodec6.encode(object, function6.apply(object2));
                streamCodec7.encode(object, function7.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final StreamCodec<? super B, T5> streamCodec5, final Function<C, T5> function5, final StreamCodec<? super B, T6> streamCodec6, final Function<C, T6> function6, final StreamCodec<? super B, T7> streamCodec7, final Function<C, T7> function7, final StreamCodec<? super B, T8> streamCodec8, final Function<C, T8> function8, final Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> function82) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                Object object4 = streamCodec3.decode(object);
                Object object5 = streamCodec4.decode(object);
                Object object6 = streamCodec5.decode(object);
                Object object7 = streamCodec6.decode(object);
                Object object8 = streamCodec7.decode(object);
                Object object9 = streamCodec8.decode(object);
                return function82.apply(object2, object3, object4, object5, object6, object7, object8, object9);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
                streamCodec4.encode(object, function4.apply(object2));
                streamCodec5.encode(object, function5.apply(object2));
                streamCodec6.encode(object, function6.apply(object2));
                streamCodec7.encode(object, function7.apply(object2));
                streamCodec8.encode(object, function8.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final StreamCodec<? super B, T5> streamCodec5, final Function<C, T5> function5, final StreamCodec<? super B, T6> streamCodec6, final Function<C, T6> function6, final StreamCodec<? super B, T7> streamCodec7, final Function<C, T7> function7, final StreamCodec<? super B, T8> streamCodec8, final Function<C, T8> function8, final StreamCodec<? super B, T9> streamCodec9, final Function<C, T9> function9, final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> function92) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                Object object4 = streamCodec3.decode(object);
                Object object5 = streamCodec4.decode(object);
                Object object6 = streamCodec5.decode(object);
                Object object7 = streamCodec6.decode(object);
                Object object8 = streamCodec7.decode(object);
                Object object9 = streamCodec8.decode(object);
                Object object10 = streamCodec9.decode(object);
                return function92.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
                streamCodec4.encode(object, function4.apply(object2));
                streamCodec5.encode(object, function5.apply(object2));
                streamCodec6.encode(object, function6.apply(object2));
                streamCodec7.encode(object, function7.apply(object2));
                streamCodec8.encode(object, function8.apply(object2));
                streamCodec9.encode(object, function9.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final StreamCodec<? super B, T5> streamCodec5, final Function<C, T5> function5, final StreamCodec<? super B, T6> streamCodec6, final Function<C, T6> function6, final StreamCodec<? super B, T7> streamCodec7, final Function<C, T7> function7, final StreamCodec<? super B, T8> streamCodec8, final Function<C, T8> function8, final StreamCodec<? super B, T9> streamCodec9, final Function<C, T9> function9, final StreamCodec<? super B, T10> streamCodec10, final Function<C, T10> function10, final Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> function102) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                Object object4 = streamCodec3.decode(object);
                Object object5 = streamCodec4.decode(object);
                Object object6 = streamCodec5.decode(object);
                Object object7 = streamCodec6.decode(object);
                Object object8 = streamCodec7.decode(object);
                Object object9 = streamCodec8.decode(object);
                Object object10 = streamCodec9.decode(object);
                Object object11 = streamCodec10.decode(object);
                return function102.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10, object11);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
                streamCodec4.encode(object, function4.apply(object2));
                streamCodec5.encode(object, function5.apply(object2));
                streamCodec6.encode(object, function6.apply(object2));
                streamCodec7.encode(object, function7.apply(object2));
                streamCodec8.encode(object, function8.apply(object2));
                streamCodec9.encode(object, function9.apply(object2));
                streamCodec10.encode(object, function10.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final StreamCodec<? super B, T5> streamCodec5, final Function<C, T5> function5, final StreamCodec<? super B, T6> streamCodec6, final Function<C, T6> function6, final StreamCodec<? super B, T7> streamCodec7, final Function<C, T7> function7, final StreamCodec<? super B, T8> streamCodec8, final Function<C, T8> function8, final StreamCodec<? super B, T9> streamCodec9, final Function<C, T9> function9, final StreamCodec<? super B, T10> streamCodec10, final Function<C, T10> function10, final StreamCodec<? super B, T11> streamCodec11, final Function<C, T11> function11, final Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, C> function112) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                Object object4 = streamCodec3.decode(object);
                Object object5 = streamCodec4.decode(object);
                Object object6 = streamCodec5.decode(object);
                Object object7 = streamCodec6.decode(object);
                Object object8 = streamCodec7.decode(object);
                Object object9 = streamCodec8.decode(object);
                Object object10 = streamCodec9.decode(object);
                Object object11 = streamCodec10.decode(object);
                Object object12 = streamCodec11.decode(object);
                return function112.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10, object11, object12);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
                streamCodec4.encode(object, function4.apply(object2));
                streamCodec5.encode(object, function5.apply(object2));
                streamCodec6.encode(object, function6.apply(object2));
                streamCodec7.encode(object, function7.apply(object2));
                streamCodec8.encode(object, function8.apply(object2));
                streamCodec9.encode(object, function9.apply(object2));
                streamCodec10.encode(object, function10.apply(object2));
                streamCodec11.encode(object, function11.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> streamCodec, final Function<C, T1> function, final StreamCodec<? super B, T2> streamCodec2, final Function<C, T2> function2, final StreamCodec<? super B, T3> streamCodec3, final Function<C, T3> function3, final StreamCodec<? super B, T4> streamCodec4, final Function<C, T4> function4, final StreamCodec<? super B, T5> streamCodec5, final Function<C, T5> function5, final StreamCodec<? super B, T6> streamCodec6, final Function<C, T6> function6, final StreamCodec<? super B, T7> streamCodec7, final Function<C, T7> function7, final StreamCodec<? super B, T8> streamCodec8, final Function<C, T8> function8, final StreamCodec<? super B, T9> streamCodec9, final Function<C, T9> function9, final StreamCodec<? super B, T10> streamCodec10, final Function<C, T10> function10, final StreamCodec<? super B, T11> streamCodec11, final Function<C, T11> function11, final StreamCodec<? super B, T12> streamCodec12, final Function<C, T12> function12, final Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> function122) {
        return new StreamCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = streamCodec.decode(object);
                Object object3 = streamCodec2.decode(object);
                Object object4 = streamCodec3.decode(object);
                Object object5 = streamCodec4.decode(object);
                Object object6 = streamCodec5.decode(object);
                Object object7 = streamCodec6.decode(object);
                Object object8 = streamCodec7.decode(object);
                Object object9 = streamCodec8.decode(object);
                Object object10 = streamCodec9.decode(object);
                Object object11 = streamCodec10.decode(object);
                Object object12 = streamCodec11.decode(object);
                Object object13 = streamCodec12.decode(object);
                return function122.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10, object11, object12, object13);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
                streamCodec4.encode(object, function4.apply(object2));
                streamCodec5.encode(object, function5.apply(object2));
                streamCodec6.encode(object, function6.apply(object2));
                streamCodec7.encode(object, function7.apply(object2));
                streamCodec8.encode(object, function8.apply(object2));
                streamCodec9.encode(object, function9.apply(object2));
                streamCodec10.encode(object, function10.apply(object2));
                streamCodec11.encode(object, function11.apply(object2));
                streamCodec12.encode(object, function12.apply(object2));
            }
        };
    }

    public static <B, T> StreamCodec<B, T> recursive(final UnaryOperator<StreamCodec<B, T>> unaryOperator) {
        return new StreamCodec<B, T>(){
            private final Supplier<StreamCodec<B, T>> inner = Suppliers.memoize(() -> (StreamCodec)unaryOperator.apply(this));

            @Override
            public T decode(B object) {
                return this.inner.get().decode(object);
            }

            @Override
            public void encode(B object, T object2) {
                this.inner.get().encode(object, object2);
            }
        };
    }

    default public <S extends B> StreamCodec<S, V> cast() {
        return this;
    }

    @FunctionalInterface
    public static interface CodecOperation<B, S, T> {
        public StreamCodec<B, T> apply(StreamCodec<B, S> var1);
    }
}


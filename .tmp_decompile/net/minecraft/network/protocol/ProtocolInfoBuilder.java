/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.CodecModifier;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.ProtocolCodecBuilder;
import net.minecraft.network.protocol.SimpleUnboundProtocol;
import net.minecraft.network.protocol.UnboundProtocol;
import net.minecraft.util.Unit;
import org.jspecify.annotations.Nullable;

public class ProtocolInfoBuilder<T extends PacketListener, B extends ByteBuf, C> {
    final ConnectionProtocol protocol;
    final PacketFlow flow;
    private final List<CodecEntry<T, ?, B, C>> codecs = new ArrayList();
    private @Nullable BundlerInfo bundlerInfo;

    public ProtocolInfoBuilder(ConnectionProtocol connectionProtocol, PacketFlow packetFlow) {
        this.protocol = connectionProtocol;
        this.flow = packetFlow;
    }

    public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B, C> addPacket(PacketType<P> packetType, StreamCodec<? super B, P> streamCodec) {
        this.codecs.add(new CodecEntry(packetType, streamCodec, null));
        return this;
    }

    public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B, C> addPacket(PacketType<P> packetType, StreamCodec<? super B, P> streamCodec, CodecModifier<B, P, C> codecModifier) {
        this.codecs.add(new CodecEntry(packetType, streamCodec, codecModifier));
        return this;
    }

    public <P extends BundlePacket<? super T>, D extends BundleDelimiterPacket<? super T>> ProtocolInfoBuilder<T, B, C> withBundlePacket(PacketType<P> packetType, Function<Iterable<Packet<? super T>>, P> function, D bundleDelimiterPacket) {
        StreamCodec streamCodec = StreamCodec.unit(bundleDelimiterPacket);
        PacketType<BundleDelimiterPacket<? super T>> packetType2 = bundleDelimiterPacket.type();
        this.codecs.add(new CodecEntry(packetType2, streamCodec, null));
        this.bundlerInfo = BundlerInfo.createForPacket(packetType, function, bundleDelimiterPacket);
        return this;
    }

    StreamCodec<ByteBuf, Packet<? super T>> buildPacketCodec(Function<ByteBuf, B> function, List<CodecEntry<T, ?, B, C>> list, C object) {
        ProtocolCodecBuilder protocolCodecBuilder = new ProtocolCodecBuilder(this.flow);
        for (CodecEntry codecEntry : list) {
            codecEntry.addToBuilder(protocolCodecBuilder, function, object);
        }
        return protocolCodecBuilder.build();
    }

    private static ProtocolInfo.Details buildDetails(final ConnectionProtocol connectionProtocol, final PacketFlow packetFlow, final List<? extends CodecEntry<?, ?, ?, ?>> list) {
        return new ProtocolInfo.Details(){

            @Override
            public ConnectionProtocol id() {
                return connectionProtocol;
            }

            @Override
            public PacketFlow flow() {
                return packetFlow;
            }

            @Override
            public void listPackets(ProtocolInfo.Details.PacketVisitor packetVisitor) {
                for (int i = 0; i < list.size(); ++i) {
                    CodecEntry codecEntry = (CodecEntry)((Object)list.get(i));
                    packetVisitor.accept(codecEntry.type, i);
                }
            }
        };
    }

    public SimpleUnboundProtocol<T, B> buildUnbound(final C object) {
        final List list = List.copyOf(this.codecs);
        final BundlerInfo bundlerInfo = this.bundlerInfo;
        final ProtocolInfo.Details details = ProtocolInfoBuilder.buildDetails(this.protocol, this.flow, list);
        return new SimpleUnboundProtocol<T, B>(){

            @Override
            public ProtocolInfo<T> bind(Function<ByteBuf, B> function) {
                return new Implementation(ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec(function, list, object), bundlerInfo);
            }

            @Override
            public ProtocolInfo.Details details() {
                return details;
            }
        };
    }

    public UnboundProtocol<T, B, C> buildUnbound() {
        final List list = List.copyOf(this.codecs);
        final BundlerInfo bundlerInfo = this.bundlerInfo;
        final ProtocolInfo.Details details = ProtocolInfoBuilder.buildDetails(this.protocol, this.flow, list);
        return new UnboundProtocol<T, B, C>(){

            @Override
            public ProtocolInfo<T> bind(Function<ByteBuf, B> function, C object) {
                return new Implementation(ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec(function, list, object), bundlerInfo);
            }

            @Override
            public ProtocolInfo.Details details() {
                return details;
            }
        };
    }

    private static <L extends PacketListener, B extends ByteBuf> SimpleUnboundProtocol<L, B> protocol(ConnectionProtocol connectionProtocol, PacketFlow packetFlow, Consumer<ProtocolInfoBuilder<L, B, Unit>> consumer) {
        ProtocolInfoBuilder protocolInfoBuilder = new ProtocolInfoBuilder(connectionProtocol, packetFlow);
        consumer.accept(protocolInfoBuilder);
        return protocolInfoBuilder.buildUnbound(Unit.INSTANCE);
    }

    public static <T extends ServerboundPacketListener, B extends ByteBuf> SimpleUnboundProtocol<T, B> serverboundProtocol(ConnectionProtocol connectionProtocol, Consumer<ProtocolInfoBuilder<T, B, Unit>> consumer) {
        return ProtocolInfoBuilder.protocol(connectionProtocol, PacketFlow.SERVERBOUND, consumer);
    }

    public static <T extends ClientboundPacketListener, B extends ByteBuf> SimpleUnboundProtocol<T, B> clientboundProtocol(ConnectionProtocol connectionProtocol, Consumer<ProtocolInfoBuilder<T, B, Unit>> consumer) {
        return ProtocolInfoBuilder.protocol(connectionProtocol, PacketFlow.CLIENTBOUND, consumer);
    }

    private static <L extends PacketListener, B extends ByteBuf, C> UnboundProtocol<L, B, C> contextProtocol(ConnectionProtocol connectionProtocol, PacketFlow packetFlow, Consumer<ProtocolInfoBuilder<L, B, C>> consumer) {
        ProtocolInfoBuilder protocolInfoBuilder = new ProtocolInfoBuilder(connectionProtocol, packetFlow);
        consumer.accept(protocolInfoBuilder);
        return protocolInfoBuilder.buildUnbound();
    }

    public static <T extends ServerboundPacketListener, B extends ByteBuf, C> UnboundProtocol<T, B, C> contextServerboundProtocol(ConnectionProtocol connectionProtocol, Consumer<ProtocolInfoBuilder<T, B, C>> consumer) {
        return ProtocolInfoBuilder.contextProtocol(connectionProtocol, PacketFlow.SERVERBOUND, consumer);
    }

    public static <T extends ClientboundPacketListener, B extends ByteBuf, C> UnboundProtocol<T, B, C> contextClientboundProtocol(ConnectionProtocol connectionProtocol, Consumer<ProtocolInfoBuilder<T, B, C>> consumer) {
        return ProtocolInfoBuilder.contextProtocol(connectionProtocol, PacketFlow.CLIENTBOUND, consumer);
    }

    static final class CodecEntry<T extends PacketListener, P extends Packet<? super T>, B extends ByteBuf, C>
    extends Record {
        final PacketType<P> type;
        private final StreamCodec<? super B, P> serializer;
        private final @Nullable CodecModifier<B, P, C> modifier;

        CodecEntry(PacketType<P> packetType, StreamCodec<? super B, P> streamCodec, @Nullable CodecModifier<B, P, C> codecModifier) {
            this.type = packetType;
            this.serializer = streamCodec;
            this.modifier = codecModifier;
        }

        public void addToBuilder(ProtocolCodecBuilder<ByteBuf, T> protocolCodecBuilder, Function<ByteBuf, B> function, C object) {
            StreamCodec<Object, P> streamCodec = this.modifier != null ? this.modifier.apply(this.serializer, object) : this.serializer;
            StreamCodec<ByteBuf, P> streamCodec2 = streamCodec.mapStream(function);
            protocolCodecBuilder.add(this.type, streamCodec2);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CodecEntry.class, "type;serializer;modifier", "type", "serializer", "modifier"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CodecEntry.class, "type;serializer;modifier", "type", "serializer", "modifier"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CodecEntry.class, "type;serializer;modifier", "type", "serializer", "modifier"}, this, object);
        }

        public PacketType<P> type() {
            return this.type;
        }

        public StreamCodec<? super B, P> serializer() {
            return this.serializer;
        }

        public @Nullable CodecModifier<B, P, C> modifier() {
            return this.modifier;
        }
    }

    record Implementation<L extends PacketListener>(ConnectionProtocol id, PacketFlow flow, StreamCodec<ByteBuf, Packet<? super L>> codec, @Nullable BundlerInfo bundlerInfo) implements ProtocolInfo<L>
    {
    }
}


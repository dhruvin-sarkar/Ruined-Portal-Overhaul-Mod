/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.ints.IntSets
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  java.lang.runtime.SwitchBootstraps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class ClientboundCommandsPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundCommandsPacket> STREAM_CODEC = Packet.codec(ClientboundCommandsPacket::write, ClientboundCommandsPacket::new);
    private static final byte MASK_TYPE = 3;
    private static final byte FLAG_EXECUTABLE = 4;
    private static final byte FLAG_REDIRECT = 8;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
    private static final byte FLAG_RESTRICTED = 32;
    private static final byte TYPE_ROOT = 0;
    private static final byte TYPE_LITERAL = 1;
    private static final byte TYPE_ARGUMENT = 2;
    private final int rootIndex;
    private final List<Entry> entries;

    public <S> ClientboundCommandsPacket(RootCommandNode<S> rootCommandNode, NodeInspector<S> nodeInspector) {
        Object2IntMap<CommandNode<S>> object2IntMap = ClientboundCommandsPacket.enumerateNodes(rootCommandNode);
        this.entries = ClientboundCommandsPacket.createEntries(object2IntMap, nodeInspector);
        this.rootIndex = object2IntMap.getInt(rootCommandNode);
    }

    private ClientboundCommandsPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entries = friendlyByteBuf.readList(ClientboundCommandsPacket::readNode);
        this.rootIndex = friendlyByteBuf.readVarInt();
        ClientboundCommandsPacket.validateEntries(this.entries);
    }

    private void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeCollection(this.entries, (friendlyByteBuf, entry) -> entry.write((FriendlyByteBuf)((Object)friendlyByteBuf)));
        friendlyByteBuf2.writeVarInt(this.rootIndex);
    }

    private static void validateEntries(List<Entry> list, BiPredicate<Entry, IntSet> biPredicate) {
        IntOpenHashSet intSet = new IntOpenHashSet((IntCollection)IntSets.fromTo((int)0, (int)list.size()));
        while (!intSet.isEmpty()) {
            boolean bl = intSet.removeIf(arg_0 -> ClientboundCommandsPacket.method_42068(biPredicate, list, (IntSet)intSet, arg_0));
            if (bl) continue;
            throw new IllegalStateException("Server sent an impossible command tree");
        }
    }

    private static void validateEntries(List<Entry> list) {
        ClientboundCommandsPacket.validateEntries(list, Entry::canBuild);
        ClientboundCommandsPacket.validateEntries(list, Entry::canResolve);
    }

    private static <S> Object2IntMap<CommandNode<S>> enumerateNodes(RootCommandNode<S> rootCommandNode) {
        CommandNode commandNode;
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
        ArrayDeque<Object> queue = new ArrayDeque<Object>();
        queue.add(rootCommandNode);
        while ((commandNode = (CommandNode)queue.poll()) != null) {
            if (object2IntMap.containsKey((Object)commandNode)) continue;
            int i = object2IntMap.size();
            object2IntMap.put((Object)commandNode, i);
            queue.addAll(commandNode.getChildren());
            if (commandNode.getRedirect() == null) continue;
            queue.add(commandNode.getRedirect());
        }
        return object2IntMap;
    }

    private static <S> List<Entry> createEntries(Object2IntMap<CommandNode<S>> object2IntMap, NodeInspector<S> nodeInspector) {
        ObjectArrayList objectArrayList = new ObjectArrayList(object2IntMap.size());
        objectArrayList.size(object2IntMap.size());
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(object2IntMap)) {
            objectArrayList.set(entry.getIntValue(), (Object)ClientboundCommandsPacket.createEntry((CommandNode)entry.getKey(), nodeInspector, object2IntMap));
        }
        return objectArrayList;
    }

    private static Entry readNode(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        int[] is = friendlyByteBuf.readVarIntArray();
        int i = (b & 8) != 0 ? friendlyByteBuf.readVarInt() : 0;
        NodeStub nodeStub = ClientboundCommandsPacket.read(friendlyByteBuf, b);
        return new Entry(nodeStub, b, i, is);
    }

    private static @Nullable NodeStub read(FriendlyByteBuf friendlyByteBuf, byte b) {
        int i = b & 3;
        if (i == 2) {
            String string = friendlyByteBuf.readUtf();
            int j = friendlyByteBuf.readVarInt();
            ArgumentTypeInfo argumentTypeInfo = (ArgumentTypeInfo)BuiltInRegistries.COMMAND_ARGUMENT_TYPE.byId(j);
            if (argumentTypeInfo == null) {
                return null;
            }
            Object template = argumentTypeInfo.deserializeFromNetwork(friendlyByteBuf);
            Identifier identifier = (b & 0x10) != 0 ? friendlyByteBuf.readIdentifier() : null;
            return new ArgumentNodeStub(string, (ArgumentTypeInfo.Template<?>)template, identifier);
        }
        if (i == 1) {
            String string = friendlyByteBuf.readUtf();
            return new LiteralNodeStub(string);
        }
        return null;
    }

    private static <S> Entry createEntry(CommandNode<S> commandNode, NodeInspector<S> nodeInspector, Object2IntMap<CommandNode<S>> object2IntMap) {
        Record nodeStub;
        int j;
        int i = 0;
        if (commandNode.getRedirect() != null) {
            i |= 8;
            j = object2IntMap.getInt((Object)commandNode.getRedirect());
        } else {
            j = 0;
        }
        if (nodeInspector.isExecutable(commandNode)) {
            i |= 4;
        }
        if (nodeInspector.isRestricted(commandNode)) {
            i |= 0x20;
        }
        CommandNode<S> commandNode2 = commandNode;
        Objects.requireNonNull(commandNode2);
        CommandNode<S> commandNode3 = commandNode2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{RootCommandNode.class, ArgumentCommandNode.class, LiteralCommandNode.class}, commandNode3, (int)n)) {
            case 0: {
                RootCommandNode rootCommandNode = (RootCommandNode)commandNode3;
                i |= 0;
                nodeStub = null;
                break;
            }
            case 1: {
                ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)commandNode3;
                Identifier identifier = nodeInspector.suggestionId(argumentCommandNode);
                nodeStub = new ArgumentNodeStub(argumentCommandNode.getName(), ArgumentTypeInfos.unpack(argumentCommandNode.getType()), identifier);
                i |= 2;
                if (identifier != null) {
                    i |= 0x10;
                }
                break;
            }
            case 2: {
                LiteralCommandNode literalCommandNode = (LiteralCommandNode)commandNode3;
                nodeStub = new LiteralNodeStub(literalCommandNode.getLiteral());
                i |= 1;
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown node type " + String.valueOf(commandNode));
            }
        }
        int[] is = commandNode.getChildren().stream().mapToInt(arg_0 -> object2IntMap.getInt(arg_0)).toArray();
        return new Entry((NodeStub)nodeStub, i, j, is);
    }

    @Override
    public PacketType<ClientboundCommandsPacket> type() {
        return GamePacketTypes.CLIENTBOUND_COMMANDS;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleCommands(this);
    }

    public <S> RootCommandNode<S> getRoot(CommandBuildContext commandBuildContext, NodeBuilder<S> nodeBuilder) {
        return (RootCommandNode)new NodeResolver<S>(commandBuildContext, nodeBuilder, this.entries).resolve(this.rootIndex);
    }

    private static /* synthetic */ boolean method_42068(BiPredicate biPredicate, List list, IntSet intSet, int i) {
        return biPredicate.test((Entry)((Object)list.get(i)), intSet);
    }

    public static interface NodeInspector<S> {
        public @Nullable Identifier suggestionId(ArgumentCommandNode<S, ?> var1);

        public boolean isExecutable(CommandNode<S> var1);

        public boolean isRestricted(CommandNode<S> var1);
    }

    static final class Entry
    extends Record {
        final @Nullable NodeStub stub;
        final int flags;
        final int redirect;
        final int[] children;

        Entry(@Nullable NodeStub nodeStub, int i, int j, int[] is) {
            this.stub = nodeStub;
            this.flags = i;
            this.redirect = j;
            this.children = is;
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeByte(this.flags);
            friendlyByteBuf.writeVarIntArray(this.children);
            if ((this.flags & 8) != 0) {
                friendlyByteBuf.writeVarInt(this.redirect);
            }
            if (this.stub != null) {
                this.stub.write(friendlyByteBuf);
            }
        }

        public boolean canBuild(IntSet intSet) {
            if ((this.flags & 8) != 0) {
                return !intSet.contains(this.redirect);
            }
            return true;
        }

        public boolean canResolve(IntSet intSet) {
            for (int i : this.children) {
                if (!intSet.contains(i)) continue;
                return false;
            }
            return true;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "stub;flags;redirect;children", "stub", "flags", "redirect", "children"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "stub;flags;redirect;children", "stub", "flags", "redirect", "children"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "stub;flags;redirect;children", "stub", "flags", "redirect", "children"}, this, object);
        }

        public @Nullable NodeStub stub() {
            return this.stub;
        }

        public int flags() {
            return this.flags;
        }

        public int redirect() {
            return this.redirect;
        }

        public int[] children() {
            return this.children;
        }
    }

    static interface NodeStub {
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext var1, NodeBuilder<S> var2);

        public void write(FriendlyByteBuf var1);
    }

    record ArgumentNodeStub(String id, ArgumentTypeInfo.Template<?> argumentType, @Nullable Identifier suggestionId) implements NodeStub
    {
        @Override
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext commandBuildContext, NodeBuilder<S> nodeBuilder) {
            Object argumentType = this.argumentType.instantiate(commandBuildContext);
            return nodeBuilder.createArgument(this.id, (ArgumentType<?>)argumentType, this.suggestionId);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(this.id);
            ArgumentNodeStub.serializeCap(friendlyByteBuf, this.argumentType);
            if (this.suggestionId != null) {
                friendlyByteBuf.writeIdentifier(this.suggestionId);
            }
        }

        private static <A extends ArgumentType<?>> void serializeCap(FriendlyByteBuf friendlyByteBuf, ArgumentTypeInfo.Template<A> template) {
            ArgumentNodeStub.serializeCap(friendlyByteBuf, template.type(), template);
        }

        private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(FriendlyByteBuf friendlyByteBuf, ArgumentTypeInfo<A, T> argumentTypeInfo, ArgumentTypeInfo.Template<A> template) {
            friendlyByteBuf.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(argumentTypeInfo));
            argumentTypeInfo.serializeToNetwork(template, friendlyByteBuf);
        }
    }

    record LiteralNodeStub(String id) implements NodeStub
    {
        @Override
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext commandBuildContext, NodeBuilder<S> nodeBuilder) {
            return nodeBuilder.createLiteral(this.id);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(this.id);
        }
    }

    static class NodeResolver<S> {
        private final CommandBuildContext context;
        private final NodeBuilder<S> builder;
        private final List<Entry> entries;
        private final List<CommandNode<S>> nodes;

        NodeResolver(CommandBuildContext commandBuildContext, NodeBuilder<S> nodeBuilder, List<Entry> list) {
            this.context = commandBuildContext;
            this.builder = nodeBuilder;
            this.entries = list;
            ObjectArrayList objectArrayList = new ObjectArrayList();
            objectArrayList.size(list.size());
            this.nodes = objectArrayList;
        }

        public CommandNode<S> resolve(int i) {
            RootCommandNode commandNode2;
            CommandNode<S> commandNode = this.nodes.get(i);
            if (commandNode != null) {
                return commandNode;
            }
            Entry entry = this.entries.get(i);
            if (entry.stub == null) {
                commandNode2 = new RootCommandNode();
            } else {
                ArgumentBuilder<S, ?> argumentBuilder = entry.stub.build(this.context, this.builder);
                if ((entry.flags & 8) != 0) {
                    argumentBuilder.redirect(this.resolve(entry.redirect));
                }
                boolean bl = (entry.flags & 4) != 0;
                boolean bl2 = (entry.flags & 0x20) != 0;
                commandNode2 = this.builder.configure(argumentBuilder, bl, bl2).build();
            }
            this.nodes.set(i, (CommandNode<S>)commandNode2);
            for (int j : entry.children) {
                CommandNode<S> commandNode3 = this.resolve(j);
                if (commandNode3 instanceof RootCommandNode) continue;
                commandNode2.addChild(commandNode3);
            }
            return commandNode2;
        }
    }

    public static interface NodeBuilder<S> {
        public ArgumentBuilder<S, ?> createLiteral(String var1);

        public ArgumentBuilder<S, ?> createArgument(String var1, ArgumentType<?> var2, @Nullable Identifier var3);

        public ArgumentBuilder<S, ?> configure(ArgumentBuilder<S, ?> var1, boolean var2, boolean var3);
    }
}


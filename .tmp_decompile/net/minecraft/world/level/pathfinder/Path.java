/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.pathfinder;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class Path {
    public static final StreamCodec<FriendlyByteBuf, Path> STREAM_CODEC = StreamCodec.of((friendlyByteBuf, path) -> path.writeToStream((FriendlyByteBuf)((Object)friendlyByteBuf)), Path::createFromStream);
    private final List<Node> nodes;
    private @Nullable DebugData debugData;
    private int nextNodeIndex;
    private final BlockPos target;
    private final float distToTarget;
    private final boolean reached;

    public Path(List<Node> list, BlockPos blockPos, boolean bl) {
        this.nodes = list;
        this.target = blockPos;
        this.distToTarget = list.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).distanceManhattan(this.target);
        this.reached = bl;
    }

    public void advance() {
        ++this.nextNodeIndex;
    }

    public boolean notStarted() {
        return this.nextNodeIndex <= 0;
    }

    public boolean isDone() {
        return this.nextNodeIndex >= this.nodes.size();
    }

    public @Nullable Node getEndNode() {
        if (!this.nodes.isEmpty()) {
            return this.nodes.get(this.nodes.size() - 1);
        }
        return null;
    }

    public Node getNode(int i) {
        return this.nodes.get(i);
    }

    public void truncateNodes(int i) {
        if (this.nodes.size() > i) {
            this.nodes.subList(i, this.nodes.size()).clear();
        }
    }

    public void replaceNode(int i, Node node) {
        this.nodes.set(i, node);
    }

    public int getNodeCount() {
        return this.nodes.size();
    }

    public int getNextNodeIndex() {
        return this.nextNodeIndex;
    }

    public void setNextNodeIndex(int i) {
        this.nextNodeIndex = i;
    }

    public Vec3 getEntityPosAtNode(Entity entity, int i) {
        Node node = this.nodes.get(i);
        double d = (double)node.x + (double)((int)(entity.getBbWidth() + 1.0f)) * 0.5;
        double e = node.y;
        double f = (double)node.z + (double)((int)(entity.getBbWidth() + 1.0f)) * 0.5;
        return new Vec3(d, e, f);
    }

    public BlockPos getNodePos(int i) {
        return this.nodes.get(i).asBlockPos();
    }

    public Vec3 getNextEntityPos(Entity entity) {
        return this.getEntityPosAtNode(entity, this.nextNodeIndex);
    }

    public BlockPos getNextNodePos() {
        return this.nodes.get(this.nextNodeIndex).asBlockPos();
    }

    public Node getNextNode() {
        return this.nodes.get(this.nextNodeIndex);
    }

    public @Nullable Node getPreviousNode() {
        return this.nextNodeIndex > 0 ? this.nodes.get(this.nextNodeIndex - 1) : null;
    }

    public boolean sameAs(@Nullable Path path) {
        return path != null && this.nodes.equals(path.nodes);
    }

    public boolean equals(Object object) {
        if (!(object instanceof Path)) {
            return false;
        }
        Path path = (Path)object;
        return this.nextNodeIndex == path.nextNodeIndex && this.debugData == path.debugData && this.reached == path.reached && this.target.equals(path.target) && this.nodes.equals(path.nodes);
    }

    public int hashCode() {
        return this.nextNodeIndex + this.nodes.hashCode() * 31;
    }

    public boolean canReach() {
        return this.reached;
    }

    @VisibleForDebug
    void setDebug(Node[] nodes, Node[] nodes2, Set<Target> set) {
        this.debugData = new DebugData(nodes, nodes2, set);
    }

    public @Nullable DebugData debugData() {
        return this.debugData;
    }

    public void writeToStream(FriendlyByteBuf friendlyByteBuf2) {
        if (this.debugData == null || this.debugData.targetNodes.isEmpty()) {
            throw new IllegalStateException("Missing debug data");
        }
        friendlyByteBuf2.writeBoolean(this.reached);
        friendlyByteBuf2.writeInt(this.nextNodeIndex);
        friendlyByteBuf2.writeBlockPos(this.target);
        friendlyByteBuf2.writeCollection(this.nodes, (friendlyByteBuf, node) -> node.writeToStream((FriendlyByteBuf)((Object)friendlyByteBuf)));
        this.debugData.write(friendlyByteBuf2);
    }

    public static Path createFromStream(FriendlyByteBuf friendlyByteBuf) {
        boolean bl = friendlyByteBuf.readBoolean();
        int i = friendlyByteBuf.readInt();
        BlockPos blockPos = friendlyByteBuf.readBlockPos();
        List<Node> list = friendlyByteBuf.readList(Node::createFromStream);
        DebugData debugData = DebugData.read(friendlyByteBuf);
        Path path = new Path(list, blockPos, bl);
        path.debugData = debugData;
        path.nextNodeIndex = i;
        return path;
    }

    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public float getDistToTarget() {
        return this.distToTarget;
    }

    static Node[] readNodeArray(FriendlyByteBuf friendlyByteBuf) {
        Node[] nodes = new Node[friendlyByteBuf.readVarInt()];
        for (int i = 0; i < nodes.length; ++i) {
            nodes[i] = Node.createFromStream(friendlyByteBuf);
        }
        return nodes;
    }

    static void writeNodeArray(FriendlyByteBuf friendlyByteBuf, Node[] nodes) {
        friendlyByteBuf.writeVarInt(nodes.length);
        for (Node node : nodes) {
            node.writeToStream(friendlyByteBuf);
        }
    }

    public Path copy() {
        Path path = new Path(this.nodes, this.target, this.reached);
        path.debugData = this.debugData;
        path.nextNodeIndex = this.nextNodeIndex;
        return path;
    }

    public static final class DebugData
    extends Record {
        private final Node[] openSet;
        private final Node[] closedSet;
        final Set<Target> targetNodes;

        public DebugData(Node[] nodes, Node[] nodes2, Set<Target> set) {
            this.openSet = nodes;
            this.closedSet = nodes2;
            this.targetNodes = set;
        }

        public void write(FriendlyByteBuf friendlyByteBuf2) {
            friendlyByteBuf2.writeCollection(this.targetNodes, (friendlyByteBuf, target) -> target.writeToStream((FriendlyByteBuf)((Object)friendlyByteBuf)));
            Path.writeNodeArray(friendlyByteBuf2, this.openSet);
            Path.writeNodeArray(friendlyByteBuf2, this.closedSet);
        }

        public static DebugData read(FriendlyByteBuf friendlyByteBuf) {
            HashSet hashSet = friendlyByteBuf.readCollection(HashSet::new, Target::createFromStream);
            Node[] nodes = Path.readNodeArray(friendlyByteBuf);
            Node[] nodes2 = Path.readNodeArray(friendlyByteBuf);
            return new DebugData(nodes, nodes2, hashSet);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DebugData.class, "openSet;closedSet;targetNodes", "openSet", "closedSet", "targetNodes"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DebugData.class, "openSet;closedSet;targetNodes", "openSet", "closedSet", "targetNodes"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DebugData.class, "openSet;closedSet;targetNodes", "openSet", "closedSet", "targetNodes"}, this, object);
        }

        public Node[] openSet() {
            return this.openSet;
        }

        public Node[] closedSet() {
            return this.closedSet;
        }

        public Set<Target> targetNodes() {
            return this.targetNodes;
        }
    }
}


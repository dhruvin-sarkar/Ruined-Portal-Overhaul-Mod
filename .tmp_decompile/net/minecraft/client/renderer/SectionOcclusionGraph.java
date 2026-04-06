/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Octree;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SectionOcclusionGraph {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MINIMUM_ADVANCED_CULLING_DISTANCE = 60;
    private static final int MINIMUM_ADVANCED_CULLING_SECTION_DISTANCE = SectionPos.blockToSectionCoord(60);
    private static final double CEILED_SECTION_DIAGONAL = Math.ceil(Math.sqrt(3.0) * 16.0);
    private boolean needsFullUpdate = true;
    private @Nullable Future<?> fullUpdateTask;
    private @Nullable ViewArea viewArea;
    private final AtomicReference<@Nullable GraphState> currentGraph = new AtomicReference();
    private final AtomicReference<@Nullable GraphEvents> nextGraphEvents = new AtomicReference();
    private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);

    public void waitAndReset(@Nullable ViewArea viewArea) {
        if (this.fullUpdateTask != null) {
            try {
                this.fullUpdateTask.get();
                this.fullUpdateTask = null;
            }
            catch (Exception exception) {
                LOGGER.warn("Full update failed", (Throwable)exception);
            }
        }
        this.viewArea = viewArea;
        if (viewArea != null) {
            this.currentGraph.set(new GraphState(viewArea));
            this.invalidate();
        } else {
            this.currentGraph.set(null);
        }
    }

    public void invalidate() {
        this.needsFullUpdate = true;
    }

    public void addSectionsInFrustum(Frustum frustum, List<SectionRenderDispatcher.RenderSection> list, List<SectionRenderDispatcher.RenderSection> list2) {
        this.currentGraph.get().storage().sectionTree.visitNodes((node, bl, i, bl2) -> {
            SectionRenderDispatcher.RenderSection renderSection = node.getSection();
            if (renderSection != null) {
                list.add(renderSection);
                if (bl2) {
                    list2.add(renderSection);
                }
            }
        }, frustum, 32);
    }

    public boolean consumeFrustumUpdate() {
        return this.needsFrustumUpdate.compareAndSet(true, false);
    }

    public void onChunkReadyToRender(ChunkPos chunkPos) {
        GraphEvents graphEvents2;
        GraphEvents graphEvents = this.nextGraphEvents.get();
        if (graphEvents != null) {
            this.addNeighbors(graphEvents, chunkPos);
        }
        if ((graphEvents2 = this.currentGraph.get().events) != graphEvents) {
            this.addNeighbors(graphEvents2, chunkPos);
        }
    }

    public void schedulePropagationFrom(SectionRenderDispatcher.RenderSection renderSection) {
        GraphEvents graphEvents2;
        GraphEvents graphEvents = this.nextGraphEvents.get();
        if (graphEvents != null) {
            graphEvents.sectionsToPropagateFrom.add(renderSection);
        }
        if ((graphEvents2 = this.currentGraph.get().events) != graphEvents) {
            graphEvents2.sectionsToPropagateFrom.add(renderSection);
        }
    }

    public void update(boolean bl, Camera camera, Frustum frustum, List<SectionRenderDispatcher.RenderSection> list, LongOpenHashSet longOpenHashSet) {
        Vec3 vec3 = camera.position();
        if (this.needsFullUpdate && (this.fullUpdateTask == null || this.fullUpdateTask.isDone())) {
            this.scheduleFullUpdate(bl, camera, vec3, longOpenHashSet);
        }
        this.runPartialUpdate(bl, frustum, list, vec3, longOpenHashSet);
    }

    private void scheduleFullUpdate(boolean bl, Camera camera, Vec3 vec3, LongOpenHashSet longOpenHashSet) {
        this.needsFullUpdate = false;
        LongOpenHashSet longOpenHashSet2 = longOpenHashSet.clone();
        this.fullUpdateTask = CompletableFuture.runAsync(() -> {
            GraphState graphState = new GraphState(this.viewArea);
            this.nextGraphEvents.set(graphState.events);
            ArrayDeque queue = Queues.newArrayDeque();
            this.initializeQueueForFullUpdate(camera, queue);
            queue.forEach(node -> graphState.storage.sectionToNodeMap.put(node.section, (Node)node));
            this.runUpdates(graphState.storage, vec3, queue, bl, renderSection -> {}, longOpenHashSet2);
            this.currentGraph.set(graphState);
            this.nextGraphEvents.set(null);
            this.needsFrustumUpdate.set(true);
        }, Util.backgroundExecutor());
    }

    private void runPartialUpdate(boolean bl, Frustum frustum, List<SectionRenderDispatcher.RenderSection> list, Vec3 vec3, LongOpenHashSet longOpenHashSet) {
        GraphState graphState = this.currentGraph.get();
        this.queueSectionsWithNewNeighbors(graphState);
        if (!graphState.events.sectionsToPropagateFrom.isEmpty()) {
            ArrayDeque queue = Queues.newArrayDeque();
            while (!graphState.events.sectionsToPropagateFrom.isEmpty()) {
                SectionRenderDispatcher.RenderSection renderSection2 = (SectionRenderDispatcher.RenderSection)graphState.events.sectionsToPropagateFrom.poll();
                Node node = graphState.storage.sectionToNodeMap.get(renderSection2);
                if (node == null || node.section != renderSection2) continue;
                queue.add(node);
            }
            Frustum frustum2 = LevelRenderer.offsetFrustum(frustum);
            Consumer<SectionRenderDispatcher.RenderSection> consumer = renderSection -> {
                if (frustum2.isVisible(renderSection.getBoundingBox())) {
                    this.needsFrustumUpdate.set(true);
                }
            };
            this.runUpdates(graphState.storage, vec3, queue, bl, consumer, longOpenHashSet);
        }
    }

    private void queueSectionsWithNewNeighbors(GraphState graphState) {
        LongIterator longIterator = graphState.events.chunksWhichReceivedNeighbors.iterator();
        while (longIterator.hasNext()) {
            long l = longIterator.nextLong();
            List list = (List)graphState.storage.chunksWaitingForNeighbors.get(l);
            if (list == null || !((SectionRenderDispatcher.RenderSection)list.get(0)).hasAllNeighbors()) continue;
            graphState.events.sectionsToPropagateFrom.addAll(list);
            graphState.storage.chunksWaitingForNeighbors.remove(l);
        }
        graphState.events.chunksWhichReceivedNeighbors.clear();
    }

    private void addNeighbors(GraphEvents graphEvents, ChunkPos chunkPos) {
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x - 1, chunkPos.z));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x, chunkPos.z - 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x + 1, chunkPos.z));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x, chunkPos.z + 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x - 1, chunkPos.z - 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x - 1, chunkPos.z + 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x + 1, chunkPos.z - 1));
        graphEvents.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(chunkPos.x + 1, chunkPos.z + 1));
    }

    private void initializeQueueForFullUpdate(Camera camera, Queue<Node> queue) {
        BlockPos blockPos = camera.blockPosition();
        long l = SectionPos.asLong(blockPos);
        int i = SectionPos.y(l);
        SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSection(l);
        if (renderSection == null) {
            LevelHeightAccessor levelHeightAccessor = this.viewArea.getLevelHeightAccessor();
            boolean bl = i < levelHeightAccessor.getMinSectionY();
            int j = bl ? levelHeightAccessor.getMinSectionY() : levelHeightAccessor.getMaxSectionY();
            int k = this.viewArea.getViewDistance();
            ArrayList list = Lists.newArrayList();
            int m = SectionPos.x(l);
            int n = SectionPos.z(l);
            for (int o = -k; o <= k; ++o) {
                for (int p = -k; p <= k; ++p) {
                    SectionRenderDispatcher.RenderSection renderSection2 = this.viewArea.getRenderSection(SectionPos.asLong(o + m, j, p + n));
                    if (renderSection2 == null || !this.isInViewDistance(l, renderSection2.getSectionNode())) continue;
                    Direction direction = bl ? Direction.UP : Direction.DOWN;
                    Node node2 = new Node(renderSection2, direction, 0);
                    node2.setDirections(node2.directions, direction);
                    if (o > 0) {
                        node2.setDirections(node2.directions, Direction.EAST);
                    } else if (o < 0) {
                        node2.setDirections(node2.directions, Direction.WEST);
                    }
                    if (p > 0) {
                        node2.setDirections(node2.directions, Direction.SOUTH);
                    } else if (p < 0) {
                        node2.setDirections(node2.directions, Direction.NORTH);
                    }
                    list.add(node2);
                }
            }
            list.sort(Comparator.comparingDouble(node -> blockPos.distSqr(SectionPos.of(node.section.getSectionNode()).center())));
            queue.addAll(list);
        } else {
            queue.add(new Node(renderSection, null, 0));
        }
    }

    private void runUpdates(GraphStorage graphStorage, Vec3 vec3, Queue<Node> queue, boolean bl, Consumer<SectionRenderDispatcher.RenderSection> consumer, LongOpenHashSet longOpenHashSet) {
        SectionPos sectionPos = SectionPos.of(vec3);
        long l2 = sectionPos.asLong();
        BlockPos blockPos = sectionPos.center();
        while (!queue.isEmpty()) {
            long m;
            Node node = queue.poll();
            SectionRenderDispatcher.RenderSection renderSection = node.section;
            if (!longOpenHashSet.contains(node.section.getSectionNode())) {
                if (graphStorage.sectionTree.add(node.section)) {
                    consumer.accept(node.section);
                }
            } else {
                node.section.sectionMesh.compareAndSet(CompiledSectionMesh.UNCOMPILED, CompiledSectionMesh.EMPTY);
            }
            boolean bl2 = Math.abs(SectionPos.x(m = renderSection.getSectionNode()) - sectionPos.x()) > MINIMUM_ADVANCED_CULLING_SECTION_DISTANCE || Math.abs(SectionPos.y(m) - sectionPos.y()) > MINIMUM_ADVANCED_CULLING_SECTION_DISTANCE || Math.abs(SectionPos.z(m) - sectionPos.z()) > MINIMUM_ADVANCED_CULLING_SECTION_DISTANCE;
            for (Direction direction : DIRECTIONS) {
                Node node2;
                int i;
                SectionRenderDispatcher.RenderSection renderSection2 = this.getRelativeFrom(l2, renderSection, direction);
                if (renderSection2 == null || bl && node.hasDirection(direction.getOpposite())) continue;
                if (bl && node.hasSourceDirections()) {
                    SectionMesh sectionMesh = renderSection.getSectionMesh();
                    boolean bl3 = false;
                    for (i = 0; i < DIRECTIONS.length; ++i) {
                        if (!node.hasSourceDirection(i) || !sectionMesh.facesCanSeeEachother(DIRECTIONS[i].getOpposite(), direction)) continue;
                        bl3 = true;
                        break;
                    }
                    if (!bl3) continue;
                }
                if (bl && bl2) {
                    boolean bl5;
                    boolean bl4;
                    int j = SectionPos.sectionToBlockCoord(SectionPos.x(m));
                    int k = SectionPos.sectionToBlockCoord(SectionPos.y(m));
                    i = SectionPos.sectionToBlockCoord(SectionPos.z(m));
                    boolean bl3 = direction.getAxis() == Direction.Axis.X ? blockPos.getX() > j : (bl4 = blockPos.getX() < j);
                    boolean bl6 = direction.getAxis() == Direction.Axis.Y ? blockPos.getY() > k : (bl5 = blockPos.getY() < k);
                    boolean bl62 = direction.getAxis() == Direction.Axis.Z ? blockPos.getZ() > i : blockPos.getZ() < i;
                    Vector3d vector3d = new Vector3d((double)(j + (bl4 ? 16 : 0)), (double)(k + (bl5 ? 16 : 0)), (double)(i + (bl62 ? 16 : 0)));
                    Vector3d vector3d2 = new Vector3d(vec3.x, vec3.y, vec3.z).sub((Vector3dc)vector3d).normalize().mul(CEILED_SECTION_DIAGONAL);
                    boolean bl7 = true;
                    while (vector3d.distanceSquared(vec3.x, vec3.y, vec3.z) > 3600.0) {
                        vector3d.add((Vector3dc)vector3d2);
                        LevelHeightAccessor levelHeightAccessor = this.viewArea.getLevelHeightAccessor();
                        if (vector3d.y > (double)levelHeightAccessor.getMaxY() || vector3d.y < (double)levelHeightAccessor.getMinY()) break;
                        SectionRenderDispatcher.RenderSection renderSection3 = this.viewArea.getRenderSectionAt(BlockPos.containing(vector3d.x, vector3d.y, vector3d.z));
                        if (renderSection3 != null && graphStorage.sectionToNodeMap.get(renderSection3) != null) continue;
                        bl7 = false;
                        break;
                    }
                    if (!bl7) continue;
                }
                if ((node2 = graphStorage.sectionToNodeMap.get(renderSection2)) != null) {
                    node2.addSourceDirection(direction);
                    continue;
                }
                Node node3 = new Node(renderSection2, direction, node.step + 1);
                node3.setDirections(node.directions, direction);
                if (renderSection2.hasAllNeighbors()) {
                    queue.add(node3);
                    graphStorage.sectionToNodeMap.put(renderSection2, node3);
                    continue;
                }
                if (!this.isInViewDistance(l2, renderSection2.getSectionNode())) continue;
                graphStorage.sectionToNodeMap.put(renderSection2, node3);
                long n = SectionPos.sectionToChunk(renderSection2.getSectionNode());
                ((List)graphStorage.chunksWaitingForNeighbors.computeIfAbsent(n, l -> new ArrayList())).add(renderSection2);
            }
        }
    }

    private boolean isInViewDistance(long l, long m) {
        return ChunkTrackingView.isInViewDistance(SectionPos.x(l), SectionPos.z(l), this.viewArea.getViewDistance(), SectionPos.x(m), SectionPos.z(m));
    }

    private @Nullable SectionRenderDispatcher.RenderSection getRelativeFrom(long l, SectionRenderDispatcher.RenderSection renderSection, Direction direction) {
        long m = renderSection.getNeighborSectionNode(direction);
        if (!this.isInViewDistance(l, m)) {
            return null;
        }
        if (Mth.abs(SectionPos.y(l) - SectionPos.y(m)) > this.viewArea.getViewDistance()) {
            return null;
        }
        return this.viewArea.getRenderSection(m);
    }

    @VisibleForDebug
    public @Nullable Node getNode(SectionRenderDispatcher.RenderSection renderSection) {
        return this.currentGraph.get().storage.sectionToNodeMap.get(renderSection);
    }

    public Octree getOctree() {
        return this.currentGraph.get().storage.sectionTree;
    }

    @Environment(value=EnvType.CLIENT)
    static final class GraphState
    extends Record {
        final GraphStorage storage;
        final GraphEvents events;

        GraphState(ViewArea viewArea) {
            this(new GraphStorage(viewArea), new GraphEvents());
        }

        private GraphState(GraphStorage graphStorage, GraphEvents graphEvents) {
            this.storage = graphStorage;
            this.events = graphEvents;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GraphState.class, "storage;events", "storage", "events"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GraphState.class, "storage;events", "storage", "events"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GraphState.class, "storage;events", "storage", "events"}, this, object);
        }

        public GraphStorage storage() {
            return this.storage;
        }

        public GraphEvents events() {
            return this.events;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class GraphStorage {
        public final SectionToNodeMap sectionToNodeMap;
        public final Octree sectionTree;
        public final Long2ObjectMap<List<SectionRenderDispatcher.RenderSection>> chunksWaitingForNeighbors;

        public GraphStorage(ViewArea viewArea) {
            this.sectionToNodeMap = new SectionToNodeMap(viewArea.sections.length);
            this.sectionTree = new Octree(viewArea.getCameraSectionPos(), viewArea.getViewDistance(), viewArea.sectionGridSizeY, viewArea.level.getMinY());
            this.chunksWaitingForNeighbors = new Long2ObjectOpenHashMap();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class GraphEvents
    extends Record {
        final LongSet chunksWhichReceivedNeighbors;
        final BlockingQueue<SectionRenderDispatcher.RenderSection> sectionsToPropagateFrom;

        GraphEvents() {
            this((LongSet)new LongOpenHashSet(), new LinkedBlockingQueue<SectionRenderDispatcher.RenderSection>());
        }

        private GraphEvents(LongSet longSet, BlockingQueue<SectionRenderDispatcher.RenderSection> blockingQueue) {
            this.chunksWhichReceivedNeighbors = longSet;
            this.sectionsToPropagateFrom = blockingQueue;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GraphEvents.class, "chunksWhichReceivedNeighbors;sectionsToPropagateFrom", "chunksWhichReceivedNeighbors", "sectionsToPropagateFrom"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GraphEvents.class, "chunksWhichReceivedNeighbors;sectionsToPropagateFrom", "chunksWhichReceivedNeighbors", "sectionsToPropagateFrom"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GraphEvents.class, "chunksWhichReceivedNeighbors;sectionsToPropagateFrom", "chunksWhichReceivedNeighbors", "sectionsToPropagateFrom"}, this, object);
        }

        public LongSet chunksWhichReceivedNeighbors() {
            return this.chunksWhichReceivedNeighbors;
        }

        public BlockingQueue<SectionRenderDispatcher.RenderSection> sectionsToPropagateFrom() {
            return this.sectionsToPropagateFrom;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class SectionToNodeMap {
        private final Node[] nodes;

        SectionToNodeMap(int i) {
            this.nodes = new Node[i];
        }

        public void put(SectionRenderDispatcher.RenderSection renderSection, Node node) {
            this.nodes[renderSection.index] = node;
        }

        public @Nullable Node get(SectionRenderDispatcher.RenderSection renderSection) {
            int i = renderSection.index;
            if (i < 0 || i >= this.nodes.length) {
                return null;
            }
            return this.nodes[i];
        }
    }

    @Environment(value=EnvType.CLIENT)
    @VisibleForDebug
    public static class Node {
        @VisibleForDebug
        protected final SectionRenderDispatcher.RenderSection section;
        private byte sourceDirections;
        byte directions;
        @VisibleForDebug
        public final int step;

        Node(SectionRenderDispatcher.RenderSection renderSection, @Nullable Direction direction, int i) {
            this.section = renderSection;
            if (direction != null) {
                this.addSourceDirection(direction);
            }
            this.step = i;
        }

        void setDirections(byte b, Direction direction) {
            this.directions = (byte)(this.directions | (b | 1 << direction.ordinal()));
        }

        boolean hasDirection(Direction direction) {
            return (this.directions & 1 << direction.ordinal()) > 0;
        }

        void addSourceDirection(Direction direction) {
            this.sourceDirections = (byte)(this.sourceDirections | (this.sourceDirections | 1 << direction.ordinal()));
        }

        @VisibleForDebug
        public boolean hasSourceDirection(int i) {
            return (this.sourceDirections & 1 << i) > 0;
        }

        boolean hasSourceDirections() {
            return this.sourceDirections != 0;
        }

        public int hashCode() {
            return Long.hashCode(this.section.getSectionNode());
        }

        public boolean equals(Object object) {
            if (!(object instanceof Node)) {
                return false;
            }
            Node node = (Node)object;
            return this.section.getSectionNode() == node.section.getSectionNode();
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.framegraph;

import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FrameGraphBuilder {
    private final List<InternalVirtualResource<?>> internalResources = new ArrayList();
    private final List<ExternalResource<?>> externalResources = new ArrayList();
    private final List<Pass> passes = new ArrayList<Pass>();

    public FramePass addPass(String string) {
        Pass pass = new Pass(this.passes.size(), string);
        this.passes.add(pass);
        return pass;
    }

    public <T> ResourceHandle<T> importExternal(String string, T object) {
        ExternalResource<T> externalResource = new ExternalResource<T>(string, null, object);
        this.externalResources.add(externalResource);
        return externalResource.handle;
    }

    public <T> ResourceHandle<T> createInternal(String string, ResourceDescriptor<T> resourceDescriptor) {
        return this.createInternalResource((String)string, resourceDescriptor, null).handle;
    }

    <T> InternalVirtualResource<T> createInternalResource(String string, ResourceDescriptor<T> resourceDescriptor, @Nullable Pass pass) {
        int i = this.internalResources.size();
        InternalVirtualResource<T> internalVirtualResource = new InternalVirtualResource<T>(i, string, pass, resourceDescriptor);
        this.internalResources.add(internalVirtualResource);
        return internalVirtualResource;
    }

    public void execute(GraphicsResourceAllocator graphicsResourceAllocator) {
        this.execute(graphicsResourceAllocator, Inspector.NONE);
    }

    public void execute(GraphicsResourceAllocator graphicsResourceAllocator, Inspector inspector) {
        BitSet bitSet = this.identifyPassesToKeep();
        ArrayList<Pass> list = new ArrayList<Pass>(bitSet.cardinality());
        BitSet bitSet2 = new BitSet(this.passes.size());
        for (Pass pass : this.passes) {
            this.resolvePassOrder(pass, bitSet, bitSet2, list);
        }
        this.assignResourceLifetimes(list);
        for (Pass pass : list) {
            for (InternalVirtualResource<?> internalVirtualResource : pass.resourcesToAcquire) {
                inspector.acquireResource(internalVirtualResource.name);
                internalVirtualResource.acquire(graphicsResourceAllocator);
            }
            inspector.beforeExecutePass(pass.name);
            pass.task.run();
            inspector.afterExecutePass(pass.name);
            int i = pass.resourcesToRelease.nextSetBit(0);
            while (i >= 0) {
                InternalVirtualResource<?> internalVirtualResource;
                internalVirtualResource = this.internalResources.get(i);
                inspector.releaseResource(internalVirtualResource.name);
                internalVirtualResource.release(graphicsResourceAllocator);
                i = pass.resourcesToRelease.nextSetBit(i + 1);
            }
        }
    }

    private BitSet identifyPassesToKeep() {
        ArrayDeque<Pass> deque = new ArrayDeque<Pass>(this.passes.size());
        BitSet bitSet = new BitSet(this.passes.size());
        for (VirtualResource virtualResource : this.externalResources) {
            Pass pass = virtualResource.handle.createdBy;
            if (pass == null) continue;
            this.discoverAllRequiredPasses(pass, bitSet, deque);
        }
        for (Pass pass : this.passes) {
            if (!pass.disableCulling) continue;
            this.discoverAllRequiredPasses(pass, bitSet, deque);
        }
        return bitSet;
    }

    private void discoverAllRequiredPasses(Pass pass, BitSet bitSet, Deque<Pass> deque) {
        deque.add(pass);
        while (!deque.isEmpty()) {
            Pass pass2 = deque.poll();
            if (bitSet.get(pass2.id)) continue;
            bitSet.set(pass2.id);
            int i = pass2.requiredPassIds.nextSetBit(0);
            while (i >= 0) {
                deque.add(this.passes.get(i));
                i = pass2.requiredPassIds.nextSetBit(i + 1);
            }
        }
    }

    private void resolvePassOrder(Pass pass, BitSet bitSet, BitSet bitSet2, List<Pass> list) {
        if (bitSet2.get(pass.id)) {
            String string = bitSet2.stream().mapToObj(i -> this.passes.get((int)i).name).collect(Collectors.joining(", "));
            throw new IllegalStateException("Frame graph cycle detected between " + string);
        }
        if (!bitSet.get(pass.id)) {
            return;
        }
        bitSet2.set(pass.id);
        bitSet.clear(pass.id);
        int i2 = pass.requiredPassIds.nextSetBit(0);
        while (i2 >= 0) {
            this.resolvePassOrder(this.passes.get(i2), bitSet, bitSet2, list);
            i2 = pass.requiredPassIds.nextSetBit(i2 + 1);
        }
        for (Handle<?> handle : pass.writesFrom) {
            int j = handle.readBy.nextSetBit(0);
            while (j >= 0) {
                if (j != pass.id) {
                    this.resolvePassOrder(this.passes.get(j), bitSet, bitSet2, list);
                }
                j = handle.readBy.nextSetBit(j + 1);
            }
        }
        list.add(pass);
        bitSet2.clear(pass.id);
    }

    private void assignResourceLifetimes(Collection<Pass> collection) {
        @Nullable Pass[] passs = new Pass[this.internalResources.size()];
        for (Pass pass : collection) {
            int i = pass.requiredResourceIds.nextSetBit(0);
            while (i >= 0) {
                InternalVirtualResource<?> internalVirtualResource = this.internalResources.get(i);
                Pass pass2 = passs[i];
                passs[i] = pass;
                if (pass2 == null) {
                    pass.resourcesToAcquire.add(internalVirtualResource);
                } else {
                    pass2.resourcesToRelease.clear(i);
                }
                pass.resourcesToRelease.set(i);
                i = pass.requiredResourceIds.nextSetBit(i + 1);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class Pass
    implements FramePass {
        final int id;
        final String name;
        final List<Handle<?>> writesFrom = new ArrayList();
        final BitSet requiredResourceIds = new BitSet();
        final BitSet requiredPassIds = new BitSet();
        Runnable task = () -> {};
        final List<InternalVirtualResource<?>> resourcesToAcquire = new ArrayList();
        final BitSet resourcesToRelease = new BitSet();
        boolean disableCulling;

        public Pass(int i, String string) {
            this.id = i;
            this.name = string;
        }

        private <T> void markResourceRequired(Handle<T> handle) {
            VirtualResource virtualResource = handle.holder;
            if (virtualResource instanceof InternalVirtualResource) {
                InternalVirtualResource internalVirtualResource = (InternalVirtualResource)virtualResource;
                this.requiredResourceIds.set(internalVirtualResource.id);
            }
        }

        private void markPassRequired(Pass pass) {
            this.requiredPassIds.set(pass.id);
        }

        @Override
        public <T> ResourceHandle<T> createsInternal(String string, ResourceDescriptor<T> resourceDescriptor) {
            InternalVirtualResource<T> internalVirtualResource = FrameGraphBuilder.this.createInternalResource(string, resourceDescriptor, this);
            this.requiredResourceIds.set(internalVirtualResource.id);
            return internalVirtualResource.handle;
        }

        @Override
        public <T> void reads(ResourceHandle<T> resourceHandle) {
            this._reads((Handle)resourceHandle);
        }

        private <T> void _reads(Handle<T> handle) {
            this.markResourceRequired(handle);
            if (handle.createdBy != null) {
                this.markPassRequired(handle.createdBy);
            }
            handle.readBy.set(this.id);
        }

        @Override
        public <T> ResourceHandle<T> readsAndWrites(ResourceHandle<T> resourceHandle) {
            return this._readsAndWrites((Handle)resourceHandle);
        }

        @Override
        public void requires(FramePass framePass) {
            this.requiredPassIds.set(((Pass)framePass).id);
        }

        @Override
        public void disableCulling() {
            this.disableCulling = true;
        }

        private <T> Handle<T> _readsAndWrites(Handle<T> handle) {
            this.writesFrom.add(handle);
            this._reads(handle);
            return handle.writeAndAlias(this);
        }

        @Override
        public void executes(Runnable runnable) {
            this.task = runnable;
        }

        public String toString() {
            return this.name;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ExternalResource<T>
    extends VirtualResource<T> {
        private final T resource;

        public ExternalResource(String string, @Nullable Pass pass, T object) {
            super(string, pass);
            this.resource = object;
        }

        @Override
        public T get() {
            return this.resource;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Handle<T>
    implements ResourceHandle<T> {
        final VirtualResource<T> holder;
        private final int version;
        final @Nullable Pass createdBy;
        final BitSet readBy = new BitSet();
        private @Nullable Handle<T> aliasedBy;

        Handle(VirtualResource<T> virtualResource, int i, @Nullable Pass pass) {
            this.holder = virtualResource;
            this.version = i;
            this.createdBy = pass;
        }

        @Override
        public T get() {
            return this.holder.get();
        }

        Handle<T> writeAndAlias(Pass pass) {
            if (this.holder.handle != this) {
                throw new IllegalStateException("Handle " + String.valueOf(this) + " is no longer valid, as its contents were moved into " + String.valueOf(this.aliasedBy));
            }
            Handle<T> handle = new Handle<T>(this.holder, this.version + 1, pass);
            this.holder.handle = handle;
            this.aliasedBy = handle;
            return handle;
        }

        public String toString() {
            if (this.createdBy != null) {
                return String.valueOf(this.holder) + "#" + this.version + " (from " + String.valueOf(this.createdBy) + ")";
            }
            return String.valueOf(this.holder) + "#" + this.version;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class InternalVirtualResource<T>
    extends VirtualResource<T> {
        final int id;
        private final ResourceDescriptor<T> descriptor;
        private @Nullable T physicalResource;

        public InternalVirtualResource(int i, String string, @Nullable Pass pass, ResourceDescriptor<T> resourceDescriptor) {
            super(string, pass);
            this.id = i;
            this.descriptor = resourceDescriptor;
        }

        @Override
        public T get() {
            return Objects.requireNonNull(this.physicalResource, "Resource is not currently available");
        }

        public void acquire(GraphicsResourceAllocator graphicsResourceAllocator) {
            if (this.physicalResource != null) {
                throw new IllegalStateException("Tried to acquire physical resource, but it was already assigned");
            }
            this.physicalResource = graphicsResourceAllocator.acquire(this.descriptor);
        }

        public void release(GraphicsResourceAllocator graphicsResourceAllocator) {
            if (this.physicalResource == null) {
                throw new IllegalStateException("Tried to release physical resource that was not allocated");
            }
            graphicsResourceAllocator.release(this.descriptor, this.physicalResource);
            this.physicalResource = null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Inspector {
        public static final Inspector NONE = new Inspector(){};

        default public void acquireResource(String string) {
        }

        default public void releaseResource(String string) {
        }

        default public void beforeExecutePass(String string) {
        }

        default public void afterExecutePass(String string) {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static abstract class VirtualResource<T> {
        public final String name;
        public Handle<T> handle;

        public VirtualResource(String string, @Nullable Pass pass) {
            this.name = string;
            this.handle = new Handle(this, 0, pass);
        }

        public abstract T get();

        public String toString() {
            return this.name;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.resource;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceDescriptor;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class CrossFrameResourcePool
implements GraphicsResourceAllocator,
AutoCloseable {
    private final int framesToKeepResource;
    private final Deque<ResourceEntry<?>> pool = new ArrayDeque();

    public CrossFrameResourcePool(int i) {
        this.framesToKeepResource = i;
    }

    public void endFrame() {
        Iterator<ResourceEntry<?>> iterator = this.pool.iterator();
        while (iterator.hasNext()) {
            ResourceEntry<?> resourceEntry = iterator.next();
            if (resourceEntry.framesToLive-- != 0) continue;
            resourceEntry.close();
            iterator.remove();
        }
    }

    @Override
    public <T> T acquire(ResourceDescriptor<T> resourceDescriptor) {
        T object = this.acquireWithoutPreparing(resourceDescriptor);
        resourceDescriptor.prepare(object);
        return object;
    }

    private <T> T acquireWithoutPreparing(ResourceDescriptor<T> resourceDescriptor) {
        Iterator<ResourceEntry<?>> iterator = this.pool.iterator();
        while (iterator.hasNext()) {
            ResourceEntry<?> resourceEntry = iterator.next();
            if (!resourceDescriptor.canUsePhysicalResource(resourceEntry.descriptor)) continue;
            iterator.remove();
            return resourceEntry.value;
        }
        return resourceDescriptor.allocate();
    }

    @Override
    public <T> void release(ResourceDescriptor<T> resourceDescriptor, T object) {
        this.pool.addFirst(new ResourceEntry<T>(resourceDescriptor, object, this.framesToKeepResource));
    }

    public void clear() {
        this.pool.forEach(ResourceEntry::close);
        this.pool.clear();
    }

    @Override
    public void close() {
        this.clear();
    }

    @VisibleForTesting
    protected Collection<ResourceEntry<?>> entries() {
        return this.pool;
    }

    @Environment(value=EnvType.CLIENT)
    @VisibleForTesting
    protected static final class ResourceEntry<T>
    implements AutoCloseable {
        final ResourceDescriptor<T> descriptor;
        final T value;
        int framesToLive;

        ResourceEntry(ResourceDescriptor<T> resourceDescriptor, T object, int i) {
            this.descriptor = resourceDescriptor;
            this.value = object;
            this.framesToLive = i;
        }

        @Override
        public void close() {
            this.descriptor.free(this.value);
        }
    }
}


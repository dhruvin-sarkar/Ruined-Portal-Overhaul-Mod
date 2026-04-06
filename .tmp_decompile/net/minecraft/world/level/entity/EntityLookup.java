/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class EntityLookup<T extends EntityAccess> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Int2ObjectMap<T> byId = new Int2ObjectLinkedOpenHashMap();
    private final Map<UUID, T> byUuid = Maps.newHashMap();

    public <U extends T> void getEntities(EntityTypeTest<T, U> entityTypeTest, AbortableIterationConsumer<U> abortableIterationConsumer) {
        for (EntityAccess entityAccess : this.byId.values()) {
            EntityAccess entityAccess2 = (EntityAccess)entityTypeTest.tryCast(entityAccess);
            if (entityAccess2 == null || !abortableIterationConsumer.accept(entityAccess2).shouldAbort()) continue;
            return;
        }
    }

    public Iterable<T> getAllEntities() {
        return Iterables.unmodifiableIterable((Iterable)this.byId.values());
    }

    public void add(T entityAccess) {
        UUID uUID = entityAccess.getUUID();
        if (this.byUuid.containsKey(uUID)) {
            LOGGER.warn("Duplicate entity UUID {}: {}", (Object)uUID, entityAccess);
            return;
        }
        this.byUuid.put(uUID, entityAccess);
        this.byId.put(entityAccess.getId(), entityAccess);
    }

    public void remove(T entityAccess) {
        this.byUuid.remove(entityAccess.getUUID());
        this.byId.remove(entityAccess.getId());
    }

    public @Nullable T getEntity(int i) {
        return (T)((EntityAccess)this.byId.get(i));
    }

    public @Nullable T getEntity(UUID uUID) {
        return (T)((EntityAccess)this.byUuid.get(uUID));
    }

    public int count() {
        return this.byUuid.size();
    }
}


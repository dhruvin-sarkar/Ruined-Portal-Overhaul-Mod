/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectFunction
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongAVLTreeSet
 *  it.unimi.dsi.fastutil.longs.LongBidirectionalIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.longs.LongSortedSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class EntitySectionStorage<T extends EntityAccess> {
    public static final int CHONKY_ENTITY_SEARCH_GRACE = 2;
    public static final int MAX_NON_CHONKY_ENTITY_SIZE = 4;
    private final Class<T> entityClass;
    private final Long2ObjectFunction<Visibility> intialSectionVisibility;
    private final Long2ObjectMap<EntitySection<T>> sections = new Long2ObjectOpenHashMap();
    private final LongSortedSet sectionIds = new LongAVLTreeSet();

    public EntitySectionStorage(Class<T> class_, Long2ObjectFunction<Visibility> long2ObjectFunction) {
        this.entityClass = class_;
        this.intialSectionVisibility = long2ObjectFunction;
    }

    public void forEachAccessibleNonEmptySection(AABB aABB, AbortableIterationConsumer<EntitySection<T>> abortableIterationConsumer) {
        int i = SectionPos.posToSectionCoord(aABB.minX - 2.0);
        int j = SectionPos.posToSectionCoord(aABB.minY - 4.0);
        int k = SectionPos.posToSectionCoord(aABB.minZ - 2.0);
        int l = SectionPos.posToSectionCoord(aABB.maxX + 2.0);
        int m = SectionPos.posToSectionCoord(aABB.maxY + 0.0);
        int n = SectionPos.posToSectionCoord(aABB.maxZ + 2.0);
        for (int o = i; o <= l; ++o) {
            long p = SectionPos.asLong(o, 0, 0);
            long q = SectionPos.asLong(o, -1, -1);
            LongBidirectionalIterator longIterator = this.sectionIds.subSet(p, q + 1L).iterator();
            while (longIterator.hasNext()) {
                EntitySection entitySection;
                long r = longIterator.nextLong();
                int s = SectionPos.y(r);
                int t = SectionPos.z(r);
                if (s < j || s > m || t < k || t > n || (entitySection = (EntitySection)this.sections.get(r)) == null || entitySection.isEmpty() || !entitySection.getStatus().isAccessible() || !abortableIterationConsumer.accept(entitySection).shouldAbort()) continue;
                return;
            }
        }
    }

    public LongStream getExistingSectionPositionsInChunk(long l) {
        int j;
        int i = ChunkPos.getX(l);
        LongSortedSet longSortedSet = this.getChunkSections(i, j = ChunkPos.getZ(l));
        if (longSortedSet.isEmpty()) {
            return LongStream.empty();
        }
        LongBidirectionalIterator ofLong = longSortedSet.iterator();
        return StreamSupport.longStream(Spliterators.spliteratorUnknownSize((PrimitiveIterator.OfLong)ofLong, 1301), false);
    }

    private LongSortedSet getChunkSections(int i, int j) {
        long l = SectionPos.asLong(i, 0, j);
        long m = SectionPos.asLong(i, -1, j);
        return this.sectionIds.subSet(l, m + 1L);
    }

    public Stream<EntitySection<T>> getExistingSectionsInChunk(long l) {
        return this.getExistingSectionPositionsInChunk(l).mapToObj(arg_0 -> this.sections.get(arg_0)).filter(Objects::nonNull);
    }

    private static long getChunkKeyFromSectionKey(long l) {
        return ChunkPos.asLong(SectionPos.x(l), SectionPos.z(l));
    }

    public EntitySection<T> getOrCreateSection(long l) {
        return (EntitySection)this.sections.computeIfAbsent(l, this::createSection);
    }

    public @Nullable EntitySection<T> getSection(long l) {
        return (EntitySection)this.sections.get(l);
    }

    private EntitySection<T> createSection(long l) {
        long m = EntitySectionStorage.getChunkKeyFromSectionKey(l);
        Visibility visibility = (Visibility)((Object)this.intialSectionVisibility.get(m));
        this.sectionIds.add(l);
        return new EntitySection<T>(this.entityClass, visibility);
    }

    public LongSet getAllChunksWithExistingSections() {
        LongOpenHashSet longSet = new LongOpenHashSet();
        this.sections.keySet().forEach(arg_0 -> EntitySectionStorage.method_31780((LongSet)longSet, arg_0));
        return longSet;
    }

    public void getEntities(AABB aABB, AbortableIterationConsumer<T> abortableIterationConsumer) {
        this.forEachAccessibleNonEmptySection(aABB, entitySection -> entitySection.getEntities(aABB, abortableIterationConsumer));
    }

    public <U extends T> void getEntities(EntityTypeTest<T, U> entityTypeTest, AABB aABB, AbortableIterationConsumer<U> abortableIterationConsumer) {
        this.forEachAccessibleNonEmptySection(aABB, entitySection -> entitySection.getEntities(entityTypeTest, aABB, abortableIterationConsumer));
    }

    public void remove(long l) {
        this.sections.remove(l);
        this.sectionIds.remove(l);
    }

    @VisibleForDebug
    public int count() {
        return this.sectionIds.size();
    }

    private static /* synthetic */ void method_31780(LongSet longSet, long l) {
        longSet.add(EntitySectionStorage.getChunkKeyFromSectionKey(l));
    }
}


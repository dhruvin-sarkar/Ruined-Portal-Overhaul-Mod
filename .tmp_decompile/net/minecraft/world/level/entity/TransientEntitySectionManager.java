/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.Long2ObjectFunction
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.level.entity.Visibility;
import org.slf4j.Logger;

public class TransientEntitySectionManager<T extends EntityAccess> {
    static final Logger LOGGER = LogUtils.getLogger();
    final LevelCallback<T> callbacks;
    final EntityLookup<T> entityStorage;
    final EntitySectionStorage<T> sectionStorage;
    private final LongSet tickingChunks = new LongOpenHashSet();
    private final LevelEntityGetter<T> entityGetter;

    public TransientEntitySectionManager(Class<T> class_, LevelCallback<T> levelCallback) {
        this.entityStorage = new EntityLookup();
        this.sectionStorage = new EntitySectionStorage<T>(class_, (Long2ObjectFunction<Visibility>)((Long2ObjectFunction)l -> this.tickingChunks.contains(l) ? Visibility.TICKING : Visibility.TRACKED));
        this.callbacks = levelCallback;
        this.entityGetter = new LevelEntityGetterAdapter<T>(this.entityStorage, this.sectionStorage);
    }

    public void startTicking(ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        this.tickingChunks.add(l);
        this.sectionStorage.getExistingSectionsInChunk(l).forEach(entitySection -> {
            Visibility visibility = entitySection.updateChunkStatus(Visibility.TICKING);
            if (!visibility.isTicking()) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this.callbacks::onTickingStart);
            }
        });
    }

    public void stopTicking(ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        this.tickingChunks.remove(l);
        this.sectionStorage.getExistingSectionsInChunk(l).forEach(entitySection -> {
            Visibility visibility = entitySection.updateChunkStatus(Visibility.TRACKED);
            if (visibility.isTicking()) {
                entitySection.getEntities().filter(entityAccess -> !entityAccess.isAlwaysTicking()).forEach(this.callbacks::onTickingEnd);
            }
        });
    }

    public LevelEntityGetter<T> getEntityGetter() {
        return this.entityGetter;
    }

    public void addEntity(T entityAccess) {
        this.entityStorage.add(entityAccess);
        long l = SectionPos.asLong(entityAccess.blockPosition());
        EntitySection<T> entitySection = this.sectionStorage.getOrCreateSection(l);
        entitySection.add(entityAccess);
        entityAccess.setLevelCallback(new Callback(this, entityAccess, l, entitySection));
        this.callbacks.onCreated(entityAccess);
        this.callbacks.onTrackingStart(entityAccess);
        if (entityAccess.isAlwaysTicking() || entitySection.getStatus().isTicking()) {
            this.callbacks.onTickingStart(entityAccess);
        }
    }

    @VisibleForDebug
    public int count() {
        return this.entityStorage.count();
    }

    void removeSectionIfEmpty(long l, EntitySection<T> entitySection) {
        if (entitySection.isEmpty()) {
            this.sectionStorage.remove(l);
        }
    }

    @VisibleForDebug
    public String gatherStats() {
        return this.entityStorage.count() + "," + this.sectionStorage.count() + "," + this.tickingChunks.size();
    }

    class Callback
    implements EntityInLevelCallback {
        private final T entity;
        private long currentSectionKey;
        private EntitySection<T> currentSection;
        final /* synthetic */ TransientEntitySectionManager field_27285;

        /*
         * WARNING - Possible parameter corruption
         * WARNING - void declaration
         */
        Callback(T t, long entitySection, EntitySection<T> entitySection2) {
            void var3_3;
            void entityAccess;
            this.field_27285 = (TransientEntitySectionManager)transientEntitySectionManager;
            this.entity = entityAccess;
            this.currentSectionKey = var3_3;
            this.currentSection = (EntitySection)entitySection;
        }

        @Override
        public void onMove() {
            BlockPos blockPos = this.entity.blockPosition();
            long l = SectionPos.asLong(blockPos);
            if (l != this.currentSectionKey) {
                Visibility visibility = this.currentSection.getStatus();
                if (!this.currentSection.remove(this.entity)) {
                    LOGGER.warn("Entity {} wasn't found in section {} (moving to {})", new Object[]{this.entity, SectionPos.of(this.currentSectionKey), l});
                }
                this.field_27285.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
                EntitySection entitySection = this.field_27285.sectionStorage.getOrCreateSection(l);
                entitySection.add(this.entity);
                this.currentSection = entitySection;
                this.currentSectionKey = l;
                this.field_27285.callbacks.onSectionChange(this.entity);
                if (!this.entity.isAlwaysTicking()) {
                    boolean bl = visibility.isTicking();
                    boolean bl2 = entitySection.getStatus().isTicking();
                    if (bl && !bl2) {
                        this.field_27285.callbacks.onTickingEnd(this.entity);
                    } else if (!bl && bl2) {
                        this.field_27285.callbacks.onTickingStart(this.entity);
                    }
                }
            }
        }

        @Override
        public void onRemove(Entity.RemovalReason removalReason) {
            Visibility visibility;
            if (!this.currentSection.remove(this.entity)) {
                LOGGER.warn("Entity {} wasn't found in section {} (destroying due to {})", new Object[]{this.entity, SectionPos.of(this.currentSectionKey), removalReason});
            }
            if ((visibility = this.currentSection.getStatus()).isTicking() || this.entity.isAlwaysTicking()) {
                this.field_27285.callbacks.onTickingEnd(this.entity);
            }
            this.field_27285.callbacks.onTrackingEnd(this.entity);
            this.field_27285.callbacks.onDestroyed(this.entity);
            this.field_27285.entityStorage.remove(this.entity);
            this.entity.setLevelCallback(NULL);
            this.field_27285.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
        }
    }
}


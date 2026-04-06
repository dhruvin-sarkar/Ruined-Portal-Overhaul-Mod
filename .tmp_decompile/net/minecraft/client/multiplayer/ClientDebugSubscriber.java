/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.multiplayer;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientDebugSubscriber {
    private final ClientPacketListener connection;
    private final DebugScreenOverlay debugScreenOverlay;
    private Set<DebugSubscription<?>> remoteSubscriptions = Set.of();
    private final Map<DebugSubscription<?>, ValueMaps<?>> valuesBySubscription = new HashMap();

    public ClientDebugSubscriber(ClientPacketListener clientPacketListener, DebugScreenOverlay debugScreenOverlay) {
        this.debugScreenOverlay = debugScreenOverlay;
        this.connection = clientPacketListener;
    }

    private static void addFlag(Set<DebugSubscription<?>> set, DebugSubscription<?> debugSubscription, boolean bl) {
        if (bl) {
            set.add(debugSubscription);
        }
    }

    private Set<DebugSubscription<?>> requestedSubscriptions() {
        ReferenceOpenHashSet set = new ReferenceOpenHashSet();
        ClientDebugSubscriber.addFlag(set, RemoteDebugSampleType.TICK_TIME.subscription(), this.debugScreenOverlay.showFpsCharts());
        if (SharedConstants.DEBUG_ENABLED) {
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.BEES, SharedConstants.DEBUG_BEES);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.BEE_HIVES, SharedConstants.DEBUG_BEES);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.BRAINS, SharedConstants.DEBUG_BRAIN);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.BREEZES, SharedConstants.DEBUG_BREEZE_MOB);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.ENTITY_BLOCK_INTERSECTIONS, SharedConstants.DEBUG_ENTITY_BLOCK_INTERSECTION);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.ENTITY_PATHS, SharedConstants.DEBUG_PATHFINDING);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.GAME_EVENTS, SharedConstants.DEBUG_GAME_EVENT_LISTENERS);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.GAME_EVENT_LISTENERS, SharedConstants.DEBUG_GAME_EVENT_LISTENERS);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.GOAL_SELECTORS, SharedConstants.DEBUG_GOAL_SELECTOR || SharedConstants.DEBUG_BEES);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.NEIGHBOR_UPDATES, SharedConstants.DEBUG_NEIGHBORSUPDATE);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.POIS, SharedConstants.DEBUG_POI);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.RAIDS, SharedConstants.DEBUG_RAIDS);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.REDSTONE_WIRE_ORIENTATIONS, SharedConstants.DEBUG_EXPERIMENTAL_REDSTONEWIRE_UPDATE_ORDER);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.STRUCTURES, SharedConstants.DEBUG_STRUCTURES);
            ClientDebugSubscriber.addFlag(set, DebugSubscriptions.VILLAGE_SECTIONS, SharedConstants.DEBUG_VILLAGE_SECTIONS);
        }
        return set;
    }

    public void clear() {
        this.remoteSubscriptions = Set.of();
        this.dropLevel();
    }

    public void tick(long l) {
        Set<DebugSubscription<?>> set = this.requestedSubscriptions();
        if (!set.equals(this.remoteSubscriptions)) {
            this.remoteSubscriptions = set;
            this.onSubscriptionsChanged(set);
        }
        this.valuesBySubscription.forEach((debugSubscription, valueMaps) -> {
            if (debugSubscription.expireAfterTicks() != 0) {
                valueMaps.purgeExpired(l);
            }
        });
    }

    private void onSubscriptionsChanged(Set<DebugSubscription<?>> set) {
        this.valuesBySubscription.keySet().retainAll(set);
        this.initializeSubscriptions(set);
        this.connection.send(new ServerboundDebugSubscriptionRequestPacket(set));
    }

    private void initializeSubscriptions(Set<DebugSubscription<?>> set) {
        for (DebugSubscription<?> debugSubscription2 : set) {
            this.valuesBySubscription.computeIfAbsent(debugSubscription2, debugSubscription -> new ValueMaps());
        }
    }

    <V> @Nullable ValueMaps<V> getValueMaps(DebugSubscription<V> debugSubscription) {
        return this.valuesBySubscription.get(debugSubscription);
    }

    private <K, V> @Nullable ValueMap<K, V> getValueMap(DebugSubscription<V> debugSubscription, ValueMapType<K, V> valueMapType) {
        ValueMaps<V> valueMaps = this.getValueMaps(debugSubscription);
        return valueMaps != null ? valueMapType.get(valueMaps) : null;
    }

    <K, V> @Nullable V getValue(DebugSubscription<V> debugSubscription, K object, ValueMapType<K, V> valueMapType) {
        ValueMap<K, V> valueMap = this.getValueMap(debugSubscription, valueMapType);
        return valueMap != null ? (V)valueMap.getValue(object) : null;
    }

    public DebugValueAccess createDebugValueAccess(final Level level) {
        return new DebugValueAccess(){

            @Override
            public <T> void forEachChunk(DebugSubscription<T> debugSubscription, BiConsumer<ChunkPos, T> biConsumer) {
                ClientDebugSubscriber.this.forEachValue(debugSubscription, ClientDebugSubscriber.chunks(), biConsumer);
            }

            @Override
            public <T> @Nullable T getChunkValue(DebugSubscription<T> debugSubscription, ChunkPos chunkPos) {
                return ClientDebugSubscriber.this.getValue(debugSubscription, chunkPos, ClientDebugSubscriber.chunks());
            }

            @Override
            public <T> void forEachBlock(DebugSubscription<T> debugSubscription, BiConsumer<BlockPos, T> biConsumer) {
                ClientDebugSubscriber.this.forEachValue(debugSubscription, ClientDebugSubscriber.blocks(), biConsumer);
            }

            @Override
            public <T> @Nullable T getBlockValue(DebugSubscription<T> debugSubscription, BlockPos blockPos) {
                return ClientDebugSubscriber.this.getValue(debugSubscription, blockPos, ClientDebugSubscriber.blocks());
            }

            @Override
            public <T> void forEachEntity(DebugSubscription<T> debugSubscription, BiConsumer<Entity, T> biConsumer) {
                ClientDebugSubscriber.this.forEachValue(debugSubscription, ClientDebugSubscriber.entities(), (uUID, object) -> {
                    Entity entity = level.getEntity((UUID)uUID);
                    if (entity != null) {
                        biConsumer.accept(entity, object);
                    }
                });
            }

            @Override
            public <T> @Nullable T getEntityValue(DebugSubscription<T> debugSubscription, Entity entity) {
                return ClientDebugSubscriber.this.getValue(debugSubscription, entity.getUUID(), ClientDebugSubscriber.entities());
            }

            @Override
            public <T> void forEachEvent(DebugSubscription<T> debugSubscription, DebugValueAccess.EventVisitor<T> eventVisitor) {
                ValueMaps<T> valueMaps = ClientDebugSubscriber.this.getValueMaps(debugSubscription);
                if (valueMaps == null) {
                    return;
                }
                long l = level.getGameTime();
                for (ValueWrapper valueWrapper : valueMaps.events) {
                    int i = (int)(valueWrapper.expiresAfterTime() - l);
                    int j = debugSubscription.expireAfterTicks();
                    eventVisitor.accept(valueWrapper.value(), i, j);
                }
            }
        };
    }

    public <T> void updateChunk(long l, ChunkPos chunkPos, DebugSubscription.Update<T> update) {
        this.updateMap(l, chunkPos, update, ClientDebugSubscriber.chunks());
    }

    public <T> void updateBlock(long l, BlockPos blockPos, DebugSubscription.Update<T> update) {
        this.updateMap(l, blockPos, update, ClientDebugSubscriber.blocks());
    }

    public <T> void updateEntity(long l, Entity entity, DebugSubscription.Update<T> update) {
        this.updateMap(l, entity.getUUID(), update, ClientDebugSubscriber.entities());
    }

    public <T> void pushEvent(long l, DebugSubscription.Event<T> event) {
        ValueMaps<T> valueMaps = this.getValueMaps(event.subscription());
        if (valueMaps != null) {
            valueMaps.events.add(new ValueWrapper<T>(event.value(), l + (long)event.subscription().expireAfterTicks()));
        }
    }

    private <K, V> void updateMap(long l, K object, DebugSubscription.Update<V> update, ValueMapType<K, V> valueMapType) {
        ValueMap<K, V> valueMap = this.getValueMap(update.subscription(), valueMapType);
        if (valueMap != null) {
            valueMap.apply(l, object, update);
        }
    }

    <K, V> void forEachValue(DebugSubscription<V> debugSubscription, ValueMapType<K, V> valueMapType, BiConsumer<K, V> biConsumer) {
        ValueMap<K, V> valueMap = this.getValueMap(debugSubscription, valueMapType);
        if (valueMap != null) {
            valueMap.forEach(biConsumer);
        }
    }

    public void dropLevel() {
        this.valuesBySubscription.clear();
        this.initializeSubscriptions(this.remoteSubscriptions);
    }

    public void dropChunk(ChunkPos chunkPos) {
        if (this.valuesBySubscription.isEmpty()) {
            return;
        }
        for (ValueMaps<?> valueMaps : this.valuesBySubscription.values()) {
            valueMaps.dropChunkAndBlocks(chunkPos);
        }
    }

    public void dropEntity(Entity entity) {
        if (this.valuesBySubscription.isEmpty()) {
            return;
        }
        for (ValueMaps<?> valueMaps : this.valuesBySubscription.values()) {
            valueMaps.entityValues.removeKey(entity.getUUID());
        }
    }

    static <T> ValueMapType<UUID, T> entities() {
        return valueMaps -> valueMaps.entityValues;
    }

    static <T> ValueMapType<BlockPos, T> blocks() {
        return valueMaps -> valueMaps.blockValues;
    }

    static <T> ValueMapType<ChunkPos, T> chunks() {
        return valueMaps -> valueMaps.chunkValues;
    }

    @Environment(value=EnvType.CLIENT)
    static class ValueMaps<V> {
        final ValueMap<ChunkPos, V> chunkValues = new ValueMap();
        final ValueMap<BlockPos, V> blockValues = new ValueMap();
        final ValueMap<UUID, V> entityValues = new ValueMap();
        final List<ValueWrapper<V>> events = new ArrayList<ValueWrapper<V>>();

        ValueMaps() {
        }

        public void purgeExpired(long l) {
            Predicate predicate = valueWrapper -> valueWrapper.hasExpired(l);
            this.chunkValues.removeValues(predicate);
            this.blockValues.removeValues(predicate);
            this.entityValues.removeValues(predicate);
            this.events.removeIf(predicate);
        }

        public void dropChunkAndBlocks(ChunkPos chunkPos) {
            this.chunkValues.removeKey(chunkPos);
            this.blockValues.removeKeys(chunkPos::contains);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface ValueMapType<K, V> {
        public ValueMap<K, V> get(ValueMaps<V> var1);
    }

    @Environment(value=EnvType.CLIENT)
    static class ValueMap<K, V> {
        private final Map<K, ValueWrapper<V>> values = new HashMap<K, ValueWrapper<V>>();

        ValueMap() {
        }

        public void removeValues(Predicate<ValueWrapper<V>> predicate) {
            this.values.values().removeIf(predicate);
        }

        public void removeKey(K object) {
            this.values.remove(object);
        }

        public void removeKeys(Predicate<K> predicate) {
            this.values.keySet().removeIf(predicate);
        }

        public @Nullable V getValue(K object) {
            ValueWrapper<V> valueWrapper = this.values.get(object);
            return valueWrapper != null ? (V)valueWrapper.value() : null;
        }

        public void apply(long l, K object, DebugSubscription.Update<V> update) {
            if (update.value().isPresent()) {
                this.values.put(object, new ValueWrapper<V>(update.value().get(), l + (long)update.subscription().expireAfterTicks()));
            } else {
                this.values.remove(object);
            }
        }

        public void forEach(BiConsumer<K, V> biConsumer) {
            this.values.forEach((? super K object, ? super V valueWrapper) -> biConsumer.accept(object, valueWrapper.value()));
        }
    }

    @Environment(value=EnvType.CLIENT)
    record ValueWrapper<T>(T value, long expiresAfterTime) {
        private static final long NO_EXPIRY = -1L;

        public boolean hasExpired(long l) {
            if (this.expiresAfterTime == -1L) {
                return false;
            }
            return l >= this.expiresAfterTime;
        }
    }
}


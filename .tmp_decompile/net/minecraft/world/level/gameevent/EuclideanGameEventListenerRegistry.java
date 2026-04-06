/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 */
package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debug.DebugGameEventListenerInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class EuclideanGameEventListenerRegistry
implements GameEventListenerRegistry {
    private final List<GameEventListener> listeners = Lists.newArrayList();
    private final Set<GameEventListener> listenersToRemove = Sets.newHashSet();
    private final List<GameEventListener> listenersToAdd = Lists.newArrayList();
    private boolean processing;
    private final ServerLevel level;
    private final int sectionY;
    private final OnEmptyAction onEmptyAction;

    public EuclideanGameEventListenerRegistry(ServerLevel serverLevel, int i, OnEmptyAction onEmptyAction) {
        this.level = serverLevel;
        this.sectionY = i;
        this.onEmptyAction = onEmptyAction;
    }

    @Override
    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    @Override
    public void register(GameEventListener gameEventListener) {
        if (this.processing) {
            this.listenersToAdd.add(gameEventListener);
        } else {
            this.listeners.add(gameEventListener);
        }
        EuclideanGameEventListenerRegistry.sendDebugInfo(this.level, gameEventListener);
    }

    private static void sendDebugInfo(ServerLevel serverLevel, GameEventListener gameEventListener) {
        EntityPositionSource entityPositionSource;
        Entity entity;
        if (!serverLevel.debugSynchronizers().hasAnySubscriberFor(DebugSubscriptions.GAME_EVENT_LISTENERS)) {
            return;
        }
        DebugGameEventListenerInfo debugGameEventListenerInfo = new DebugGameEventListenerInfo(gameEventListener.getListenerRadius());
        PositionSource positionSource = gameEventListener.getListenerSource();
        if (positionSource instanceof BlockPositionSource) {
            BlockPositionSource blockPositionSource = (BlockPositionSource)positionSource;
            serverLevel.debugSynchronizers().sendBlockValue(blockPositionSource.pos(), DebugSubscriptions.GAME_EVENT_LISTENERS, debugGameEventListenerInfo);
        } else if (positionSource instanceof EntityPositionSource && (entity = serverLevel.getEntity((entityPositionSource = (EntityPositionSource)positionSource).getUuid())) != null) {
            serverLevel.debugSynchronizers().sendEntityValue(entity, DebugSubscriptions.GAME_EVENT_LISTENERS, debugGameEventListenerInfo);
        }
    }

    @Override
    public void unregister(GameEventListener gameEventListener) {
        if (this.processing) {
            this.listenersToRemove.add(gameEventListener);
        } else {
            this.listeners.remove(gameEventListener);
        }
        if (this.listeners.isEmpty()) {
            this.onEmptyAction.apply(this.sectionY);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean visitInRangeListeners(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context, GameEventListenerRegistry.ListenerVisitor listenerVisitor) {
        this.processing = true;
        boolean bl = false;
        try {
            Iterator<GameEventListener> iterator = this.listeners.iterator();
            while (iterator.hasNext()) {
                GameEventListener gameEventListener = iterator.next();
                if (this.listenersToRemove.remove(gameEventListener)) {
                    iterator.remove();
                    continue;
                }
                Optional<Vec3> optional = EuclideanGameEventListenerRegistry.getPostableListenerPosition(this.level, vec3, gameEventListener);
                if (!optional.isPresent()) continue;
                listenerVisitor.visit(gameEventListener, optional.get());
                bl = true;
            }
        }
        finally {
            this.processing = false;
        }
        if (!this.listenersToAdd.isEmpty()) {
            this.listeners.addAll(this.listenersToAdd);
            this.listenersToAdd.clear();
        }
        if (!this.listenersToRemove.isEmpty()) {
            this.listeners.removeAll(this.listenersToRemove);
            this.listenersToRemove.clear();
        }
        return bl;
    }

    private static Optional<Vec3> getPostableListenerPosition(ServerLevel serverLevel, Vec3 vec3, GameEventListener gameEventListener) {
        int i;
        Optional<Vec3> optional = gameEventListener.getListenerSource().getPosition(serverLevel);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        double d = BlockPos.containing(optional.get()).distSqr(BlockPos.containing(vec3));
        if (d > (double)(i = gameEventListener.getListenerRadius() * gameEventListener.getListenerRadius())) {
            return Optional.empty();
        }
        return optional;
    }

    @FunctionalInterface
    public static interface OnEmptyAction {
        public void apply(int var1);
    }
}


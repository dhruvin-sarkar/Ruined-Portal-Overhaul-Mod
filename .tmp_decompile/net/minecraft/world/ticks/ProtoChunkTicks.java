/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet
 */
package net.minecraft.world.ticks;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.SavedTick;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.SerializableTickContainer;
import net.minecraft.world.ticks.TickContainerAccess;

public class ProtoChunkTicks<T>
implements SerializableTickContainer<T>,
TickContainerAccess<T> {
    private final List<SavedTick<T>> ticks = Lists.newArrayList();
    private final Set<SavedTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet(SavedTick.UNIQUE_TICK_HASH);

    @Override
    public void schedule(ScheduledTick<T> scheduledTick) {
        SavedTick<T> savedTick = new SavedTick<T>(scheduledTick.type(), scheduledTick.pos(), 0, scheduledTick.priority());
        this.schedule(savedTick);
    }

    @Override
    private void schedule(SavedTick<T> savedTick) {
        if (this.ticksPerPosition.add(savedTick)) {
            this.ticks.add(savedTick);
        }
    }

    @Override
    public boolean hasScheduledTick(BlockPos blockPos, T object) {
        return this.ticksPerPosition.contains(SavedTick.probe(object, blockPos));
    }

    @Override
    public int count() {
        return this.ticks.size();
    }

    @Override
    public List<SavedTick<T>> pack(long l) {
        return this.ticks;
    }

    public List<SavedTick<T>> scheduledTicks() {
        return List.copyOf(this.ticks);
    }

    public static <T> ProtoChunkTicks<T> load(List<SavedTick<T>> list) {
        ProtoChunkTicks<T> protoChunkTicks = new ProtoChunkTicks<T>();
        list.forEach(protoChunkTicks::schedule);
        return protoChunkTicks;
    }
}


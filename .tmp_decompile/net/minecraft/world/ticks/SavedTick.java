/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.Hash$Strategy
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.ticks;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Hash;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.jspecify.annotations.Nullable;

public record SavedTick<T>(T type, BlockPos pos, int delay, TickPriority priority) {
    public static final Hash.Strategy<SavedTick<?>> UNIQUE_TICK_HASH = new Hash.Strategy<SavedTick<?>>(){

        public int hashCode(SavedTick<?> savedTick) {
            return 31 * savedTick.pos().hashCode() + savedTick.type().hashCode();
        }

        public boolean equals(@Nullable SavedTick<?> savedTick, @Nullable SavedTick<?> savedTick2) {
            if (savedTick == savedTick2) {
                return true;
            }
            if (savedTick == null || savedTick2 == null) {
                return false;
            }
            return savedTick.type() == savedTick2.type() && savedTick.pos().equals(savedTick2.pos());
        }

        public /* synthetic */ boolean equals(@Nullable Object object, @Nullable Object object2) {
            return this.equals((SavedTick)((Object)object), (SavedTick)((Object)object2));
        }

        public /* synthetic */ int hashCode(Object object) {
            return this.hashCode((SavedTick)((Object)object));
        }
    };

    public static <T> Codec<SavedTick<T>> codec(Codec<T> codec) {
        MapCodec mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("x").forGetter(Vec3i::getX), (App)Codec.INT.fieldOf("y").forGetter(Vec3i::getY), (App)Codec.INT.fieldOf("z").forGetter(Vec3i::getZ)).apply((Applicative)instance, BlockPos::new));
        return RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf("i").forGetter(SavedTick::type), (App)mapCodec.forGetter(SavedTick::pos), (App)Codec.INT.fieldOf("t").forGetter(SavedTick::delay), (App)TickPriority.CODEC.fieldOf("p").forGetter(SavedTick::priority)).apply((Applicative)instance, SavedTick::new));
    }

    public static <T> List<SavedTick<T>> filterTickListForChunk(List<SavedTick<T>> list, ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        return list.stream().filter(savedTick -> ChunkPos.asLong(savedTick.pos()) == l).toList();
    }

    public ScheduledTick<T> unpack(long l, long m) {
        return new ScheduledTick<T>(this.type, this.pos, l + (long)this.delay, this.priority, m);
    }

    public static <T> SavedTick<T> probe(T object, BlockPos blockPos) {
        return new SavedTick<T>(object, blockPos, 0, TickPriority.NORMAL);
    }
}


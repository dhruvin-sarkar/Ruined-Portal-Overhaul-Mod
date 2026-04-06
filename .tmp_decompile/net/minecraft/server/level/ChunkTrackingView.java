/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import net.minecraft.world.level.ChunkPos;

public interface ChunkTrackingView {
    public static final ChunkTrackingView EMPTY = new ChunkTrackingView(){

        @Override
        public boolean contains(int i, int j, boolean bl) {
            return false;
        }

        @Override
        public void forEach(Consumer<ChunkPos> consumer) {
        }
    };

    public static ChunkTrackingView of(ChunkPos chunkPos, int i) {
        return new Positioned(chunkPos, i);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static void difference(ChunkTrackingView chunkTrackingView, ChunkTrackingView chunkTrackingView2, Consumer<ChunkPos> consumer, Consumer<ChunkPos> consumer2) {
        Positioned positioned2;
        Positioned positioned;
        block8: {
            block7: {
                if (chunkTrackingView.equals(chunkTrackingView2)) {
                    return;
                }
                if (!(chunkTrackingView instanceof Positioned)) break block7;
                positioned = (Positioned)chunkTrackingView;
                if (chunkTrackingView2 instanceof Positioned && positioned.squareIntersects(positioned2 = (Positioned)chunkTrackingView2)) break block8;
            }
            chunkTrackingView.forEach(consumer2);
            chunkTrackingView2.forEach(consumer);
            return;
        }
        int i = Math.min(positioned.minX(), positioned2.minX());
        int j = Math.min(positioned.minZ(), positioned2.minZ());
        int k = Math.max(positioned.maxX(), positioned2.maxX());
        int l = Math.max(positioned.maxZ(), positioned2.maxZ());
        int m = i;
        while (m <= k) {
            for (int n = j; n <= l; ++n) {
                boolean bl2;
                boolean bl = positioned.contains(m, n);
                if (bl == (bl2 = positioned2.contains(m, n))) continue;
                if (bl2) {
                    consumer.accept(new ChunkPos(m, n));
                    continue;
                }
                consumer2.accept(new ChunkPos(m, n));
            }
            ++m;
        }
        return;
    }

    default public boolean contains(ChunkPos chunkPos) {
        return this.contains(chunkPos.x, chunkPos.z);
    }

    default public boolean contains(int i, int j) {
        return this.contains(i, j, true);
    }

    public boolean contains(int var1, int var2, boolean var3);

    public void forEach(Consumer<ChunkPos> var1);

    default public boolean isInViewDistance(int i, int j) {
        return this.contains(i, j, false);
    }

    public static boolean isInViewDistance(int i, int j, int k, int l, int m) {
        return ChunkTrackingView.isWithinDistance(i, j, k, l, m, false);
    }

    public static boolean isWithinDistance(int i, int j, int k, int l, int m, boolean bl) {
        int n = bl ? 2 : 1;
        long o = Math.max(0, Math.abs(l - i) - n);
        long p = Math.max(0, Math.abs(m - j) - n);
        long q = o * o + p * p;
        int r = k * k;
        return q < (long)r;
    }

    public record Positioned(ChunkPos center, int viewDistance) implements ChunkTrackingView
    {
        int minX() {
            return this.center.x - this.viewDistance - 1;
        }

        int minZ() {
            return this.center.z - this.viewDistance - 1;
        }

        int maxX() {
            return this.center.x + this.viewDistance + 1;
        }

        int maxZ() {
            return this.center.z + this.viewDistance + 1;
        }

        @VisibleForTesting
        protected boolean squareIntersects(Positioned positioned) {
            return this.minX() <= positioned.maxX() && this.maxX() >= positioned.minX() && this.minZ() <= positioned.maxZ() && this.maxZ() >= positioned.minZ();
        }

        @Override
        public boolean contains(int i, int j, boolean bl) {
            return ChunkTrackingView.isWithinDistance(this.center.x, this.center.z, this.viewDistance, i, j, bl);
        }

        @Override
        public void forEach(Consumer<ChunkPos> consumer) {
            for (int i = this.minX(); i <= this.maxX(); ++i) {
                for (int j = this.minZ(); j <= this.maxZ(); ++j) {
                    if (!this.contains(i, j)) continue;
                    consumer.accept(new ChunkPos(i, j));
                }
            }
        }
    }
}


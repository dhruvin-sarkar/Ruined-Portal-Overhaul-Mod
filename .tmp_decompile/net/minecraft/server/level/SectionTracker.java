/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.level;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;

public abstract class SectionTracker
extends DynamicGraphMinFixedPoint {
    protected SectionTracker(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected void checkNeighborsAfterUpdate(long l, int i, boolean bl) {
        if (bl && i >= this.levelCount - 2) {
            return;
        }
        for (int j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                for (int m = -1; m <= 1; ++m) {
                    long n = SectionPos.offset(l, j, k, m);
                    if (n == l) continue;
                    this.checkNeighbor(l, n, i, bl);
                }
            }
        }
    }

    @Override
    protected int getComputedLevel(long l, long m, int i) {
        int j = i;
        for (int k = -1; k <= 1; ++k) {
            for (int n = -1; n <= 1; ++n) {
                for (int o = -1; o <= 1; ++o) {
                    long p = SectionPos.offset(l, k, n, o);
                    if (p == l) {
                        p = Long.MAX_VALUE;
                    }
                    if (p == m) continue;
                    int q = this.computeLevelFromNeighbor(p, l, this.getLevel(p));
                    if (j > q) {
                        j = q;
                    }
                    if (j != 0) continue;
                    return j;
                }
            }
        }
        return j;
    }

    @Override
    protected int computeLevelFromNeighbor(long l, long m, int i) {
        if (this.isSource(l)) {
            return this.getLevelFromSource(m);
        }
        return i + 1;
    }

    protected abstract int getLevelFromSource(long var1);

    public void update(long l, int i, boolean bl) {
        this.checkEdge(Long.MAX_VALUE, l, i, bl);
    }
}


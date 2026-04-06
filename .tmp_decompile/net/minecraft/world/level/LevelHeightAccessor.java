/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LevelHeightAccessor {
    public int getHeight();

    public int getMinY();

    default public int getMaxY() {
        return this.getMinY() + this.getHeight() - 1;
    }

    default public int getSectionsCount() {
        return this.getMaxSectionY() - this.getMinSectionY() + 1;
    }

    default public int getMinSectionY() {
        return SectionPos.blockToSectionCoord(this.getMinY());
    }

    default public int getMaxSectionY() {
        return SectionPos.blockToSectionCoord(this.getMaxY());
    }

    default public boolean isInsideBuildHeight(int i) {
        return i >= this.getMinY() && i <= this.getMaxY();
    }

    default public boolean isOutsideBuildHeight(BlockPos blockPos) {
        return this.isOutsideBuildHeight(blockPos.getY());
    }

    default public boolean isOutsideBuildHeight(int i) {
        return i < this.getMinY() || i > this.getMaxY();
    }

    default public int getSectionIndex(int i) {
        return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(i));
    }

    default public int getSectionIndexFromSectionY(int i) {
        return i - this.getMinSectionY();
    }

    default public int getSectionYFromSectionIndex(int i) {
        return i + this.getMinSectionY();
    }

    public static LevelHeightAccessor create(final int i, final int j) {
        return new LevelHeightAccessor(){

            @Override
            public int getHeight() {
                return j;
            }

            @Override
            public int getMinY() {
                return i;
            }
        };
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.border;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.BorderStatus;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WorldBorder
extends SavedData {
    public static final double MAX_SIZE = 5.9999968E7;
    public static final double MAX_CENTER_COORDINATE = 2.9999984E7;
    public static final Codec<WorldBorder> CODEC = Settings.CODEC.xmap(WorldBorder::new, Settings::new);
    public static final SavedDataType<WorldBorder> TYPE = new SavedDataType<WorldBorder>("world_border", WorldBorder::new, CODEC, DataFixTypes.SAVED_DATA_WORLD_BORDER);
    private final Settings settings;
    private boolean initialized;
    private final List<BorderChangeListener> listeners = Lists.newArrayList();
    double damagePerBlock = 0.2;
    double safeZone = 5.0;
    int warningTime = 15;
    int warningBlocks = 5;
    double centerX;
    double centerZ;
    int absoluteMaxSize = 29999984;
    BorderExtent extent = new StaticBorderExtent(5.9999968E7);

    public WorldBorder() {
        this(Settings.DEFAULT);
    }

    public WorldBorder(Settings settings) {
        this.settings = settings;
    }

    public boolean isWithinBounds(BlockPos blockPos) {
        return this.isWithinBounds(blockPos.getX(), blockPos.getZ());
    }

    public boolean isWithinBounds(Vec3 vec3) {
        return this.isWithinBounds(vec3.x, vec3.z);
    }

    public boolean isWithinBounds(ChunkPos chunkPos) {
        return this.isWithinBounds(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ()) && this.isWithinBounds(chunkPos.getMaxBlockX(), chunkPos.getMaxBlockZ());
    }

    public boolean isWithinBounds(AABB aABB) {
        return this.isWithinBounds(aABB.minX, aABB.minZ, aABB.maxX - (double)1.0E-5f, aABB.maxZ - (double)1.0E-5f);
    }

    private boolean isWithinBounds(double d, double e, double f, double g) {
        return this.isWithinBounds(d, e) && this.isWithinBounds(f, g);
    }

    public boolean isWithinBounds(double d, double e) {
        return this.isWithinBounds(d, e, 0.0);
    }

    public boolean isWithinBounds(double d, double e, double f) {
        return d >= this.getMinX() - f && d < this.getMaxX() + f && e >= this.getMinZ() - f && e < this.getMaxZ() + f;
    }

    public BlockPos clampToBounds(BlockPos blockPos) {
        return this.clampToBounds(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public BlockPos clampToBounds(Vec3 vec3) {
        return this.clampToBounds(vec3.x(), vec3.y(), vec3.z());
    }

    public BlockPos clampToBounds(double d, double e, double f) {
        return BlockPos.containing(this.clampVec3ToBound(d, e, f));
    }

    public Vec3 clampVec3ToBound(Vec3 vec3) {
        return this.clampVec3ToBound(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 clampVec3ToBound(double d, double e, double f) {
        return new Vec3(Mth.clamp(d, this.getMinX(), this.getMaxX() - (double)1.0E-5f), e, Mth.clamp(f, this.getMinZ(), this.getMaxZ() - (double)1.0E-5f));
    }

    public double getDistanceToBorder(Entity entity) {
        return this.getDistanceToBorder(entity.getX(), entity.getZ());
    }

    public VoxelShape getCollisionShape() {
        return this.extent.getCollisionShape();
    }

    public double getDistanceToBorder(double d, double e) {
        double f = e - this.getMinZ();
        double g = this.getMaxZ() - e;
        double h = d - this.getMinX();
        double i = this.getMaxX() - d;
        double j = Math.min(h, i);
        j = Math.min(j, f);
        return Math.min(j, g);
    }

    public boolean isInsideCloseToBorder(Entity entity, AABB aABB) {
        double d = Math.max(Mth.absMax(aABB.getXsize(), aABB.getZsize()), 1.0);
        return this.getDistanceToBorder(entity) < d * 2.0 && this.isWithinBounds(entity.getX(), entity.getZ(), d);
    }

    public BorderStatus getStatus() {
        return this.extent.getStatus();
    }

    public double getMinX() {
        return this.getMinX(0.0f);
    }

    public double getMinX(float f) {
        return this.extent.getMinX(f);
    }

    public double getMinZ() {
        return this.getMinZ(0.0f);
    }

    public double getMinZ(float f) {
        return this.extent.getMinZ(f);
    }

    public double getMaxX() {
        return this.getMaxX(0.0f);
    }

    public double getMaxX(float f) {
        return this.extent.getMaxX(f);
    }

    public double getMaxZ() {
        return this.getMaxZ(0.0f);
    }

    public double getMaxZ(float f) {
        return this.extent.getMaxZ(f);
    }

    public double getCenterX() {
        return this.centerX;
    }

    public double getCenterZ() {
        return this.centerZ;
    }

    public void setCenter(double d, double e) {
        this.centerX = d;
        this.centerZ = e;
        this.extent.onCenterChange();
        this.setDirty();
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onSetCenter(this, d, e);
        }
    }

    public double getSize() {
        return this.extent.getSize();
    }

    public long getLerpTime() {
        return this.extent.getLerpTime();
    }

    public double getLerpTarget() {
        return this.extent.getLerpTarget();
    }

    public void setSize(double d) {
        this.extent = new StaticBorderExtent(d);
        this.setDirty();
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onSetSize(this, d);
        }
    }

    public void lerpSizeBetween(double d, double e, long l, long m) {
        this.extent = d == e ? new StaticBorderExtent(e) : new MovingBorderExtent(d, e, l, m);
        this.setDirty();
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onLerpSize(this, d, e, l, m);
        }
    }

    protected List<BorderChangeListener> getListeners() {
        return Lists.newArrayList(this.listeners);
    }

    public void addListener(BorderChangeListener borderChangeListener) {
        this.listeners.add(borderChangeListener);
    }

    public void removeListener(BorderChangeListener borderChangeListener) {
        this.listeners.remove(borderChangeListener);
    }

    public void setAbsoluteMaxSize(int i) {
        this.absoluteMaxSize = i;
        this.extent.onAbsoluteMaxSizeChange();
    }

    public int getAbsoluteMaxSize() {
        return this.absoluteMaxSize;
    }

    public double getSafeZone() {
        return this.safeZone;
    }

    public void setSafeZone(double d) {
        this.safeZone = d;
        this.setDirty();
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onSetSafeZone(this, d);
        }
    }

    public double getDamagePerBlock() {
        return this.damagePerBlock;
    }

    public void setDamagePerBlock(double d) {
        this.damagePerBlock = d;
        this.setDirty();
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onSetDamagePerBlock(this, d);
        }
    }

    public double getLerpSpeed() {
        return this.extent.getLerpSpeed();
    }

    public int getWarningTime() {
        return this.warningTime;
    }

    public void setWarningTime(int i) {
        this.warningTime = i;
        this.setDirty();
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onSetWarningTime(this, i);
        }
    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }

    public void setWarningBlocks(int i) {
        this.warningBlocks = i;
        this.setDirty();
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onSetWarningBlocks(this, i);
        }
    }

    public void tick() {
        this.extent = this.extent.update();
    }

    public void applyInitialSettings(long l) {
        if (!this.initialized) {
            this.setCenter(this.settings.centerX(), this.settings.centerZ());
            this.setDamagePerBlock(this.settings.damagePerBlock());
            this.setSafeZone(this.settings.safeZone());
            this.setWarningBlocks(this.settings.warningBlocks());
            this.setWarningTime(this.settings.warningTime());
            if (this.settings.lerpTime() > 0L) {
                this.lerpSizeBetween(this.settings.size(), this.settings.lerpTarget(), this.settings.lerpTime(), l);
            } else {
                this.setSize(this.settings.size());
            }
            this.initialized = true;
        }
    }

    public record Settings(double centerX, double centerZ, double damagePerBlock, double safeZone, int warningBlocks, int warningTime, double size, long lerpTime, double lerpTarget) {
        public static final Settings DEFAULT = new Settings(0.0, 0.0, 0.2, 5.0, 5, 300, 5.9999968E7, 0L, 0.0);
        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.doubleRange((double)-2.9999984E7, (double)2.9999984E7).fieldOf("center_x").forGetter(Settings::centerX), (App)Codec.doubleRange((double)-2.9999984E7, (double)2.9999984E7).fieldOf("center_z").forGetter(Settings::centerZ), (App)Codec.DOUBLE.fieldOf("damage_per_block").forGetter(Settings::damagePerBlock), (App)Codec.DOUBLE.fieldOf("safe_zone").forGetter(Settings::safeZone), (App)Codec.INT.fieldOf("warning_blocks").forGetter(Settings::warningBlocks), (App)Codec.INT.fieldOf("warning_time").forGetter(Settings::warningTime), (App)Codec.DOUBLE.fieldOf("size").forGetter(Settings::size), (App)Codec.LONG.fieldOf("lerp_time").forGetter(Settings::lerpTime), (App)Codec.DOUBLE.fieldOf("lerp_target").forGetter(Settings::lerpTarget)).apply((Applicative)instance, Settings::new));

        public Settings(WorldBorder worldBorder) {
            this(worldBorder.centerX, worldBorder.centerZ, worldBorder.damagePerBlock, worldBorder.safeZone, worldBorder.warningBlocks, worldBorder.warningTime, worldBorder.extent.getSize(), worldBorder.extent.getLerpTime(), worldBorder.extent.getLerpTarget());
        }
    }

    class StaticBorderExtent
    implements BorderExtent {
        private final double size;
        private double minX;
        private double minZ;
        private double maxX;
        private double maxZ;
        private VoxelShape shape;

        public StaticBorderExtent(double d) {
            this.size = d;
            this.updateBox();
        }

        @Override
        public double getMinX(float f) {
            return this.minX;
        }

        @Override
        public double getMaxX(float f) {
            return this.maxX;
        }

        @Override
        public double getMinZ(float f) {
            return this.minZ;
        }

        @Override
        public double getMaxZ(float f) {
            return this.maxZ;
        }

        @Override
        public double getSize() {
            return this.size;
        }

        @Override
        public BorderStatus getStatus() {
            return BorderStatus.STATIONARY;
        }

        @Override
        public double getLerpSpeed() {
            return 0.0;
        }

        @Override
        public long getLerpTime() {
            return 0L;
        }

        @Override
        public double getLerpTarget() {
            return this.size;
        }

        private void updateBox() {
            this.minX = Mth.clamp(WorldBorder.this.getCenterX() - this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.minZ = Mth.clamp(WorldBorder.this.getCenterZ() - this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.maxX = Mth.clamp(WorldBorder.this.getCenterX() + this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.maxZ = Mth.clamp(WorldBorder.this.getCenterZ() + this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.shape = Shapes.join(Shapes.INFINITY, Shapes.box(Math.floor(this.getMinX(0.0f)), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ(0.0f)), Math.ceil(this.getMaxX(0.0f)), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ(0.0f))), BooleanOp.ONLY_FIRST);
        }

        @Override
        public void onAbsoluteMaxSizeChange() {
            this.updateBox();
        }

        @Override
        public void onCenterChange() {
            this.updateBox();
        }

        @Override
        public BorderExtent update() {
            return this;
        }

        @Override
        public VoxelShape getCollisionShape() {
            return this.shape;
        }
    }

    static interface BorderExtent {
        public double getMinX(float var1);

        public double getMaxX(float var1);

        public double getMinZ(float var1);

        public double getMaxZ(float var1);

        public double getSize();

        public double getLerpSpeed();

        public long getLerpTime();

        public double getLerpTarget();

        public BorderStatus getStatus();

        public void onAbsoluteMaxSizeChange();

        public void onCenterChange();

        public BorderExtent update();

        public VoxelShape getCollisionShape();
    }

    class MovingBorderExtent
    implements BorderExtent {
        private final double from;
        private final double to;
        private final long lerpEnd;
        private final long lerpBegin;
        private final double lerpDuration;
        private long lerpProgress;
        private double size;
        private double previousSize;

        MovingBorderExtent(double d, double e, long l, long m) {
            double f;
            this.from = d;
            this.to = e;
            this.lerpDuration = l;
            this.lerpProgress = l;
            this.lerpBegin = m;
            this.lerpEnd = this.lerpBegin + l;
            this.size = f = this.calculateSize();
            this.previousSize = f;
        }

        @Override
        public double getMinX(float f) {
            return Mth.clamp(WorldBorder.this.getCenterX() - Mth.lerp((double)f, this.getPreviousSize(), this.getSize()) / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getMinZ(float f) {
            return Mth.clamp(WorldBorder.this.getCenterZ() - Mth.lerp((double)f, this.getPreviousSize(), this.getSize()) / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getMaxX(float f) {
            return Mth.clamp(WorldBorder.this.getCenterX() + Mth.lerp((double)f, this.getPreviousSize(), this.getSize()) / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getMaxZ(float f) {
            return Mth.clamp(WorldBorder.this.getCenterZ() + Mth.lerp((double)f, this.getPreviousSize(), this.getSize()) / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getSize() {
            return this.size;
        }

        public double getPreviousSize() {
            return this.previousSize;
        }

        private double calculateSize() {
            double d = (this.lerpDuration - (double)this.lerpProgress) / this.lerpDuration;
            return d < 1.0 ? Mth.lerp(d, this.from, this.to) : this.to;
        }

        @Override
        public double getLerpSpeed() {
            return Math.abs(this.from - this.to) / (double)(this.lerpEnd - this.lerpBegin);
        }

        @Override
        public long getLerpTime() {
            return this.lerpProgress;
        }

        @Override
        public double getLerpTarget() {
            return this.to;
        }

        @Override
        public BorderStatus getStatus() {
            return this.to < this.from ? BorderStatus.SHRINKING : BorderStatus.GROWING;
        }

        @Override
        public void onCenterChange() {
        }

        @Override
        public void onAbsoluteMaxSizeChange() {
        }

        @Override
        public BorderExtent update() {
            --this.lerpProgress;
            this.previousSize = this.size;
            this.size = this.calculateSize();
            if (this.lerpProgress <= 0L) {
                WorldBorder.this.setDirty();
                return new StaticBorderExtent(this.to);
            }
            return this;
        }

        @Override
        public VoxelShape getCollisionShape() {
            return Shapes.join(Shapes.INFINITY, Shapes.box(Math.floor(this.getMinX(0.0f)), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ(0.0f)), Math.ceil(this.getMaxX(0.0f)), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ(0.0f))), BooleanOp.ONLY_FIRST);
        }
    }
}


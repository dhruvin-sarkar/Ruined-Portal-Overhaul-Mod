/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.floats.FloatUnaryOperator
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface DeltaTracker {
    public static final DeltaTracker ZERO = new DefaultValue(0.0f);
    public static final DeltaTracker ONE = new DefaultValue(1.0f);

    public float getGameTimeDeltaTicks();

    public float getGameTimeDeltaPartialTick(boolean var1);

    public float getRealtimeDeltaTicks();

    @Environment(value=EnvType.CLIENT)
    public static class DefaultValue
    implements DeltaTracker {
        private final float value;

        DefaultValue(float f) {
            this.value = f;
        }

        @Override
        public float getGameTimeDeltaTicks() {
            return this.value;
        }

        @Override
        public float getGameTimeDeltaPartialTick(boolean bl) {
            return this.value;
        }

        @Override
        public float getRealtimeDeltaTicks() {
            return this.value;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Timer
    implements DeltaTracker {
        private float deltaTicks;
        private float deltaTickResidual;
        private float realtimeDeltaTicks;
        private float pausedDeltaTickResidual;
        private long lastMs;
        private long lastUiMs;
        private final float msPerTick;
        private final FloatUnaryOperator targetMsptProvider;
        private boolean paused;
        private boolean frozen;

        public Timer(float f, long l, FloatUnaryOperator floatUnaryOperator) {
            this.msPerTick = 1000.0f / f;
            this.lastUiMs = this.lastMs = l;
            this.targetMsptProvider = floatUnaryOperator;
        }

        public int advanceTime(long l, boolean bl) {
            this.advanceRealTime(l);
            if (bl) {
                return this.advanceGameTime(l);
            }
            return 0;
        }

        private int advanceGameTime(long l) {
            this.deltaTicks = (float)(l - this.lastMs) / this.targetMsptProvider.apply(this.msPerTick);
            this.lastMs = l;
            this.deltaTickResidual += this.deltaTicks;
            int i = (int)this.deltaTickResidual;
            this.deltaTickResidual -= (float)i;
            return i;
        }

        private void advanceRealTime(long l) {
            this.realtimeDeltaTicks = (float)(l - this.lastUiMs) / this.msPerTick;
            this.lastUiMs = l;
        }

        public void updatePauseState(boolean bl) {
            if (bl) {
                this.pause();
            } else {
                this.unPause();
            }
        }

        private void pause() {
            if (!this.paused) {
                this.pausedDeltaTickResidual = this.deltaTickResidual;
            }
            this.paused = true;
        }

        private void unPause() {
            if (this.paused) {
                this.deltaTickResidual = this.pausedDeltaTickResidual;
            }
            this.paused = false;
        }

        public void updateFrozenState(boolean bl) {
            this.frozen = bl;
        }

        @Override
        public float getGameTimeDeltaTicks() {
            return this.deltaTicks;
        }

        @Override
        public float getGameTimeDeltaPartialTick(boolean bl) {
            if (!bl && this.frozen) {
                return 1.0f;
            }
            return this.paused ? this.pausedDeltaTickResidual : this.deltaTickResidual;
        }

        @Override
        public float getRealtimeDeltaTicks() {
            if (this.realtimeDeltaTicks > 7.0f) {
                return 0.5f;
            }
            return this.realtimeDeltaTicks;
        }
    }
}


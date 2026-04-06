/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.openal.AL10
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.audio;

import com.mojang.blaze3d.audio.OpenAlUtil;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.lwjgl.openal.AL10;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Channel {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int QUEUED_BUFFER_COUNT = 4;
    public static final int BUFFER_DURATION_SECONDS = 1;
    private final int source;
    private final AtomicBoolean initialized = new AtomicBoolean(true);
    private int streamingBufferSize = 16384;
    private @Nullable AudioStream stream;

    static @Nullable Channel create() {
        int[] is = new int[1];
        AL10.alGenSources((int[])is);
        if (OpenAlUtil.checkALError("Allocate new source")) {
            return null;
        }
        return new Channel(is[0]);
    }

    private Channel(int i) {
        this.source = i;
    }

    public void destroy() {
        if (this.initialized.compareAndSet(true, false)) {
            AL10.alSourceStop((int)this.source);
            OpenAlUtil.checkALError("Stop");
            if (this.stream != null) {
                try {
                    this.stream.close();
                }
                catch (IOException iOException) {
                    LOGGER.error("Failed to close audio stream", (Throwable)iOException);
                }
                this.removeProcessedBuffers();
                this.stream = null;
            }
            AL10.alDeleteSources((int[])new int[]{this.source});
            OpenAlUtil.checkALError("Cleanup");
        }
    }

    public void play() {
        AL10.alSourcePlay((int)this.source);
    }

    private int getState() {
        if (!this.initialized.get()) {
            return 4116;
        }
        return AL10.alGetSourcei((int)this.source, (int)4112);
    }

    public void pause() {
        if (this.getState() == 4114) {
            AL10.alSourcePause((int)this.source);
        }
    }

    public void unpause() {
        if (this.getState() == 4115) {
            AL10.alSourcePlay((int)this.source);
        }
    }

    public void stop() {
        if (this.initialized.get()) {
            AL10.alSourceStop((int)this.source);
            OpenAlUtil.checkALError("Stop");
        }
    }

    public boolean playing() {
        return this.getState() == 4114;
    }

    public boolean stopped() {
        return this.getState() == 4116;
    }

    public void setSelfPosition(Vec3 vec3) {
        AL10.alSourcefv((int)this.source, (int)4100, (float[])new float[]{(float)vec3.x, (float)vec3.y, (float)vec3.z});
    }

    public void setPitch(float f) {
        AL10.alSourcef((int)this.source, (int)4099, (float)f);
    }

    public void setLooping(boolean bl) {
        AL10.alSourcei((int)this.source, (int)4103, (int)(bl ? 1 : 0));
    }

    public void setVolume(float f) {
        AL10.alSourcef((int)this.source, (int)4106, (float)f);
    }

    public void disableAttenuation() {
        AL10.alSourcei((int)this.source, (int)53248, (int)0);
    }

    public void linearAttenuation(float f) {
        AL10.alSourcei((int)this.source, (int)53248, (int)53251);
        AL10.alSourcef((int)this.source, (int)4131, (float)f);
        AL10.alSourcef((int)this.source, (int)4129, (float)1.0f);
        AL10.alSourcef((int)this.source, (int)4128, (float)0.0f);
    }

    public void setRelative(boolean bl) {
        AL10.alSourcei((int)this.source, (int)514, (int)(bl ? 1 : 0));
    }

    public void attachStaticBuffer(SoundBuffer soundBuffer) {
        soundBuffer.getAlBuffer().ifPresent(i -> AL10.alSourcei((int)this.source, (int)4105, (int)i));
    }

    public void attachBufferStream(AudioStream audioStream) {
        this.stream = audioStream;
        AudioFormat audioFormat = audioStream.getFormat();
        this.streamingBufferSize = Channel.calculateBufferSize(audioFormat, 1);
        this.pumpBuffers(4);
    }

    private static int calculateBufferSize(AudioFormat audioFormat, int i) {
        return (int)((float)(i * audioFormat.getSampleSizeInBits()) / 8.0f * (float)audioFormat.getChannels() * audioFormat.getSampleRate());
    }

    private void pumpBuffers(int i2) {
        if (this.stream != null) {
            try {
                for (int j = 0; j < i2; ++j) {
                    ByteBuffer byteBuffer = this.stream.read(this.streamingBufferSize);
                    if (byteBuffer == null) continue;
                    new SoundBuffer(byteBuffer, this.stream.getFormat()).releaseAlBuffer().ifPresent(i -> AL10.alSourceQueueBuffers((int)this.source, (int[])new int[]{i}));
                }
            }
            catch (IOException iOException) {
                LOGGER.error("Failed to read from audio stream", (Throwable)iOException);
            }
        }
    }

    public void updateStream() {
        if (this.stream != null) {
            int i = this.removeProcessedBuffers();
            this.pumpBuffers(i);
        }
    }

    private int removeProcessedBuffers() {
        int i = AL10.alGetSourcei((int)this.source, (int)4118);
        if (i > 0) {
            int[] is = new int[i];
            AL10.alSourceUnqueueBuffers((int)this.source, (int[])is);
            OpenAlUtil.checkALError("Unqueue buffers");
            AL10.alDeleteBuffers((int[])is);
            OpenAlUtil.checkALError("Remove processed buffers");
        }
        return i;
    }
}


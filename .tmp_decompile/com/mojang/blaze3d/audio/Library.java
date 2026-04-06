/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.openal.AL
 *  org.lwjgl.openal.AL10
 *  org.lwjgl.openal.ALC
 *  org.lwjgl.openal.ALC10
 *  org.lwjgl.openal.ALC11
 *  org.lwjgl.openal.ALCCapabilities
 *  org.lwjgl.openal.ALCapabilities
 *  org.lwjgl.openal.ALUtil
 *  org.lwjgl.system.MemoryStack
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.audio;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.blaze3d.audio.OpenAlUtil;
import com.mojang.logging.LogUtils;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Library {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int NO_DEVICE = 0;
    private static final int DEFAULT_CHANNEL_COUNT = 30;
    private long currentDevice;
    private long context;
    private boolean supportsDisconnections;
    private @Nullable String defaultDeviceName;
    private static final ChannelPool EMPTY = new ChannelPool(){

        @Override
        public @Nullable Channel acquire() {
            return null;
        }

        @Override
        public boolean release(Channel channel) {
            return false;
        }

        @Override
        public void cleanup() {
        }

        @Override
        public int getMaxCount() {
            return 0;
        }

        @Override
        public int getUsedCount() {
            return 0;
        }
    };
    private ChannelPool staticChannels = EMPTY;
    private ChannelPool streamingChannels = EMPTY;
    private final Listener listener = new Listener();

    public Library() {
        this.defaultDeviceName = Library.getDefaultDeviceName();
    }

    public void init(@Nullable String string, boolean bl) {
        this.currentDevice = Library.openDeviceOrFallback(string);
        this.supportsDisconnections = false;
        ALCCapabilities aLCCapabilities = ALC.createCapabilities((long)this.currentDevice);
        if (OpenAlUtil.checkALCError(this.currentDevice, "Get capabilities")) {
            throw new IllegalStateException("Failed to get OpenAL capabilities");
        }
        if (!aLCCapabilities.OpenALC11) {
            throw new IllegalStateException("OpenAL 1.1 not supported");
        }
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            IntBuffer intBuffer = this.createAttributes(memoryStack, aLCCapabilities.ALC_SOFT_HRTF && bl);
            this.context = ALC10.alcCreateContext((long)this.currentDevice, (IntBuffer)intBuffer);
        }
        if (OpenAlUtil.checkALCError(this.currentDevice, "Create context")) {
            throw new IllegalStateException("Unable to create OpenAL context");
        }
        ALC10.alcMakeContextCurrent((long)this.context);
        int i = this.getChannelCount();
        int j = Mth.clamp((int)Mth.sqrt(i), 2, 8);
        int k = Mth.clamp(i - j, 8, 255);
        this.staticChannels = new CountingChannelPool(k);
        this.streamingChannels = new CountingChannelPool(j);
        ALCapabilities aLCapabilities = AL.createCapabilities((ALCCapabilities)aLCCapabilities);
        OpenAlUtil.checkALError("Initialization");
        if (!aLCapabilities.AL_EXT_source_distance_model) {
            throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
        }
        AL10.alEnable((int)512);
        if (!aLCapabilities.AL_EXT_LINEAR_DISTANCE) {
            throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
        }
        OpenAlUtil.checkALError("Enable per-source distance models");
        LOGGER.info("OpenAL initialized on device {}", (Object)this.getCurrentDeviceName());
        this.supportsDisconnections = ALC10.alcIsExtensionPresent((long)this.currentDevice, (CharSequence)"ALC_EXT_disconnect");
    }

    private IntBuffer createAttributes(MemoryStack memoryStack, boolean bl) {
        int i = 5;
        IntBuffer intBuffer = memoryStack.callocInt(11);
        int j = ALC10.alcGetInteger((long)this.currentDevice, (int)6548);
        if (j > 0) {
            intBuffer.put(6546).put(bl ? 1 : 0);
            intBuffer.put(6550).put(0);
        }
        intBuffer.put(6554).put(1);
        return intBuffer.put(0).flip();
    }

    private int getChannelCount() {
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            int i = ALC10.alcGetInteger((long)this.currentDevice, (int)4098);
            if (OpenAlUtil.checkALCError(this.currentDevice, "Get attributes size")) {
                throw new IllegalStateException("Failed to get OpenAL attributes");
            }
            IntBuffer intBuffer = memoryStack.mallocInt(i);
            ALC10.alcGetIntegerv((long)this.currentDevice, (int)4099, (IntBuffer)intBuffer);
            if (OpenAlUtil.checkALCError(this.currentDevice, "Get attributes")) {
                throw new IllegalStateException("Failed to get OpenAL attributes");
            }
            int j = 0;
            while (j < i) {
                int k;
                if ((k = intBuffer.get(j++)) == 0) {
                    break;
                }
                int l = intBuffer.get(j++);
                if (k != 4112) continue;
                int n = l;
                return n;
            }
        }
        return 30;
    }

    public static @Nullable String getDefaultDeviceName() {
        if (!ALC10.alcIsExtensionPresent((long)0L, (CharSequence)"ALC_ENUMERATE_ALL_EXT")) {
            return null;
        }
        ALUtil.getStringList((long)0L, (int)4115);
        return ALC10.alcGetString((long)0L, (int)4114);
    }

    public String getCurrentDeviceName() {
        String string = ALC10.alcGetString((long)this.currentDevice, (int)4115);
        if (string == null) {
            string = ALC10.alcGetString((long)this.currentDevice, (int)4101);
        }
        if (string == null) {
            string = "Unknown";
        }
        return string;
    }

    public synchronized boolean hasDefaultDeviceChanged() {
        String string = Library.getDefaultDeviceName();
        if (Objects.equals(this.defaultDeviceName, string)) {
            return false;
        }
        this.defaultDeviceName = string;
        return true;
    }

    private static long openDeviceOrFallback(@Nullable String string) {
        OptionalLong optionalLong = OptionalLong.empty();
        if (string != null) {
            optionalLong = Library.tryOpenDevice(string);
        }
        if (optionalLong.isEmpty()) {
            optionalLong = Library.tryOpenDevice(Library.getDefaultDeviceName());
        }
        if (optionalLong.isEmpty()) {
            optionalLong = Library.tryOpenDevice(null);
        }
        if (optionalLong.isEmpty()) {
            throw new IllegalStateException("Failed to open OpenAL device");
        }
        return optionalLong.getAsLong();
    }

    private static OptionalLong tryOpenDevice(@Nullable String string) {
        long l = ALC10.alcOpenDevice((CharSequence)string);
        if (l != 0L && !OpenAlUtil.checkALCError(l, "Open device")) {
            return OptionalLong.of(l);
        }
        return OptionalLong.empty();
    }

    public void cleanup() {
        this.staticChannels.cleanup();
        this.streamingChannels.cleanup();
        ALC10.alcDestroyContext((long)this.context);
        if (this.currentDevice != 0L) {
            ALC10.alcCloseDevice((long)this.currentDevice);
        }
    }

    public Listener getListener() {
        return this.listener;
    }

    public @Nullable Channel acquireChannel(Pool pool) {
        return (pool == Pool.STREAMING ? this.streamingChannels : this.staticChannels).acquire();
    }

    public void releaseChannel(Channel channel) {
        if (!this.staticChannels.release(channel) && !this.streamingChannels.release(channel)) {
            throw new IllegalStateException("Tried to release unknown channel");
        }
    }

    public String getDebugString() {
        return String.format(Locale.ROOT, "Sounds: %d/%d + %d/%d", this.staticChannels.getUsedCount(), this.staticChannels.getMaxCount(), this.streamingChannels.getUsedCount(), this.streamingChannels.getMaxCount());
    }

    public List<String> getAvailableSoundDevices() {
        List list = ALUtil.getStringList((long)0L, (int)4115);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public boolean isCurrentDeviceDisconnected() {
        return this.supportsDisconnections && ALC11.alcGetInteger((long)this.currentDevice, (int)787) == 0;
    }

    @Environment(value=EnvType.CLIENT)
    static interface ChannelPool {
        public @Nullable Channel acquire();

        public boolean release(Channel var1);

        public void cleanup();

        public int getMaxCount();

        public int getUsedCount();
    }

    @Environment(value=EnvType.CLIENT)
    static class CountingChannelPool
    implements ChannelPool {
        private final int limit;
        private final Set<Channel> activeChannels = Sets.newIdentityHashSet();

        public CountingChannelPool(int i) {
            this.limit = i;
        }

        @Override
        public @Nullable Channel acquire() {
            if (this.activeChannels.size() >= this.limit) {
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    LOGGER.warn("Maximum sound pool size {} reached", (Object)this.limit);
                }
                return null;
            }
            Channel channel = Channel.create();
            if (channel != null) {
                this.activeChannels.add(channel);
            }
            return channel;
        }

        @Override
        public boolean release(Channel channel) {
            if (!this.activeChannels.remove(channel)) {
                return false;
            }
            channel.destroy();
            return true;
        }

        @Override
        public void cleanup() {
            this.activeChannels.forEach(Channel::destroy);
            this.activeChannels.clear();
        }

        @Override
        public int getMaxCount() {
            return this.limit;
        }

        @Override
        public int getUsedCount() {
            return this.activeChannels.size();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Pool {
        STATIC,
        STREAMING;

    }
}


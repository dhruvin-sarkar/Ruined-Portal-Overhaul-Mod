/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2FloatMap
 *  it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.Marker
 *  org.slf4j.MarkerFactory
 */
package net.minecraft.client.sounds;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.blaze3d.audio.ListenerTransform;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngineExecutor;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Environment(value=EnvType.CLIENT)
public class SoundEngine {
    private static final Marker MARKER = MarkerFactory.getMarker((String)"SOUNDS");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float PITCH_MIN = 0.5f;
    private static final float PITCH_MAX = 2.0f;
    private static final float VOLUME_MIN = 0.0f;
    private static final float VOLUME_MAX = 1.0f;
    private static final int MIN_SOURCE_LIFETIME = 20;
    private static final Set<Identifier> ONLY_WARN_ONCE = Sets.newHashSet();
    private static final long DEFAULT_DEVICE_CHECK_INTERVAL_MS = 1000L;
    public static final String MISSING_SOUND = "FOR THE DEBUG!";
    public static final String OPEN_AL_SOFT_PREFIX = "OpenAL Soft on ";
    public static final int OPEN_AL_SOFT_PREFIX_LENGTH = "OpenAL Soft on ".length();
    private final SoundManager soundManager;
    private final Options options;
    private boolean loaded;
    private final Library library = new Library();
    private final Listener listener = this.library.getListener();
    private final SoundBufferLibrary soundBuffers;
    private final SoundEngineExecutor executor = new SoundEngineExecutor();
    private final ChannelAccess channelAccess = new ChannelAccess(this.library, this.executor);
    private int tickCount;
    private long lastDeviceCheckTime;
    private final AtomicReference<DeviceCheckState> devicePoolState = new AtomicReference<DeviceCheckState>(DeviceCheckState.NO_CHANGE);
    private final Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = Maps.newHashMap();
    private final Multimap<SoundSource, SoundInstance> instanceBySource = HashMultimap.create();
    private final Object2FloatMap<SoundSource> gainBySource = (Object2FloatMap)Util.make(new Object2FloatOpenHashMap(), object2FloatOpenHashMap -> object2FloatOpenHashMap.defaultReturnValue(1.0f));
    private final List<TickableSoundInstance> tickingSounds = Lists.newArrayList();
    private final Map<SoundInstance, Integer> queuedSounds = Maps.newHashMap();
    private final Map<SoundInstance, Integer> soundDeleteTime = Maps.newHashMap();
    private final List<SoundEventListener> listeners = Lists.newArrayList();
    private final List<TickableSoundInstance> queuedTickableSounds = Lists.newArrayList();
    private final List<Sound> preloadQueue = Lists.newArrayList();

    public SoundEngine(SoundManager soundManager, Options options, ResourceProvider resourceProvider) {
        this.soundManager = soundManager;
        this.options = options;
        this.soundBuffers = new SoundBufferLibrary(resourceProvider);
    }

    public void reload() {
        ONLY_WARN_ONCE.clear();
        for (SoundEvent soundEvent : BuiltInRegistries.SOUND_EVENT) {
            Identifier identifier;
            if (soundEvent == SoundEvents.EMPTY || this.soundManager.getSoundEvent(identifier = soundEvent.location()) != null) continue;
            LOGGER.warn("Missing sound for event: {}", (Object)BuiltInRegistries.SOUND_EVENT.getKey(soundEvent));
            ONLY_WARN_ONCE.add(identifier);
        }
        this.destroy();
        this.loadLibrary();
    }

    private synchronized void loadLibrary() {
        if (this.loaded) {
            return;
        }
        try {
            String string = this.options.soundDevice().get();
            this.library.init("".equals(string) ? null : string, this.options.directionalAudio().get());
            this.listener.reset();
            this.soundBuffers.preload(this.preloadQueue).thenRun(this.preloadQueue::clear);
            this.loaded = true;
            LOGGER.info(MARKER, "Sound engine started");
        }
        catch (RuntimeException runtimeException) {
            LOGGER.error(MARKER, "Error starting SoundSystem. Turning off sounds & music", (Throwable)runtimeException);
        }
    }

    public void refreshCategoryVolume(SoundSource soundSource) {
        if (!this.loaded) {
            return;
        }
        this.instanceToChannel.forEach((soundInstance, channelHandle) -> {
            if (soundSource == soundInstance.getSource() || soundSource == SoundSource.MASTER) {
                float f = this.calculateVolume((SoundInstance)soundInstance);
                channelHandle.execute(channel -> channel.setVolume(f));
            }
        });
    }

    public void destroy() {
        if (this.loaded) {
            this.stopAll();
            this.soundBuffers.clear();
            this.library.cleanup();
            this.loaded = false;
        }
    }

    public void emergencyShutdown() {
        if (this.loaded) {
            this.library.cleanup();
        }
    }

    public void stop(SoundInstance soundInstance) {
        ChannelAccess.ChannelHandle channelHandle;
        if (this.loaded && (channelHandle = this.instanceToChannel.get(soundInstance)) != null) {
            channelHandle.execute(Channel::stop);
        }
    }

    public void updateCategoryVolume(SoundSource soundSource, float f) {
        this.gainBySource.put((Object)soundSource, Mth.clamp(f, 0.0f, 1.0f));
        this.refreshCategoryVolume(soundSource);
    }

    public void stopAll() {
        if (this.loaded) {
            this.executor.shutDown();
            this.instanceToChannel.clear();
            this.channelAccess.clear();
            this.queuedSounds.clear();
            this.tickingSounds.clear();
            this.instanceBySource.clear();
            this.soundDeleteTime.clear();
            this.queuedTickableSounds.clear();
            this.gainBySource.clear();
            this.executor.startUp();
        }
    }

    public void addEventListener(SoundEventListener soundEventListener) {
        this.listeners.add(soundEventListener);
    }

    public void removeEventListener(SoundEventListener soundEventListener) {
        this.listeners.remove(soundEventListener);
    }

    private boolean shouldChangeDevice() {
        boolean bl;
        if (this.library.isCurrentDeviceDisconnected()) {
            LOGGER.info("Audio device was lost!");
            return true;
        }
        long l = Util.getMillis();
        boolean bl2 = bl = l - this.lastDeviceCheckTime >= 1000L;
        if (bl) {
            this.lastDeviceCheckTime = l;
            if (this.devicePoolState.compareAndSet(DeviceCheckState.NO_CHANGE, DeviceCheckState.ONGOING)) {
                String string = this.options.soundDevice().get();
                Util.ioPool().execute(() -> {
                    if ("".equals(string)) {
                        if (this.library.hasDefaultDeviceChanged()) {
                            LOGGER.info("System default audio device has changed!");
                            this.devicePoolState.compareAndSet(DeviceCheckState.ONGOING, DeviceCheckState.CHANGE_DETECTED);
                        }
                    } else if (!this.library.getCurrentDeviceName().equals(string) && this.library.getAvailableSoundDevices().contains(string)) {
                        LOGGER.info("Preferred audio device has become available!");
                        this.devicePoolState.compareAndSet(DeviceCheckState.ONGOING, DeviceCheckState.CHANGE_DETECTED);
                    }
                    this.devicePoolState.compareAndSet(DeviceCheckState.ONGOING, DeviceCheckState.NO_CHANGE);
                });
            }
        }
        return this.devicePoolState.compareAndSet(DeviceCheckState.CHANGE_DETECTED, DeviceCheckState.NO_CHANGE);
    }

    public void tick(boolean bl) {
        if (this.shouldChangeDevice()) {
            this.reload();
        }
        if (!bl) {
            this.tickInGameSound();
        } else {
            this.tickMusicWhenPaused();
        }
        this.channelAccess.scheduleTick();
    }

    private void tickInGameSound() {
        ++this.tickCount;
        this.queuedTickableSounds.stream().filter(SoundInstance::canPlaySound).forEach(this::play);
        this.queuedTickableSounds.clear();
        for (TickableSoundInstance tickableSoundInstance : this.tickingSounds) {
            if (!tickableSoundInstance.canPlaySound()) {
                this.stop(tickableSoundInstance);
            }
            tickableSoundInstance.tick();
            if (tickableSoundInstance.isStopped()) {
                this.stop(tickableSoundInstance);
                continue;
            }
            float f = this.calculateVolume(tickableSoundInstance);
            float g = this.calculatePitch(tickableSoundInstance);
            Vec3 vec3 = new Vec3(tickableSoundInstance.getX(), tickableSoundInstance.getY(), tickableSoundInstance.getZ());
            ChannelAccess.ChannelHandle channelHandle = this.instanceToChannel.get(tickableSoundInstance);
            if (channelHandle == null) continue;
            channelHandle.execute(channel -> {
                channel.setVolume(f);
                channel.setPitch(g);
                channel.setSelfPosition(vec3);
            });
        }
        Iterator<Map.Entry<SoundInstance, ChannelAccess.ChannelHandle>> iterator = this.instanceToChannel.entrySet().iterator();
        while (iterator.hasNext()) {
            int i;
            Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry = iterator.next();
            ChannelAccess.ChannelHandle channelHandle2 = entry.getValue();
            SoundInstance soundInstance = entry.getKey();
            if (!channelHandle2.isStopped() || (i = this.soundDeleteTime.get(soundInstance).intValue()) > this.tickCount) continue;
            if (SoundEngine.shouldLoopManually(soundInstance)) {
                this.queuedSounds.put(soundInstance, this.tickCount + soundInstance.getDelay());
            }
            iterator.remove();
            LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", (Object)channelHandle2);
            this.soundDeleteTime.remove(soundInstance);
            try {
                this.instanceBySource.remove((Object)soundInstance.getSource(), (Object)soundInstance);
            }
            catch (RuntimeException runtimeException) {
                // empty catch block
            }
            if (!(soundInstance instanceof TickableSoundInstance)) continue;
            this.tickingSounds.remove(soundInstance);
        }
        Iterator<Map.Entry<SoundInstance, Integer>> iterator2 = this.queuedSounds.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<SoundInstance, Integer> entry2 = iterator2.next();
            if (this.tickCount < entry2.getValue()) continue;
            SoundInstance soundInstance = entry2.getKey();
            if (soundInstance instanceof TickableSoundInstance) {
                ((TickableSoundInstance)soundInstance).tick();
            }
            this.play(soundInstance);
            iterator2.remove();
        }
    }

    private void tickMusicWhenPaused() {
        Iterator<Map.Entry<SoundInstance, ChannelAccess.ChannelHandle>> iterator = this.instanceToChannel.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry = iterator.next();
            ChannelAccess.ChannelHandle channelHandle = entry.getValue();
            SoundInstance soundInstance = entry.getKey();
            if (soundInstance.getSource() != SoundSource.MUSIC || !channelHandle.isStopped()) continue;
            iterator.remove();
            LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", (Object)channelHandle);
            this.soundDeleteTime.remove(soundInstance);
            this.instanceBySource.remove((Object)soundInstance.getSource(), (Object)soundInstance);
        }
    }

    private static boolean requiresManualLooping(SoundInstance soundInstance) {
        return soundInstance.getDelay() > 0;
    }

    private static boolean shouldLoopManually(SoundInstance soundInstance) {
        return soundInstance.isLooping() && SoundEngine.requiresManualLooping(soundInstance);
    }

    private static boolean shouldLoopAutomatically(SoundInstance soundInstance) {
        return soundInstance.isLooping() && !SoundEngine.requiresManualLooping(soundInstance);
    }

    public boolean isActive(SoundInstance soundInstance) {
        if (!this.loaded) {
            return false;
        }
        if (this.soundDeleteTime.containsKey(soundInstance) && this.soundDeleteTime.get(soundInstance) <= this.tickCount) {
            return true;
        }
        return this.instanceToChannel.containsKey(soundInstance);
    }

    public PlayResult play(SoundInstance soundInstance) {
        Sound sound;
        if (!this.loaded) {
            return PlayResult.NOT_STARTED;
        }
        if (!soundInstance.canPlaySound()) {
            return PlayResult.NOT_STARTED;
        }
        WeighedSoundEvents weighedSoundEvents = soundInstance.resolve(this.soundManager);
        Identifier identifier = soundInstance.getIdentifier();
        if (weighedSoundEvents == null) {
            if (ONLY_WARN_ONCE.add(identifier)) {
                LOGGER.warn(MARKER, "Unable to play unknown soundEvent: {}", (Object)identifier);
            }
            if (!SharedConstants.DEBUG_SUBTITLES) {
                return PlayResult.NOT_STARTED;
            }
            weighedSoundEvents = new WeighedSoundEvents(identifier, MISSING_SOUND);
        }
        if ((sound = soundInstance.getSound()) == SoundManager.INTENTIONALLY_EMPTY_SOUND) {
            return PlayResult.NOT_STARTED;
        }
        if (sound == SoundManager.EMPTY_SOUND) {
            if (ONLY_WARN_ONCE.add(identifier)) {
                LOGGER.warn(MARKER, "Unable to play empty soundEvent: {}", (Object)identifier);
            }
            return PlayResult.NOT_STARTED;
        }
        float f = soundInstance.getVolume();
        float g = Math.max(f, 1.0f) * (float)sound.getAttenuationDistance();
        SoundSource soundSource = soundInstance.getSource();
        float h = this.calculateVolume(f, soundSource);
        float i = this.calculatePitch(soundInstance);
        SoundInstance.Attenuation attenuation = soundInstance.getAttenuation();
        boolean bl = soundInstance.isRelative();
        if (!this.listeners.isEmpty()) {
            float j = bl || attenuation == SoundInstance.Attenuation.NONE ? Float.POSITIVE_INFINITY : g;
            for (SoundEventListener soundEventListener : this.listeners) {
                soundEventListener.onPlaySound(soundInstance, weighedSoundEvents, j);
            }
        }
        boolean bl2 = false;
        if (h == 0.0f) {
            if (soundInstance.canStartSilent() || soundSource == SoundSource.MUSIC) {
                bl2 = true;
            } else {
                LOGGER.debug(MARKER, "Skipped playing sound {}, volume was zero.", (Object)sound.getLocation());
                return PlayResult.NOT_STARTED;
            }
        }
        Vec3 vec3 = new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
        boolean bl3 = SoundEngine.shouldLoopAutomatically(soundInstance);
        boolean bl4 = sound.shouldStream();
        CompletableFuture<@Nullable ChannelAccess.ChannelHandle> completableFuture = this.channelAccess.createHandle(sound.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC);
        ChannelAccess.ChannelHandle channelHandle = completableFuture.join();
        if (channelHandle == null) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                LOGGER.warn("Failed to create new sound handle");
            }
            return PlayResult.NOT_STARTED;
        }
        LOGGER.debug(MARKER, "Playing sound {} for event {}", (Object)sound.getLocation(), (Object)identifier);
        this.soundDeleteTime.put(soundInstance, this.tickCount + 20);
        this.instanceToChannel.put(soundInstance, channelHandle);
        this.instanceBySource.put((Object)soundSource, (Object)soundInstance);
        channelHandle.execute(channel -> {
            channel.setPitch(i);
            channel.setVolume(h);
            if (attenuation == SoundInstance.Attenuation.LINEAR) {
                channel.linearAttenuation(g);
            } else {
                channel.disableAttenuation();
            }
            channel.setLooping(bl3 && !bl4);
            channel.setSelfPosition(vec3);
            channel.setRelative(bl);
        });
        if (!bl4) {
            this.soundBuffers.getCompleteBuffer(sound.getPath()).thenAccept(soundBuffer -> channelHandle.execute(channel -> {
                channel.attachStaticBuffer((SoundBuffer)soundBuffer);
                channel.play();
            }));
        } else {
            this.soundBuffers.getStream(sound.getPath(), bl3).thenAccept(audioStream -> channelHandle.execute(channel -> {
                channel.attachBufferStream((AudioStream)audioStream);
                channel.play();
            }));
        }
        if (soundInstance instanceof TickableSoundInstance) {
            this.tickingSounds.add((TickableSoundInstance)soundInstance);
        }
        if (bl2) {
            return PlayResult.STARTED_SILENTLY;
        }
        return PlayResult.STARTED;
    }

    public void queueTickingSound(TickableSoundInstance tickableSoundInstance) {
        this.queuedTickableSounds.add(tickableSoundInstance);
    }

    public void requestPreload(Sound sound) {
        this.preloadQueue.add(sound);
    }

    private float calculatePitch(SoundInstance soundInstance) {
        return Mth.clamp(soundInstance.getPitch(), 0.5f, 2.0f);
    }

    private float calculateVolume(SoundInstance soundInstance) {
        return this.calculateVolume(soundInstance.getVolume(), soundInstance.getSource());
    }

    private float calculateVolume(float f, SoundSource soundSource) {
        return Mth.clamp(f, 0.0f, 1.0f) * Mth.clamp(this.options.getFinalSoundSourceVolume(soundSource), 0.0f, 1.0f) * this.gainBySource.getFloat((Object)soundSource);
    }

    public void pauseAllExcept(SoundSource ... soundSources) {
        if (!this.loaded) {
            return;
        }
        for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry : this.instanceToChannel.entrySet()) {
            if (List.of((Object[])soundSources).contains((Object)entry.getKey().getSource())) continue;
            entry.getValue().execute(Channel::pause);
        }
    }

    public void resume() {
        if (this.loaded) {
            this.channelAccess.executeOnChannels(stream -> stream.forEach(Channel::unpause));
        }
    }

    public void playDelayed(SoundInstance soundInstance, int i) {
        this.queuedSounds.put(soundInstance, this.tickCount + i);
    }

    public void updateSource(Camera camera) {
        if (!this.loaded || !camera.isInitialized()) {
            return;
        }
        ListenerTransform listenerTransform = new ListenerTransform(camera.position(), new Vec3(camera.forwardVector()), new Vec3(camera.upVector()));
        this.executor.execute(() -> this.listener.setTransform(listenerTransform));
    }

    public void stop(@Nullable Identifier identifier, @Nullable SoundSource soundSource) {
        if (soundSource != null) {
            for (SoundInstance soundInstance : this.instanceBySource.get((Object)soundSource)) {
                if (identifier != null && !soundInstance.getIdentifier().equals(identifier)) continue;
                this.stop(soundInstance);
            }
        } else if (identifier == null) {
            this.stopAll();
        } else {
            for (SoundInstance soundInstance : this.instanceToChannel.keySet()) {
                if (!soundInstance.getIdentifier().equals(identifier)) continue;
                this.stop(soundInstance);
            }
        }
    }

    public String getDebugString() {
        return this.library.getDebugString();
    }

    public List<String> getAvailableSoundDevices() {
        return this.library.getAvailableSoundDevices();
    }

    public ListenerTransform getListenerTransform() {
        return this.listener.getTransform();
    }

    @Environment(value=EnvType.CLIENT)
    static enum DeviceCheckState {
        ONGOING,
        CHANGE_DETECTED,
        NO_CHANGE;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum PlayResult {
        STARTED,
        STARTED_SILENTLY,
        NOT_STARTED;

    }
}


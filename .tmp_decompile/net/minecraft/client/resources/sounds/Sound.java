/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.SampledFloat;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Sound
implements Weighted<Sound> {
    public static final FileToIdConverter SOUND_LISTER = new FileToIdConverter("sounds", ".ogg");
    private final Identifier location;
    private final SampledFloat volume;
    private final SampledFloat pitch;
    private final int weight;
    private final Type type;
    private final boolean stream;
    private final boolean preload;
    private final int attenuationDistance;

    public Sound(Identifier identifier, SampledFloat sampledFloat, SampledFloat sampledFloat2, int i, Type type, boolean bl, boolean bl2, int j) {
        this.location = identifier;
        this.volume = sampledFloat;
        this.pitch = sampledFloat2;
        this.weight = i;
        this.type = type;
        this.stream = bl;
        this.preload = bl2;
        this.attenuationDistance = j;
    }

    public Identifier getLocation() {
        return this.location;
    }

    public Identifier getPath() {
        return SOUND_LISTER.idToFile(this.location);
    }

    public SampledFloat getVolume() {
        return this.volume;
    }

    public SampledFloat getPitch() {
        return this.pitch;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public Sound getSound(RandomSource randomSource) {
        return this;
    }

    @Override
    public void preloadIfRequired(SoundEngine soundEngine) {
        if (this.preload) {
            soundEngine.requestPreload(this);
        }
    }

    public Type getType() {
        return this.type;
    }

    public boolean shouldStream() {
        return this.stream;
    }

    public boolean shouldPreload() {
        return this.preload;
    }

    public int getAttenuationDistance() {
        return this.attenuationDistance;
    }

    public String toString() {
        return "Sound[" + String.valueOf(this.location) + "]";
    }

    @Override
    public /* synthetic */ Object getSound(RandomSource randomSource) {
        return this.getSound(randomSource);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        FILE("file"),
        SOUND_EVENT("event");

        private final String name;

        private Type(String string2) {
            this.name = string2;
        }

        public static @Nullable Type getByName(String string) {
            for (Type type : Type.values()) {
                if (!type.name.equals(string)) continue;
                return type;
            }
            return null;
        }
    }
}


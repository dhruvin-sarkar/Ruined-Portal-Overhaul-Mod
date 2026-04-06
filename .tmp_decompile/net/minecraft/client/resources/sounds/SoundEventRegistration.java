/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.resources.sounds;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.Sound;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SoundEventRegistration {
    private final List<Sound> sounds;
    private final boolean replace;
    private final @Nullable String subtitle;

    public SoundEventRegistration(List<Sound> list, boolean bl, @Nullable String string) {
        this.sounds = list;
        this.replace = bl;
        this.subtitle = string;
    }

    public List<Sound> getSounds() {
        return this.sounds;
    }

    public boolean isReplace() {
        return this.replace;
    }

    public @Nullable String getSubtitle() {
        return this.subtitle;
    }
}


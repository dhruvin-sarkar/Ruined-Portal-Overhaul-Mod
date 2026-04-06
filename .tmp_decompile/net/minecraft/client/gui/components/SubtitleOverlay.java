/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.audio.ListenerTransform;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SubtitleOverlay
implements SoundEventListener {
    private static final long DISPLAY_TIME = 3000L;
    private final Minecraft minecraft;
    private final List<Subtitle> subtitles = Lists.newArrayList();
    private boolean isListening;
    private final List<Subtitle> audibleSubtitles = new ArrayList<Subtitle>();

    public SubtitleOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(GuiGraphics guiGraphics) {
        SoundManager soundManager = this.minecraft.getSoundManager();
        if (!this.isListening && this.minecraft.options.showSubtitles().get().booleanValue()) {
            soundManager.addListener(this);
            this.isListening = true;
        } else if (this.isListening && !this.minecraft.options.showSubtitles().get().booleanValue()) {
            soundManager.removeListener(this);
            this.isListening = false;
        }
        if (!this.isListening) {
            return;
        }
        ListenerTransform listenerTransform = soundManager.getListenerTransform();
        Vec3 vec3 = listenerTransform.position();
        Vec3 vec32 = listenerTransform.forward();
        Vec3 vec33 = listenerTransform.right();
        this.audibleSubtitles.clear();
        for (Subtitle subtitle : this.subtitles) {
            if (!subtitle.isAudibleFrom(vec3)) continue;
            this.audibleSubtitles.add(subtitle);
        }
        if (this.audibleSubtitles.isEmpty()) {
            return;
        }
        int i = 0;
        int j = 0;
        double d = this.minecraft.options.notificationDisplayTime().get();
        Iterator<Subtitle> iterator = this.audibleSubtitles.iterator();
        while (iterator.hasNext()) {
            Subtitle subtitle2 = iterator.next();
            subtitle2.purgeOldInstances(3000.0 * d);
            if (!subtitle2.isStillActive()) {
                iterator.remove();
                continue;
            }
            j = Math.max(j, this.minecraft.font.width(subtitle2.getText()));
        }
        j += this.minecraft.font.width("<") + this.minecraft.font.width(" ") + this.minecraft.font.width(">") + this.minecraft.font.width(" ");
        if (!this.audibleSubtitles.isEmpty()) {
            guiGraphics.nextStratum();
        }
        for (Subtitle subtitle2 : this.audibleSubtitles) {
            int k = 255;
            Component component = subtitle2.getText();
            SoundPlayedAt soundPlayedAt = subtitle2.getClosest(vec3);
            if (soundPlayedAt == null) continue;
            Vec3 vec34 = soundPlayedAt.location.subtract(vec3).normalize();
            double e = vec33.dot(vec34);
            double f = vec32.dot(vec34);
            boolean bl = f > 0.5;
            int l = j / 2;
            int m = this.minecraft.font.lineHeight;
            int n = m / 2;
            float g = 1.0f;
            int o = this.minecraft.font.width(component);
            int p = Mth.floor(Mth.clampedLerp((float)(Util.getMillis() - soundPlayedAt.time) / (float)(3000.0 * d), 255.0f, 75.0f));
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)guiGraphics.guiWidth() - (float)l * 1.0f - 2.0f, (float)(guiGraphics.guiHeight() - 35) - (float)(i * (m + 1)) * 1.0f);
            guiGraphics.pose().scale(1.0f, 1.0f);
            guiGraphics.fill(-l - 1, -n - 1, l + 1, n + 1, this.minecraft.options.getBackgroundColor(0.8f));
            int q = ARGB.color(255, p, p, p);
            if (!bl) {
                if (e > 0.0) {
                    guiGraphics.drawString(this.minecraft.font, ">", l - this.minecraft.font.width(">"), -n, q);
                } else if (e < 0.0) {
                    guiGraphics.drawString(this.minecraft.font, "<", -l, -n, q);
                }
            }
            guiGraphics.drawString(this.minecraft.font, component, -o / 2, -n, q);
            guiGraphics.pose().popMatrix();
            ++i;
        }
    }

    @Override
    public void onPlaySound(SoundInstance soundInstance, WeighedSoundEvents weighedSoundEvents, float f) {
        if (weighedSoundEvents.getSubtitle() == null) {
            return;
        }
        Component component = weighedSoundEvents.getSubtitle();
        if (!this.subtitles.isEmpty()) {
            for (Subtitle subtitle : this.subtitles) {
                if (!subtitle.getText().equals(component)) continue;
                subtitle.refresh(new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ()));
                return;
            }
        }
        this.subtitles.add(new Subtitle(component, f, new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ())));
    }

    @Environment(value=EnvType.CLIENT)
    static class Subtitle {
        private final Component text;
        private final float range;
        private final List<SoundPlayedAt> playedAt = new ArrayList<SoundPlayedAt>();

        public Subtitle(Component component, float f, Vec3 vec3) {
            this.text = component;
            this.range = f;
            this.playedAt.add(new SoundPlayedAt(vec3, Util.getMillis()));
        }

        public Component getText() {
            return this.text;
        }

        public @Nullable SoundPlayedAt getClosest(Vec3 vec3) {
            if (this.playedAt.isEmpty()) {
                return null;
            }
            if (this.playedAt.size() == 1) {
                return (SoundPlayedAt)((Object)this.playedAt.getFirst());
            }
            return this.playedAt.stream().min(Comparator.comparingDouble(soundPlayedAt -> soundPlayedAt.location().distanceTo(vec3))).orElse(null);
        }

        public void refresh(Vec3 vec3) {
            this.playedAt.removeIf(soundPlayedAt -> vec3.equals(soundPlayedAt.location()));
            this.playedAt.add(new SoundPlayedAt(vec3, Util.getMillis()));
        }

        public boolean isAudibleFrom(Vec3 vec3) {
            if (Float.isInfinite(this.range)) {
                return true;
            }
            if (this.playedAt.isEmpty()) {
                return false;
            }
            SoundPlayedAt soundPlayedAt = this.getClosest(vec3);
            if (soundPlayedAt == null) {
                return false;
            }
            return vec3.closerThan(soundPlayedAt.location, this.range);
        }

        public void purgeOldInstances(double d) {
            long l = Util.getMillis();
            this.playedAt.removeIf(soundPlayedAt -> (double)(l - soundPlayedAt.time()) > d);
        }

        public boolean isStillActive() {
            return !this.playedAt.isEmpty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class SoundPlayedAt
    extends Record {
        final Vec3 location;
        final long time;

        SoundPlayedAt(Vec3 vec3, long l) {
            this.location = vec3;
            this.time = l;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SoundPlayedAt.class, "location;time", "location", "time"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SoundPlayedAt.class, "location;time", "location", "time"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SoundPlayedAt.class, "location;time", "location", "time"}, this, object);
        }

        public Vec3 location() {
            return this.location;
        }

        public long time() {
            return this.time;
        }
    }
}


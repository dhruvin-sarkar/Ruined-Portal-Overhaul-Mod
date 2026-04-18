package com.ruinedportaloverhaul.client.atmosphere;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.network.PortalAtmospherePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class PortalAtmosphereClient {
    private static final Identifier OVERLAY_ID = Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "portal_atmosphere_overlay");
    private static final Music RED_STORM_MUSIC = new Music(SoundEvents.MUSIC_BIOME_BASALT_DELTAS, 0, 0, true);
    private static final long PACKET_FADE_NANOS = 1_600_000_000L;
    private static final long STORM_UPDATE_MIN_NANOS = 1_000_000L;
    private static final long FLASH_MIN_DELAY_NANOS = 450_000_000L;
    private static final long FLASH_RANDOM_DELAY_NANOS = 1_400_000_000L;
    private static final double PULSE_PERIOD_NANOS = 4_200_000_000.0;
    private static final float PULSE_FLOOR = 0.72f;
    private static final float PULSE_RANGE = 0.28f;
    private static final RandomSource STORM_RANDOM = RandomSource.create();
    private static float targetIntensity;
    private static float targetDescent;
    private static float displayIntensity;
    private static float displayDescent;
    private static float stormIntensity;
    private static float stormPulse = 1.0f;
    private static long lastPacketNanos;
    private static long lastStormUpdateNanos;
    private static long nextFlashNanos;
    private static boolean musicPlaying;

    private PortalAtmosphereClient() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(PortalAtmospherePayload.TYPE, (payload, context) ->
            context.client().execute(() -> receive(payload))
        );
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, OVERLAY_ID, PortalAtmosphereClient::renderOverlay);
    }

    private static void receive(PortalAtmospherePayload payload) {
        targetIntensity = clamp01(payload.intensity());
        targetDescent = clamp01(payload.descent());
        lastPacketNanos = System.nanoTime();
    }

    private static void renderOverlay(GuiGraphics graphics, DeltaTracker tickCounter) {
        long now = System.nanoTime();
        updateStormState(now);
        triggerStormFlashes(now);
        if (stormIntensity < 0.01f) {
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float pulsedIntensity = stormIntensity * stormPulse;
        int fullAlpha = Math.min(38, Math.round(pulsedIntensity * (14.0f + displayDescent * 24.0f)));
        int lowerAlpha = Math.min(62, Math.round(pulsedIntensity * (20.0f + displayDescent * 42.0f)));
        int fullColor = argb(fullAlpha, 142, 12, 18);
        int lowerColor = argb(lowerAlpha, 210, 18, 24);
        graphics.fill(0, 0, width, height, fullColor);
        graphics.fillGradient(0, height - Math.max(48, height / 3), width, height, argb(0, 210, 18, 24), lowerColor);
    }

    public static boolean isStormActive() {
        updateStormState(System.nanoTime());
        return stormIntensity > 0.04f;
    }

    public static float stormIntensity() {
        updateStormState(System.nanoTime());
        return stormIntensity;
    }

    public static float stormPulse() {
        updateStormState(System.nanoTime());
        return stormPulse;
    }

    public static float stormFogIntensity() {
        updateStormState(System.nanoTime());
        return clamp01(stormIntensity * (0.78f + displayDescent * 0.30f));
    }

    private static void updateStormState(long now) {
        if (lastStormUpdateNanos != 0L && now - lastStormUpdateNanos < STORM_UPDATE_MIN_NANOS) {
            return;
        }
        lastStormUpdateNanos = now;
        if (now - lastPacketNanos > PACKET_FADE_NANOS) {
            targetIntensity = 0.0f;
            targetDescent = 0.0f;
        }

        displayIntensity += (targetIntensity - displayIntensity) * 0.10f;
        displayDescent += (targetDescent - displayDescent) * 0.08f;
        stormPulse = breathingPulse(now);
        stormIntensity = clamp01(displayIntensity * (0.82f + displayDescent * 0.22f));
        updateStormMusic();
    }

    private static void triggerStormFlashes(long now) {
        if (stormIntensity < 0.42f) {
            nextFlashNanos = 0L;
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        if (nextFlashNanos == 0L) {
            nextFlashNanos = now + FLASH_MIN_DELAY_NANOS + randomNanos(FLASH_RANDOM_DELAY_NANOS);
        }
        if (now >= nextFlashNanos) {
            int flashTicks = 2 + STORM_RANDOM.nextInt(4);
            client.level.setSkyFlashTime(flashTicks);
            playRedThunder(client);
            nextFlashNanos = now
                + FLASH_MIN_DELAY_NANOS
                + randomNanos(FLASH_RANDOM_DELAY_NANOS)
                - (long) (stormIntensity * 260_000_000L);
        }
    }

    private static void updateStormMusic() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            musicPlaying = false;
            return;
        }

        if (stormIntensity > 0.18f) {
            if (!musicPlaying || !client.getMusicManager().isPlayingMusic(RED_STORM_MUSIC)) {
                client.getMusicManager().startPlaying(RED_STORM_MUSIC);
                musicPlaying = true;
            }
            return;
        }

        if (musicPlaying) {
            client.getMusicManager().stopPlaying(RED_STORM_MUSIC);
            musicPlaying = false;
        }
    }

    private static void playRedThunder(Minecraft client) {
        if (client.player == null) {
            return;
        }

        float pulse = stormPulse();
        float thunderVolume = 0.70f + stormIntensity * 0.75f;
        float thunderPitch = 0.58f + pulse * 0.12f;
        client.getSoundManager().play(new SimpleSoundInstance(
            SoundEvents.LIGHTNING_BOLT_THUNDER,
            SoundSource.WEATHER,
            thunderVolume,
            thunderPitch,
            STORM_RANDOM,
            client.player.blockPosition()
        ));
        client.getSoundManager().play(new SimpleSoundInstance(
            SoundEvents.WITHER_SPAWN,
            SoundSource.WEATHER,
            0.16f + stormIntensity * 0.18f,
            0.44f + pulse * 0.08f,
            STORM_RANDOM,
            client.player.blockPosition()
        ));
        client.getSoundManager().play(new SimpleSoundInstance(
            SoundEvents.PORTAL_TRIGGER,
            SoundSource.WEATHER,
            0.26f + stormIntensity * 0.22f,
            0.50f + pulse * 0.12f,
            STORM_RANDOM,
            client.player.blockPosition()
        ));
    }

    private static long randomNanos(long maxExclusive) {
        return (long) (STORM_RANDOM.nextDouble() * maxExclusive);
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24)
            | ((red & 0xFF) << 16)
            | ((green & 0xFF) << 8)
            | (blue & 0xFF);
    }

    private static float breathingPulse(long now) {
        double cycle = (now % (long) PULSE_PERIOD_NANOS) / PULSE_PERIOD_NANOS;
        double wave = (Math.sin(cycle * Math.PI * 2.0 - Math.PI / 2.0) + 1.0) * 0.5;
        return PULSE_FLOOR + (float) wave * PULSE_RANGE;
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}

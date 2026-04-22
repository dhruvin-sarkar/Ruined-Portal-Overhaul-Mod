package com.ruinedportaloverhaul.client.atmosphere;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.config.ModConfigManager;
import com.ruinedportaloverhaul.network.DragonPhaseFlashPayload;
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
    private static final long FLASH_TICK_NANOS = 50_000_000L;
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
    private static long redFlashEndNanos;
    private static boolean musicPlaying;

    private PortalAtmosphereClient() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(DragonPhaseFlashPayload.TYPE, (payload, context) ->
            context.client().execute(() -> receiveDragonPhaseFlash(payload))
        );
        ClientPlayNetworking.registerGlobalReceiver(PortalAtmospherePayload.TYPE, (payload, context) ->
            context.client().execute(() -> receive(payload))
        );
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, OVERLAY_ID, PortalAtmosphereClient::renderOverlay);
    }

    private static void receive(PortalAtmospherePayload payload) {
        // Fix: the client storm now respects the live config toggle so disabling it fades out instead of leaving stale target intensity behind.
        if (!ModConfigManager.enableRedStorm()) {
            targetIntensity = 0.0f;
            targetDescent = 0.0f;
            lastPacketNanos = 0L;
            return;
        }
        targetIntensity = clamp01(payload.intensity());
        targetDescent = clamp01(payload.descent());
        lastPacketNanos = System.nanoTime();
    }

    private static void receiveDragonPhaseFlash(DragonPhaseFlashPayload payload) {
        // Fix: dragon phase transitions needed a guaranteed fullscreen cue, so the overlay now accepts an explicit flash packet even when the red storm is faded out.
        redFlashEndNanos = Math.max(redFlashEndNanos, System.nanoTime() + Math.max(1, payload.ticks()) * FLASH_TICK_NANOS);
    }

    private static void renderOverlay(GuiGraphics graphics, DeltaTracker tickCounter) {
        long now = System.nanoTime();
        updateStormState(now);
        triggerStormFlashes(now);
        boolean flashActive = now < redFlashEndNanos;
        if (stormIntensity < 0.01f && !flashActive) {
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        if (stormIntensity >= 0.01f) {
            float pulsedIntensity = stormIntensity * stormPulse;
            int fullAlpha = Math.min(45, Math.round(pulsedIntensity * (16.5f + displayDescent * 28.0f)));
            int lowerAlpha = Math.min(74, Math.round(pulsedIntensity * (24.0f + displayDescent * 50.0f)));
            int fullColor = argb(fullAlpha, 142, 12, 18);
            int lowerColor = argb(lowerAlpha, 210, 18, 24);
            graphics.fill(0, 0, width, height, fullColor);
            graphics.fillGradient(0, height - Math.max(48, height / 3), width, height, argb(0, 210, 18, 24), lowerColor);
        }
        if (flashActive) {
            float flashProgress = (redFlashEndNanos - now) / (float) (3L * FLASH_TICK_NANOS);
            int flashAlpha = Math.min(84, Math.max(0, Math.round(84.0f * flashProgress)));
            graphics.fill(0, 0, width, height, argb(flashAlpha, 138, 0, 0));
        }
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
        return clamp01(stormIntensity * (0.90f + displayDescent * 0.36f));
    }

    private static void updateStormState(long now) {
        // Fix: storm intensity was hardcoded, so the client now fades cleanly when disabled and multiplies the live config intensity instead of freezing one preset.
        if (lastStormUpdateNanos != 0L && now - lastStormUpdateNanos < STORM_UPDATE_MIN_NANOS) {
            return;
        }
        lastStormUpdateNanos = now;
        if (!ModConfigManager.enableRedStorm()) {
            targetIntensity = 0.0f;
            targetDescent = 0.0f;
        }
        if (now - lastPacketNanos > PACKET_FADE_NANOS) {
            targetIntensity = 0.0f;
            targetDescent = 0.0f;
        }

        displayIntensity += (targetIntensity - displayIntensity) * 0.10f;
        displayDescent += (targetDescent - displayDescent) * 0.08f;
        stormPulse = breathingPulse(now);
        stormIntensity = clamp01((float) (displayIntensity * ModConfigManager.stormIntensity()) * (0.94f + displayDescent * 0.26f));
        updateStormMusic();
    }

    private static void triggerStormFlashes(long now) {
        // Fix: thunder cadence was fixed in nanoseconds, so flash scheduling now tracks the live frequency setting and still accelerates under heavier storms.
        if (stormIntensity < 0.42f) {
            nextFlashNanos = 0L;
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        if (nextFlashNanos == 0L) {
            nextFlashNanos = now + baseFlashDelayNanos() + randomNanos(randomFlashDelayNanos());
        }
        if (now >= nextFlashNanos) {
            int flashTicks = 2 + STORM_RANDOM.nextInt(2);
            redFlashEndNanos = now + flashTicks * FLASH_TICK_NANOS;
            playRedThunder(client);
            nextFlashNanos = now
                + baseFlashDelayNanos()
                + randomNanos(randomFlashDelayNanos())
                - tickToNanos(Math.max(0, (int) Math.round(ModConfigManager.thunderFrequency() * stormIntensity * 0.25)));
        }
    }

    private static void updateStormMusic() {
        // Fix: music needed to stop when the storm is disabled live, not only when packets taper out on their own.
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            musicPlaying = false;
            return;
        }

        if (ModConfigManager.enableRedStorm() && stormIntensity > 0.18f) {
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

    private static long baseFlashDelayNanos() {
        return tickToNanos(Math.max(20, (int) Math.round(ModConfigManager.thunderFrequency() * 0.75)));
    }

    private static long randomFlashDelayNanos() {
        return tickToNanos(Math.max(10, (int) Math.round(ModConfigManager.thunderFrequency() * 0.75)));
    }

    private static long tickToNanos(int ticks) {
        return ticks * FLASH_TICK_NANOS;
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

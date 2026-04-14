package com.ruinedportaloverhaul.client.atmosphere;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.network.PortalAtmospherePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public final class PortalAtmosphereClient {
    private static final Identifier OVERLAY_ID = Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "portal_atmosphere_overlay");
    private static final long PACKET_FADE_NANOS = 1_600_000_000L;
    private static float targetIntensity;
    private static float targetDescent;
    private static float displayIntensity;
    private static float displayDescent;
    private static long lastPacketNanos;

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
        if (now - lastPacketNanos > PACKET_FADE_NANOS) {
            targetIntensity = 0.0f;
            targetDescent = 0.0f;
        }

        displayIntensity += (targetIntensity - displayIntensity) * 0.10f;
        displayDescent += (targetDescent - displayDescent) * 0.08f;
        if (displayIntensity < 0.01f) {
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int fullAlpha = Math.min(70, Math.round(displayIntensity * (28.0f + displayDescent * 44.0f)));
        int lowerAlpha = Math.min(96, Math.round(displayIntensity * (34.0f + displayDescent * 62.0f)));
        int fullColor = argb(fullAlpha, 214, 64, 18);
        int lowerColor = argb(lowerAlpha, 255, 86, 20);
        graphics.fill(0, 0, width, height, fullColor);
        graphics.fillGradient(0, height - Math.max(48, height / 3), width, height, argb(0, 255, 86, 20), lowerColor);
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24)
            | ((red & 0xFF) << 16)
            | ((green & 0xFF) << 8)
            | (blue & 0xFF);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}

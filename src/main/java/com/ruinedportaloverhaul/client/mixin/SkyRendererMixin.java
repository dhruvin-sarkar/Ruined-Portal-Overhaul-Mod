package com.ruinedportaloverhaul.client.mixin;

import com.ruinedportaloverhaul.client.atmosphere.PortalAtmosphereClient;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void ruinedportaloverhaul$tintSky(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
        float storm = PortalAtmosphereClient.stormIntensity();
        if (storm <= 0.02f) {
            return;
        }

        float pulse = PortalAtmosphereClient.stormPulse();
        int target = rgb(
            Math.round(94.0f + 28.0f * pulse),
            Math.round(9.0f + 8.0f * pulse),
            Math.round(17.0f + 10.0f * pulse)
        );
        state.skyColor = lerpColor(state.skyColor, target, Math.min(0.94f, storm * 0.92f));
        state.rainBrightness = Math.max(0.0f, state.rainBrightness * (1.0f - storm * 0.62f));
        state.starBrightness = Math.max(0.0f, state.starBrightness * (1.0f - storm * 0.88f));
    }

    private static int rgb(int red, int green, int blue) {
        return (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    private static int lerpColor(int from, int to, float amount) {
        int red = lerp(from >> 16 & 0xFF, to >> 16 & 0xFF, amount);
        int green = lerp(from >> 8 & 0xFF, to >> 8 & 0xFF, amount);
        int blue = lerp(from & 0xFF, to & 0xFF, amount);
        return rgb(red, green, blue);
    }

    private static int lerp(int from, int to, float amount) {
        return Math.round(from + (to - from) * amount) & 0xFF;
    }
}

package com.ruinedportaloverhaul.client.mixin;

import com.ruinedportaloverhaul.client.atmosphere.PortalAtmosphereClient;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin {
    @Inject(method = "getPrecipitationAt", at = @At("RETURN"), cancellable = true)
    private void ruinedportaloverhaul$forceBloodRain(Level level, BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir) {
        if (PortalAtmosphereClient.stormIntensity() > 0.14f) {
            cir.setReturnValue(Biome.Precipitation.RAIN);
        }
    }

    @ModifyArg(
        method = "renderInstances",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
        index = 0
    )
    private int ruinedportaloverhaul$tintRainColor(int color) {
        float storm = PortalAtmosphereClient.stormIntensity();
        if (storm <= 0.02f) {
            return color;
        }

        int alpha = color >>> 24;
        if (alpha == 0) {
            alpha = 255;
        }
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        float pulse = PortalAtmosphereClient.stormPulse();
        int targetRed = Math.round(128.0f + 84.0f * pulse);
        int targetGreen = Math.round(8.0f + 10.0f * pulse);
        int targetBlue = Math.round(16.0f + 16.0f * pulse);
        return alpha << 24
            | lerp(red, targetRed, storm) << 16
            | lerp(green, targetGreen, storm) << 8
            | lerp(blue, targetBlue, storm);
    }

    private static int lerp(int from, int to, float amount) {
        return Math.round(from + (to - from) * amount) & 0xFF;
    }
}

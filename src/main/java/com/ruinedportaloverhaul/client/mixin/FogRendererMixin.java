package com.ruinedportaloverhaul.client.mixin;

import com.ruinedportaloverhaul.client.atmosphere.PortalAtmosphereClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Inject(method = "computeFogColor", at = @At("RETURN"), cancellable = true)
    private void ruinedportaloverhaul$tintFogColor(
        Camera camera,
        float partialTick,
        ClientLevel level,
        int renderDistance,
        float darkenWorldAmount,
        CallbackInfoReturnable<Vector4f> cir
    ) {
        float storm = PortalAtmosphereClient.stormFogIntensity();
        if (storm <= 0.02f) {
            return;
        }

        Vector4f color = new Vector4f(cir.getReturnValue());
        float pulse = PortalAtmosphereClient.stormPulse();
        color.x = lerp(color.x, 0.55f + 0.12f * pulse, storm);
        color.y = lerp(color.y, 0.035f + 0.025f * pulse, storm);
        color.z = lerp(color.z, 0.055f + 0.035f * pulse, storm);
        cir.setReturnValue(color);
    }

    @ModifyArg(
        method = "setupFog",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"),
        index = 5
    )
    private float ruinedportaloverhaul$tightenRenderFogStart(float renderDistanceStart) {
        float storm = PortalAtmosphereClient.stormFogIntensity();
        if (storm <= 0.02f) {
            return renderDistanceStart;
        }
        return Math.min(renderDistanceStart, 4.0f + 10.0f * (1.0f - storm));
    }

    @ModifyArg(
        method = "setupFog",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"),
        index = 6
    )
    private float ruinedportaloverhaul$tightenRenderFogEnd(float renderDistanceEnd) {
        float storm = PortalAtmosphereClient.stormFogIntensity();
        if (storm <= 0.02f) {
            return renderDistanceEnd;
        }
        return Math.min(renderDistanceEnd, 32.0f + 44.0f * (1.0f - storm));
    }

    @ModifyArg(
        method = "setupFog",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"),
        index = 7
    )
    private float ruinedportaloverhaul$tightenSkyFogEnd(float skyEnd) {
        float storm = PortalAtmosphereClient.stormFogIntensity();
        if (storm <= 0.02f) {
            return skyEnd;
        }
        return Math.min(skyEnd, 44.0f + 42.0f * (1.0f - storm));
    }

    @SuppressWarnings("unused")
    private static void ruinedportaloverhaul$signatureAnchor(Camera camera, int renderDistance, DeltaTracker tracker, float darkenWorldAmount, ClientLevel level) {
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * amount;
    }
}

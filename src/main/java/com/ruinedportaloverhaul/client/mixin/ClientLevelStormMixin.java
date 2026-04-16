package com.ruinedportaloverhaul.client.mixin;

import com.ruinedportaloverhaul.client.atmosphere.PortalAtmosphereClient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class ClientLevelStormMixin {
    @Inject(method = "getRainLevel", at = @At("RETURN"), cancellable = true)
    private void ruinedportaloverhaul$forceLocalRedRain(float partialTick, CallbackInfoReturnable<Float> cir) {
        float storm = PortalAtmosphereClient.stormIntensity();
        if (storm > 0.02f) {
            cir.setReturnValue(Math.max(cir.getReturnValue(), storm * 0.96f));
        }
    }

    @Inject(method = "getThunderLevel", at = @At("RETURN"), cancellable = true)
    private void ruinedportaloverhaul$forceLocalThunder(float partialTick, CallbackInfoReturnable<Float> cir) {
        float storm = PortalAtmosphereClient.stormIntensity();
        if (storm > 0.08f) {
            cir.setReturnValue(Math.max(cir.getReturnValue(), storm * 0.86f));
        }
    }

    @Inject(method = "isRaining", at = @At("RETURN"), cancellable = true)
    private void ruinedportaloverhaul$isLocallyRaining(CallbackInfoReturnable<Boolean> cir) {
        if (PortalAtmosphereClient.stormIntensity() > 0.14f) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isThundering", at = @At("RETURN"), cancellable = true)
    private void ruinedportaloverhaul$isLocallyThundering(CallbackInfoReturnable<Boolean> cir) {
        if (PortalAtmosphereClient.stormIntensity() > 0.32f) {
            cir.setReturnValue(true);
        }
    }
}

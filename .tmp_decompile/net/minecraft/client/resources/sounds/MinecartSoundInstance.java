/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;

@Environment(value=EnvType.CLIENT)
public class MinecartSoundInstance
extends AbstractTickableSoundInstance {
    private static final float VOLUME_MIN = 0.0f;
    private static final float VOLUME_MAX = 0.7f;
    private static final float PITCH_MIN = 0.0f;
    private static final float PITCH_MAX = 1.0f;
    private static final float PITCH_DELTA = 0.0025f;
    private final AbstractMinecart minecart;
    private float pitch = 0.0f;

    public MinecartSoundInstance(AbstractMinecart abstractMinecart) {
        super(SoundEvents.MINECART_RIDING, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.minecart = abstractMinecart;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
        this.x = (float)abstractMinecart.getX();
        this.y = (float)abstractMinecart.getY();
        this.z = (float)abstractMinecart.getZ();
    }

    @Override
    public boolean canPlaySound() {
        return !this.minecart.isSilent();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        boolean bl;
        if (this.minecart.isRemoved()) {
            this.stop();
            return;
        }
        this.x = (float)this.minecart.getX();
        this.y = (float)this.minecart.getY();
        this.z = (float)this.minecart.getZ();
        float f = (float)this.minecart.getDeltaMovement().horizontalDistance();
        boolean bl2 = bl = !this.minecart.isOnRails() && this.minecart.getBehavior() instanceof NewMinecartBehavior;
        if (f >= 0.01f && this.minecart.level().tickRateManager().runsNormally() && !bl) {
            this.pitch = Mth.clamp(this.pitch + 0.0025f, 0.0f, 1.0f);
            this.volume = Mth.lerp(Mth.clamp(f, 0.0f, 0.5f), 0.0f, 0.7f);
        } else {
            this.pitch = 0.0f;
            this.volume = 0.0f;
        }
    }
}


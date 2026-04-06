/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class DustParticleBase<T extends ScalableParticleOptionsBase>
extends SingleQuadParticle {
    private final SpriteSet sprites;

    protected DustParticleBase(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, T scalableParticleOptionsBase, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, g, h, i, spriteSet.first());
        this.friction = 0.96f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = spriteSet;
        this.xd *= (double)0.1f;
        this.yd *= (double)0.1f;
        this.zd *= (double)0.1f;
        this.quadSize *= 0.75f * ((ScalableParticleOptionsBase)scalableParticleOptionsBase).getScale();
        int j = (int)(8.0 / (this.random.nextDouble() * 0.8 + 0.2));
        this.lifetime = (int)Math.max((float)j * ((ScalableParticleOptionsBase)scalableParticleOptionsBase).getScale(), 1.0f);
        this.setSpriteFromAge(spriteSet);
    }

    protected float randomizeColor(float f, float g) {
        return (this.random.nextFloat() * 0.2f + 0.8f) * f * g;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0f, 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }
}


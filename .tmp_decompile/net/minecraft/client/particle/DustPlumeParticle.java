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
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class DustPlumeParticle
extends BaseAshSmokeParticle {
    private static final int COLOR_RGB24 = 12235202;

    protected DustPlumeParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, float j, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, 0.7f, 0.6f, 0.7f, g, h + (double)0.15f, i, j, spriteSet, 0.5f, 7, 0.5f, false);
        float k = this.random.nextFloat() * 0.2f;
        this.rCol = (float)ARGB.red(12235202) / 255.0f - k;
        this.gCol = (float)ARGB.green(12235202) / 255.0f - k;
        this.bCol = (float)ARGB.blue(12235202) / 255.0f - k;
    }

    @Override
    public void tick() {
        this.gravity = 0.88f * this.gravity;
        this.friction = 0.92f * this.friction;
        super.tick();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            return new DustPlumeParticle(clientLevel, d, e, f, g, h, i, 1.0f, this.sprites);
        }
    }
}


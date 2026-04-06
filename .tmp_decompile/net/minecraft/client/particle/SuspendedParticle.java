/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleLimit;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class SuspendedParticle
extends SingleQuadParticle {
    SuspendedParticle(ClientLevel clientLevel, double d, double e, double f, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e - 0.125, f, textureAtlasSprite);
        this.setSize(0.01f, 0.01f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.2f;
        this.lifetime = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.friction = 1.0f;
        this.gravity = 0.0f;
    }

    SuspendedParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e - 0.125, f, g, h, i, textureAtlasSprite);
        this.setSize(0.01f, 0.01f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.6f;
        this.lifetime = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.friction = 1.0f;
        this.gravity = 0.0f;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Environment(value=EnvType.CLIENT)
    public static class WarpedSporeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WarpedSporeProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            double j = (double)randomSource.nextFloat() * -1.9 * (double)randomSource.nextFloat() * 0.1;
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, d, e, f, 0.0, j, 0.0, this.sprite.get(randomSource));
            suspendedParticle.setColor(0.1f, 0.1f, 0.3f);
            suspendedParticle.setSize(0.001f, 0.001f);
            return suspendedParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CrimsonSporeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public CrimsonSporeProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            double j = randomSource.nextGaussian() * (double)1.0E-6f;
            double k = randomSource.nextGaussian() * (double)1.0E-4f;
            double l = randomSource.nextGaussian() * (double)1.0E-6f;
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, d, e, f, j, k, l, this.sprite.get(randomSource));
            suspendedParticle.setColor(0.9f, 0.4f, 0.5f);
            return suspendedParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SporeBlossomAirProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SporeBlossomAirProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            SuspendedParticle suspendedParticle = new SuspendedParticle(this, clientLevel, d, e, f, 0.0, -0.8f, 0.0, this.sprite.get(randomSource)){

                @Override
                public Optional<ParticleLimit> getParticleLimit() {
                    return Optional.of(ParticleLimit.SPORE_BLOSSOM);
                }
            };
            suspendedParticle.lifetime = Mth.randomBetweenInclusive(randomSource, 500, 1000);
            suspendedParticle.gravity = 0.01f;
            suspendedParticle.setColor(0.32f, 0.5f, 0.22f);
            return suspendedParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class UnderwaterProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public UnderwaterProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, d, e, f, this.sprite.get(randomSource));
            suspendedParticle.setColor(0.4f, 0.4f, 0.7f);
            return suspendedParticle;
        }
    }
}


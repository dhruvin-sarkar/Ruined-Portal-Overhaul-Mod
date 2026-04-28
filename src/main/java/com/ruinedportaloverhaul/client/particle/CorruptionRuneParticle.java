package com.ruinedportaloverhaul.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class CorruptionRuneParticle extends SimpleAnimatedParticle {
    private final SpriteSet sprites;

    private CorruptionRuneParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, sprites, 0.0f);
        this.sprites = sprites;
        this.xd = xSpeed * 0.05;
        this.yd = 0.006 + ySpeed * 0.05;
        this.zd = zSpeed * 0.05;
        this.lifetime = 52 + this.random.nextInt(16);
        this.quadSize = 0.18f + this.random.nextFloat() * 0.08f;
        this.friction = 0.88f;
        this.roll = this.random.nextFloat() * (float) Math.PI * 2.0f;
        this.oRoll = this.roll;
        this.setColor(0.48f, 0.0f, 0.9f);
        this.setAlpha(0.85f);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += 0.05f;
        this.setSpriteFromAge(this.sprites);
        this.setAlpha(0.85f * (1.0f - (float) this.age / (float) this.lifetime));
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType type,
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            RandomSource random
        ) {
            return new CorruptionRuneParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}

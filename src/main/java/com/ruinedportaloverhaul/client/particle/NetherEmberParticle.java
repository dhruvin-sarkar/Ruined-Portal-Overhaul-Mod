package com.ruinedportaloverhaul.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class NetherEmberParticle extends SimpleAnimatedParticle {
    private final SpriteSet sprites;

    private NetherEmberParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, sprites, 0.0f);
        this.sprites = sprites;
        this.xd = xSpeed * 0.15;
        this.yd = 0.018 + ySpeed * 0.1;
        this.zd = zSpeed * 0.15;
        this.lifetime = 32 + this.random.nextInt(16);
        this.quadSize = 0.08f + this.random.nextFloat() * 0.06f;
        this.friction = 0.92f;
        this.gravity = -0.01f;
        this.setColor(1.0f, 0.34f, 0.03f);
        this.setAlpha(0.92f);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.setAlpha(0.92f * (1.0f - (float) this.age / (float) this.lifetime));
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
            return new NetherEmberParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}

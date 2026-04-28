package com.ruinedportaloverhaul.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class DragonBloodParticle extends SimpleAnimatedParticle {
    private final SpriteSet sprites;

    private DragonBloodParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, sprites, 0.0f);
        this.sprites = sprites;
        this.xd = xSpeed * 0.08;
        this.yd = ySpeed * 0.08;
        this.zd = zSpeed * 0.08;
        this.lifetime = 24 + this.random.nextInt(12);
        this.quadSize = 0.07f;
        this.gravity = 0.08f;
        this.friction = 0.86f;
        this.setColor(0.35f, 0.0f, 0.0f);
        this.setAlpha(0.9f);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.setAlpha(0.9f * (1.0f - (float) this.age / (float) this.lifetime));
        if (this.onGround) {
            this.remove();
        }
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
            return new DragonBloodParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}

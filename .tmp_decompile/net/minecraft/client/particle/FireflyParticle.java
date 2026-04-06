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
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class FireflyParticle
extends SingleQuadParticle {
    private static final float PARTICLE_FADE_OUT_LIGHT_TIME = 0.3f;
    private static final float PARTICLE_FADE_IN_LIGHT_TIME = 0.1f;
    private static final float PARTICLE_FADE_OUT_ALPHA_TIME = 0.5f;
    private static final float PARTICLE_FADE_IN_ALPHA_TIME = 0.3f;
    private static final int PARTICLE_MIN_LIFETIME = 200;
    private static final int PARTICLE_MAX_LIFETIME = 300;

    FireflyParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e, f, g, h, i, textureAtlasSprite);
        this.speedUpWhenYMotionIsBlocked = true;
        this.friction = 0.96f;
        this.quadSize *= 0.75f;
        this.yd *= (double)0.8f;
        this.xd *= (double)0.8f;
        this.zd *= (double)0.8f;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public int getLightColor(float f) {
        return (int)(255.0f * FireflyParticle.getFadeAmount(this.getLifetimeProgress((float)this.age + f), 0.1f, 0.3f));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z)).isAir()) {
            this.remove();
            return;
        }
        this.setAlpha(FireflyParticle.getFadeAmount(this.getLifetimeProgress(this.age), 0.3f, 0.5f));
        if (this.random.nextFloat() > 0.95f || this.age == 1) {
            this.setParticleSpeed(-0.05f + 0.1f * this.random.nextFloat(), -0.05f + 0.1f * this.random.nextFloat(), -0.05f + 0.1f * this.random.nextFloat());
        }
    }

    private float getLifetimeProgress(float f) {
        return Mth.clamp(f / (float)this.lifetime, 0.0f, 1.0f);
    }

    private static float getFadeAmount(float f, float g, float h) {
        if (f >= 1.0f - g) {
            return (1.0f - f) / g;
        }
        if (f <= h) {
            return f / h;
        }
        return 1.0f;
    }

    @Environment(value=EnvType.CLIENT)
    public static class FireflyProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public FireflyProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FireflyParticle fireflyParticle = new FireflyParticle(clientLevel, d, e, f, 0.5 - randomSource.nextDouble(), randomSource.nextBoolean() ? h : -h, 0.5 - randomSource.nextDouble(), this.sprite.get(randomSource));
            fireflyParticle.setLifetime(randomSource.nextIntBetweenInclusive(200, 300));
            fireflyParticle.scale(1.5f);
            fireflyParticle.setAlpha(0.0f);
            return fireflyParticle;
        }
    }
}


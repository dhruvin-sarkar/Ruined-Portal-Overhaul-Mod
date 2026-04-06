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
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class FlyTowardsPositionParticle
extends SingleQuadParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;
    private final boolean isGlowing;
    private final Particle.LifetimeAlpha lifetimeAlpha;

    FlyTowardsPositionParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, TextureAtlasSprite textureAtlasSprite) {
        this(clientLevel, d, e, f, g, h, i, false, Particle.LifetimeAlpha.ALWAYS_OPAQUE, textureAtlasSprite);
    }

    FlyTowardsPositionParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, boolean bl, Particle.LifetimeAlpha lifetimeAlpha, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e, f, textureAtlasSprite);
        this.isGlowing = bl;
        this.lifetimeAlpha = lifetimeAlpha;
        this.setAlpha(lifetimeAlpha.startAlpha());
        this.xd = g;
        this.yd = h;
        this.zd = i;
        this.xStart = d;
        this.yStart = e;
        this.zStart = f;
        this.xo = d + g;
        this.yo = e + h;
        this.zo = f + i;
        this.x = this.xo;
        this.y = this.yo;
        this.z = this.zo;
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.5f + 0.2f);
        float j = this.random.nextFloat() * 0.6f + 0.4f;
        this.rCol = 0.9f * j;
        this.gCol = 0.9f * j;
        this.bCol = j;
        this.hasPhysics = false;
        this.lifetime = (int)(this.random.nextFloat() * 10.0f) + 30;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        if (this.lifetimeAlpha.isOpaque()) {
            return SingleQuadParticle.Layer.OPAQUE;
        }
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void move(double d, double e, double f) {
        this.setBoundingBox(this.getBoundingBox().move(d, e, f));
        this.setLocationFromBoundingbox();
    }

    @Override
    public int getLightColor(float f) {
        if (this.isGlowing) {
            return 240;
        }
        int i = super.getLightColor(f);
        float g = (float)this.age / (float)this.lifetime;
        g *= g;
        g *= g;
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        if ((k += (int)(g * 15.0f * 16.0f)) > 240) {
            k = 240;
        }
        return j | k << 16;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float f = (float)this.age / (float)this.lifetime;
        f = 1.0f - f;
        float g = 1.0f - f;
        g *= g;
        g *= g;
        this.x = this.xStart + this.xd * (double)f;
        this.y = this.yStart + this.yd * (double)f - (double)(g * 1.2f);
        this.z = this.zStart + this.zd * (double)f;
    }

    @Override
    public void extract(QuadParticleRenderState quadParticleRenderState, Camera camera, float f) {
        this.setAlpha(this.lifetimeAlpha.currentAlphaForAge(this.age, this.lifetime, f));
        super.extract(quadParticleRenderState, camera, f);
    }

    @Environment(value=EnvType.CLIENT)
    public static class VaultConnectionProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public VaultConnectionProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FlyTowardsPositionParticle flyTowardsPositionParticle = new FlyTowardsPositionParticle(clientLevel, d, e, f, g, h, i, true, new Particle.LifetimeAlpha(0.0f, 0.6f, 0.25f, 1.0f), this.sprite.get(randomSource));
            flyTowardsPositionParticle.scale(1.5f);
            return flyTowardsPositionParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class NautilusProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public NautilusProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FlyTowardsPositionParticle flyTowardsPositionParticle = new FlyTowardsPositionParticle(clientLevel, d, e, f, g, h, i, this.sprite.get(randomSource));
            return flyTowardsPositionParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class EnchantProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public EnchantProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FlyTowardsPositionParticle flyTowardsPositionParticle = new FlyTowardsPositionParticle(clientLevel, d, e, f, g, h, i, this.sprite.get(randomSource));
            return flyTowardsPositionParticle;
        }
    }
}


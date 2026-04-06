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
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class FlyStraightTowardsParticle
extends SingleQuadParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;
    private final int startColor;
    private final int endColor;

    FlyStraightTowardsParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, int j, int k, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e, f, textureAtlasSprite);
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
        this.hasPhysics = false;
        this.lifetime = (int)(this.random.nextFloat() * 5.0f) + 25;
        this.startColor = j;
        this.endColor = k;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public void move(double d, double e, double f) {
    }

    @Override
    public int getLightColor(float f) {
        return 240;
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
        float g = 1.0f - f;
        this.x = this.xStart + this.xd * (double)g;
        this.y = this.yStart + this.yd * (double)g;
        this.z = this.zStart + this.zd * (double)g;
        int i = ARGB.srgbLerp(f, this.startColor, this.endColor);
        this.setColor((float)ARGB.red(i) / 255.0f, (float)ARGB.green(i) / 255.0f, (float)ARGB.blue(i) / 255.0f);
        this.setAlpha((float)ARGB.alpha(i) / 255.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public static class OminousSpawnProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public OminousSpawnProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FlyStraightTowardsParticle flyStraightTowardsParticle = new FlyStraightTowardsParticle(clientLevel, d, e, f, g, h, i, -12210434, -1, this.sprite.get(randomSource));
            flyStraightTowardsParticle.scale(Mth.randomBetween(clientLevel.getRandom(), 3.0f, 5.0f));
            return flyStraightTowardsParticle;
        }
    }
}


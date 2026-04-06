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
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class FallingLeavesParticle
extends SingleQuadParticle {
    private static final float ACCELERATION_SCALE = 0.0025f;
    private static final int INITIAL_LIFETIME = 300;
    private static final int CURVE_ENDPOINT_TIME = 300;
    private float rotSpeed;
    private final float spinAcceleration;
    private final float windBig;
    private final boolean swirl;
    private final boolean flowAway;
    private final double xaFlowScale;
    private final double zaFlowScale;
    private final double swirlPeriod;

    protected FallingLeavesParticle(ClientLevel clientLevel, double d, double e, double f, TextureAtlasSprite textureAtlasSprite, float g, float h, boolean bl, boolean bl2, float i, float j) {
        super(clientLevel, d, e, f, textureAtlasSprite);
        float k;
        this.rotSpeed = (float)Math.toRadians(this.random.nextBoolean() ? -30.0 : 30.0);
        this.spinAcceleration = (float)Math.toRadians(this.random.nextBoolean() ? -5.0 : 5.0);
        this.windBig = h;
        this.swirl = bl;
        this.flowAway = bl2;
        this.lifetime = 300;
        this.gravity = g * 1.2f * 0.0025f;
        this.quadSize = k = i * (this.random.nextBoolean() ? 0.05f : 0.075f);
        this.setSize(k, k);
        this.friction = 1.0f;
        this.yd = -j;
        float l = this.random.nextFloat();
        this.xaFlowScale = Math.cos(Math.toRadians(l * 60.0f)) * (double)this.windBig;
        this.zaFlowScale = Math.sin(Math.toRadians(l * 60.0f)) * (double)this.windBig;
        this.swirlPeriod = Math.toRadians(1000.0f + l * 3000.0f);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        }
        if (this.removed) {
            return;
        }
        float f = 300 - this.lifetime;
        float g = Math.min(f / 300.0f, 1.0f);
        double d = 0.0;
        double e = 0.0;
        if (this.flowAway) {
            d += this.xaFlowScale * Math.pow(g, 1.25);
            e += this.zaFlowScale * Math.pow(g, 1.25);
        }
        if (this.swirl) {
            d += (double)g * Math.cos((double)g * this.swirlPeriod) * (double)this.windBig;
            e += (double)g * Math.sin((double)g * this.swirlPeriod) * (double)this.windBig;
        }
        this.xd += d * (double)0.0025f;
        this.zd += e * (double)0.0025f;
        this.yd -= (double)this.gravity;
        this.rotSpeed += this.spinAcceleration / 20.0f;
        this.oRoll = this.roll;
        this.roll += this.rotSpeed / 20.0f;
        this.move(this.xd, this.yd, this.zd);
        if (this.onGround || this.lifetime < 299 && (this.xd == 0.0 || this.zd == 0.0)) {
            this.remove();
        }
        if (this.removed) {
            return;
        }
        this.xd *= (double)this.friction;
        this.yd *= (double)this.friction;
        this.zd *= (double)this.friction;
    }

    @Environment(value=EnvType.CLIENT)
    public static class TintedLeavesProvider
    implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprites;

        public TintedLeavesProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(ColorParticleOption colorParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FallingLeavesParticle fallingLeavesParticle = new FallingLeavesParticle(clientLevel, d, e, f, this.sprites.get(randomSource), 0.07f, 10.0f, true, false, 2.0f, 0.021f);
            fallingLeavesParticle.setColor(colorParticleOption.getRed(), colorParticleOption.getGreen(), colorParticleOption.getBlue());
            return fallingLeavesParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class PaleOakProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public PaleOakProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            return new FallingLeavesParticle(clientLevel, d, e, f, this.sprites.get(randomSource), 0.07f, 10.0f, true, false, 2.0f, 0.021f);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CherryProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public CherryProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            return new FallingLeavesParticle(clientLevel, d, e, f, this.sprites.get(randomSource), 0.25f, 2.0f, false, true, 1.0f, 0.0f);
        }
    }
}


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
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class BubbleColumnUpParticle
extends SingleQuadParticle {
    BubbleColumnUpParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e, f, textureAtlasSprite);
        this.gravity = -0.125f;
        this.friction = 0.85f;
        this.setSize(0.02f, 0.02f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.2f;
        this.xd = g * (double)0.2f + (double)((this.random.nextFloat() * 2.0f - 1.0f) * 0.02f);
        this.yd = h * (double)0.2f + (double)((this.random.nextFloat() * 2.0f - 1.0f) * 0.02f);
        this.zd = i * (double)0.2f + (double)((this.random.nextFloat() * 2.0f - 1.0f) * 0.02f);
        this.lifetime = (int)(40.0 / ((double)this.random.nextFloat() * 0.8 + 0.2));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed && !this.level.getFluidState(BlockPos.containing(this.x, this.y, this.z)).is(FluidTags.WATER)) {
            this.remove();
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            BubbleColumnUpParticle bubbleColumnUpParticle = new BubbleColumnUpParticle(clientLevel, d, e, f, g, h, i, this.sprite.get(randomSource));
            return bubbleColumnUpParticle;
        }
    }
}


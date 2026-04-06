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
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class WaterDropParticle
extends SingleQuadParticle {
    protected WaterDropParticle(ClientLevel clientLevel, double d, double e, double f, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0, textureAtlasSprite);
        this.xd *= (double)0.3f;
        this.yd = this.random.nextFloat() * 0.2f + 0.1f;
        this.zd *= (double)0.3f;
        this.setSize(0.01f, 0.01f);
        this.gravity = 0.06f;
        this.lifetime = (int)(8.0 / ((double)this.random.nextFloat() * 0.8 + 0.2));
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public void tick() {
        BlockPos blockPos;
        double d;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }
        this.yd -= (double)this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= (double)0.98f;
        this.yd *= (double)0.98f;
        this.zd *= (double)0.98f;
        if (this.onGround) {
            if (this.random.nextFloat() < 0.5f) {
                this.remove();
            }
            this.xd *= (double)0.7f;
            this.zd *= (double)0.7f;
        }
        if ((d = Math.max(this.level.getBlockState(blockPos = BlockPos.containing(this.x, this.y, this.z)).getCollisionShape(this.level, blockPos).max(Direction.Axis.Y, this.x - (double)blockPos.getX(), this.z - (double)blockPos.getZ()), (double)this.level.getFluidState(blockPos).getHeight(this.level, blockPos))) > 0.0 && this.y < (double)blockPos.getY() + d) {
            this.remove();
        }
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
            return new WaterDropParticle(clientLevel, d, e, f, this.sprite.get(randomSource));
        }
    }
}


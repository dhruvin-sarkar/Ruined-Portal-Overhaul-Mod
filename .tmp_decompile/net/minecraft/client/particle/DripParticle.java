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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

@Environment(value=EnvType.CLIENT)
public class DripParticle
extends SingleQuadParticle {
    private final Fluid type;
    protected boolean isGlowing;

    DripParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e, f, textureAtlasSprite);
        this.setSize(0.01f, 0.01f);
        this.gravity = 0.06f;
        this.type = fluid;
    }

    protected Fluid getType() {
        return this.type;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public int getLightColor(float f) {
        if (this.isGlowing) {
            return 240;
        }
        return super.getLightColor(f);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.preMoveUpdate();
        if (this.removed) {
            return;
        }
        this.yd -= (double)this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.postMoveUpdate();
        if (this.removed) {
            return;
        }
        this.xd *= (double)0.98f;
        this.yd *= (double)0.98f;
        this.zd *= (double)0.98f;
        if (this.type == Fluids.EMPTY) {
            return;
        }
        BlockPos blockPos = BlockPos.containing(this.x, this.y, this.z);
        FluidState fluidState = this.level.getFluidState(blockPos);
        if (fluidState.getType() == this.type && this.y < (double)((float)blockPos.getY() + fluidState.getHeight(this.level, blockPos))) {
            this.remove();
        }
    }

    protected void preMoveUpdate() {
        if (this.lifetime-- <= 0) {
            this.remove();
        }
    }

    protected void postMoveUpdate() {
    }

    @Environment(value=EnvType.CLIENT)
    public static class ObsidianTearLandProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public ObsidianTearLandProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            DripLandParticle dripParticle = new DripLandParticle(clientLevel, d, e, f, Fluids.EMPTY, this.sprite.get(randomSource));
            dripParticle.isGlowing = true;
            dripParticle.lifetime = (int)(28.0 / ((double)randomSource.nextFloat() * 0.8 + 0.2));
            dripParticle.setColor(0.51171875f, 0.03125f, 0.890625f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ObsidianTearFallProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public ObsidianTearFallProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FallAndLandParticle dripParticle = new FallAndLandParticle(clientLevel, d, e, f, Fluids.EMPTY, ParticleTypes.LANDING_OBSIDIAN_TEAR, this.sprite.get(randomSource));
            dripParticle.isGlowing = true;
            dripParticle.gravity = 0.01f;
            dripParticle.setColor(0.51171875f, 0.03125f, 0.890625f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ObsidianTearHangProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public ObsidianTearHangProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            DripHangParticle dripHangParticle = new DripHangParticle(clientLevel, d, e, f, Fluids.EMPTY, ParticleTypes.FALLING_OBSIDIAN_TEAR, this.sprite.get(randomSource));
            dripHangParticle.isGlowing = true;
            dripHangParticle.gravity *= 0.01f;
            dripHangParticle.lifetime = 100;
            dripHangParticle.setColor(0.51171875f, 0.03125f, 0.890625f);
            return dripHangParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SporeBlossomFallProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SporeBlossomFallProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FallingParticle dripParticle = new FallingParticle(clientLevel, d, e, f, Fluids.EMPTY, this.sprite.get(randomSource));
            dripParticle.lifetime = (int)(64.0f / Mth.randomBetween(dripParticle.random, 0.1f, 0.9f));
            dripParticle.gravity = 0.005f;
            dripParticle.setColor(0.32f, 0.5f, 0.22f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class NectarFallProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public NectarFallProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FallingParticle dripParticle = new FallingParticle(clientLevel, d, e, f, Fluids.EMPTY, this.sprite.get(randomSource));
            dripParticle.lifetime = (int)(16.0 / ((double)randomSource.nextFloat() * 0.8 + 0.2));
            dripParticle.gravity = 0.007f;
            dripParticle.setColor(0.92f, 0.782f, 0.72f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DripstoneLavaFallProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public DripstoneLavaFallProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            DripstoneFallAndLandParticle dripParticle = new DripstoneFallAndLandParticle(clientLevel, d, e, f, Fluids.LAVA, ParticleTypes.LANDING_LAVA, this.sprite.get(randomSource));
            dripParticle.setColor(1.0f, 0.2857143f, 0.083333336f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DripstoneLavaHangProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public DripstoneLavaHangProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            CoolingDripHangParticle coolingDripHangParticle = new CoolingDripHangParticle(clientLevel, d, e, f, Fluids.LAVA, ParticleTypes.FALLING_DRIPSTONE_LAVA, this.sprite.get(randomSource));
            return coolingDripHangParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DripstoneWaterFallProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public DripstoneWaterFallProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            DripstoneFallAndLandParticle dripParticle = new DripstoneFallAndLandParticle(clientLevel, d, e, f, Fluids.WATER, ParticleTypes.SPLASH, this.sprite.get(randomSource));
            dripParticle.setColor(0.2f, 0.3f, 1.0f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DripstoneWaterHangProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public DripstoneWaterHangProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            DripHangParticle dripParticle = new DripHangParticle(clientLevel, d, e, f, Fluids.WATER, ParticleTypes.FALLING_DRIPSTONE_WATER, this.sprite.get(randomSource));
            dripParticle.setColor(0.2f, 0.3f, 1.0f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class HoneyLandProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public HoneyLandProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            DripLandParticle dripParticle = new DripLandParticle(clientLevel, d, e, f, Fluids.EMPTY, this.sprite.get(randomSource));
            dripParticle.lifetime = (int)(128.0 / ((double)randomSource.nextFloat() * 0.8 + 0.2));
            dripParticle.setColor(0.522f, 0.408f, 0.082f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class HoneyFallProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public HoneyFallProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            HoneyFallAndLandParticle dripParticle = new HoneyFallAndLandParticle(clientLevel, d, e, f, Fluids.EMPTY, ParticleTypes.LANDING_HONEY, this.sprite.get(randomSource));
            dripParticle.gravity = 0.01f;
            dripParticle.setColor(0.582f, 0.448f, 0.082f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class HoneyHangProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public HoneyHangProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            DripHangParticle dripHangParticle = new DripHangParticle(clientLevel, d, e, f, Fluids.EMPTY, ParticleTypes.FALLING_HONEY, this.sprite.get(randomSource));
            dripHangParticle.gravity *= 0.01f;
            dripHangParticle.lifetime = 100;
            dripHangParticle.setColor(0.622f, 0.508f, 0.082f);
            return dripHangParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class LavaLandProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public LavaLandProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            DripLandParticle dripParticle = new DripLandParticle(clientLevel, d, e, f, Fluids.LAVA, this.sprite.get(randomSource));
            dripParticle.setColor(1.0f, 0.2857143f, 0.083333336f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class LavaFallProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public LavaFallProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FallAndLandParticle dripParticle = new FallAndLandParticle(clientLevel, d, e, f, Fluids.LAVA, ParticleTypes.LANDING_LAVA, this.sprite.get(randomSource));
            dripParticle.setColor(1.0f, 0.2857143f, 0.083333336f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class LavaHangProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public LavaHangProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            CoolingDripHangParticle coolingDripHangParticle = new CoolingDripHangParticle(clientLevel, d, e, f, Fluids.LAVA, ParticleTypes.FALLING_LAVA, this.sprite.get(randomSource));
            return coolingDripHangParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WaterFallProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WaterFallProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            FallAndLandParticle dripParticle = new FallAndLandParticle(clientLevel, d, e, f, Fluids.WATER, ParticleTypes.SPLASH, this.sprite.get(randomSource));
            dripParticle.setColor(0.2f, 0.3f, 1.0f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WaterHangProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WaterHangProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            DripHangParticle dripParticle = new DripHangParticle(clientLevel, d, e, f, Fluids.WATER, ParticleTypes.FALLING_WATER, this.sprite.get(randomSource));
            dripParticle.setColor(0.2f, 0.3f, 1.0f);
            return dripParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DripLandParticle
    extends DripParticle {
        DripLandParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, TextureAtlasSprite textureAtlasSprite) {
            super(clientLevel, d, e, f, fluid, textureAtlasSprite);
            this.lifetime = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class FallingParticle
    extends DripParticle {
        FallingParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, TextureAtlasSprite textureAtlasSprite) {
            super(clientLevel, d, e, f, fluid, textureAtlasSprite);
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DripstoneFallAndLandParticle
    extends FallAndLandParticle {
        DripstoneFallAndLandParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions, TextureAtlasSprite textureAtlasSprite) {
            super(clientLevel, d, e, f, fluid, particleOptions, textureAtlasSprite);
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                SoundEvent soundEvent = this.getType() == Fluids.LAVA ? SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA : SoundEvents.POINTED_DRIPSTONE_DRIP_WATER;
                float f = Mth.randomBetween(this.random, 0.3f, 1.0f);
                this.level.playLocalSound(this.x, this.y, this.z, soundEvent, SoundSource.BLOCKS, f, 1.0f, false);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class HoneyFallAndLandParticle
    extends FallAndLandParticle {
        HoneyFallAndLandParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions, TextureAtlasSprite textureAtlasSprite) {
            super(clientLevel, d, e, f, fluid, particleOptions, textureAtlasSprite);
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
                float f = Mth.randomBetween(this.random, 0.3f, 1.0f);
                this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, f, 1.0f, false);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class FallAndLandParticle
    extends FallingParticle {
        protected final ParticleOptions landParticle;

        FallAndLandParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions, TextureAtlasSprite textureAtlasSprite) {
            super(clientLevel, d, e, f, fluid, textureAtlasSprite);
            this.lifetime = (int)(64.0 / ((double)this.random.nextFloat() * 0.8 + 0.2));
            this.landParticle = particleOptions;
        }

        @Override
        protected void postMoveUpdate() {
            if (this.onGround) {
                this.remove();
                this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CoolingDripHangParticle
    extends DripHangParticle {
        CoolingDripHangParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions, TextureAtlasSprite textureAtlasSprite) {
            super(clientLevel, d, e, f, fluid, particleOptions, textureAtlasSprite);
        }

        @Override
        protected void preMoveUpdate() {
            this.rCol = 1.0f;
            this.gCol = 16.0f / (float)(40 - this.lifetime + 16);
            this.bCol = 4.0f / (float)(40 - this.lifetime + 8);
            super.preMoveUpdate();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DripHangParticle
    extends DripParticle {
        private final ParticleOptions fallingParticle;

        DripHangParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions, TextureAtlasSprite textureAtlasSprite) {
            super(clientLevel, d, e, f, fluid, textureAtlasSprite);
            this.fallingParticle = particleOptions;
            this.gravity *= 0.02f;
            this.lifetime = 40;
        }

        @Override
        protected void preMoveUpdate() {
            if (this.lifetime-- <= 0) {
                this.remove();
                this.level.addParticle(this.fallingParticle, this.x, this.y, this.z, this.xd, this.yd, this.zd);
            }
        }

        @Override
        protected void postMoveUpdate() {
            this.xd *= 0.02;
            this.yd *= 0.02;
            this.zd *= 0.02;
        }
    }
}


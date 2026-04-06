/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TintedParticleLeavesBlock
extends LeavesBlock {
    public static final MapCodec<TintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.floatRange(0.0f, 1.0f).fieldOf("leaf_particle_chance").forGetter(tintedParticleLeavesBlock -> Float.valueOf(tintedParticleLeavesBlock.leafParticleChance)), TintedParticleLeavesBlock.propertiesCodec()).apply((Applicative)instance, TintedParticleLeavesBlock::new));

    public TintedParticleLeavesBlock(float f, BlockBehaviour.Properties properties) {
        super(f, properties);
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos blockPos, RandomSource randomSource) {
        ColorParticleOption colorParticleOption = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, level.getClientLeafTintColor(blockPos));
        ParticleUtils.spawnParticleBelow(level, blockPos, randomSource, colorParticleOption);
    }

    public MapCodec<? extends TintedParticleLeavesBlock> codec() {
        return CODEC;
    }
}


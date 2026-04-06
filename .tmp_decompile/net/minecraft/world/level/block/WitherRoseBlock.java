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
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WitherRoseBlock
extends FlowerBlock {
    public static final MapCodec<WitherRoseBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), WitherRoseBlock.propertiesCodec()).apply((Applicative)instance, WitherRoseBlock::new));

    public MapCodec<WitherRoseBlock> codec() {
        return CODEC;
    }

    public WitherRoseBlock(Holder<MobEffect> holder, float f, BlockBehaviour.Properties properties) {
        this(WitherRoseBlock.makeEffectList(holder, f), properties);
    }

    public WitherRoseBlock(SuspiciousStewEffects suspiciousStewEffects, BlockBehaviour.Properties properties) {
        super(suspiciousStewEffects, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return super.mayPlaceOn(blockState, blockGetter, blockPos) || blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.SOUL_SAND) || blockState.is(Blocks.SOUL_SOIL);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        VoxelShape voxelShape = this.getShape(blockState, level, blockPos, CollisionContext.empty());
        Vec3 vec3 = voxelShape.bounds().getCenter();
        double d = (double)blockPos.getX() + vec3.x;
        double e = (double)blockPos.getZ() + vec3.z;
        for (int i = 0; i < 3; ++i) {
            if (!randomSource.nextBoolean()) continue;
            level.addParticle(ParticleTypes.SMOKE, d + randomSource.nextDouble() / 5.0, (double)blockPos.getY() + (0.5 - randomSource.nextDouble()), e + randomSource.nextDouble() / 5.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        if (level instanceof ServerLevel) {
            LivingEntity livingEntity;
            ServerLevel serverLevel = (ServerLevel)level;
            if (level.getDifficulty() != Difficulty.PEACEFUL && entity instanceof LivingEntity && !(livingEntity = (LivingEntity)entity).isInvulnerableTo(serverLevel, level.damageSources().wither())) {
                livingEntity.addEffect(this.getBeeInteractionEffect());
            }
        }
    }

    @Override
    public MobEffectInstance getBeeInteractionEffect() {
        return new MobEffectInstance(MobEffects.WITHER, 40);
    }
}


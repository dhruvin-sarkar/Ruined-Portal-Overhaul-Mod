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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class WeatheringCopperChestBlock
extends CopperChestBlock
implements WeatheringCopper {
    public static final MapCodec<WeatheringCopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(CopperChestBlock::getState), (App)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound").forGetter(ChestBlock::getOpenChestSound), (App)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound").forGetter(ChestBlock::getCloseChestSound), WeatheringCopperChestBlock.propertiesCodec()).apply((Applicative)instance, WeatheringCopperChestBlock::new));

    @Override
    public MapCodec<WeatheringCopperChestBlock> codec() {
        return CODEC;
    }

    public WeatheringCopperChestBlock(WeatheringCopper.WeatherState weatherState, SoundEvent soundEvent, SoundEvent soundEvent2, BlockBehaviour.Properties properties) {
        super(weatherState, soundEvent, soundEvent2, properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return WeatheringCopper.getNext(blockState.getBlock()).isPresent();
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        ChestBlockEntity chestBlockEntity;
        BlockEntity blockEntity;
        if (!blockState.getValue(ChestBlock.TYPE).equals(ChestType.RIGHT) && (blockEntity = serverLevel.getBlockEntity(blockPos)) instanceof ChestBlockEntity && (chestBlockEntity = (ChestBlockEntity)blockEntity).getEntitiesWithContainerOpen().isEmpty()) {
            this.changeOverTime(blockState, serverLevel, blockPos, randomSource);
        }
    }

    @Override
    public WeatheringCopper.WeatherState getAge() {
        return this.getState();
    }

    @Override
    public boolean isWaxed() {
        return false;
    }

    @Override
    public /* synthetic */ Enum getAge() {
        return this.getAge();
    }
}


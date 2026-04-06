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
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class CopperChestBlock
extends ChestBlock {
    public static final MapCodec<CopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(CopperChestBlock::getState), (App)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound").forGetter(ChestBlock::getOpenChestSound), (App)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound").forGetter(ChestBlock::getCloseChestSound), CopperChestBlock.propertiesCodec()).apply((Applicative)instance, CopperChestBlock::new));
    private static final Map<Block, Supplier<Block>> COPPER_TO_COPPER_CHEST_MAPPING = Map.of((Object)Blocks.COPPER_BLOCK, () -> Blocks.COPPER_CHEST, (Object)Blocks.EXPOSED_COPPER, () -> Blocks.EXPOSED_COPPER_CHEST, (Object)Blocks.WEATHERED_COPPER, () -> Blocks.WEATHERED_COPPER_CHEST, (Object)Blocks.OXIDIZED_COPPER, () -> Blocks.OXIDIZED_COPPER_CHEST, (Object)Blocks.WAXED_COPPER_BLOCK, () -> Blocks.COPPER_CHEST, (Object)Blocks.WAXED_EXPOSED_COPPER, () -> Blocks.EXPOSED_COPPER_CHEST, (Object)Blocks.WAXED_WEATHERED_COPPER, () -> Blocks.WEATHERED_COPPER_CHEST, (Object)Blocks.WAXED_OXIDIZED_COPPER, () -> Blocks.OXIDIZED_COPPER_CHEST);
    private final WeatheringCopper.WeatherState weatherState;

    @Override
    public MapCodec<? extends CopperChestBlock> codec() {
        return CODEC;
    }

    public CopperChestBlock(WeatheringCopper.WeatherState weatherState, SoundEvent soundEvent, SoundEvent soundEvent2, BlockBehaviour.Properties properties) {
        super(() -> BlockEntityType.CHEST, soundEvent, soundEvent2, properties);
        this.weatherState = weatherState;
    }

    @Override
    public boolean chestCanConnectTo(BlockState blockState) {
        return blockState.is(BlockTags.COPPER_CHESTS) && blockState.hasProperty(ChestBlock.TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = super.getStateForPlacement(blockPlaceContext);
        return CopperChestBlock.getLeastOxidizedChestOfConnectedBlocks(blockState, blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }

    private static BlockState getLeastOxidizedChestOfConnectedBlocks(BlockState blockState, Level level, BlockPos blockPos) {
        Block block;
        BlockState blockState2 = level.getBlockState(blockPos.relative(CopperChestBlock.getConnectedDirection(blockState)));
        if (!blockState.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE) && (block = blockState.getBlock()) instanceof CopperChestBlock) {
            CopperChestBlock copperChestBlock = (CopperChestBlock)block;
            block = blockState2.getBlock();
            if (block instanceof CopperChestBlock) {
                CopperChestBlock copperChestBlock2 = (CopperChestBlock)block;
                BlockState blockState3 = blockState;
                BlockState blockState4 = blockState2;
                if (copperChestBlock.isWaxed() != copperChestBlock2.isWaxed()) {
                    blockState3 = CopperChestBlock.unwaxBlock(copperChestBlock, blockState).orElse(blockState3);
                    blockState4 = CopperChestBlock.unwaxBlock(copperChestBlock2, blockState2).orElse(blockState4);
                }
                Block block2 = copperChestBlock.weatherState.ordinal() <= copperChestBlock2.weatherState.ordinal() ? blockState3.getBlock() : blockState4.getBlock();
                return block2.withPropertiesOf(blockState3);
            }
        }
        return blockState;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        ChestType chestType;
        BlockState blockState3 = super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
        if (this.chestCanConnectTo(blockState2) && !(chestType = blockState3.getValue(ChestBlock.TYPE)).equals(ChestType.SINGLE) && CopperChestBlock.getConnectedDirection(blockState3) == direction) {
            return blockState2.getBlock().withPropertiesOf(blockState3);
        }
        return blockState3;
    }

    private static Optional<BlockState> unwaxBlock(CopperChestBlock copperChestBlock, BlockState blockState) {
        if (!copperChestBlock.isWaxed()) {
            return Optional.of(blockState);
        }
        return Optional.ofNullable((Block)HoneycombItem.WAX_OFF_BY_BLOCK.get().get((Object)blockState.getBlock())).map(block -> block.withPropertiesOf(blockState));
    }

    public WeatheringCopper.WeatherState getState() {
        return this.weatherState;
    }

    public static BlockState getFromCopperBlock(Block block, Direction direction, Level level, BlockPos blockPos) {
        CopperChestBlock copperChestBlock = (CopperChestBlock)COPPER_TO_COPPER_CHEST_MAPPING.getOrDefault(block, Blocks.COPPER_CHEST::asBlock).get();
        ChestType chestType = copperChestBlock.getChestType(level, blockPos, direction);
        BlockState blockState = (BlockState)((BlockState)copperChestBlock.defaultBlockState().setValue(FACING, direction)).setValue(TYPE, chestType);
        return CopperChestBlock.getLeastOxidizedChestOfConnectedBlocks(blockState, level, blockPos);
    }

    public boolean isWaxed() {
        return true;
    }

    @Override
    public boolean shouldChangedStateKeepBlockEntity(BlockState blockState) {
        return blockState.is(BlockTags.COPPER_CHESTS);
    }
}


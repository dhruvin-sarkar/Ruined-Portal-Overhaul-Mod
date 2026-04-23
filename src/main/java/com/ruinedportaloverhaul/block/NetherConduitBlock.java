package com.ruinedportaloverhaul.block;

import com.mojang.serialization.MapCodec;
import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import com.ruinedportaloverhaul.block.entity.ModBlockEntities;
import com.ruinedportaloverhaul.block.entity.NetherConduitBlockEntity;
import com.ruinedportaloverhaul.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherConduitBlock extends BaseEntityBlock {
    public static final MapCodec<NetherConduitBlock> CODEC = BlockBehaviour.simpleCodec(NetherConduitBlock::new);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape SHAPE = box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

    public NetherConduitBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, Boolean.FALSE));
    }

    @Override
    public MapCodec<NetherConduitBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NetherConduitBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide()
            ? null
            : createTickerHelper(blockEntityType, ModBlockEntities.NETHER_CONDUIT, NetherConduitBlockEntity::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Fix: the conduit renderer previously had no synced active flag to read, so any future client animation would drift from the server's real frame state. The block now exposes that state through block properties.
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        if (!stack.is(Items.ANCIENT_DEBRIS)) {
            return InteractionResult.PASS;
        }

        // Fix: conduit upgrade feedback now uses translation keys so the action-bar text localizes cleanly instead of hardcoding English strings.
        if (!(level.getBlockEntity(pos) instanceof NetherConduitBlockEntity conduit)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        int cost = conduit.nextUpgradeCost();
        if (cost == 0) {
            player.displayClientMessage(Component.translatable("message.ruined_portal_overhaul.conduit.maxed"), true);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!player.isCreative() && stack.getCount() < cost) {
            player.displayClientMessage(Component.translatable("message.ruined_portal_overhaul.conduit.upgrade_cost", cost), true);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!player.isCreative()) {
            stack.shrink(cost);
        }
        conduit.upgrade();
        level.playSound(null, pos, ModSounds.BLOCK_NETHER_CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0f, 0.7f + conduit.conduitLevel() * 0.2f);
        player.displayClientMessage(Component.translatable("message.ruined_portal_overhaul.conduit.upgrade_success", conduit.conduitLevel()), true);
        if (conduit.conduitLevel() == 2 && player instanceof ServerPlayer serverPlayer) {
            ModAdvancementTriggers.trigger(ModAdvancementTriggers.NETHER_CONDUIT_LEVEL_2, serverPlayer);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}

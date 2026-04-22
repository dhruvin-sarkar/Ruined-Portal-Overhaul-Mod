package com.ruinedportaloverhaul.block;

import com.ruinedportaloverhaul.block.entity.NetherConduitBlockEntity;
import com.ruinedportaloverhaul.block.entity.NetherConduitPowerTracker;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class NetherConduitEvents {
    private static final int NETHER_SLEEP_RADIUS = 16;

    private NetherConduitEvents() {
    }

    public static void initialize() {
        // The conduit power tracker is runtime-only state; clear it on shutdown so integrated-server world swaps do not leak old boosts.
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> NetherConduitPowerTracker.clear());
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            BlockPos bedPos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(bedPos);
            if (!state.is(BlockTags.BEDS)) {
                return InteractionResult.PASS;
            }

            BlockPos headPos = bedHeadPos(state, bedPos);
            BlockState headState = level.getBlockState(headPos);
            if (!headState.is(BlockTags.BEDS) || !bedExplodes(level, headPos)) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)
                || !NetherConduitBlockEntity.hasActiveConduitNear(serverLevel, serverPlayer.blockPosition(), NETHER_SLEEP_RADIUS)) {
                return InteractionResult.PASS;
            }

            if (Boolean.TRUE.equals(headState.getValue(BedBlock.OCCUPIED))) {
                serverPlayer.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
                return InteractionResult.SUCCESS_SERVER;
            }

            serverPlayer.startSleeping(headPos);
            serverLevel.updateSleepingPlayerList();
            return InteractionResult.SUCCESS_SERVER;
        });
    }

    private static BlockPos bedHeadPos(BlockState state, BlockPos pos) {
        if (state.getValue(BedBlock.PART) == BedPart.HEAD) {
            return pos;
        }
        Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
        return pos.relative(direction);
    }

    private static boolean bedExplodes(Level level, BlockPos pos) {
        BedRule bedRule = level.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, pos);
        return bedRule.explodes();
    }
}

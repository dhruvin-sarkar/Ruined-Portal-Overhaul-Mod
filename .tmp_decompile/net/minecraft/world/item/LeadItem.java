/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class LeadItem
extends Item {
    public LeadItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (blockState.is(BlockTags.FENCES)) {
            Player player = useOnContext.getPlayer();
            if (!level.isClientSide() && player != null) {
                return LeadItem.bindPlayerMobs(player, level, blockPos);
            }
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult bindPlayerMobs(Player player, Level level, BlockPos blockPos) {
        LeashFenceKnotEntity leashFenceKnotEntity = null;
        List<Leashable> list = Leashable.leashableInArea(level, Vec3.atCenterOf(blockPos), leashable -> leashable.getLeashHolder() == player);
        boolean bl = false;
        for (Leashable leashable2 : list) {
            if (leashFenceKnotEntity == null) {
                leashFenceKnotEntity = LeashFenceKnotEntity.getOrCreateKnot(level, blockPos);
                leashFenceKnotEntity.playPlacementSound();
            }
            if (!leashable2.canHaveALeashAttachedTo(leashFenceKnotEntity)) continue;
            leashable2.setLeashedTo(leashFenceKnotEntity, true);
            bl = true;
        }
        if (bl) {
            level.gameEvent(GameEvent.BLOCK_ATTACH, blockPos, GameEvent.Context.of(player));
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }
}


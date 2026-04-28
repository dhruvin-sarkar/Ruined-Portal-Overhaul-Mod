package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.component.ModDataComponents;
import com.ruinedportaloverhaul.raid.PortalRaidState;
import com.ruinedportaloverhaul.sound.ModSounds;
import com.ruinedportaloverhaul.world.ModParticles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PortalShardItem extends Item {
    public static final int COOLDOWN_TICKS = 600;
    private static final int SEARCH_RADIUS_CHUNKS = 625;

    public PortalShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        long gameTime = serverLevel.getGameTime();
        int remaining = remainingCooldownTicks(stack, gameTime);
        if (remaining > 0) {
            player.displayClientMessage(
                Component.translatable("message.ruined_portal_overhaul.portal_shard.cooldown", formatDuration(remaining)).withStyle(ChatFormatting.GRAY),
                true
            );
            return InteractionResult.CONSUME;
        }

        BlockPos target = findNearestUncompletedPortal(serverLevel, player.blockPosition());
        stack.set(ModDataComponents.LAST_PORTAL_SHARD_USE_TICK, gameTime);
        if (target == null) {
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.PLAYERS, 0.7f, 0.6f);
            player.displayClientMessage(Component.translatable("message.ruined_portal_overhaul.portal_shard.silent").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }

        spawnGuideTrail(serverLevel, player.position(), Vec3.atCenterOf(target));
        serverLevel.playSound(null, player.blockPosition(), ModSounds.ITEM_PORTAL_SHARD_LOCATE, SoundSource.PLAYERS, 0.9f, 1.15f);
        player.displayClientMessage(
            Component.translatable(
                "message.ruined_portal_overhaul.portal_shard.located",
                compassLabel(player.position(), Vec3.atCenterOf(target)),
                Math.round(horizontalDistance(player.position(), Vec3.atCenterOf(target)))
            ).withStyle(ChatFormatting.LIGHT_PURPLE),
            true
        );
        return InteractionResult.CONSUME;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplay tooltipDisplay,
        Consumer<Component> tooltip,
        TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.portal_shard.tooltip.line1").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(Component.translatable("item.ruined_portal_overhaul.portal_shard.tooltip.line2").withStyle(ChatFormatting.GRAY));

        long clientGameTime = TooltipClientState.currentClientGameTime();
        if (clientGameTime != Long.MIN_VALUE) {
            int remaining = remainingCooldownTicks(stack, clientGameTime);
            if (remaining > 0) {
                tooltip.accept(Component.translatable("item.ruined_portal_overhaul.portal_shard.tooltip.cooldown", formatDuration(remaining)).withStyle(ChatFormatting.GOLD));
            } else {
                tooltip.accept(Component.translatable("item.ruined_portal_overhaul.portal_shard.tooltip.ready").withStyle(ChatFormatting.GOLD));
            }
        }
    }

    private static int remainingCooldownTicks(ItemStack stack, long gameTime) {
        long lastUse = stack.getOrDefault(ModDataComponents.LAST_PORTAL_SHARD_USE_TICK, Long.MIN_VALUE);
        if (lastUse == Long.MIN_VALUE) {
            return 0;
        }
        return (int) Math.max(0L, COOLDOWN_TICKS - Math.max(0L, gameTime - lastUse));
    }

    private static BlockPos findNearestUncompletedPortal(ServerLevel level, BlockPos playerPos) {
        PortalRaidState state = PortalRaidState.get(level.getServer());
        List<BlockPos> candidates = new ArrayList<>();
        addCandidate(level, candidates, playerPos);
        int[] radii = {512, 1536, 3072, 6144};
        for (int radius : radii) {
            addCandidate(level, candidates, playerPos.offset(radius, 0, 0));
            addCandidate(level, candidates, playerPos.offset(-radius, 0, 0));
            addCandidate(level, candidates, playerPos.offset(0, 0, radius));
            addCandidate(level, candidates, playerPos.offset(0, 0, -radius));
        }

        return candidates.stream()
            .distinct()
            .filter(candidate -> !state.isCompleted(candidate))
            .min(Comparator.comparingDouble(candidate -> candidate.distSqr(playerPos)))
            .orElse(null);
    }

    private static void addCandidate(ServerLevel level, List<BlockPos> candidates, BlockPos searchOrigin) {
        BlockPos found = level.findNearestMapStructure(StructureTags.RUINED_PORTAL, searchOrigin, SEARCH_RADIUS_CHUNKS, false);
        if (found != null) {
            candidates.add(found.immutable());
        }
    }

    private static void spawnGuideTrail(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 direction = to.subtract(from).multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() < 0.001) {
            level.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 1.0, from.z, 10, 0.25, 0.25, 0.25, 0.04);
            return;
        }

        Vec3 step = direction.normalize();
        for (int i = 1; i <= 10; i++) {
            Vec3 point = from.add(step.scale(i * 1.25)).add(0.0, 1.1, 0.0);
            level.sendParticles(ModParticles.CORRUPTION_RUNE, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    private static String compassLabel(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double degrees = (Math.toDegrees(Math.atan2(dx, -dz)) + 360.0) % 360.0;
        String[] labels = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        String label = labels[(int) Math.floor((degrees + 22.5) / 45.0) % labels.length];
        return "%s %.0f deg".formatted(label, degrees);
    }

    private static double horizontalDistance(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = Math.max(0, ticks) / 20;
        return "%d:%02d".formatted(totalSeconds / 60, totalSeconds % 60);
    }
}

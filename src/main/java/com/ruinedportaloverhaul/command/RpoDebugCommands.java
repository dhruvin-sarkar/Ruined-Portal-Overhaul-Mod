package com.ruinedportaloverhaul.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.ruinedportaloverhaul.raid.GoldRaidManager;
import com.ruinedportaloverhaul.raid.NetherDragonRituals;
import com.ruinedportaloverhaul.raid.PortalRaidState;
import com.ruinedportaloverhaul.structure.PortalStructureHelper;
import java.util.Comparator;
import java.util.Optional;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class RpoDebugCommands {
    private static final String WAVE_ARGUMENT = "wave";
    private static final int MAX_STATUS_DISTANCE = 100_000;

    private RpoDebugCommands() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            Commands.literal("rpo")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("locate").executes(RpoDebugCommands::locate))
                .then(Commands.literal("status").executes(RpoDebugCommands::status))
                .then(Commands.literal("reset").executes(RpoDebugCommands::reset))
                .then(Commands.literal("wave")
                    .then(Commands.argument(WAVE_ARGUMENT, IntegerArgumentType.integer(1, 5))
                        .executes(RpoDebugCommands::wave)))
                .then(Commands.literal("dragon").executes(RpoDebugCommands::dragon))
                .then(Commands.literal("complete").executes(RpoDebugCommands::complete))
        ));
    }

    private static int locate(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Optional<NearestPortal> nearest = nearestPortal(source);
        if (nearest.isEmpty()) {
            source.sendFailure(Component.translatable("commands.ruined_portal_overhaul.rpo.no_known_portals"));
            return 0;
        }

        NearestPortal portal = nearest.get();
        source.sendSuccess(
            () -> Component.translatable(
                "commands.ruined_portal_overhaul.rpo.locate",
                formatPos(portal.origin()),
                Math.round(portal.distance())
            ).withStyle(ChatFormatting.GOLD),
            false
        );
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Optional<NearestPortal> nearest = nearestPortal(source);
        if (nearest.isEmpty()) {
            source.sendFailure(Component.translatable("commands.ruined_portal_overhaul.rpo.no_known_portals"));
            return 0;
        }

        PortalRaidState state = PortalRaidState.get(source.getServer());
        BlockPos origin = nearest.get().origin();
        Component status = portalStatus(state, origin);
        int filledPedestals = state.filledRitualPedestals(origin).size();
        source.sendSuccess(
            () -> Component.translatable(
                "commands.ruined_portal_overhaul.rpo.status",
                formatPos(origin),
                status,
                Math.max(0, state.currentWaveNumber(origin)),
                filledPedestals,
                PortalStructureHelper.ritualPedestalPositions(origin).size(),
                yesNo(state.isDragonActive(origin))
            ),
            false
        );
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Optional<NearestPortal> nearest = nearestPortal(source);
        if (nearest.isEmpty()) {
            source.sendFailure(Component.translatable("commands.ruined_portal_overhaul.rpo.no_known_portals"));
            return 0;
        }

        ServerLevel level = source.getServer().overworld();
        BlockPos origin = nearest.get().origin();
        GoldRaidManager.AdminResetResult result = GoldRaidManager.adminResetPortal(level, origin);
        NetherDragonRituals.adminResetPortal(level, origin);
        source.sendSuccess(
            () -> Component.translatable(
                "commands.ruined_portal_overhaul.rpo.reset",
                formatPos(origin),
                result.restoredSpawners()
            ).withStyle(ChatFormatting.YELLOW),
            true
        );
        return 1;
    }

    private static int wave(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Optional<NearestPortal> nearest = nearestPortal(source);
        if (nearest.isEmpty()) {
            source.sendFailure(Component.translatable("commands.ruined_portal_overhaul.rpo.no_known_portals"));
            return 0;
        }

        int wave = IntegerArgumentType.getInteger(context, WAVE_ARGUMENT);
        int spawned = GoldRaidManager.adminSpawnWave(source.getServer().overworld(), nearest.get().origin(), wave);
        if (spawned < 0) {
            source.sendFailure(Component.translatable("commands.ruined_portal_overhaul.rpo.wave_completed"));
            return 0;
        }

        source.sendSuccess(
            () -> Component.translatable(
                "commands.ruined_portal_overhaul.rpo.wave",
                wave,
                formatPos(nearest.get().origin()),
                spawned
            ).withStyle(ChatFormatting.RED),
            true
        );
        return 1;
    }

    private static int dragon(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Optional<NearestPortal> nearest = nearestPortal(source);
        if (nearest.isEmpty()) {
            source.sendFailure(Component.translatable("commands.ruined_portal_overhaul.rpo.no_known_portals"));
            return 0;
        }

        BlockPos origin = nearest.get().origin();
        NetherDragonRituals.AdminSummonResult result = NetherDragonRituals.adminBeginSummoning(source.getServer().overworld(), origin);
        if (result != NetherDragonRituals.AdminSummonResult.STARTED) {
            source.sendFailure(Component.translatable("commands.ruined_portal_overhaul.rpo.dragon_failed." + result.name().toLowerCase()));
            return 0;
        }

        source.sendSuccess(
            () -> Component.translatable(
                "commands.ruined_portal_overhaul.rpo.dragon",
                formatPos(origin)
            ).withStyle(ChatFormatting.DARK_RED),
            true
        );
        return 1;
    }

    private static int complete(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Optional<NearestPortal> nearest = nearestPortal(source);
        if (nearest.isEmpty()) {
            source.sendFailure(Component.translatable("commands.ruined_portal_overhaul.rpo.no_known_portals"));
            return 0;
        }

        BlockPos origin = nearest.get().origin();
        if (!GoldRaidManager.adminCompletePortal(source.getServer().overworld(), origin)) {
            source.sendFailure(Component.translatable("commands.ruined_portal_overhaul.rpo.complete_already"));
            return 0;
        }

        source.sendSuccess(
            () -> Component.translatable(
                "commands.ruined_portal_overhaul.rpo.complete",
                formatPos(origin)
            ).withStyle(ChatFormatting.GOLD),
            true
        );
        return 1;
    }

    private static Optional<NearestPortal> nearestPortal(CommandSourceStack source) {
        PortalRaidState state = PortalRaidState.get(source.getServer());
        BlockPos sourcePos = BlockPos.containing(source.getPosition());
        return state.knownPortalOrigins().stream()
            .map(origin -> new NearestPortal(origin, horizontalDistance(sourcePos, origin)))
            .filter(portal -> portal.distance() <= MAX_STATUS_DISTANCE)
            .min(Comparator.comparingDouble(NearestPortal::distance));
    }

    private static Component portalStatus(PortalRaidState state, BlockPos origin) {
        if (state.isCompleted(origin)) {
            return Component.translatable("commands.ruined_portal_overhaul.rpo.status.completed");
        }
        if (state.isRaidActive(origin)) {
            return Component.translatable("commands.ruined_portal_overhaul.rpo.status.raid");
        }
        if (state.isApproachActivated(origin)) {
            return Component.translatable("commands.ruined_portal_overhaul.rpo.status.awake");
        }
        return Component.translatable("commands.ruined_portal_overhaul.rpo.status.incomplete");
    }

    private static Component yesNo(boolean value) {
        return Component.translatable(value
            ? "commands.ruined_portal_overhaul.rpo.yes"
            : "commands.ruined_portal_overhaul.rpo.no");
    }

    private static String formatPos(BlockPos pos) {
        return "%s %s %s".formatted(pos.getX(), pos.getY(), pos.getZ());
    }

    private static double horizontalDistance(BlockPos first, BlockPos second) {
        double dx = first.getX() - second.getX();
        double dz = first.getZ() - second.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private record NearestPortal(BlockPos origin, double distance) {
    }
}

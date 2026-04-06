/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.InCommandFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.ticks.LevelTicks;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class CloneCommands {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType((Message)Component.translatable("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.clone.toobig", object, object2));
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.clone.failed"));
    public static final Predicate<BlockInWorld> FILTER_AIR = blockInWorld -> !blockInWorld.getState().isAir();

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clone").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(CloneCommands.beginEndDestinationAndModeSuffix(commandBuildContext, commandContext -> ((CommandSourceStack)commandContext.getSource()).getLevel()))).then(Commands.literal("from").then(Commands.argument("sourceDimension", DimensionArgument.dimension()).then(CloneCommands.beginEndDestinationAndModeSuffix(commandBuildContext, commandContext -> DimensionArgument.getDimension((CommandContext<CommandSourceStack>)commandContext, "sourceDimension"))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> beginEndDestinationAndModeSuffix(CommandBuildContext commandBuildContext, InCommandFunction<CommandContext<CommandSourceStack>, ServerLevel> inCommandFunction) {
        return Commands.argument("begin", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("end", BlockPosArgument.blockPos()).then(CloneCommands.destinationAndStrictSuffix(commandBuildContext, inCommandFunction, commandContext -> ((CommandSourceStack)commandContext.getSource()).getLevel()))).then(Commands.literal("to").then(Commands.argument("targetDimension", DimensionArgument.dimension()).then(CloneCommands.destinationAndStrictSuffix(commandBuildContext, inCommandFunction, commandContext -> DimensionArgument.getDimension((CommandContext<CommandSourceStack>)commandContext, "targetDimension"))))));
    }

    private static DimensionAndPosition getLoadedDimensionAndPosition(CommandContext<CommandSourceStack> commandContext, ServerLevel serverLevel, String string) throws CommandSyntaxException {
        BlockPos blockPos = BlockPosArgument.getLoadedBlockPos(commandContext, serverLevel, string);
        return new DimensionAndPosition(serverLevel, blockPos);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> destinationAndStrictSuffix(CommandBuildContext commandBuildContext, InCommandFunction<CommandContext<CommandSourceStack>, ServerLevel> inCommandFunction, InCommandFunction<CommandContext<CommandSourceStack>, ServerLevel> inCommandFunction2) {
        InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> inCommandFunction3 = commandContext -> CloneCommands.getLoadedDimensionAndPosition((CommandContext<CommandSourceStack>)commandContext, (ServerLevel)inCommandFunction.apply((CommandContext<CommandSourceStack>)commandContext), "begin");
        InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> inCommandFunction4 = commandContext -> CloneCommands.getLoadedDimensionAndPosition((CommandContext<CommandSourceStack>)commandContext, (ServerLevel)inCommandFunction.apply((CommandContext<CommandSourceStack>)commandContext), "end");
        InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> inCommandFunction5 = commandContext -> CloneCommands.getLoadedDimensionAndPosition((CommandContext<CommandSourceStack>)commandContext, (ServerLevel)inCommandFunction2.apply((CommandContext<CommandSourceStack>)commandContext), "destination");
        return CloneCommands.modeSuffix(commandBuildContext, inCommandFunction3, inCommandFunction4, inCommandFunction5, false, Commands.argument("destination", BlockPosArgument.blockPos())).then(CloneCommands.modeSuffix(commandBuildContext, inCommandFunction3, inCommandFunction4, inCommandFunction5, true, Commands.literal("strict")));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> modeSuffix(CommandBuildContext commandBuildContext, InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> inCommandFunction, InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> inCommandFunction2, InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> inCommandFunction3, boolean bl, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder) {
        return argumentBuilder.executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)((Object)((Object)inCommandFunction.apply(commandContext))), (DimensionAndPosition)((Object)((Object)inCommandFunction2.apply(commandContext))), (DimensionAndPosition)((Object)((Object)inCommandFunction3.apply(commandContext))), blockInWorld -> true, Mode.NORMAL, bl)).then(CloneCommands.wrapWithCloneMode(inCommandFunction, inCommandFunction2, inCommandFunction3, commandContext -> blockInWorld -> true, bl, Commands.literal("replace"))).then(CloneCommands.wrapWithCloneMode(inCommandFunction, inCommandFunction2, inCommandFunction3, commandContext -> FILTER_AIR, bl, Commands.literal("masked"))).then(Commands.literal("filtered").then(CloneCommands.wrapWithCloneMode(inCommandFunction, inCommandFunction2, inCommandFunction3, commandContext -> BlockPredicateArgument.getBlockPredicate((CommandContext<CommandSourceStack>)commandContext, "filter"), bl, Commands.argument("filter", BlockPredicateArgument.blockPredicate(commandBuildContext)))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapWithCloneMode(InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> inCommandFunction, InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> inCommandFunction2, InCommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> inCommandFunction3, InCommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> inCommandFunction4, boolean bl, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder) {
        return argumentBuilder.executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)((Object)((Object)inCommandFunction.apply(commandContext))), (DimensionAndPosition)((Object)((Object)inCommandFunction2.apply(commandContext))), (DimensionAndPosition)((Object)((Object)inCommandFunction3.apply(commandContext))), (Predicate)inCommandFunction4.apply(commandContext), Mode.NORMAL, bl)).then(Commands.literal("force").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)((Object)((Object)inCommandFunction.apply(commandContext))), (DimensionAndPosition)((Object)((Object)inCommandFunction2.apply(commandContext))), (DimensionAndPosition)((Object)((Object)inCommandFunction3.apply(commandContext))), (Predicate)inCommandFunction4.apply(commandContext), Mode.FORCE, bl))).then(Commands.literal("move").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)((Object)((Object)inCommandFunction.apply(commandContext))), (DimensionAndPosition)((Object)((Object)inCommandFunction2.apply(commandContext))), (DimensionAndPosition)((Object)((Object)inCommandFunction3.apply(commandContext))), (Predicate)inCommandFunction4.apply(commandContext), Mode.MOVE, bl))).then(Commands.literal("normal").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)((Object)((Object)inCommandFunction.apply(commandContext))), (DimensionAndPosition)((Object)((Object)inCommandFunction2.apply(commandContext))), (DimensionAndPosition)((Object)((Object)inCommandFunction3.apply(commandContext))), (Predicate)inCommandFunction4.apply(commandContext), Mode.NORMAL, bl)));
    }

    private static int clone(CommandSourceStack commandSourceStack, DimensionAndPosition dimensionAndPosition, DimensionAndPosition dimensionAndPosition2, DimensionAndPosition dimensionAndPosition3, Predicate<BlockInWorld> predicate, Mode mode, boolean bl) throws CommandSyntaxException {
        int j;
        BlockPos blockPos = dimensionAndPosition.position();
        BlockPos blockPos2 = dimensionAndPosition2.position();
        BoundingBox boundingBox = BoundingBox.fromCorners(blockPos, blockPos2);
        BlockPos blockPos3 = dimensionAndPosition3.position();
        BlockPos blockPos4 = blockPos3.offset(boundingBox.getLength());
        BoundingBox boundingBox2 = BoundingBox.fromCorners(blockPos3, blockPos4);
        ServerLevel serverLevel = dimensionAndPosition.dimension();
        ServerLevel serverLevel2 = dimensionAndPosition3.dimension();
        if (!mode.canOverlap() && serverLevel == serverLevel2 && boundingBox2.intersects(boundingBox)) {
            throw ERROR_OVERLAP.create();
        }
        int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (i > (j = commandSourceStack.getLevel().getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS).intValue())) {
            throw ERROR_AREA_TOO_LARGE.create((Object)j, (Object)i);
        }
        if (!serverLevel.hasChunksAt(blockPos, blockPos2) || !serverLevel2.hasChunksAt(blockPos3, blockPos4)) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
        if (serverLevel2.isDebug()) {
            throw ERROR_FAILED.create();
        }
        ArrayList list = Lists.newArrayList();
        ArrayList list2 = Lists.newArrayList();
        ArrayList list3 = Lists.newArrayList();
        LinkedList deque = Lists.newLinkedList();
        int k = 0;
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(LOGGER);){
            int m;
            int l;
            BlockPos blockPos5 = new BlockPos(boundingBox2.minX() - boundingBox.minX(), boundingBox2.minY() - boundingBox.minY(), boundingBox2.minZ() - boundingBox.minZ());
            for (l = boundingBox.minZ(); l <= boundingBox.maxZ(); ++l) {
                for (m = boundingBox.minY(); m <= boundingBox.maxY(); ++m) {
                    for (int n = boundingBox.minX(); n <= boundingBox.maxX(); ++n) {
                        BlockPos blockPos6 = new BlockPos(n, m, l);
                        BlockPos blockPos7 = blockPos6.offset(blockPos5);
                        BlockInWorld blockInWorld = new BlockInWorld(serverLevel, blockPos6, false);
                        BlockState blockState = blockInWorld.getState();
                        if (!predicate.test(blockInWorld)) continue;
                        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos6);
                        if (blockEntity != null) {
                            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector.forChild(blockEntity.problemPath()), commandSourceStack.registryAccess());
                            blockEntity.saveCustomOnly(tagValueOutput);
                            CloneBlockEntityInfo cloneBlockEntityInfo = new CloneBlockEntityInfo(tagValueOutput.buildResult(), blockEntity.components());
                            list2.add(new CloneBlockInfo(blockPos7, blockState, cloneBlockEntityInfo, serverLevel2.getBlockState(blockPos7)));
                            deque.addLast(blockPos6);
                            continue;
                        }
                        if (blockState.isSolidRender() || blockState.isCollisionShapeFullBlock(serverLevel, blockPos6)) {
                            list.add(new CloneBlockInfo(blockPos7, blockState, null, serverLevel2.getBlockState(blockPos7)));
                            deque.addLast(blockPos6);
                            continue;
                        }
                        list3.add(new CloneBlockInfo(blockPos7, blockState, null, serverLevel2.getBlockState(blockPos7)));
                        deque.addFirst(blockPos6);
                    }
                }
            }
            l = 2 | (bl ? 816 : 0);
            if (mode == Mode.MOVE) {
                for (BlockPos blockPos8 : deque) {
                    serverLevel.setBlock(blockPos8, Blocks.BARRIER.defaultBlockState(), l | 0x330);
                }
                m = bl ? l : 3;
                for (BlockPos blockPos6 : deque) {
                    serverLevel.setBlock(blockPos6, Blocks.AIR.defaultBlockState(), m);
                }
            }
            ArrayList list4 = Lists.newArrayList();
            list4.addAll(list);
            list4.addAll(list2);
            list4.addAll(list3);
            List list5 = Lists.reverse((List)list4);
            for (CloneBlockInfo cloneBlockInfo : list5) {
                serverLevel2.setBlock(cloneBlockInfo.pos, Blocks.BARRIER.defaultBlockState(), l | 0x330);
            }
            for (CloneBlockInfo cloneBlockInfo : list4) {
                if (!serverLevel2.setBlock(cloneBlockInfo.pos, cloneBlockInfo.state, l)) continue;
                ++k;
            }
            for (CloneBlockInfo cloneBlockInfo : list2) {
                BlockEntity blockEntity2 = serverLevel2.getBlockEntity(cloneBlockInfo.pos);
                if (cloneBlockInfo.blockEntityInfo != null && blockEntity2 != null) {
                    blockEntity2.loadCustomOnly(TagValueInput.create(scopedCollector.forChild(blockEntity2.problemPath()), (HolderLookup.Provider)serverLevel2.registryAccess(), cloneBlockInfo.blockEntityInfo.tag));
                    blockEntity2.setComponents(cloneBlockInfo.blockEntityInfo.components);
                    blockEntity2.setChanged();
                }
                serverLevel2.setBlock(cloneBlockInfo.pos, cloneBlockInfo.state, l);
            }
            if (!bl) {
                for (CloneBlockInfo cloneBlockInfo : list5) {
                    serverLevel2.updateNeighboursOnBlockSet(cloneBlockInfo.pos, cloneBlockInfo.previousStateAtDestination);
                }
            }
            ((LevelTicks)serverLevel2.getBlockTicks()).copyAreaFrom(serverLevel.getBlockTicks(), boundingBox, blockPos5);
        }
        if (k == 0) {
            throw ERROR_FAILED.create();
        }
        int o = k;
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.clone.success", o), true);
        return k;
    }

    record DimensionAndPosition(ServerLevel dimension, BlockPos position) {
    }

    static enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean canOverlap;

        private Mode(boolean bl) {
            this.canOverlap = bl;
        }

        public boolean canOverlap() {
            return this.canOverlap;
        }
    }

    static final class CloneBlockEntityInfo
    extends Record {
        final CompoundTag tag;
        final DataComponentMap components;

        CloneBlockEntityInfo(CompoundTag compoundTag, DataComponentMap dataComponentMap) {
            this.tag = compoundTag;
            this.components = dataComponentMap;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CloneBlockEntityInfo.class, "tag;components", "tag", "components"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CloneBlockEntityInfo.class, "tag;components", "tag", "components"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CloneBlockEntityInfo.class, "tag;components", "tag", "components"}, this, object);
        }

        public CompoundTag tag() {
            return this.tag;
        }

        public DataComponentMap components() {
            return this.components;
        }
    }

    static final class CloneBlockInfo
    extends Record {
        final BlockPos pos;
        final BlockState state;
        final @Nullable CloneBlockEntityInfo blockEntityInfo;
        final BlockState previousStateAtDestination;

        CloneBlockInfo(BlockPos blockPos, BlockState blockState, @Nullable CloneBlockEntityInfo cloneBlockEntityInfo, BlockState blockState2) {
            this.pos = blockPos;
            this.state = blockState;
            this.blockEntityInfo = cloneBlockEntityInfo;
            this.previousStateAtDestination = blockState2;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CloneBlockInfo.class, "pos;state;blockEntityInfo;previousStateAtDestination", "pos", "state", "blockEntityInfo", "previousStateAtDestination"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CloneBlockInfo.class, "pos;state;blockEntityInfo;previousStateAtDestination", "pos", "state", "blockEntityInfo", "previousStateAtDestination"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CloneBlockInfo.class, "pos;state;blockEntityInfo;previousStateAtDestination", "pos", "state", "blockEntityInfo", "previousStateAtDestination"}, this, object);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public BlockState state() {
            return this.state;
        }

        public @Nullable CloneBlockEntityInfo blockEntityInfo() {
            return this.blockEntityInfo;
        }

        public BlockState previousStateAtDestination() {
            return this.previousStateAtDestination;
        }
    }
}


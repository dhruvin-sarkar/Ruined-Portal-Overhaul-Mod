/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.InCommandFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

public class FillCommand {
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.fill.toobig", object, object2));
    static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), null);
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.fill.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fill").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).then(FillCommand.wrapWithMode(commandBuildContext, Commands.argument("block", BlockStateArgument.block(commandBuildContext)), commandContext -> BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), commandContext -> BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to"), commandContext -> BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), commandContext -> null).then(((LiteralArgumentBuilder)Commands.literal("replace").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to")), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), Mode.REPLACE, null, false))).then(FillCommand.wrapWithMode(commandBuildContext, Commands.argument("filter", BlockPredicateArgument.blockPredicate(commandBuildContext)), commandContext -> BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), commandContext -> BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to"), commandContext -> BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), commandContext -> BlockPredicateArgument.getBlockPredicate((CommandContext<CommandSourceStack>)commandContext, "filter")))).then(Commands.literal("keep").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to")), BlockStateArgument.getBlock((CommandContext<CommandSourceStack>)commandContext, "block"), Mode.REPLACE, blockInWorld -> blockInWorld.getLevel().isEmptyBlock(blockInWorld.getPos()), false)))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapWithMode(CommandBuildContext commandBuildContext, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, InCommandFunction<CommandContext<CommandSourceStack>, BlockPos> inCommandFunction, InCommandFunction<CommandContext<CommandSourceStack>, BlockPos> inCommandFunction2, InCommandFunction<CommandContext<CommandSourceStack>, BlockInput> inCommandFunction3, NullableCommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> nullableCommandFunction) {
        return argumentBuilder.executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners((Vec3i)inCommandFunction.apply(commandContext), (Vec3i)inCommandFunction2.apply(commandContext)), (BlockInput)inCommandFunction3.apply(commandContext), Mode.REPLACE, (Predicate)nullableCommandFunction.apply(commandContext), false)).then(Commands.literal("outline").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners((Vec3i)inCommandFunction.apply(commandContext), (Vec3i)inCommandFunction2.apply(commandContext)), (BlockInput)inCommandFunction3.apply(commandContext), Mode.OUTLINE, (Predicate)nullableCommandFunction.apply(commandContext), false))).then(Commands.literal("hollow").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners((Vec3i)inCommandFunction.apply(commandContext), (Vec3i)inCommandFunction2.apply(commandContext)), (BlockInput)inCommandFunction3.apply(commandContext), Mode.HOLLOW, (Predicate)nullableCommandFunction.apply(commandContext), false))).then(Commands.literal("destroy").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners((Vec3i)inCommandFunction.apply(commandContext), (Vec3i)inCommandFunction2.apply(commandContext)), (BlockInput)inCommandFunction3.apply(commandContext), Mode.DESTROY, (Predicate)nullableCommandFunction.apply(commandContext), false))).then(Commands.literal("strict").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners((Vec3i)inCommandFunction.apply(commandContext), (Vec3i)inCommandFunction2.apply(commandContext)), (BlockInput)inCommandFunction3.apply(commandContext), Mode.REPLACE, (Predicate)nullableCommandFunction.apply(commandContext), true)));
    }

    private static int fillBlocks(CommandSourceStack commandSourceStack, BoundingBox boundingBox, BlockInput blockInput, Mode mode, @Nullable Predicate<BlockInWorld> predicate, boolean bl) throws CommandSyntaxException {
        final class UpdatedPosition
        extends Record {
            final BlockPos pos;
            final BlockState oldState;

            UpdatedPosition(BlockPos blockPos, BlockState blockState) {
                this.pos = blockPos;
                this.oldState = blockState;
            }

            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{UpdatedPosition.class, "pos;oldState", "pos", "oldState"}, this);
            }

            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{UpdatedPosition.class, "pos;oldState", "pos", "oldState"}, this);
            }

            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{UpdatedPosition.class, "pos;oldState", "pos", "oldState"}, this, object);
            }

            public BlockPos pos() {
                return this.pos;
            }

            public BlockState oldState() {
                return this.oldState;
            }
        }
        int j;
        int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (i > (j = commandSourceStack.getLevel().getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS).intValue())) {
            throw ERROR_AREA_TOO_LARGE.create((Object)j, (Object)i);
        }
        ArrayList list = Lists.newArrayList();
        ServerLevel serverLevel = commandSourceStack.getLevel();
        if (serverLevel.isDebug()) {
            throw ERROR_FAILED.create();
        }
        int k = 0;
        for (BlockPos blockPos : BlockPos.betweenClosed(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ())) {
            BlockInput blockInput2;
            if (predicate != null && !predicate.test(new BlockInWorld(serverLevel, blockPos, true))) continue;
            BlockState blockState = serverLevel.getBlockState(blockPos);
            boolean bl2 = false;
            if (mode.affector.affect(serverLevel, blockPos)) {
                bl2 = true;
            }
            if ((blockInput2 = mode.filter.filter(boundingBox, blockPos, blockInput, serverLevel)) == null) {
                if (!bl2) continue;
                ++k;
                continue;
            }
            if (!blockInput2.place(serverLevel, blockPos, 2 | (bl ? 816 : 256))) {
                if (!bl2) continue;
                ++k;
                continue;
            }
            if (!bl) {
                list.add(new UpdatedPosition(blockPos.immutable(), blockState));
            }
            ++k;
        }
        for (UpdatedPosition lv : list) {
            serverLevel.updateNeighboursOnBlockSet(lv.pos, lv.oldState);
        }
        if (k == 0) {
            throw ERROR_FAILED.create();
        }
        int l = k;
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.fill.success", l), true);
        return k;
    }

    @FunctionalInterface
    static interface NullableCommandFunction<T, R> {
        public @Nullable R apply(T var1) throws CommandSyntaxException;
    }

    static enum Mode {
        REPLACE(Affector.NOOP, Filter.NOOP),
        OUTLINE(Affector.NOOP, (boundingBox, blockPos, blockInput, serverLevel) -> {
            if (blockPos.getX() == boundingBox.minX() || blockPos.getX() == boundingBox.maxX() || blockPos.getY() == boundingBox.minY() || blockPos.getY() == boundingBox.maxY() || blockPos.getZ() == boundingBox.minZ() || blockPos.getZ() == boundingBox.maxZ()) {
                return blockInput;
            }
            return null;
        }),
        HOLLOW(Affector.NOOP, (boundingBox, blockPos, blockInput, serverLevel) -> {
            if (blockPos.getX() == boundingBox.minX() || blockPos.getX() == boundingBox.maxX() || blockPos.getY() == boundingBox.minY() || blockPos.getY() == boundingBox.maxY() || blockPos.getZ() == boundingBox.minZ() || blockPos.getZ() == boundingBox.maxZ()) {
                return blockInput;
            }
            return HOLLOW_CORE;
        }),
        DESTROY((serverLevel, blockPos) -> serverLevel.destroyBlock(blockPos, true), Filter.NOOP);

        public final Filter filter;
        public final Affector affector;

        private Mode(Affector affector, Filter filter) {
            this.affector = affector;
            this.filter = filter;
        }
    }

    @FunctionalInterface
    public static interface Affector {
        public static final Affector NOOP = (serverLevel, blockPos) -> false;

        public boolean affect(ServerLevel var1, BlockPos var2);
    }

    @FunctionalInterface
    public static interface Filter {
        public static final Filter NOOP = (boundingBox, blockPos, blockInput, serverLevel) -> blockInput;

        public @Nullable BlockInput filter(BoundingBox var1, BlockPos var2, BlockInput var3, ServerLevel var4);
    }
}


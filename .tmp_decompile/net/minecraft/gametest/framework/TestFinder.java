/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.gametest.framework;

import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FailedTestTracker;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestInstanceFinder;
import net.minecraft.gametest.framework.TestPosFinder;

public class TestFinder
implements TestInstanceFinder,
TestPosFinder {
    static final TestInstanceFinder NO_FUNCTIONS = Stream::empty;
    static final TestPosFinder NO_STRUCTURES = Stream::empty;
    private final TestInstanceFinder testInstanceFinder;
    private final TestPosFinder testPosFinder;
    private final CommandSourceStack source;

    @Override
    public Stream<BlockPos> findTestPos() {
        return this.testPosFinder.findTestPos();
    }

    public static Builder builder() {
        return new Builder();
    }

    TestFinder(CommandSourceStack commandSourceStack, TestInstanceFinder testInstanceFinder, TestPosFinder testPosFinder) {
        this.source = commandSourceStack;
        this.testInstanceFinder = testInstanceFinder;
        this.testPosFinder = testPosFinder;
    }

    public CommandSourceStack source() {
        return this.source;
    }

    @Override
    public Stream<Holder.Reference<GameTestInstance>> findTests() {
        return this.testInstanceFinder.findTests();
    }

    public static class Builder {
        private final UnaryOperator<Supplier<Stream<Holder.Reference<GameTestInstance>>>> testFinderWrapper;
        private final UnaryOperator<Supplier<Stream<BlockPos>>> structureBlockPosFinderWrapper;

        public Builder() {
            this.testFinderWrapper = supplier -> supplier;
            this.structureBlockPosFinderWrapper = supplier -> supplier;
        }

        private Builder(UnaryOperator<Supplier<Stream<Holder.Reference<GameTestInstance>>>> unaryOperator, UnaryOperator<Supplier<Stream<BlockPos>>> unaryOperator2) {
            this.testFinderWrapper = unaryOperator;
            this.structureBlockPosFinderWrapper = unaryOperator2;
        }

        public Builder createMultipleCopies(int i) {
            return new Builder(Builder.createCopies(i), Builder.createCopies(i));
        }

        private static <Q> UnaryOperator<Supplier<Stream<Q>>> createCopies(int i) {
            return supplier -> {
                LinkedList list = new LinkedList();
                List list2 = ((Stream)supplier.get()).toList();
                for (int j = 0; j < i; ++j) {
                    list.addAll(list2);
                }
                return list::stream;
            };
        }

        private TestFinder build(CommandSourceStack commandSourceStack, TestInstanceFinder testInstanceFinder, TestPosFinder testPosFinder) {
            return new TestFinder(commandSourceStack, ((Supplier)((Supplier)this.testFinderWrapper.apply(testInstanceFinder::findTests)))::get, ((Supplier)((Supplier)this.structureBlockPosFinderWrapper.apply(testPosFinder::findTestPos)))::get);
        }

        public TestFinder radius(CommandContext<CommandSourceStack> commandContext, int i) {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
            return this.build(commandSourceStack, NO_FUNCTIONS, () -> StructureUtils.findTestBlocks(blockPos, i, commandSourceStack.getLevel()));
        }

        public TestFinder nearest(CommandContext<CommandSourceStack> commandContext) {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
            return this.build(commandSourceStack, NO_FUNCTIONS, () -> StructureUtils.findNearestTest(blockPos, 15, commandSourceStack.getLevel()).stream());
        }

        public TestFinder allNearby(CommandContext<CommandSourceStack> commandContext) {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
            return this.build(commandSourceStack, NO_FUNCTIONS, () -> StructureUtils.findTestBlocks(blockPos, 250, commandSourceStack.getLevel()));
        }

        public TestFinder lookedAt(CommandContext<CommandSourceStack> commandContext) {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            return this.build(commandSourceStack, NO_FUNCTIONS, () -> StructureUtils.lookedAtTestPos(BlockPos.containing(commandSourceStack.getPosition()), commandSourceStack.getPlayer().getCamera(), commandSourceStack.getLevel()));
        }

        public TestFinder failedTests(CommandContext<CommandSourceStack> commandContext, boolean bl) {
            return this.build((CommandSourceStack)commandContext.getSource(), () -> FailedTestTracker.getLastFailedTests().filter(reference -> !bl || ((GameTestInstance)reference.value()).required()), NO_STRUCTURES);
        }

        public TestFinder byResourceSelection(CommandContext<CommandSourceStack> commandContext, Collection<Holder.Reference<GameTestInstance>> collection) {
            return this.build((CommandSourceStack)commandContext.getSource(), collection::stream, NO_STRUCTURES);
        }

        public TestFinder failedTests(CommandContext<CommandSourceStack> commandContext) {
            return this.failedTests(commandContext, false);
        }
    }
}


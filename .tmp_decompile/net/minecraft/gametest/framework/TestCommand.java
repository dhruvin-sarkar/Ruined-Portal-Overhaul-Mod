/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceSelectorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FailedTestTracker;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchFactory;
import net.minecraft.gametest.framework.GameTestBatchListener;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.StructureGridSpawner;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFinder;
import net.minecraft.gametest.framework.TestInstanceFinder;
import net.minecraft.gametest.framework.TestPosFinder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundGameTestHighlightPosPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.InCommandFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.mutable.MutableInt;

public class TestCommand {
    public static final int TEST_NEARBY_SEARCH_RADIUS = 15;
    public static final int TEST_FULL_SEARCH_RADIUS = 250;
    public static final int VERIFY_TEST_GRID_AXIS_SIZE = 10;
    public static final int VERIFY_TEST_BATCH_SIZE = 100;
    private static final int DEFAULT_CLEAR_RADIUS = 250;
    private static final int MAX_CLEAR_RADIUS = 1024;
    private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
    private static final int DEFAULT_X_SIZE = 5;
    private static final int DEFAULT_Y_SIZE = 5;
    private static final int DEFAULT_Z_SIZE = 5;
    private static final SimpleCommandExceptionType CLEAR_NO_TESTS = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.clear.error.no_tests"));
    private static final SimpleCommandExceptionType RESET_NO_TESTS = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.reset.error.no_tests"));
    private static final SimpleCommandExceptionType TEST_INSTANCE_COULD_NOT_BE_FOUND = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.error.test_instance_not_found"));
    private static final SimpleCommandExceptionType NO_STRUCTURES_TO_EXPORT = new SimpleCommandExceptionType((Message)Component.literal("Could not find any structures to export"));
    private static final SimpleCommandExceptionType NO_TEST_INSTANCES = new SimpleCommandExceptionType((Message)Component.translatable("commands.test.error.no_test_instances"));
    private static final Dynamic3CommandExceptionType NO_TEST_CONTAINING = new Dynamic3CommandExceptionType((object, object2, object3) -> Component.translatableEscape("commands.test.error.no_test_containing_pos", object, object2, object3));
    private static final DynamicCommandExceptionType TOO_LARGE = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.test.error.too_large", object));

    private static int reset(TestFinder testFinder) throws CommandSyntaxException {
        TestCommand.stopTests();
        int i = TestCommand.toGameTestInfos(testFinder.source(), RetryOptions.noRetries(), testFinder).map(gameTestInfo -> TestCommand.resetGameTestInfo(testFinder.source(), gameTestInfo)).toList().size();
        if (i == 0) {
            throw CLEAR_NO_TESTS.create();
        }
        testFinder.source().sendSuccess(() -> Component.translatable("commands.test.reset.success", i), true);
        return i;
    }

    private static int clear(TestFinder testFinder) throws CommandSyntaxException {
        TestCommand.stopTests();
        CommandSourceStack commandSourceStack = testFinder.source();
        ServerLevel serverLevel = commandSourceStack.getLevel();
        List list = testFinder.findTestPos().flatMap(blockPos -> serverLevel.getBlockEntity((BlockPos)blockPos, BlockEntityType.TEST_INSTANCE_BLOCK).stream()).toList();
        for (TestInstanceBlockEntity testInstanceBlockEntity : list) {
            StructureUtils.clearSpaceForStructure(testInstanceBlockEntity.getStructureBoundingBox(), serverLevel);
            testInstanceBlockEntity.removeBarriers();
            serverLevel.destroyBlock(testInstanceBlockEntity.getBlockPos(), false);
        }
        if (list.isEmpty()) {
            throw CLEAR_NO_TESTS.create();
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.test.clear.success", list.size()), true);
        return list.size();
    }

    private static int export(TestFinder testFinder) throws CommandSyntaxException {
        CommandSourceStack commandSourceStack = testFinder.source();
        ServerLevel serverLevel = commandSourceStack.getLevel();
        int i = 0;
        boolean bl = true;
        Iterator iterator = testFinder.findTestPos().iterator();
        while (iterator.hasNext()) {
            BlockPos blockPos = (BlockPos)iterator.next();
            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
            if (blockEntity instanceof TestInstanceBlockEntity) {
                TestInstanceBlockEntity testInstanceBlockEntity = (TestInstanceBlockEntity)blockEntity;
                if (!testInstanceBlockEntity.exportTest(commandSourceStack::sendSystemMessage)) {
                    bl = false;
                }
                ++i;
                continue;
            }
            throw TEST_INSTANCE_COULD_NOT_BE_FOUND.create();
        }
        if (i == 0) {
            throw NO_STRUCTURES_TO_EXPORT.create();
        }
        String string = "Exported " + i + " structures";
        testFinder.source().sendSuccess(() -> Component.literal(string), true);
        return bl ? 0 : 1;
    }

    private static int verify(TestFinder testFinder) {
        TestCommand.stopTests();
        CommandSourceStack commandSourceStack = testFinder.source();
        ServerLevel serverLevel = commandSourceStack.getLevel();
        BlockPos blockPos = TestCommand.createTestPositionAround(commandSourceStack);
        List collection = Stream.concat(TestCommand.toGameTestInfos(commandSourceStack, RetryOptions.noRetries(), testFinder), TestCommand.toGameTestInfo(commandSourceStack, RetryOptions.noRetries(), testFinder, 0)).toList();
        FailedTestTracker.forgetFailedTests();
        ArrayList<GameTestBatch> collection2 = new ArrayList<GameTestBatch>();
        for (GameTestInfo gameTestInfo : collection) {
            for (Rotation rotation : Rotation.values()) {
                ArrayList<GameTestInfo> collection3 = new ArrayList<GameTestInfo>();
                for (int i = 0; i < 100; ++i) {
                    GameTestInfo gameTestInfo2 = new GameTestInfo(gameTestInfo.getTestHolder(), rotation, serverLevel, new RetryOptions(1, true));
                    gameTestInfo2.setTestBlockPos(gameTestInfo.getTestBlockPos());
                    collection3.add(gameTestInfo2);
                }
                GameTestBatch gameTestBatch = GameTestBatchFactory.toGameTestBatch(collection3, gameTestInfo.getTest().batch(), rotation.ordinal());
                collection2.add(gameTestBatch);
            }
        }
        StructureGridSpawner structureGridSpawner = new StructureGridSpawner(blockPos, 10, true);
        GameTestRunner gameTestRunner = GameTestRunner.Builder.fromBatches(collection2, serverLevel).batcher(GameTestBatchFactory.fromGameTestInfo(100)).newStructureSpawner(structureGridSpawner).existingStructureSpawner(structureGridSpawner).haltOnError().clearBetweenBatches().build();
        return TestCommand.trackAndStartRunner(commandSourceStack, gameTestRunner);
    }

    private static int run(TestFinder testFinder, RetryOptions retryOptions, int i, int j) {
        TestCommand.stopTests();
        CommandSourceStack commandSourceStack = testFinder.source();
        ServerLevel serverLevel = commandSourceStack.getLevel();
        BlockPos blockPos = TestCommand.createTestPositionAround(commandSourceStack);
        List collection = Stream.concat(TestCommand.toGameTestInfos(commandSourceStack, retryOptions, testFinder), TestCommand.toGameTestInfo(commandSourceStack, retryOptions, testFinder, i)).toList();
        if (collection.isEmpty()) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.test.no_tests"), false);
            return 0;
        }
        FailedTestTracker.forgetFailedTests();
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.test.run.running", collection.size()), false);
        GameTestRunner gameTestRunner = GameTestRunner.Builder.fromInfo(collection, serverLevel).newStructureSpawner(new StructureGridSpawner(blockPos, j, false)).build();
        return TestCommand.trackAndStartRunner(commandSourceStack, gameTestRunner);
    }

    private static int locate(TestFinder testFinder) throws CommandSyntaxException {
        testFinder.source().sendSystemMessage(Component.translatable("commands.test.locate.started"));
        MutableInt mutableInt = new MutableInt(0);
        BlockPos blockPos = BlockPos.containing(testFinder.source().getPosition());
        testFinder.findTestPos().forEach(blockPos2 -> {
            BlockEntity blockEntity = testFinder.source().getLevel().getBlockEntity((BlockPos)blockPos2);
            if (!(blockEntity instanceof TestInstanceBlockEntity)) {
                return;
            }
            TestInstanceBlockEntity testInstanceBlockEntity = (TestInstanceBlockEntity)blockEntity;
            Direction direction = testInstanceBlockEntity.getRotation().rotate(Direction.NORTH);
            BlockPos blockPos3 = testInstanceBlockEntity.getBlockPos().relative(direction, 2);
            int i = (int)direction.getOpposite().toYRot();
            String string = String.format(Locale.ROOT, "/tp @s %d %d %d %d 0", blockPos3.getX(), blockPos3.getY(), blockPos3.getZ(), i);
            int j = blockPos.getX() - blockPos2.getX();
            int k = blockPos.getZ() - blockPos2.getZ();
            int l = Mth.floor(Mth.sqrt(j * j + k * k));
            MutableComponent component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockPos2.getX(), blockPos2.getY(), blockPos2.getZ())).withStyle(style -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent.SuggestCommand(string)).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip"))));
            testFinder.source().sendSuccess(() -> Component.translatable("commands.test.locate.found", component, l), false);
            mutableInt.increment();
        });
        int i = mutableInt.intValue();
        if (i == 0) {
            throw NO_TEST_INSTANCES.create();
        }
        testFinder.source().sendSuccess(() -> Component.translatable("commands.test.locate.done", i), true);
        return i;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> inCommandFunction, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function) {
        return argumentBuilder.executes(commandContext -> TestCommand.run((TestFinder)inCommandFunction.apply(commandContext), RetryOptions.noRetries(), 0, 8)).then(((RequiredArgumentBuilder)Commands.argument("numberOfTimes", IntegerArgumentType.integer((int)0)).executes(commandContext -> TestCommand.run((TestFinder)inCommandFunction.apply(commandContext), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"numberOfTimes"), false), 0, 8))).then(function.apply(Commands.argument("untilFailed", BoolArgumentType.bool()).executes(commandContext -> TestCommand.run((TestFinder)inCommandFunction.apply(commandContext), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"untilFailed")), 0, 8)))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder2, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> inCommandFunction) {
        return TestCommand.runWithRetryOptions(argumentBuilder2, inCommandFunction, argumentBuilder -> argumentBuilder);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptionsAndBuildInfo(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder2, InCommandFunction<CommandContext<CommandSourceStack>, TestFinder> inCommandFunction) {
        return TestCommand.runWithRetryOptions(argumentBuilder2, inCommandFunction, argumentBuilder -> argumentBuilder.then(((RequiredArgumentBuilder)Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.run((TestFinder)inCommandFunction.apply(commandContext), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"untilFailed")), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"rotationSteps"), 8))).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.run((TestFinder)inCommandFunction.apply(commandContext), new RetryOptions(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"numberOfTimes"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"untilFailed")), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"rotationSteps"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"testsPerRow"))))));
    }

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        ArgumentBuilder<CommandSourceStack, ?> argumentBuilder = TestCommand.runWithRetryOptionsAndBuildInfo(Commands.argument("onlyRequiredTests", BoolArgumentType.bool()), commandContext -> TestFinder.builder().failedTests((CommandContext<CommandSourceStack>)commandContext, BoolArgumentType.getBool((CommandContext)commandContext, (String)"onlyRequiredTests")));
        LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("test").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("run").then(TestCommand.runWithRetryOptionsAndBuildInfo(Commands.argument("tests", ResourceSelectorArgument.resourceSelector(commandBuildContext, Registries.TEST_INSTANCE)), commandContext -> TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)commandContext, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)commandContext, "tests")))))).then(Commands.literal("runmultiple").then(((RequiredArgumentBuilder)Commands.argument("tests", ResourceSelectorArgument.resourceSelector(commandBuildContext, Registries.TEST_INSTANCE)).executes(commandContext -> TestCommand.run(TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)commandContext, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)commandContext, "tests")), RetryOptions.noRetries(), 0, 8))).then(Commands.argument("amount", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.run(TestFinder.builder().createMultipleCopies(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"amount")).byResourceSelection((CommandContext<CommandSourceStack>)commandContext, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)commandContext, "tests")), RetryOptions.noRetries(), 0, 8)))))).then(TestCommand.runWithRetryOptions(Commands.literal("runthese"), TestFinder.builder()::allNearby))).then(TestCommand.runWithRetryOptions(Commands.literal("runclosest"), TestFinder.builder()::nearest))).then(TestCommand.runWithRetryOptions(Commands.literal("runthat"), TestFinder.builder()::lookedAt))).then(TestCommand.runWithRetryOptionsAndBuildInfo(Commands.literal("runfailed").then(argumentBuilder), TestFinder.builder()::failedTests))).then(Commands.literal("verify").then(Commands.argument("tests", ResourceSelectorArgument.resourceSelector(commandBuildContext, Registries.TEST_INSTANCE)).executes(commandContext -> TestCommand.verify(TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)commandContext, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)commandContext, "tests"))))))).then(Commands.literal("locate").then(Commands.argument("tests", ResourceSelectorArgument.resourceSelector(commandBuildContext, Registries.TEST_INSTANCE)).executes(commandContext -> TestCommand.locate(TestFinder.builder().byResourceSelection((CommandContext<CommandSourceStack>)commandContext, ResourceSelectorArgument.getSelectedResources((CommandContext<CommandSourceStack>)commandContext, "tests"))))))).then(Commands.literal("resetclosest").executes(commandContext -> TestCommand.reset(TestFinder.builder().nearest((CommandContext<CommandSourceStack>)commandContext))))).then(Commands.literal("resetthese").executes(commandContext -> TestCommand.reset(TestFinder.builder().allNearby((CommandContext<CommandSourceStack>)commandContext))))).then(Commands.literal("resetthat").executes(commandContext -> TestCommand.reset(TestFinder.builder().lookedAt((CommandContext<CommandSourceStack>)commandContext))))).then(Commands.literal("clearthat").executes(commandContext -> TestCommand.clear(TestFinder.builder().lookedAt((CommandContext<CommandSourceStack>)commandContext))))).then(Commands.literal("clearthese").executes(commandContext -> TestCommand.clear(TestFinder.builder().allNearby((CommandContext<CommandSourceStack>)commandContext))))).then(((LiteralArgumentBuilder)Commands.literal("clearall").executes(commandContext -> TestCommand.clear(TestFinder.builder().radius((CommandContext<CommandSourceStack>)commandContext, 250)))).then(Commands.argument("radius", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.clear(TestFinder.builder().radius((CommandContext<CommandSourceStack>)commandContext, Mth.clamp(IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"radius"), 0, 1024))))))).then(Commands.literal("stop").executes(commandContext -> TestCommand.stopTests()))).then(((LiteralArgumentBuilder)Commands.literal("pos").executes(commandContext -> TestCommand.showPos((CommandSourceStack)commandContext.getSource(), "pos"))).then(Commands.argument("var", StringArgumentType.word()).executes(commandContext -> TestCommand.showPos((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"var")))))).then(Commands.literal("create").then(((RequiredArgumentBuilder)Commands.argument("id", IdentifierArgument.id()).suggests(TestCommand::suggestTestFunction).executes(commandContext -> TestCommand.createNewStructure((CommandSourceStack)commandContext.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"), 5, 5, 5))).then(((RequiredArgumentBuilder)Commands.argument("width", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.createNewStructure((CommandSourceStack)commandContext.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"width"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"width"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"width")))).then(Commands.argument("height", IntegerArgumentType.integer()).then(Commands.argument("depth", IntegerArgumentType.integer()).executes(commandContext -> TestCommand.createNewStructure((CommandSourceStack)commandContext.getSource(), IdentifierArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"width"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"height"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"depth"))))))));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            literalArgumentBuilder = (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(Commands.literal("export").then(Commands.argument("test", ResourceArgument.resource(commandBuildContext, Registries.TEST_INSTANCE)).executes(commandContext -> TestCommand.exportTestStructure((CommandSourceStack)commandContext.getSource(), ResourceArgument.getResource((CommandContext<CommandSourceStack>)commandContext, "test", Registries.TEST_INSTANCE)))))).then(Commands.literal("exportclosest").executes(commandContext -> TestCommand.export(TestFinder.builder().nearest((CommandContext<CommandSourceStack>)commandContext))))).then(Commands.literal("exportthese").executes(commandContext -> TestCommand.export(TestFinder.builder().allNearby((CommandContext<CommandSourceStack>)commandContext))))).then(Commands.literal("exportthat").executes(commandContext -> TestCommand.export(TestFinder.builder().lookedAt((CommandContext<CommandSourceStack>)commandContext))));
        }
        commandDispatcher.register(literalArgumentBuilder);
    }

    public static CompletableFuture<Suggestions> suggestTestFunction(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) {
        Stream<String> stream = ((CommandSourceStack)commandContext.getSource()).registryAccess().lookupOrThrow(Registries.TEST_FUNCTION).listElements().map(Holder::getRegisteredName);
        return SharedSuggestionProvider.suggest(stream, suggestionsBuilder);
    }

    private static int resetGameTestInfo(CommandSourceStack commandSourceStack, GameTestInfo gameTestInfo) {
        TestInstanceBlockEntity testInstanceBlockEntity = gameTestInfo.getTestInstanceBlockEntity();
        testInstanceBlockEntity.resetTest(commandSourceStack::sendSystemMessage);
        return 1;
    }

    private static Stream<GameTestInfo> toGameTestInfos(CommandSourceStack commandSourceStack, RetryOptions retryOptions, TestPosFinder testPosFinder) {
        return testPosFinder.findTestPos().map(blockPos -> TestCommand.createGameTestInfo(blockPos, commandSourceStack, retryOptions)).flatMap((Function<Optional, Stream>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, stream(), (Ljava/util/Optional;)Ljava/util/stream/Stream;)());
    }

    private static Stream<GameTestInfo> toGameTestInfo(CommandSourceStack commandSourceStack, RetryOptions retryOptions, TestInstanceFinder testInstanceFinder, int i) {
        return testInstanceFinder.findTests().filter(reference -> TestCommand.verifyStructureExists(commandSourceStack, ((GameTestInstance)reference.value()).structure())).map(reference -> new GameTestInfo((Holder.Reference<GameTestInstance>)reference, StructureUtils.getRotationForRotationSteps(i), commandSourceStack.getLevel(), retryOptions));
    }

    private static Optional<GameTestInfo> createGameTestInfo(BlockPos blockPos, CommandSourceStack commandSourceStack, RetryOptions retryOptions) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (!(blockEntity instanceof TestInstanceBlockEntity)) {
            commandSourceStack.sendFailure(Component.translatable("commands.test.error.test_instance_not_found.position", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            return Optional.empty();
        }
        TestInstanceBlockEntity testInstanceBlockEntity = (TestInstanceBlockEntity)blockEntity;
        Optional optional = testInstanceBlockEntity.test().flatMap(((Registry)commandSourceStack.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE))::get);
        if (optional.isEmpty()) {
            commandSourceStack.sendFailure(Component.translatable("commands.test.error.non_existant_test", testInstanceBlockEntity.getTestName()));
            return Optional.empty();
        }
        Holder.Reference reference = (Holder.Reference)optional.get();
        GameTestInfo gameTestInfo = new GameTestInfo(reference, testInstanceBlockEntity.getRotation(), serverLevel, retryOptions);
        gameTestInfo.setTestBlockPos(blockPos);
        if (!TestCommand.verifyStructureExists(commandSourceStack, gameTestInfo.getStructure())) {
            return Optional.empty();
        }
        return Optional.of(gameTestInfo);
    }

    private static int createNewStructure(CommandSourceStack commandSourceStack, Identifier identifier, int i, int j, int k) throws CommandSyntaxException {
        if (i > 48 || j > 48 || k > 48) {
            throw TOO_LARGE.create((Object)48);
        }
        ServerLevel serverLevel = commandSourceStack.getLevel();
        BlockPos blockPos2 = TestCommand.createTestPositionAround(commandSourceStack);
        TestInstanceBlockEntity testInstanceBlockEntity = StructureUtils.createNewEmptyTest(identifier, blockPos2, new Vec3i(i, j, k), Rotation.NONE, serverLevel);
        BlockPos blockPos22 = testInstanceBlockEntity.getStructurePos();
        BlockPos blockPos3 = blockPos22.offset(i - 1, 0, k - 1);
        BlockPos.betweenClosedStream(blockPos22, blockPos3).forEach(blockPos -> serverLevel.setBlockAndUpdate((BlockPos)blockPos, Blocks.BEDROCK.defaultBlockState()));
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.test.create.success", testInstanceBlockEntity.getTestName()), true);
        return 1;
    }

    private static int showPos(CommandSourceStack commandSourceStack, String string) throws CommandSyntaxException {
        ServerLevel serverLevel;
        ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
        BlockHitResult blockHitResult = (BlockHitResult)serverPlayer.pick(10.0, 1.0f, false);
        BlockPos blockPos = blockHitResult.getBlockPos();
        Optional<BlockPos> optional = StructureUtils.findTestContainingPos(blockPos, 15, serverLevel = commandSourceStack.getLevel());
        if (optional.isEmpty()) {
            optional = StructureUtils.findTestContainingPos(blockPos, 250, serverLevel);
        }
        if (optional.isEmpty()) {
            throw NO_TEST_CONTAINING.create((Object)blockPos.getX(), (Object)blockPos.getY(), (Object)blockPos.getZ());
        }
        BlockEntity blockEntity = serverLevel.getBlockEntity(optional.get());
        if (!(blockEntity instanceof TestInstanceBlockEntity)) {
            throw TEST_INSTANCE_COULD_NOT_BE_FOUND.create();
        }
        TestInstanceBlockEntity testInstanceBlockEntity = (TestInstanceBlockEntity)blockEntity;
        BlockPos blockPos2 = testInstanceBlockEntity.getStructurePos();
        BlockPos blockPos3 = blockPos.subtract(blockPos2);
        String string2 = blockPos3.getX() + ", " + blockPos3.getY() + ", " + blockPos3.getZ();
        String string3 = testInstanceBlockEntity.getTestName().getString();
        MutableComponent component = Component.translatable("commands.test.coordinates", blockPos3.getX(), blockPos3.getY(), blockPos3.getZ()).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withHoverEvent(new HoverEvent.ShowText(Component.translatable("commands.test.coordinates.copy"))).withClickEvent(new ClickEvent.CopyToClipboard("final BlockPos " + string + " = new BlockPos(" + string2 + ");")));
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.test.relative_position", string3, component), false);
        serverPlayer.connection.send(new ClientboundGameTestHighlightPosPacket(blockPos, blockPos3));
        return 1;
    }

    private static int stopTests() {
        GameTestTicker.SINGLETON.clear();
        return 1;
    }

    public static int trackAndStartRunner(CommandSourceStack commandSourceStack, GameTestRunner gameTestRunner) {
        gameTestRunner.addListener(new TestBatchSummaryDisplayer(commandSourceStack));
        MultipleTestTracker multipleTestTracker = new MultipleTestTracker(gameTestRunner.getTestInfos());
        multipleTestTracker.addListener(new TestSummaryDisplayer(commandSourceStack, multipleTestTracker));
        multipleTestTracker.addFailureListener(gameTestInfo -> FailedTestTracker.rememberFailedTest(gameTestInfo.getTestHolder()));
        gameTestRunner.start();
        return 1;
    }

    private static int exportTestStructure(CommandSourceStack commandSourceStack, Holder<GameTestInstance> holder) {
        if (!TestInstanceBlockEntity.export(commandSourceStack.getLevel(), holder.value().structure(), commandSourceStack::sendSystemMessage)) {
            return 0;
        }
        return 1;
    }

    private static boolean verifyStructureExists(CommandSourceStack commandSourceStack, Identifier identifier) {
        if (commandSourceStack.getLevel().getStructureManager().get(identifier).isEmpty()) {
            commandSourceStack.sendFailure(Component.translatable("commands.test.error.structure_not_found", Component.translationArg(identifier)));
            return false;
        }
        return true;
    }

    private static BlockPos createTestPositionAround(CommandSourceStack commandSourceStack) {
        BlockPos blockPos = BlockPos.containing(commandSourceStack.getPosition());
        int i = commandSourceStack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
        return new BlockPos(blockPos.getX(), i, blockPos.getZ() + 3);
    }

    record TestBatchSummaryDisplayer(CommandSourceStack source) implements GameTestBatchListener
    {
        @Override
        public void testBatchStarting(GameTestBatch gameTestBatch) {
            this.source.sendSuccess(() -> Component.translatable("commands.test.batch.starting", gameTestBatch.environment().getRegisteredName(), gameTestBatch.index()), true);
        }

        @Override
        public void testBatchFinished(GameTestBatch gameTestBatch) {
        }
    }

    public record TestSummaryDisplayer(CommandSourceStack source, MultipleTestTracker tracker) implements GameTestListener
    {
        @Override
        public void testStructureLoaded(GameTestInfo gameTestInfo) {
        }

        @Override
        public void testPassed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
            this.showTestSummaryIfAllDone();
        }

        @Override
        public void testFailed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
            this.showTestSummaryIfAllDone();
        }

        @Override
        public void testAddedForRerun(GameTestInfo gameTestInfo, GameTestInfo gameTestInfo2, GameTestRunner gameTestRunner) {
            this.tracker.addTestToTrack(gameTestInfo2);
        }

        private void showTestSummaryIfAllDone() {
            if (this.tracker.isDone()) {
                this.source.sendSuccess(() -> Component.translatable("commands.test.summary", this.tracker.getTotalCount()).withStyle(ChatFormatting.WHITE), true);
                if (this.tracker.hasFailedRequired()) {
                    this.source.sendFailure(Component.translatable("commands.test.summary.failed", this.tracker.getFailedRequiredCount()));
                } else {
                    this.source.sendSuccess(() -> Component.translatable("commands.test.summary.all_required_passed").withStyle(ChatFormatting.GREEN), true);
                }
                if (this.tracker.hasFailedOptional()) {
                    this.source.sendSystemMessage(Component.translatable("commands.test.summary.optional_failed", this.tracker.getFailedOptionalCount()));
                }
            }
        }
    }
}


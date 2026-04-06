/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongArraySet
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import java.lang.invoke.LambdaMetafactory;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestBatchFactory;
import net.minecraft.gametest.framework.GameTestBatchListener;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.ReportGameListener;
import net.minecraft.gametest.framework.StructureGridSpawner;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class GameTestRunner {
    public static final int DEFAULT_TESTS_PER_ROW = 8;
    private static final Logger LOGGER = LogUtils.getLogger();
    final ServerLevel level;
    private final GameTestTicker testTicker;
    private final List<GameTestInfo> allTestInfos;
    private ImmutableList<GameTestBatch> batches;
    final List<GameTestBatchListener> batchListeners = Lists.newArrayList();
    private final List<GameTestInfo> scheduledForRerun = Lists.newArrayList();
    private final GameTestBatcher testBatcher;
    private boolean stopped = true;
    private @Nullable Holder<TestEnvironmentDefinition> currentEnvironment;
    private final StructureSpawner existingStructureSpawner;
    private final StructureSpawner newStructureSpawner;
    final boolean haltOnError;
    private final boolean clearBetweenBatches;

    protected GameTestRunner(GameTestBatcher gameTestBatcher, Collection<GameTestBatch> collection, ServerLevel serverLevel, GameTestTicker gameTestTicker, StructureSpawner structureSpawner, StructureSpawner structureSpawner2, boolean bl, boolean bl2) {
        this.level = serverLevel;
        this.testTicker = gameTestTicker;
        this.testBatcher = gameTestBatcher;
        this.existingStructureSpawner = structureSpawner;
        this.newStructureSpawner = structureSpawner2;
        this.batches = ImmutableList.copyOf(collection);
        this.haltOnError = bl;
        this.clearBetweenBatches = bl2;
        this.allTestInfos = this.batches.stream().flatMap(gameTestBatch -> gameTestBatch.gameTestInfos().stream()).collect(Util.toMutableList());
        gameTestTicker.setRunner(this);
        this.allTestInfos.forEach(gameTestInfo -> gameTestInfo.addListener(new ReportGameListener()));
    }

    public List<GameTestInfo> getTestInfos() {
        return this.allTestInfos;
    }

    public void start() {
        this.stopped = false;
        this.runBatch(0);
    }

    public void stop() {
        this.stopped = true;
        if (this.currentEnvironment != null) {
            this.endCurrentEnvironment();
        }
    }

    public void rerunTest(GameTestInfo gameTestInfo) {
        GameTestInfo gameTestInfo2 = gameTestInfo.copyReset();
        gameTestInfo.getListeners().forEach(gameTestListener -> gameTestListener.testAddedForRerun(gameTestInfo, gameTestInfo2, this));
        this.allTestInfos.add(gameTestInfo2);
        this.scheduledForRerun.add(gameTestInfo2);
        if (this.stopped) {
            this.runScheduledRerunTests();
        }
    }

    void runBatch(final int i) {
        GameTestBatch gameTestBatch;
        if (i >= this.batches.size()) {
            this.endCurrentEnvironment();
            this.runScheduledRerunTests();
            return;
        }
        if (i > 0 && this.clearBetweenBatches) {
            gameTestBatch = (GameTestBatch)((Object)this.batches.get(i - 1));
            gameTestBatch.gameTestInfos().forEach(gameTestInfo -> {
                TestInstanceBlockEntity testInstanceBlockEntity = gameTestInfo.getTestInstanceBlockEntity();
                StructureUtils.clearSpaceForStructure(testInstanceBlockEntity.getStructureBoundingBox(), this.level);
                this.level.destroyBlock(testInstanceBlockEntity.getBlockPos(), false);
            });
        }
        gameTestBatch = (GameTestBatch)((Object)this.batches.get(i));
        this.existingStructureSpawner.onBatchStart(this.level);
        this.newStructureSpawner.onBatchStart(this.level);
        Collection<GameTestInfo> collection = this.createStructuresForBatch(gameTestBatch.gameTestInfos());
        LOGGER.info("Running test environment '{}' batch {} ({} tests)...", new Object[]{gameTestBatch.environment().getRegisteredName(), gameTestBatch.index(), collection.size()});
        this.endCurrentEnvironment();
        this.currentEnvironment = gameTestBatch.environment();
        this.currentEnvironment.value().setup(this.level);
        this.batchListeners.forEach(gameTestBatchListener -> gameTestBatchListener.testBatchStarting(gameTestBatch));
        final MultipleTestTracker multipleTestTracker = new MultipleTestTracker();
        collection.forEach(multipleTestTracker::addTestToTrack);
        multipleTestTracker.addListener(new GameTestListener(){

            private void testCompleted(GameTestInfo gameTestInfo) {
                gameTestInfo.getTestInstanceBlockEntity().removeBarriers();
                if (multipleTestTracker.isDone()) {
                    GameTestRunner.this.batchListeners.forEach(gameTestBatchListener -> gameTestBatchListener.testBatchFinished(gameTestBatch));
                    LongArraySet longSet = new LongArraySet(GameTestRunner.this.level.getForceLoadedChunks());
                    longSet.forEach(l -> GameTestRunner.this.level.setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), false));
                    GameTestRunner.this.runBatch(i + 1);
                }
            }

            @Override
            public void testStructureLoaded(GameTestInfo gameTestInfo) {
            }

            @Override
            public void testPassed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
                this.testCompleted(gameTestInfo);
            }

            @Override
            public void testFailed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
                if (GameTestRunner.this.haltOnError) {
                    GameTestRunner.this.endCurrentEnvironment();
                    LongArraySet longSet = new LongArraySet(GameTestRunner.this.level.getForceLoadedChunks());
                    longSet.forEach(l -> GameTestRunner.this.level.setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), false));
                    GameTestTicker.SINGLETON.clear();
                    gameTestInfo.getTestInstanceBlockEntity().removeBarriers();
                } else {
                    this.testCompleted(gameTestInfo);
                }
            }

            @Override
            public void testAddedForRerun(GameTestInfo gameTestInfo, GameTestInfo gameTestInfo2, GameTestRunner gameTestRunner) {
            }
        });
        collection.forEach(this.testTicker::add);
    }

    void endCurrentEnvironment() {
        if (this.currentEnvironment != null) {
            this.currentEnvironment.value().teardown(this.level);
            this.currentEnvironment = null;
        }
    }

    private void runScheduledRerunTests() {
        if (!this.scheduledForRerun.isEmpty()) {
            LOGGER.info("Starting re-run of tests: {}", (Object)this.scheduledForRerun.stream().map(gameTestInfo -> gameTestInfo.id().toString()).collect(Collectors.joining(", ")));
            this.batches = ImmutableList.copyOf(this.testBatcher.batch(this.scheduledForRerun));
            this.scheduledForRerun.clear();
            this.stopped = false;
            this.runBatch(0);
        } else {
            this.batches = ImmutableList.of();
            this.stopped = true;
        }
    }

    public void addListener(GameTestBatchListener gameTestBatchListener) {
        this.batchListeners.add(gameTestBatchListener);
    }

    private Collection<GameTestInfo> createStructuresForBatch(Collection<GameTestInfo> collection) {
        return collection.stream().map(this::spawn).flatMap((Function<Optional, Stream>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, stream(), (Ljava/util/Optional;)Ljava/util/stream/Stream;)()).toList();
    }

    private Optional<GameTestInfo> spawn(GameTestInfo gameTestInfo) {
        if (gameTestInfo.getTestBlockPos() == null) {
            return this.newStructureSpawner.spawnStructure(gameTestInfo);
        }
        return this.existingStructureSpawner.spawnStructure(gameTestInfo);
    }

    public static interface GameTestBatcher {
        public Collection<GameTestBatch> batch(Collection<GameTestInfo> var1);
    }

    public static interface StructureSpawner {
        public static final StructureSpawner IN_PLACE = gameTestInfo2 -> Optional.ofNullable(gameTestInfo2.prepareTestStructure()).map(gameTestInfo -> gameTestInfo.startExecution(1));
        public static final StructureSpawner NOT_SET = gameTestInfo -> Optional.empty();

        public Optional<GameTestInfo> spawnStructure(GameTestInfo var1);

        default public void onBatchStart(ServerLevel serverLevel) {
        }
    }

    public static class Builder {
        private final ServerLevel level;
        private final GameTestTicker testTicker = GameTestTicker.SINGLETON;
        private GameTestBatcher batcher = GameTestBatchFactory.fromGameTestInfo();
        private StructureSpawner existingStructureSpawner = StructureSpawner.IN_PLACE;
        private StructureSpawner newStructureSpawner = StructureSpawner.NOT_SET;
        private final Collection<GameTestBatch> batches;
        private boolean haltOnError = false;
        private boolean clearBetweenBatches = false;

        private Builder(Collection<GameTestBatch> collection, ServerLevel serverLevel) {
            this.batches = collection;
            this.level = serverLevel;
        }

        public static Builder fromBatches(Collection<GameTestBatch> collection, ServerLevel serverLevel) {
            return new Builder(collection, serverLevel);
        }

        public static Builder fromInfo(Collection<GameTestInfo> collection, ServerLevel serverLevel) {
            return Builder.fromBatches(GameTestBatchFactory.fromGameTestInfo().batch(collection), serverLevel);
        }

        public Builder haltOnError() {
            this.haltOnError = true;
            return this;
        }

        public Builder clearBetweenBatches() {
            this.clearBetweenBatches = true;
            return this;
        }

        public Builder newStructureSpawner(StructureSpawner structureSpawner) {
            this.newStructureSpawner = structureSpawner;
            return this;
        }

        public Builder existingStructureSpawner(StructureGridSpawner structureGridSpawner) {
            this.existingStructureSpawner = structureGridSpawner;
            return this;
        }

        public Builder batcher(GameTestBatcher gameTestBatcher) {
            this.batcher = gameTestBatcher;
            return this;
        }

        public GameTestRunner build() {
            return new GameTestRunner(this.batcher, this.batches, this.level, this.testTicker, this.existingStructureSpawner, this.newStructureSpawner, this.haltOnError, this.clearBetweenBatches);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Streams
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

public class GameTestBatchFactory {
    private static final int MAX_TESTS_PER_BATCH = 50;
    public static final TestDecorator DIRECT = (reference, serverLevel) -> Stream.of(new GameTestInfo(reference, Rotation.NONE, serverLevel, RetryOptions.noRetries()));

    public static List<GameTestBatch> divideIntoBatches(Collection<Holder.Reference<GameTestInstance>> collection, TestDecorator testDecorator, ServerLevel serverLevel) {
        Map<Holder, List<GameTestInfo>> map = collection.stream().flatMap(reference -> testDecorator.decorate((Holder.Reference<GameTestInstance>)reference, serverLevel)).collect(Collectors.groupingBy(gameTestInfo -> gameTestInfo.getTest().batch()));
        return map.entrySet().stream().flatMap(entry -> {
            Holder holder = (Holder)entry.getKey();
            List list2 = (List)entry.getValue();
            return Streams.mapWithIndex(Lists.partition((List)list2, (int)50).stream(), (list, l) -> GameTestBatchFactory.toGameTestBatch(list, holder, (int)l));
        }).toList();
    }

    public static GameTestRunner.GameTestBatcher fromGameTestInfo() {
        return GameTestBatchFactory.fromGameTestInfo(50);
    }

    public static GameTestRunner.GameTestBatcher fromGameTestInfo(int i) {
        return collection -> {
            Map<Holder, List<GameTestInfo>> map = collection.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(gameTestInfo -> gameTestInfo.getTest().batch()));
            return map.entrySet().stream().flatMap(entry -> {
                Holder holder = (Holder)entry.getKey();
                List list2 = (List)entry.getValue();
                return Streams.mapWithIndex(Lists.partition((List)list2, (int)i).stream(), (list, l) -> GameTestBatchFactory.toGameTestBatch(List.copyOf((Collection)list), holder, (int)l));
            }).toList();
        };
    }

    public static GameTestBatch toGameTestBatch(Collection<GameTestInfo> collection, Holder<TestEnvironmentDefinition> holder, int i) {
        return new GameTestBatch(i, collection, holder);
    }

    @FunctionalInterface
    public static interface TestDecorator {
        public Stream<GameTestInfo> decorate(Holder.Reference<GameTestInstance> var1, ServerLevel var2);
    }
}


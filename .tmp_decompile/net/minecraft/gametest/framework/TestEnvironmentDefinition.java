/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.gametest.framework;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;

public interface TestEnvironmentDefinition {
    public static final Codec<TestEnvironmentDefinition> DIRECT_CODEC = BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE.byNameCodec().dispatch(TestEnvironmentDefinition::codec, mapCodec -> mapCodec);
    public static final Codec<Holder<TestEnvironmentDefinition>> CODEC = RegistryFileCodec.create(Registries.TEST_ENVIRONMENT, DIRECT_CODEC);

    public static MapCodec<? extends TestEnvironmentDefinition> bootstrap(Registry<MapCodec<? extends TestEnvironmentDefinition>> registry) {
        Registry.register(registry, "all_of", AllOf.CODEC);
        Registry.register(registry, "game_rules", SetGameRules.CODEC);
        Registry.register(registry, "time_of_day", TimeOfDay.CODEC);
        Registry.register(registry, "weather", Weather.CODEC);
        return Registry.register(registry, "function", Functions.CODEC);
    }

    public void setup(ServerLevel var1);

    default public void teardown(ServerLevel serverLevel) {
    }

    public MapCodec<? extends TestEnvironmentDefinition> codec();

    public record AllOf(List<Holder<TestEnvironmentDefinition>> definitions) implements TestEnvironmentDefinition
    {
        public static final MapCodec<AllOf> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CODEC.listOf().fieldOf("definitions").forGetter(AllOf::definitions)).apply((Applicative)instance, AllOf::new));

        public AllOf(TestEnvironmentDefinition ... testEnvironmentDefinitions) {
            this(Arrays.stream(testEnvironmentDefinitions).map(Holder::direct).toList());
        }

        @Override
        public void setup(ServerLevel serverLevel) {
            this.definitions.forEach(holder -> ((TestEnvironmentDefinition)holder.value()).setup(serverLevel));
        }

        @Override
        public void teardown(ServerLevel serverLevel) {
            this.definitions.forEach(holder -> ((TestEnvironmentDefinition)holder.value()).teardown(serverLevel));
        }

        public MapCodec<AllOf> codec() {
            return CODEC;
        }
    }

    public record SetGameRules(GameRuleMap gameRulesMap) implements TestEnvironmentDefinition
    {
        public static final MapCodec<SetGameRules> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)GameRuleMap.CODEC.fieldOf("rules").forGetter(SetGameRules::gameRulesMap)).apply((Applicative)instance, SetGameRules::new));

        @Override
        public void setup(ServerLevel serverLevel) {
            GameRules gameRules = serverLevel.getGameRules();
            MinecraftServer minecraftServer = serverLevel.getServer();
            gameRules.setAll(this.gameRulesMap, minecraftServer);
        }

        @Override
        public void teardown(ServerLevel serverLevel) {
            this.gameRulesMap.keySet().forEach(gameRule -> this.resetRule(serverLevel, (GameRule)gameRule));
        }

        private <T> void resetRule(ServerLevel serverLevel, GameRule<T> gameRule) {
            serverLevel.getGameRules().set(gameRule, gameRule.defaultValue(), serverLevel.getServer());
        }

        public MapCodec<SetGameRules> codec() {
            return CODEC;
        }
    }

    public record TimeOfDay(int time) implements TestEnvironmentDefinition
    {
        public static final MapCodec<TimeOfDay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("time").forGetter(TimeOfDay::time)).apply((Applicative)instance, TimeOfDay::new));

        @Override
        public void setup(ServerLevel serverLevel) {
            serverLevel.setDayTime(this.time);
        }

        public MapCodec<TimeOfDay> codec() {
            return CODEC;
        }
    }

    public record Weather(Type weather) implements TestEnvironmentDefinition
    {
        public static final MapCodec<Weather> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Type.CODEC.fieldOf("weather").forGetter(Weather::weather)).apply((Applicative)instance, Weather::new));

        @Override
        public void setup(ServerLevel serverLevel) {
            this.weather.apply(serverLevel);
        }

        @Override
        public void teardown(ServerLevel serverLevel) {
            serverLevel.resetWeatherCycle();
        }

        public MapCodec<Weather> codec() {
            return CODEC;
        }

        public static enum Type implements StringRepresentable
        {
            CLEAR("clear", 100000, 0, false, false),
            RAIN("rain", 0, 100000, true, false),
            THUNDER("thunder", 0, 100000, true, true);

            public static final Codec<Type> CODEC;
            private final String id;
            private final int clearTime;
            private final int rainTime;
            private final boolean raining;
            private final boolean thundering;

            private Type(String string2, int j, int k, boolean bl, boolean bl2) {
                this.id = string2;
                this.clearTime = j;
                this.rainTime = k;
                this.raining = bl;
                this.thundering = bl2;
            }

            void apply(ServerLevel serverLevel) {
                serverLevel.setWeatherParameters(this.clearTime, this.rainTime, this.raining, this.thundering);
            }

            @Override
            public String getSerializedName() {
                return this.id;
            }

            static {
                CODEC = StringRepresentable.fromEnum(Type::values);
            }
        }
    }

    public record Functions(Optional<Identifier> setupFunction, Optional<Identifier> teardownFunction) implements TestEnvironmentDefinition
    {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<Functions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.optionalFieldOf("setup").forGetter(Functions::setupFunction), (App)Identifier.CODEC.optionalFieldOf("teardown").forGetter(Functions::teardownFunction)).apply((Applicative)instance, Functions::new));

        @Override
        public void setup(ServerLevel serverLevel) {
            this.setupFunction.ifPresent(identifier -> Functions.run(serverLevel, identifier));
        }

        @Override
        public void teardown(ServerLevel serverLevel) {
            this.teardownFunction.ifPresent(identifier -> Functions.run(serverLevel, identifier));
        }

        private static void run(ServerLevel serverLevel, Identifier identifier) {
            MinecraftServer minecraftServer = serverLevel.getServer();
            ServerFunctionManager serverFunctionManager = minecraftServer.getFunctions();
            Optional<CommandFunction<CommandSourceStack>> optional = serverFunctionManager.get(identifier);
            if (optional.isPresent()) {
                CommandSourceStack commandSourceStack = minecraftServer.createCommandSourceStack().withPermission(LevelBasedPermissionSet.GAMEMASTER).withSuppressedOutput().withLevel(serverLevel);
                serverFunctionManager.execute(optional.get(), commandSourceStack);
            } else {
                LOGGER.error("Test Batch failed for non-existent function {}", (Object)identifier);
            }
        }

        public MapCodec<Functions> codec() {
            return CODEC;
        }
    }
}


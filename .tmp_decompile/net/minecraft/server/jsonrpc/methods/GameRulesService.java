/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.InvalidParameterJsonRpcException;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;

public class GameRulesService {
    public static List<GameRuleUpdate<?>> get(MinecraftApi minecraftApi) {
        ArrayList list = new ArrayList();
        minecraftApi.gameRuleService().getAvailableGameRules().forEach(gameRule -> GameRulesService.addGameRule(minecraftApi, gameRule, list));
        return list;
    }

    private static <T> void addGameRule(MinecraftApi minecraftApi, GameRule<T> gameRule, List<GameRuleUpdate<?>> list) {
        T object = minecraftApi.gameRuleService().getRuleValue(gameRule);
        list.add(GameRulesService.getTypedRule(minecraftApi, gameRule, Objects.requireNonNull(object)));
    }

    public static <T> GameRuleUpdate<T> getTypedRule(MinecraftApi minecraftApi, GameRule<T> gameRule, T object) {
        return minecraftApi.gameRuleService().getTypedRule(gameRule, object);
    }

    public static <T> GameRuleUpdate<T> update(MinecraftApi minecraftApi, GameRuleUpdate<T> gameRuleUpdate, ClientInfo clientInfo) {
        return minecraftApi.gameRuleService().updateGameRule(gameRuleUpdate, clientInfo);
    }

    public record GameRuleUpdate<T>(GameRule<T> gameRule, T value) {
        public static final Codec<GameRuleUpdate<?>> TYPED_CODEC = BuiltInRegistries.GAME_RULE.byNameCodec().dispatch("key", GameRuleUpdate::gameRule, GameRuleUpdate::getValueAndTypeCodec);
        public static final Codec<GameRuleUpdate<?>> CODEC = BuiltInRegistries.GAME_RULE.byNameCodec().dispatch("key", GameRuleUpdate::gameRule, GameRuleUpdate::getValueCodec);

        private static <T> MapCodec<? extends GameRuleUpdate<T>> getValueCodec(GameRule<T> gameRule) {
            return gameRule.valueCodec().fieldOf("value").xmap(object -> new GameRuleUpdate<Object>(gameRule, object), GameRuleUpdate::value);
        }

        private static <T> MapCodec<? extends GameRuleUpdate<T>> getValueAndTypeCodec(GameRule<T> gameRule) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)StringRepresentable.fromEnum(GameRuleType::values).fieldOf("type").forGetter(gameRuleUpdate -> gameRuleUpdate.gameRule.gameRuleType()), (App)gameRule.valueCodec().fieldOf("value").forGetter(GameRuleUpdate::value)).apply((Applicative)instance, (gameRuleType, object) -> GameRuleUpdate.getUntypedRule(gameRule, gameRuleType, object)));
        }

        private static <T> GameRuleUpdate<T> getUntypedRule(GameRule<T> gameRule, GameRuleType gameRuleType, T object) {
            if (gameRule.gameRuleType() != gameRuleType) {
                throw new InvalidParameterJsonRpcException("Stated type \"" + String.valueOf(gameRuleType) + "\" mismatches with actual type \"" + String.valueOf(gameRule.gameRuleType()) + "\" of gamerule \"" + gameRule.id() + "\"");
            }
            return new GameRuleUpdate<T>(gameRule, object);
        }
    }
}


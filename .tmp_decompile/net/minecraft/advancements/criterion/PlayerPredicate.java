/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMaps
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.advancements.criterion.GameTypePredicate;
import net.minecraft.advancements.criterion.InputPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record PlayerPredicate(MinMaxBounds.Ints level, GameTypePredicate gameType, List<StatMatcher<?>> stats, Object2BooleanMap<ResourceKey<Recipe<?>>> recipes, Map<Identifier, AdvancementPredicate> advancements, Optional<EntityPredicate> lookingAt, Optional<InputPredicate> input) implements EntitySubPredicate
{
    public static final int LOOKING_AT_RANGE = 100;
    public static final MapCodec<PlayerPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("level", (Object)MinMaxBounds.Ints.ANY).forGetter(PlayerPredicate::level), (App)GameTypePredicate.CODEC.optionalFieldOf("gamemode", (Object)GameTypePredicate.ANY).forGetter(PlayerPredicate::gameType), (App)StatMatcher.CODEC.listOf().optionalFieldOf("stats", (Object)List.of()).forGetter(PlayerPredicate::stats), (App)ExtraCodecs.object2BooleanMap(Recipe.KEY_CODEC).optionalFieldOf("recipes", (Object)Object2BooleanMaps.emptyMap()).forGetter(PlayerPredicate::recipes), (App)Codec.unboundedMap(Identifier.CODEC, AdvancementPredicate.CODEC).optionalFieldOf("advancements", (Object)Map.of()).forGetter(PlayerPredicate::advancements), (App)EntityPredicate.CODEC.optionalFieldOf("looking_at").forGetter(PlayerPredicate::lookingAt), (App)InputPredicate.CODEC.optionalFieldOf("input").forGetter(PlayerPredicate::input)).apply((Applicative)instance, PlayerPredicate::new));

    @Override
    public boolean matches(Entity entity2, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if (!(entity2 instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer serverPlayer = (ServerPlayer)entity2;
        if (!this.level.matches(serverPlayer.experienceLevel)) {
            return false;
        }
        if (!this.gameType.matches(serverPlayer.gameMode())) {
            return false;
        }
        ServerStatsCounter statsCounter = serverPlayer.getStats();
        for (StatMatcher<?> statMatcher : this.stats) {
            if (statMatcher.matches(statsCounter)) continue;
            return false;
        }
        ServerRecipeBook serverRecipeBook = serverPlayer.getRecipeBook();
        for (Object2BooleanMap.Entry entry : this.recipes.object2BooleanEntrySet()) {
            if (serverRecipeBook.contains((ResourceKey)entry.getKey()) == entry.getBooleanValue()) continue;
            return false;
        }
        if (!this.advancements.isEmpty()) {
            PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
            ServerAdvancementManager serverAdvancementManager = serverPlayer.level().getServer().getAdvancements();
            for (Map.Entry<Identifier, AdvancementPredicate> entry2 : this.advancements.entrySet()) {
                AdvancementHolder advancementHolder = serverAdvancementManager.get(entry2.getKey());
                if (advancementHolder != null && entry2.getValue().test(playerAdvancements.getOrStartProgress(advancementHolder))) continue;
                return false;
            }
        }
        if (this.lookingAt.isPresent()) {
            Vec3 vec32 = serverPlayer.getEyePosition();
            Vec3 vec33 = serverPlayer.getViewVector(1.0f);
            Vec3 vec34 = vec32.add(vec33.x * 100.0, vec33.y * 100.0, vec33.z * 100.0);
            EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(serverPlayer.level(), serverPlayer, vec32, vec34, new AABB(vec32, vec34).inflate(1.0), entity -> !entity.isSpectator(), 0.0f);
            if (entityHitResult == null || entityHitResult.getType() != HitResult.Type.ENTITY) {
                return false;
            }
            Entity entity22 = entityHitResult.getEntity();
            if (!this.lookingAt.get().matches(serverPlayer, entity22) || !serverPlayer.hasLineOfSight(entity22)) {
                return false;
            }
        }
        return !this.input.isPresent() || this.input.get().matches(serverPlayer.getLastClientInput());
    }

    public MapCodec<PlayerPredicate> codec() {
        return EntitySubPredicates.PLAYER;
    }

    record StatMatcher<T>(StatType<T> type, Holder<T> value, MinMaxBounds.Ints range, Supplier<Stat<T>> stat) {
        public static final Codec<StatMatcher<?>> CODEC = BuiltInRegistries.STAT_TYPE.byNameCodec().dispatch(StatMatcher::type, StatMatcher::createTypedCodec);

        public StatMatcher(StatType<T> statType, Holder<T> holder, MinMaxBounds.Ints ints) {
            this(statType, holder, ints, (Supplier<Stat<T>>)Suppliers.memoize(() -> statType.get(holder.value())));
        }

        private static <T> MapCodec<StatMatcher<T>> createTypedCodec(StatType<T> statType) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)statType.getRegistry().holderByNameCodec().fieldOf("stat").forGetter(StatMatcher::value), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("value", (Object)MinMaxBounds.Ints.ANY).forGetter(StatMatcher::range)).apply((Applicative)instance, (holder, ints) -> new StatMatcher(statType, holder, (MinMaxBounds.Ints)ints)));
        }

        public boolean matches(StatsCounter statsCounter) {
            return this.range.matches(statsCounter.getValue(this.stat.get()));
        }
    }

    static interface AdvancementPredicate
    extends Predicate<AdvancementProgress> {
        public static final Codec<AdvancementPredicate> CODEC = Codec.either(AdvancementDonePredicate.CODEC, AdvancementCriterionsPredicate.CODEC).xmap(Either::unwrap, advancementPredicate -> {
            if (advancementPredicate instanceof AdvancementDonePredicate) {
                AdvancementDonePredicate advancementDonePredicate = (AdvancementDonePredicate)advancementPredicate;
                return Either.left((Object)advancementDonePredicate);
            }
            if (advancementPredicate instanceof AdvancementCriterionsPredicate) {
                AdvancementCriterionsPredicate advancementCriterionsPredicate = (AdvancementCriterionsPredicate)advancementPredicate;
                return Either.right((Object)advancementCriterionsPredicate);
            }
            throw new UnsupportedOperationException();
        });
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        private GameTypePredicate gameType = GameTypePredicate.ANY;
        private final ImmutableList.Builder<StatMatcher<?>> stats = ImmutableList.builder();
        private final Object2BooleanMap<ResourceKey<Recipe<?>>> recipes = new Object2BooleanOpenHashMap();
        private final Map<Identifier, AdvancementPredicate> advancements = Maps.newHashMap();
        private Optional<EntityPredicate> lookingAt = Optional.empty();
        private Optional<InputPredicate> input = Optional.empty();

        public static Builder player() {
            return new Builder();
        }

        public Builder setLevel(MinMaxBounds.Ints ints) {
            this.level = ints;
            return this;
        }

        public <T> Builder addStat(StatType<T> statType, Holder.Reference<T> reference, MinMaxBounds.Ints ints) {
            this.stats.add(new StatMatcher<T>(statType, reference, ints));
            return this;
        }

        public Builder addRecipe(ResourceKey<Recipe<?>> resourceKey, boolean bl) {
            this.recipes.put(resourceKey, bl);
            return this;
        }

        public Builder setGameType(GameTypePredicate gameTypePredicate) {
            this.gameType = gameTypePredicate;
            return this;
        }

        public Builder setLookingAt(EntityPredicate.Builder builder) {
            this.lookingAt = Optional.of(builder.build());
            return this;
        }

        public Builder checkAdvancementDone(Identifier identifier, boolean bl) {
            this.advancements.put(identifier, new AdvancementDonePredicate(bl));
            return this;
        }

        public Builder checkAdvancementCriterions(Identifier identifier, Map<String, Boolean> map) {
            this.advancements.put(identifier, new AdvancementCriterionsPredicate((Object2BooleanMap<String>)new Object2BooleanOpenHashMap(map)));
            return this;
        }

        public Builder hasInput(InputPredicate inputPredicate) {
            this.input = Optional.of(inputPredicate);
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.level, this.gameType, (List<StatMatcher<?>>)this.stats.build(), this.recipes, this.advancements, this.lookingAt, this.input);
        }
    }

    record AdvancementCriterionsPredicate(Object2BooleanMap<String> criterions) implements AdvancementPredicate
    {
        public static final Codec<AdvancementCriterionsPredicate> CODEC = ExtraCodecs.object2BooleanMap(Codec.STRING).xmap(AdvancementCriterionsPredicate::new, AdvancementCriterionsPredicate::criterions);

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            for (Object2BooleanMap.Entry entry : this.criterions.object2BooleanEntrySet()) {
                CriterionProgress criterionProgress = advancementProgress.getCriterion((String)entry.getKey());
                if (criterionProgress != null && criterionProgress.isDone() == entry.getBooleanValue()) continue;
                return false;
            }
            return true;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((AdvancementProgress)object);
        }
    }

    record AdvancementDonePredicate(boolean state) implements AdvancementPredicate
    {
        public static final Codec<AdvancementDonePredicate> CODEC = Codec.BOOL.xmap(AdvancementDonePredicate::new, AdvancementDonePredicate::state);

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            return advancementProgress.isDone() == this.state;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((AdvancementProgress)object);
        }
    }
}


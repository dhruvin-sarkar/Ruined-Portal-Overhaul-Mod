/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyBonusCount
extends LootItemConditionalFunction {
    private static final Map<Identifier, FormulaType> FORMULAS = Stream.of(BinomialWithBonusCount.TYPE, OreDrops.TYPE, UniformBonusCount.TYPE).collect(Collectors.toMap(FormulaType::id, Function.identity()));
    private static final Codec<FormulaType> FORMULA_TYPE_CODEC = Identifier.CODEC.comapFlatMap(identifier -> {
        FormulaType formulaType = FORMULAS.get(identifier);
        if (formulaType != null) {
            return DataResult.success((Object)((Object)formulaType));
        }
        return DataResult.error(() -> "No formula type with id: '" + String.valueOf(identifier) + "'");
    }, FormulaType::id);
    private static final MapCodec<Formula> FORMULA_CODEC = ExtraCodecs.dispatchOptionalValue("formula", "parameters", FORMULA_TYPE_CODEC, Formula::getType, FormulaType::codec);
    public static final MapCodec<ApplyBonusCount> CODEC = RecordCodecBuilder.mapCodec(instance -> ApplyBonusCount.commonFields(instance).and(instance.group((App)Enchantment.CODEC.fieldOf("enchantment").forGetter(applyBonusCount -> applyBonusCount.enchantment), (App)FORMULA_CODEC.forGetter(applyBonusCount -> applyBonusCount.formula))).apply((Applicative)instance, ApplyBonusCount::new));
    private final Holder<Enchantment> enchantment;
    private final Formula formula;

    private ApplyBonusCount(List<LootItemCondition> list, Holder<Enchantment> holder, Formula formula) {
        super(list);
        this.enchantment = holder;
        this.formula = formula;
    }

    public LootItemFunctionType<ApplyBonusCount> getType() {
        return LootItemFunctions.APPLY_BONUS;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        ItemStack itemStack2 = lootContext.getOptionalParameter(LootContextParams.TOOL);
        if (itemStack2 != null) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemStack2);
            int j = this.formula.calculateNewCount(lootContext.getRandom(), itemStack.getCount(), i);
            itemStack.setCount(j);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> addBonusBinomialDistributionCount(Holder<Enchantment> holder, float f, int i) {
        return ApplyBonusCount.simpleBuilder(list -> new ApplyBonusCount((List<LootItemCondition>)list, holder, new BinomialWithBonusCount(i, f)));
    }

    public static LootItemConditionalFunction.Builder<?> addOreBonusCount(Holder<Enchantment> holder) {
        return ApplyBonusCount.simpleBuilder(list -> new ApplyBonusCount((List<LootItemCondition>)list, holder, OreDrops.INSTANCE));
    }

    public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Holder<Enchantment> holder) {
        return ApplyBonusCount.simpleBuilder(list -> new ApplyBonusCount((List<LootItemCondition>)list, holder, new UniformBonusCount(1)));
    }

    public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Holder<Enchantment> holder, int i) {
        return ApplyBonusCount.simpleBuilder(list -> new ApplyBonusCount((List<LootItemCondition>)list, holder, new UniformBonusCount(i)));
    }

    static interface Formula {
        public int calculateNewCount(RandomSource var1, int var2, int var3);

        public FormulaType getType();
    }

    record UniformBonusCount(int bonusMultiplier) implements Formula
    {
        public static final Codec<UniformBonusCount> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("bonusMultiplier").forGetter(UniformBonusCount::bonusMultiplier)).apply((Applicative)instance, UniformBonusCount::new));
        public static final FormulaType TYPE = new FormulaType(Identifier.withDefaultNamespace("uniform_bonus_count"), CODEC);

        @Override
        public int calculateNewCount(RandomSource randomSource, int i, int j) {
            return i + randomSource.nextInt(this.bonusMultiplier * j + 1);
        }

        @Override
        public FormulaType getType() {
            return TYPE;
        }
    }

    record OreDrops() implements Formula
    {
        public static final OreDrops INSTANCE = new OreDrops();
        public static final Codec<OreDrops> CODEC = MapCodec.unitCodec((Object)INSTANCE);
        public static final FormulaType TYPE = new FormulaType(Identifier.withDefaultNamespace("ore_drops"), CODEC);

        @Override
        public int calculateNewCount(RandomSource randomSource, int i, int j) {
            if (j > 0) {
                int k = randomSource.nextInt(j + 2) - 1;
                if (k < 0) {
                    k = 0;
                }
                return i * (k + 1);
            }
            return i;
        }

        @Override
        public FormulaType getType() {
            return TYPE;
        }
    }

    record BinomialWithBonusCount(int extraRounds, float probability) implements Formula
    {
        private static final Codec<BinomialWithBonusCount> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("extra").forGetter(BinomialWithBonusCount::extraRounds), (App)Codec.FLOAT.fieldOf("probability").forGetter(BinomialWithBonusCount::probability)).apply((Applicative)instance, BinomialWithBonusCount::new));
        public static final FormulaType TYPE = new FormulaType(Identifier.withDefaultNamespace("binomial_with_bonus_count"), CODEC);

        @Override
        public int calculateNewCount(RandomSource randomSource, int i, int j) {
            for (int k = 0; k < j + this.extraRounds; ++k) {
                if (!(randomSource.nextFloat() < this.probability)) continue;
                ++i;
            }
            return i;
        }

        @Override
        public FormulaType getType() {
            return TYPE;
        }
    }

    record FormulaType(Identifier id, Codec<? extends Formula> codec) {
    }
}


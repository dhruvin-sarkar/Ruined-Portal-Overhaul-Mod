/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.MossyCarpetBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public abstract class BlockLootSubProvider
implements LootTableSubProvider {
    protected final HolderLookup.Provider registries;
    protected final Set<Item> explosionResistant;
    protected final FeatureFlagSet enabledFeatures;
    protected final Map<ResourceKey<LootTable>, LootTable.Builder> map;
    protected static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05f, 0.0625f, 0.083333336f, 0.1f};
    private static final float[] NORMAL_LEAVES_STICK_CHANCES = new float[]{0.02f, 0.022222223f, 0.025f, 0.033333335f, 0.1f};

    protected LootItemCondition.Builder hasSilkTouch() {
        return MatchTool.toolMatches(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of((Object)((Object)new EnchantmentPredicate(this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1)))))).build()));
    }

    protected LootItemCondition.Builder doesNotHaveSilkTouch() {
        return this.hasSilkTouch().invert();
    }

    protected LootItemCondition.Builder hasShears() {
        return MatchTool.toolMatches(ItemPredicate.Builder.item().of((HolderGetter<Item>)this.registries.lookupOrThrow(Registries.ITEM), Items.SHEARS));
    }

    private LootItemCondition.Builder hasShearsOrSilkTouch() {
        return this.hasShears().or(this.hasSilkTouch());
    }

    private LootItemCondition.Builder doesNotHaveShearsOrSilkTouch() {
        return this.hasShearsOrSilkTouch().invert();
    }

    protected BlockLootSubProvider(Set<Item> set, FeatureFlagSet featureFlagSet, HolderLookup.Provider provider) {
        this(set, featureFlagSet, new HashMap<ResourceKey<LootTable>, LootTable.Builder>(), provider);
    }

    protected BlockLootSubProvider(Set<Item> set, FeatureFlagSet featureFlagSet, Map<ResourceKey<LootTable>, LootTable.Builder> map, HolderLookup.Provider provider) {
        this.explosionResistant = set;
        this.enabledFeatures = featureFlagSet;
        this.map = map;
        this.registries = provider;
    }

    protected <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike itemLike, FunctionUserBuilder<T> functionUserBuilder) {
        if (!this.explosionResistant.contains(itemLike.asItem())) {
            return functionUserBuilder.apply(ApplyExplosionDecay.explosionDecay());
        }
        return functionUserBuilder.unwrap();
    }

    protected <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike itemLike, ConditionUserBuilder<T> conditionUserBuilder) {
        if (!this.explosionResistant.contains(itemLike.asItem())) {
            return conditionUserBuilder.when(ExplosionCondition.survivesExplosion());
        }
        return conditionUserBuilder.unwrap();
    }

    public LootTable.Builder createSingleItemTable(ItemLike itemLike) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(itemLike, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem(itemLike))));
    }

    private static LootTable.Builder createSelfDropDispatchTable(Block block, LootItemCondition.Builder builder, LootPoolEntryContainer.Builder<?> builder2) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).when(builder)).otherwise(builder2)));
    }

    protected LootTable.Builder createSilkTouchDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return BlockLootSubProvider.createSelfDropDispatchTable(block, this.hasSilkTouch(), builder);
    }

    protected LootTable.Builder createShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return BlockLootSubProvider.createSelfDropDispatchTable(block, this.hasShears(), builder);
    }

    protected LootTable.Builder createSilkTouchOrShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return BlockLootSubProvider.createSelfDropDispatchTable(block, this.hasShearsOrSilkTouch(), builder);
    }

    protected LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike itemLike) {
        return this.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)this.applyExplosionCondition(block, LootItem.lootTableItem(itemLike)));
    }

    protected LootTable.Builder createSingleItemTable(ItemLike itemLike, NumberProvider numberProvider) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder)this.applyExplosionDecay(itemLike, LootItem.lootTableItem(itemLike).apply(SetItemCountFunction.setCount(numberProvider)))));
    }

    protected LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike itemLike, NumberProvider numberProvider) {
        return this.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, LootItem.lootTableItem(itemLike).apply(SetItemCountFunction.setCount(numberProvider))));
    }

    private LootTable.Builder createSilkTouchOnlyTable(ItemLike itemLike) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(this.hasSilkTouch()).setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem(itemLike)));
    }

    private LootTable.Builder createPotFlowerItemTable(ItemLike itemLike) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem(Blocks.FLOWER_POT)))).withPool(this.applyExplosionCondition(itemLike, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem(itemLike))));
    }

    protected LootTable.Builder createSlabItemTable(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, LootItem.lootTableItem(block).apply((LootItemFunction.Builder)((Object)SetItemCountFunction.setCount(ConstantValue.exactly(2.0f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE))))))));
    }

    protected <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block block, Property<T> property, T comparable) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(block).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, comparable))))));
    }

    protected LootTable.Builder createNameableBlockEntityTable(Block block) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME))))));
    }

    protected LootTable.Builder createShulkerBoxDrop(Block block) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME).include(DataComponents.CONTAINER).include(DataComponents.LOCK).include(DataComponents.CONTAINER_LOOT))))));
    }

    protected LootTable.Builder createCopperOreDrops(Block block) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.RAW_COPPER).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 5.0f)))).apply(ApplyBonusCount.addOreBonusCount(registryLookup.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder createLapisOreDrops(Block block) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.LAPIS_LAZULI).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0f, 9.0f)))).apply(ApplyBonusCount.addOreBonusCount(registryLookup.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder createRedstoneOreDrops(Block block) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.REDSTONE).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0f, 5.0f)))).apply(ApplyBonusCount.addUniformBonusCount(registryLookup.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder createBannerDrop(Block block) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME).include(DataComponents.ITEM_NAME).include(DataComponents.TOOLTIP_DISPLAY).include(DataComponents.BANNER_PATTERNS).include(DataComponents.RARITY))))));
    }

    protected LootTable.Builder createBeeNestDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(this.hasSilkTouch()).setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.BEES))).apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL)))));
    }

    protected LootTable.Builder createBeeHiveDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(((LootPoolEntryContainer.Builder)((Object)((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).when(this.hasSilkTouch())).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.BEES))).apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL)))).otherwise(LootItem.lootTableItem(block))));
    }

    protected LootTable.Builder createCaveVinesDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.GLOW_BERRIES)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CaveVines.BERRIES, true))));
    }

    protected LootTable.Builder createCopperGolemStatueBlock(Block block) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME))).apply(CopyBlockState.copyState(block).copy(CopperGolemStatueBlock.POSE))))));
    }

    protected LootTable.Builder createOreDrop(Block block, Item item) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, LootItem.lootTableItem(item).apply(ApplyBonusCount.addOreBonusCount(registryLookup.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder createMushroomBlockDrop(Block block, ItemLike itemLike) {
        return this.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(itemLike).apply(SetItemCountFunction.setCount(UniformGenerator.between(-6.0f, 2.0f)))).apply(LimitCount.limitCount(IntRange.lowerBound(0)))));
    }

    protected LootTable.Builder createGrassDrops(Block block) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createShearsDispatchTable(block, (LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.WHEAT_SEEDS).when(LootItemRandomChanceCondition.randomChance(0.125f))).apply(ApplyBonusCount.addUniformBonusCount(registryLookup.getOrThrow(Enchantments.FORTUNE), 2))));
    }

    public LootTable.Builder createStemDrops(Block block, Item item) {
        return LootTable.lootTable().withPool(this.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder)LootItem.lootTableItem(item).apply(StemBlock.AGE.getPossibleValues(), integer -> SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, (float)(integer + 1) / 15.0f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, integer.intValue())))))));
    }

    public LootTable.Builder createAttachedStemDrops(Block block, Item item) {
        return LootTable.lootTable().withPool(this.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336f)))))));
    }

    protected LootTable.Builder createShearsOnlyDrop(ItemLike itemLike) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).when(this.hasShears()).add(LootItem.lootTableItem(itemLike)));
    }

    protected LootTable.Builder createShearsOrSilkTouchOnlyDrop(ItemLike itemLike) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).when(this.hasShearsOrSilkTouch()).add(LootItem.lootTableItem(itemLike)));
    }

    protected LootTable.Builder createMultifaceBlockDrops(Block block, LootItemCondition.Builder builder) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).when(builder)).apply(Direction.values(), direction -> SetItemCountFunction.setCount(ConstantValue.exactly(1.0f), true).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MultifaceBlock.getFaceProperty(direction), true))))).apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0f), true)))));
    }

    protected LootTable.Builder createMultifaceBlockDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).apply(Direction.values(), direction -> SetItemCountFunction.setCount(ConstantValue.exactly(1.0f), true).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MultifaceBlock.getFaceProperty(direction), true))))).apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0f), true)))));
    }

    protected LootTable.Builder createMossyCarpetBlockDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, (FunctionUserBuilder)((Object)LootItem.lootTableItem(block).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MossyCarpetBlock.BASE, true)))))));
    }

    protected LootTable.Builder createLeavesDrops(Block block, Block block2, float ... fs) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchOrShearsDispatchTable(block, (LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(block, LootItem.lootTableItem(block2))).when(BonusLevelTableCondition.bonusLevelFlatChance(registryLookup.getOrThrow(Enchantments.FORTUNE), fs))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).when(this.doesNotHaveShearsOrSilkTouch()).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)this.applyExplosionDecay(block, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f))))).when(BonusLevelTableCondition.bonusLevelFlatChance(registryLookup.getOrThrow(Enchantments.FORTUNE), NORMAL_LEAVES_STICK_CHANCES))));
    }

    protected LootTable.Builder createOakLeavesDrops(Block block, Block block2, float ... fs) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createLeavesDrops(block, block2, fs).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).when(this.doesNotHaveShearsOrSilkTouch()).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(block, LootItem.lootTableItem(Items.APPLE))).when(BonusLevelTableCondition.bonusLevelFlatChance(registryLookup.getOrThrow(Enchantments.FORTUNE), 0.005f, 0.0055555557f, 0.00625f, 0.008333334f, 0.025f))));
    }

    protected LootTable.Builder createMangroveLeavesDrops(Block block) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchOrShearsDispatchTable(block, (LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)this.applyExplosionDecay(Blocks.MANGROVE_LEAVES, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f))))).when(BonusLevelTableCondition.bonusLevelFlatChance(registryLookup.getOrThrow(Enchantments.FORTUNE), NORMAL_LEAVES_STICK_CHANCES)));
    }

    protected LootTable.Builder createCropDrops(Block block, Item item, Item item2, LootItemCondition.Builder builder) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.applyExplosionDecay(block, LootTable.lootTable().withPool(LootPool.lootPool().add(((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(item).when(builder)).otherwise(LootItem.lootTableItem(item2)))).withPool(LootPool.lootPool().when(builder).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(item2).apply(ApplyBonusCount.addBonusBinomialDistributionCount(registryLookup.getOrThrow(Enchantments.FORTUNE), 0.5714286f, 3))))));
    }

    protected LootTable.Builder createDoublePlantShearsDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(this.hasShears()).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(block).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0f))))));
    }

    protected LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block block2) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.BLOCK);
        AlternativesEntry.Builder builder = ((LootPoolSingletonContainer.Builder)((LootPoolEntryContainer.Builder)((Object)LootItem.lootTableItem(block2).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0f))))).when(this.hasShears())).otherwise((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(block, LootItem.lootTableItem(Items.WHEAT_SEEDS))).when(LootItemRandomChanceCondition.randomChance(0.125f)));
        return LootTable.lootTable().withPool(LootPool.lootPool().add(builder).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)registryLookup, block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))), new BlockPos(0, 1, 0)))).withPool(LootPool.lootPool().add(builder).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)registryLookup, block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))), new BlockPos(0, -1, 0))));
    }

    protected LootTable.Builder createCandleDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, (FunctionUserBuilder)LootItem.lootTableItem(block).apply(List.of((Object)2, (Object)3, (Object)4), integer -> SetItemCountFunction.setCount(ConstantValue.exactly(integer.intValue())).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.CANDLES, integer.intValue())))))));
    }

    public LootTable.Builder createSegmentedBlockDrops(Block block) {
        if (block instanceof SegmentableBlock) {
            SegmentableBlock segmentableBlock = (SegmentableBlock)((Object)block);
            return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder)this.applyExplosionDecay(block, (FunctionUserBuilder)LootItem.lootTableItem(block).apply(IntStream.rangeClosed(1, 4).boxed().toList(), integer -> SetItemCountFunction.setCount(ConstantValue.exactly(integer.intValue())).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(segmentableBlock.getSegmentAmountProperty(), integer.intValue())))))));
        }
        return BlockLootSubProvider.noDrop();
    }

    protected static LootTable.Builder createCandleCakeDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem(block)));
    }

    public static LootTable.Builder noDrop() {
        return LootTable.lootTable();
    }

    protected abstract void generate();

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        this.generate();
        HashSet set = new HashSet();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!block.isEnabled(this.enabledFeatures)) continue;
            block.getLootTable().ifPresent(resourceKey -> {
                if (set.add(resourceKey)) {
                    LootTable.Builder builder = this.map.remove(resourceKey);
                    if (builder == null) {
                        throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", resourceKey.identifier(), BuiltInRegistries.BLOCK.getKey(block)));
                    }
                    biConsumer.accept((ResourceKey<LootTable>)resourceKey, builder);
                }
            });
        }
        if (!this.map.isEmpty()) {
            throw new IllegalStateException("Created block loot tables for non-blocks: " + String.valueOf(this.map.keySet()));
        }
    }

    protected void addNetherVinesDropTable(Block block, Block block2) {
        HolderGetter registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        LootTable.Builder builder = this.createSilkTouchOrShearsDispatchTable(block, (LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(block).when(BonusLevelTableCondition.bonusLevelFlatChance(registryLookup.getOrThrow(Enchantments.FORTUNE), 0.33f, 0.55f, 0.77f, 1.0f)));
        this.add(block, builder);
        this.add(block2, builder);
    }

    protected LootTable.Builder createDoorTable(Block block) {
        return this.createSinglePropConditionTable(block, DoorBlock.HALF, DoubleBlockHalf.LOWER);
    }

    protected void dropPottedContents(Block block2) {
        this.add(block2, (Block block) -> this.createPotFlowerItemTable(((FlowerPotBlock)block).getPotted()));
    }

    protected void otherWhenSilkTouch(Block block, Block block2) {
        this.add(block, this.createSilkTouchOnlyTable(block2));
    }

    protected void dropOther(Block block, ItemLike itemLike) {
        this.add(block, this.createSingleItemTable(itemLike));
    }

    protected void dropWhenSilkTouch(Block block) {
        this.otherWhenSilkTouch(block, block);
    }

    protected void dropSelf(Block block) {
        this.dropOther(block, block);
    }

    protected void add(Block block, Function<Block, LootTable.Builder> function) {
        this.add(block, function.apply(block));
    }

    protected void add(Block block, LootTable.Builder builder) {
        this.map.put(block.getLootTable().orElseThrow(() -> new IllegalStateException("Block " + String.valueOf(block) + " does not have loot table")), builder);
    }
}


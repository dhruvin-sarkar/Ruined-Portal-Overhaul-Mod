/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  joptsimple.AbstractOptionSpec
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 */
package net.minecraft.data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.info.BiomeParametersDumpReport;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.DatapackStructureReport;
import net.minecraft.data.info.ItemListReport;
import net.minecraft.data.info.PacketReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.loot.packs.TradeRebalanceLootTableProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.TradeRebalanceRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.DialogTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FlatLevelGeneratorPresetTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.TimelineTagsProvider;
import net.minecraft.data.tags.TradeRebalanceEnchantmentTagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.data.tags.VanillaEnchantmentTagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.jsonrpc.dataprovider.JsonRpcApiSchema;
import net.minecraft.util.Util;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class Main {
    @SuppressForbidden(reason="System.out needed before bootstrap")
    @DontObfuscate
    public static void main(String[] strings) throws IOException {
        SharedConstants.tryDetectVersion();
        OptionParser optionParser = new OptionParser();
        AbstractOptionSpec optionSpec = optionParser.accepts("help", "Show the help menu").forHelp();
        OptionSpecBuilder optionSpec2 = optionParser.accepts("server", "Include server generators");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("dev", "Include development tools");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("reports", "Include data reports");
        optionParser.accepts("validate", "Validate inputs");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("all", "Include all generators");
        ArgumentAcceptingOptionSpec optionSpec6 = optionParser.accepts("output", "Output folder").withRequiredArg().defaultsTo((Object)"generated", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec optionSpec7 = optionParser.accepts("input", "Input folder").withRequiredArg();
        OptionSet optionSet = optionParser.parse(strings);
        if (optionSet.has((OptionSpec)optionSpec) || !optionSet.hasOptions()) {
            optionParser.printHelpOn((OutputStream)System.out);
            return;
        }
        Path path = Paths.get((String)optionSpec6.value(optionSet), new String[0]);
        boolean bl = optionSet.has((OptionSpec)optionSpec5);
        boolean bl2 = bl || optionSet.has((OptionSpec)optionSpec2);
        boolean bl3 = bl || optionSet.has((OptionSpec)optionSpec3);
        boolean bl4 = bl || optionSet.has((OptionSpec)optionSpec4);
        List collection = optionSet.valuesOf((OptionSpec)optionSpec7).stream().map(string -> Paths.get(string, new String[0])).toList();
        DataGenerator dataGenerator = new DataGenerator(path, SharedConstants.getCurrentVersion(), true);
        Main.addServerProviders(dataGenerator, collection, bl2, bl3, bl4);
        dataGenerator.run();
        Util.shutdownExecutors();
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> biFunction, CompletableFuture<HolderLookup.Provider> completableFuture) {
        return packOutput -> (DataProvider)biFunction.apply(packOutput, completableFuture);
    }

    public static void addServerProviders(DataGenerator dataGenerator, Collection<Path> collection, boolean bl, boolean bl2, boolean bl3) {
        DataGenerator.PackGenerator packGenerator = dataGenerator.getVanillaPack(bl);
        packGenerator.addProvider(packOutput -> new SnbtToNbt(packOutput, collection).addFilter(new StructureUpdater()));
        CompletableFuture<HolderLookup.Provider> completableFuture = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
        DataGenerator.PackGenerator packGenerator2 = dataGenerator.getVanillaPack(bl);
        packGenerator2.addProvider(Main.bindRegistries(RegistriesDatapackGenerator::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(VanillaAdvancementProvider::create, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(VanillaLootTableProvider::create, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(VanillaRecipeProvider.Runner::new, completableFuture));
        TagsProvider tagsProvider = packGenerator2.addProvider(Main.bindRegistries(VanillaBlockTagsProvider::new, completableFuture));
        TagsProvider tagsProvider2 = packGenerator2.addProvider(Main.bindRegistries(VanillaItemTagsProvider::new, completableFuture));
        TagsProvider tagsProvider3 = packGenerator2.addProvider(Main.bindRegistries(BiomeTagsProvider::new, completableFuture));
        TagsProvider tagsProvider4 = packGenerator2.addProvider(Main.bindRegistries(BannerPatternTagsProvider::new, completableFuture));
        TagsProvider tagsProvider5 = packGenerator2.addProvider(Main.bindRegistries(StructureTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(DamageTypeTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(DialogTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(EntityTypeTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(FlatLevelGeneratorPresetTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(FluidTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(GameEventTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(InstrumentTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(PaintingVariantTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(PoiTypeTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(WorldPresetTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(VanillaEnchantmentTagsProvider::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(TimelineTagsProvider::new, completableFuture));
        packGenerator2 = dataGenerator.getVanillaPack(bl2);
        packGenerator2.addProvider(packOutput -> new NbtToSnbt(packOutput, collection));
        packGenerator2 = dataGenerator.getVanillaPack(bl3);
        packGenerator2.addProvider(Main.bindRegistries(BiomeParametersDumpReport::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(ItemListReport::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(BlockListReport::new, completableFuture));
        packGenerator2.addProvider(Main.bindRegistries(CommandsReport::new, completableFuture));
        packGenerator2.addProvider(RegistryDumpReport::new);
        packGenerator2.addProvider(PacketReport::new);
        packGenerator2.addProvider(DatapackStructureReport::new);
        packGenerator2.addProvider(JsonRpcApiSchema::new);
        CompletableFuture<RegistrySetBuilder.PatchedRegistries> completableFuture2 = TradeRebalanceRegistries.createLookup(completableFuture);
        CompletionStage completableFuture3 = completableFuture2.thenApply(RegistrySetBuilder.PatchedRegistries::patches);
        DataGenerator.PackGenerator packGenerator3 = dataGenerator.getBuiltinDatapack(bl, "trade_rebalance");
        packGenerator3.addProvider(Main.bindRegistries(RegistriesDatapackGenerator::new, (CompletableFuture<HolderLookup.Provider>)completableFuture3));
        packGenerator3.addProvider(packOutput -> PackMetadataGenerator.forFeaturePack(packOutput, Component.translatable("dataPack.trade_rebalance.description"), FeatureFlagSet.of(FeatureFlags.TRADE_REBALANCE)));
        packGenerator3.addProvider(Main.bindRegistries(TradeRebalanceLootTableProvider::create, completableFuture));
        packGenerator3.addProvider(Main.bindRegistries(TradeRebalanceEnchantmentTagsProvider::new, completableFuture));
        packGenerator2 = dataGenerator.getBuiltinDatapack(bl, "redstone_experiments");
        packGenerator2.addProvider(packOutput -> PackMetadataGenerator.forFeaturePack(packOutput, Component.translatable("dataPack.redstone_experiments.description"), FeatureFlagSet.of(FeatureFlags.REDSTONE_EXPERIMENTS)));
        packGenerator2 = dataGenerator.getBuiltinDatapack(bl, "minecart_improvements");
        packGenerator2.addProvider(packOutput -> PackMetadataGenerator.forFeaturePack(packOutput, Component.translatable("dataPack.minecart_improvements.description"), FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS)));
    }
}


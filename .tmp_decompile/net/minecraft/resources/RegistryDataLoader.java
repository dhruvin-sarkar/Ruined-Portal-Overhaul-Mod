/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.slf4j.Logger
 */
package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.timeline.Timeline;
import org.slf4j.Logger;

public class RegistryDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Comparator<ResourceKey<?>> ERROR_KEY_COMPARATOR = Comparator.comparing(ResourceKey::registry).thenComparing(ResourceKey::identifier);
    private static final RegistrationInfo NETWORK_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());
    private static final Function<Optional<KnownPack>, RegistrationInfo> REGISTRATION_INFO_CACHE = Util.memoize(optional -> {
        Lifecycle lifecycle = optional.map(KnownPack::isVanilla).map(boolean_ -> Lifecycle.stable()).orElse(Lifecycle.experimental());
        return new RegistrationInfo((Optional<KnownPack>)optional, lifecycle);
    });
    public static final List<RegistryData<?>> WORLDGEN_REGISTRIES = List.of((Object[])new RegistryData[]{new RegistryData<DimensionType>(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC), new RegistryData<Biome>(Registries.BIOME, Biome.DIRECT_CODEC), new RegistryData<ChatType>(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC), new RegistryData(Registries.CONFIGURED_CARVER, ConfiguredWorldCarver.DIRECT_CODEC), new RegistryData(Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC), new RegistryData<PlacedFeature>(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC), new RegistryData<Structure>(Registries.STRUCTURE, Structure.DIRECT_CODEC), new RegistryData<StructureSet>(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC), new RegistryData<StructureProcessorList>(Registries.PROCESSOR_LIST, StructureProcessorType.DIRECT_CODEC), new RegistryData<StructureTemplatePool>(Registries.TEMPLATE_POOL, StructureTemplatePool.DIRECT_CODEC), new RegistryData<NoiseGeneratorSettings>(Registries.NOISE_SETTINGS, NoiseGeneratorSettings.DIRECT_CODEC), new RegistryData<NormalNoise.NoiseParameters>(Registries.NOISE, NormalNoise.NoiseParameters.DIRECT_CODEC), new RegistryData<DensityFunction>(Registries.DENSITY_FUNCTION, DensityFunction.DIRECT_CODEC), new RegistryData<WorldPreset>(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC), new RegistryData<FlatLevelGeneratorPreset>(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.DIRECT_CODEC), new RegistryData<TrimPattern>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC), new RegistryData<TrimMaterial>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC), new RegistryData<TrialSpawnerConfig>(Registries.TRIAL_SPAWNER_CONFIG, TrialSpawnerConfig.DIRECT_CODEC), new RegistryData<WolfVariant>(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC, true), new RegistryData<WolfSoundVariant>(Registries.WOLF_SOUND_VARIANT, WolfSoundVariant.DIRECT_CODEC, true), new RegistryData<PigVariant>(Registries.PIG_VARIANT, PigVariant.DIRECT_CODEC, true), new RegistryData<FrogVariant>(Registries.FROG_VARIANT, FrogVariant.DIRECT_CODEC, true), new RegistryData<CatVariant>(Registries.CAT_VARIANT, CatVariant.DIRECT_CODEC, true), new RegistryData<CowVariant>(Registries.COW_VARIANT, CowVariant.DIRECT_CODEC, true), new RegistryData<ChickenVariant>(Registries.CHICKEN_VARIANT, ChickenVariant.DIRECT_CODEC, true), new RegistryData<ZombieNautilusVariant>(Registries.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.DIRECT_CODEC, true), new RegistryData<PaintingVariant>(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true), new RegistryData<DamageType>(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC), new RegistryData<MultiNoiseBiomeSourceParameterList>(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.DIRECT_CODEC), new RegistryData<BannerPattern>(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC), new RegistryData<Enchantment>(Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC), new RegistryData<EnchantmentProvider>(Registries.ENCHANTMENT_PROVIDER, EnchantmentProvider.DIRECT_CODEC), new RegistryData<JukeboxSong>(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC), new RegistryData<Instrument>(Registries.INSTRUMENT, Instrument.DIRECT_CODEC), new RegistryData<TestEnvironmentDefinition>(Registries.TEST_ENVIRONMENT, TestEnvironmentDefinition.DIRECT_CODEC), new RegistryData<GameTestInstance>(Registries.TEST_INSTANCE, GameTestInstance.DIRECT_CODEC), new RegistryData<Dialog>(Registries.DIALOG, Dialog.DIRECT_CODEC), new RegistryData<Timeline>(Registries.TIMELINE, Timeline.DIRECT_CODEC)});
    public static final List<RegistryData<?>> DIMENSION_REGISTRIES = List.of(new RegistryData<LevelStem>(Registries.LEVEL_STEM, LevelStem.CODEC));
    public static final List<RegistryData<?>> SYNCHRONIZED_REGISTRIES = List.of((Object[])new RegistryData[]{new RegistryData<Biome>(Registries.BIOME, Biome.NETWORK_CODEC), new RegistryData<ChatType>(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC), new RegistryData<TrimPattern>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC), new RegistryData<TrimMaterial>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC), new RegistryData<WolfVariant>(Registries.WOLF_VARIANT, WolfVariant.NETWORK_CODEC, true), new RegistryData<WolfSoundVariant>(Registries.WOLF_SOUND_VARIANT, WolfSoundVariant.NETWORK_CODEC, true), new RegistryData<PigVariant>(Registries.PIG_VARIANT, PigVariant.NETWORK_CODEC, true), new RegistryData<FrogVariant>(Registries.FROG_VARIANT, FrogVariant.NETWORK_CODEC, true), new RegistryData<CatVariant>(Registries.CAT_VARIANT, CatVariant.NETWORK_CODEC, true), new RegistryData<CowVariant>(Registries.COW_VARIANT, CowVariant.NETWORK_CODEC, true), new RegistryData<ChickenVariant>(Registries.CHICKEN_VARIANT, ChickenVariant.NETWORK_CODEC, true), new RegistryData<ZombieNautilusVariant>(Registries.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.NETWORK_CODEC, true), new RegistryData<PaintingVariant>(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true), new RegistryData<DimensionType>(Registries.DIMENSION_TYPE, DimensionType.NETWORK_CODEC), new RegistryData<DamageType>(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC), new RegistryData<BannerPattern>(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC), new RegistryData<Enchantment>(Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC), new RegistryData<JukeboxSong>(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC), new RegistryData<Instrument>(Registries.INSTRUMENT, Instrument.DIRECT_CODEC), new RegistryData<TestEnvironmentDefinition>(Registries.TEST_ENVIRONMENT, TestEnvironmentDefinition.DIRECT_CODEC), new RegistryData<GameTestInstance>(Registries.TEST_INSTANCE, GameTestInstance.DIRECT_CODEC), new RegistryData<Dialog>(Registries.DIALOG, Dialog.DIRECT_CODEC), new RegistryData<Timeline>(Registries.TIMELINE, Timeline.NETWORK_CODEC)});

    public static RegistryAccess.Frozen load(ResourceManager resourceManager, List<HolderLookup.RegistryLookup<?>> list, List<RegistryData<?>> list2) {
        return RegistryDataLoader.load((Loader<?> loader, RegistryOps.RegistryInfoLookup registryInfoLookup) -> loader.loadFromResources(resourceManager, registryInfoLookup), list, list2);
    }

    public static RegistryAccess.Frozen load(Map<ResourceKey<? extends Registry<?>>, NetworkedRegistryData> map, ResourceProvider resourceProvider, List<HolderLookup.RegistryLookup<?>> list, List<RegistryData<?>> list2) {
        return RegistryDataLoader.load((Loader<?> loader, RegistryOps.RegistryInfoLookup registryInfoLookup) -> loader.loadFromNetwork(map, resourceProvider, registryInfoLookup), list, list2);
    }

    private static RegistryAccess.Frozen load(LoadingFunction loadingFunction, List<HolderLookup.RegistryLookup<?>> list, List<RegistryData<?>> list2) {
        HashMap map = new HashMap();
        List list3 = (List)list2.stream().map(registryData -> registryData.create(Lifecycle.stable(), map)).collect(Collectors.toUnmodifiableList());
        RegistryOps.RegistryInfoLookup registryInfoLookup = RegistryDataLoader.createContext(list, list3);
        list3.forEach(loader -> loadingFunction.apply((Loader<?>)((Object)loader), registryInfoLookup));
        list3.forEach(loader -> {
            WritableRegistry registry = loader.registry();
            try {
                registry.freeze();
            }
            catch (Exception exception) {
                map.put(registry.key(), exception);
            }
            if (loader.data.requiredNonEmpty && registry.size() == 0) {
                map.put(registry.key(), new IllegalStateException("Registry must be non-empty: " + String.valueOf(registry.key().identifier())));
            }
        });
        if (!map.isEmpty()) {
            throw RegistryDataLoader.logErrors(map);
        }
        return new RegistryAccess.ImmutableRegistryAccess(list3.stream().map(Loader::registry).toList()).freeze();
    }

    private static RegistryOps.RegistryInfoLookup createContext(List<HolderLookup.RegistryLookup<?>> list, List<Loader<?>> list2) {
        final HashMap map = new HashMap();
        list.forEach(registryLookup -> map.put(registryLookup.key(), RegistryDataLoader.createInfoForContextRegistry(registryLookup)));
        list2.forEach(loader -> map.put(loader.registry.key(), RegistryDataLoader.createInfoForNewRegistry(loader.registry)));
        return new RegistryOps.RegistryInfoLookup(){

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return Optional.ofNullable((RegistryOps.RegistryInfo)((Object)map.get(resourceKey)));
            }
        };
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> writableRegistry) {
        return new RegistryOps.RegistryInfo<T>(writableRegistry, writableRegistry.createRegistrationLookup(), writableRegistry.registryLifecycle());
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(HolderLookup.RegistryLookup<T> registryLookup) {
        return new RegistryOps.RegistryInfo<T>(registryLookup, registryLookup, registryLookup.registryLifecycle());
    }

    private static ReportedException logErrors(Map<ResourceKey<?>, Exception> map) {
        RegistryDataLoader.printFullDetailsToLog(map);
        return RegistryDataLoader.createReportWithBriefInfo(map);
    }

    private static void printFullDetailsToLog(Map<ResourceKey<?>, Exception> map) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Map<Identifier, Map<Identifier, Exception>> map2 = map.entrySet().stream().collect(Collectors.groupingBy(entry -> ((ResourceKey)entry.getKey()).registry(), Collectors.toMap(entry -> ((ResourceKey)entry.getKey()).identifier(), Map.Entry::getValue)));
        map2.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry2 -> {
            printWriter.printf(Locale.ROOT, "> Errors in registry %s:%n", entry2.getKey());
            ((Map)entry2.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                printWriter.printf(Locale.ROOT, ">> Errors in element %s:%n", entry.getKey());
                ((Exception)entry.getValue()).printStackTrace(printWriter);
            });
        });
        printWriter.flush();
        LOGGER.error("Registry loading errors:\n{}", (Object)stringWriter);
    }

    private static ReportedException createReportWithBriefInfo(Map<ResourceKey<?>, Exception> map) {
        CrashReport crashReport = CrashReport.forThrowable(new IllegalStateException("Failed to load registries due to errors"), "Registry Loading");
        CrashReportCategory crashReportCategory = crashReport.addCategory("Loading info");
        crashReportCategory.setDetail("Errors", () -> {
            StringBuilder stringBuilder = new StringBuilder();
            map.entrySet().stream().sorted(Map.Entry.comparingByKey(ERROR_KEY_COMPARATOR)).forEach(entry -> stringBuilder.append("\n\t\t").append(((ResourceKey)entry.getKey()).registry()).append("/").append(((ResourceKey)entry.getKey()).identifier()).append(": ").append(((Exception)entry.getValue()).getMessage()));
            return stringBuilder.toString();
        });
        return new ReportedException(crashReport);
    }

    private static <E> void loadElementFromResource(WritableRegistry<E> writableRegistry, Decoder<E> decoder, RegistryOps<JsonElement> registryOps, ResourceKey<E> resourceKey, Resource resource, RegistrationInfo registrationInfo) throws IOException {
        try (BufferedReader reader = resource.openAsReader();){
            JsonElement jsonElement = StrictJsonParser.parse(reader);
            DataResult dataResult = decoder.parse(registryOps, (Object)jsonElement);
            Object object = dataResult.getOrThrow();
            writableRegistry.register(resourceKey, object, registrationInfo);
        }
    }

    static <E> void loadContentsFromManager(ResourceManager resourceManager, RegistryOps.RegistryInfoLookup registryInfoLookup, WritableRegistry<E> writableRegistry, Decoder<E> decoder, Map<ResourceKey<?>, Exception> map) {
        FileToIdConverter fileToIdConverter = FileToIdConverter.registry(writableRegistry.key());
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup);
        for (Map.Entry<Identifier, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
            Identifier identifier = entry.getKey();
            ResourceKey resourceKey = ResourceKey.create(writableRegistry.key(), fileToIdConverter.fileToId(identifier));
            Resource resource = entry.getValue();
            RegistrationInfo registrationInfo = REGISTRATION_INFO_CACHE.apply(resource.knownPackInfo());
            try {
                RegistryDataLoader.loadElementFromResource(writableRegistry, decoder, registryOps, resourceKey, resource, registrationInfo);
            }
            catch (Exception exception) {
                map.put(resourceKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", identifier, resource.sourcePackId()), exception));
            }
        }
        TagLoader.loadTagsForRegistry(resourceManager, writableRegistry);
    }

    static <E> void loadContentsFromNetwork(Map<ResourceKey<? extends Registry<?>>, NetworkedRegistryData> map, ResourceProvider resourceProvider, RegistryOps.RegistryInfoLookup registryInfoLookup, WritableRegistry<E> writableRegistry, Decoder<E> decoder, Map<ResourceKey<?>, Exception> map2) {
        NetworkedRegistryData networkedRegistryData = map.get(writableRegistry.key());
        if (networkedRegistryData == null) {
            return;
        }
        RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, registryInfoLookup);
        RegistryOps<JsonElement> registryOps2 = RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup);
        FileToIdConverter fileToIdConverter = FileToIdConverter.registry(writableRegistry.key());
        for (RegistrySynchronization.PackedRegistryEntry packedRegistryEntry : networkedRegistryData.elements) {
            ResourceKey resourceKey = ResourceKey.create(writableRegistry.key(), packedRegistryEntry.id());
            Optional<Tag> optional = packedRegistryEntry.data();
            if (optional.isPresent()) {
                try {
                    DataResult dataResult = decoder.parse(registryOps, (Object)optional.get());
                    Object object = dataResult.getOrThrow();
                    writableRegistry.register(resourceKey, object, NETWORK_REGISTRATION_INFO);
                }
                catch (Exception exception) {
                    map2.put(resourceKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse value %s from server", optional.get()), exception));
                }
                continue;
            }
            Identifier identifier = fileToIdConverter.idToFile(packedRegistryEntry.id());
            try {
                Resource resource = resourceProvider.getResourceOrThrow(identifier);
                RegistryDataLoader.loadElementFromResource(writableRegistry, decoder, registryOps2, resourceKey, resource, NETWORK_REGISTRATION_INFO);
            }
            catch (Exception exception2) {
                map2.put(resourceKey, new IllegalStateException("Failed to parse local data", exception2));
            }
        }
        TagLoader.loadTagsFromNetwork(networkedRegistryData.tags, writableRegistry);
    }

    @FunctionalInterface
    static interface LoadingFunction {
        public void apply(Loader<?> var1, RegistryOps.RegistryInfoLookup var2);
    }

    public static final class NetworkedRegistryData
    extends Record {
        final List<RegistrySynchronization.PackedRegistryEntry> elements;
        final TagNetworkSerialization.NetworkPayload tags;

        public NetworkedRegistryData(List<RegistrySynchronization.PackedRegistryEntry> list, TagNetworkSerialization.NetworkPayload networkPayload) {
            this.elements = list;
            this.tags = networkPayload;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{NetworkedRegistryData.class, "elements;tags", "elements", "tags"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{NetworkedRegistryData.class, "elements;tags", "elements", "tags"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{NetworkedRegistryData.class, "elements;tags", "elements", "tags"}, this, object);
        }

        public List<RegistrySynchronization.PackedRegistryEntry> elements() {
            return this.elements;
        }

        public TagNetworkSerialization.NetworkPayload tags() {
            return this.tags;
        }
    }

    static final class Loader<T>
    extends Record {
        final RegistryData<T> data;
        final WritableRegistry<T> registry;
        private final Map<ResourceKey<?>, Exception> loadingErrors;

        Loader(RegistryData<T> registryData, WritableRegistry<T> writableRegistry, Map<ResourceKey<?>, Exception> map) {
            this.data = registryData;
            this.registry = writableRegistry;
            this.loadingErrors = map;
        }

        public void loadFromResources(ResourceManager resourceManager, RegistryOps.RegistryInfoLookup registryInfoLookup) {
            RegistryDataLoader.loadContentsFromManager(resourceManager, registryInfoLookup, this.registry, this.data.elementCodec, this.loadingErrors);
        }

        public void loadFromNetwork(Map<ResourceKey<? extends Registry<?>>, NetworkedRegistryData> map, ResourceProvider resourceProvider, RegistryOps.RegistryInfoLookup registryInfoLookup) {
            RegistryDataLoader.loadContentsFromNetwork(map, resourceProvider, registryInfoLookup, this.registry, this.data.elementCodec, this.loadingErrors);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Loader.class, "data;registry;loadingErrors", "data", "registry", "loadingErrors"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Loader.class, "data;registry;loadingErrors", "data", "registry", "loadingErrors"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Loader.class, "data;registry;loadingErrors", "data", "registry", "loadingErrors"}, this, object);
        }

        public RegistryData<T> data() {
            return this.data;
        }

        public WritableRegistry<T> registry() {
            return this.registry;
        }

        public Map<ResourceKey<?>, Exception> loadingErrors() {
            return this.loadingErrors;
        }
    }

    public static final class RegistryData<T>
    extends Record {
        private final ResourceKey<? extends Registry<T>> key;
        final Codec<T> elementCodec;
        final boolean requiredNonEmpty;

        RegistryData(ResourceKey<? extends Registry<T>> resourceKey, Codec<T> codec) {
            this(resourceKey, codec, false);
        }

        public RegistryData(ResourceKey<? extends Registry<T>> resourceKey, Codec<T> codec, boolean bl) {
            this.key = resourceKey;
            this.elementCodec = codec;
            this.requiredNonEmpty = bl;
        }

        Loader<T> create(Lifecycle lifecycle, Map<ResourceKey<?>, Exception> map) {
            MappedRegistry writableRegistry = new MappedRegistry(this.key, lifecycle);
            return new Loader(this, writableRegistry, map);
        }

        public void runWithArguments(BiConsumer<ResourceKey<? extends Registry<T>>, Codec<T>> biConsumer) {
            biConsumer.accept(this.key, this.elementCodec);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegistryData.class, "key;elementCodec;requiredNonEmpty", "key", "elementCodec", "requiredNonEmpty"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegistryData.class, "key;elementCodec;requiredNonEmpty", "key", "elementCodec", "requiredNonEmpty"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegistryData.class, "key;elementCodec;requiredNonEmpty", "key", "elementCodec", "requiredNonEmpty"}, this, object);
        }

        public ResourceKey<? extends Registry<T>> key() {
            return this.key;
        }

        public Codec<T> elementCodec() {
            return this.elementCodec;
        }

        public boolean requiredNonEmpty() {
            return this.requiredNonEmpty;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.level.DryFoliageColor;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class Biome {
    public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ClimateSettings.CODEC.forGetter(biome -> biome.climateSettings), (App)EnvironmentAttributeMap.CODEC_ONLY_POSITIONAL.optionalFieldOf("attributes", (Object)EnvironmentAttributeMap.EMPTY).forGetter(biome -> biome.attributes), (App)BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(biome -> biome.specialEffects), (App)BiomeGenerationSettings.CODEC.forGetter(biome -> biome.generationSettings), (App)MobSpawnSettings.CODEC.forGetter(biome -> biome.mobSettings)).apply((Applicative)instance, Biome::new));
    public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ClimateSettings.CODEC.forGetter(biome -> biome.climateSettings), (App)EnvironmentAttributeMap.NETWORK_CODEC.optionalFieldOf("attributes", (Object)EnvironmentAttributeMap.EMPTY).forGetter(biome -> biome.attributes), (App)BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(biome -> biome.specialEffects)).apply((Applicative)instance, (climateSettings, environmentAttributeMap, biomeSpecialEffects) -> new Biome((ClimateSettings)((Object)((Object)climateSettings)), (EnvironmentAttributeMap)environmentAttributeMap, (BiomeSpecialEffects)((Object)((Object)biomeSpecialEffects)), BiomeGenerationSettings.EMPTY, MobSpawnSettings.EMPTY)));
    public static final Codec<Holder<Biome>> CODEC = RegistryFileCodec.create(Registries.BIOME, DIRECT_CODEC);
    public static final Codec<HolderSet<Biome>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.BIOME, DIRECT_CODEC);
    private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise((RandomSource)new WorldgenRandom(new LegacyRandomSource(1234L)), (List<Integer>)ImmutableList.of((Object)0));
    static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise((RandomSource)new WorldgenRandom(new LegacyRandomSource(3456L)), (List<Integer>)ImmutableList.of((Object)-2, (Object)-1, (Object)0));
    @Deprecated(forRemoval=true)
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise((RandomSource)new WorldgenRandom(new LegacyRandomSource(2345L)), (List<Integer>)ImmutableList.of((Object)0));
    private static final int TEMPERATURE_CACHE_SIZE = 1024;
    private final ClimateSettings climateSettings;
    private final BiomeGenerationSettings generationSettings;
    private final MobSpawnSettings mobSettings;
    private final EnvironmentAttributeMap attributes;
    private final BiomeSpecialEffects specialEffects;
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> {
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25f){

            protected void rehash(int i) {
            }
        };
        long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
        return long2FloatLinkedOpenHashMap;
    });

    Biome(ClimateSettings climateSettings, EnvironmentAttributeMap environmentAttributeMap, BiomeSpecialEffects biomeSpecialEffects, BiomeGenerationSettings biomeGenerationSettings, MobSpawnSettings mobSpawnSettings) {
        this.climateSettings = climateSettings;
        this.generationSettings = biomeGenerationSettings;
        this.mobSettings = mobSpawnSettings;
        this.attributes = environmentAttributeMap;
        this.specialEffects = biomeSpecialEffects;
    }

    public MobSpawnSettings getMobSettings() {
        return this.mobSettings;
    }

    public boolean hasPrecipitation() {
        return this.climateSettings.hasPrecipitation();
    }

    public Precipitation getPrecipitationAt(BlockPos blockPos, int i) {
        if (!this.hasPrecipitation()) {
            return Precipitation.NONE;
        }
        return this.coldEnoughToSnow(blockPos, i) ? Precipitation.SNOW : Precipitation.RAIN;
    }

    private float getHeightAdjustedTemperature(BlockPos blockPos, int i) {
        float f = this.climateSettings.temperatureModifier.modifyTemperature(blockPos, this.getBaseTemperature());
        int j = i + 17;
        if (blockPos.getY() > j) {
            float g = (float)(TEMPERATURE_NOISE.getValue((float)blockPos.getX() / 8.0f, (float)blockPos.getZ() / 8.0f, false) * 8.0);
            return f - (g + (float)blockPos.getY() - (float)j) * 0.05f / 40.0f;
        }
        return f;
    }

    @Deprecated
    private float getTemperature(BlockPos blockPos, int i) {
        long l = blockPos.asLong();
        Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = this.temperatureCache.get();
        float f = long2FloatLinkedOpenHashMap.get(l);
        if (!Float.isNaN(f)) {
            return f;
        }
        float g = this.getHeightAdjustedTemperature(blockPos, i);
        if (long2FloatLinkedOpenHashMap.size() == 1024) {
            long2FloatLinkedOpenHashMap.removeFirstFloat();
        }
        long2FloatLinkedOpenHashMap.put(l, g);
        return g;
    }

    public boolean shouldFreeze(LevelReader levelReader, BlockPos blockPos) {
        return this.shouldFreeze(levelReader, blockPos, true);
    }

    public boolean shouldFreeze(LevelReader levelReader, BlockPos blockPos, boolean bl) {
        if (this.warmEnoughToRain(blockPos, levelReader.getSeaLevel())) {
            return false;
        }
        if (levelReader.isInsideBuildHeight(blockPos.getY()) && levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10) {
            BlockState blockState = levelReader.getBlockState(blockPos);
            FluidState fluidState = levelReader.getFluidState(blockPos);
            if (fluidState.getType() == Fluids.WATER && blockState.getBlock() instanceof LiquidBlock) {
                boolean bl2;
                if (!bl) {
                    return true;
                }
                boolean bl3 = bl2 = levelReader.isWaterAt(blockPos.west()) && levelReader.isWaterAt(blockPos.east()) && levelReader.isWaterAt(blockPos.north()) && levelReader.isWaterAt(blockPos.south());
                if (!bl2) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean coldEnoughToSnow(BlockPos blockPos, int i) {
        return !this.warmEnoughToRain(blockPos, i);
    }

    public boolean warmEnoughToRain(BlockPos blockPos, int i) {
        return this.getTemperature(blockPos, i) >= 0.15f;
    }

    public boolean shouldMeltFrozenOceanIcebergSlightly(BlockPos blockPos, int i) {
        return this.getTemperature(blockPos, i) > 0.1f;
    }

    public boolean shouldSnow(LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState;
        if (this.getPrecipitationAt(blockPos, levelReader.getSeaLevel()) != Precipitation.SNOW) {
            return false;
        }
        return levelReader.isInsideBuildHeight(blockPos.getY()) && levelReader.getBrightness(LightLayer.BLOCK, blockPos) < 10 && ((blockState = levelReader.getBlockState(blockPos)).isAir() || blockState.is(Blocks.SNOW)) && Blocks.SNOW.defaultBlockState().canSurvive(levelReader, blockPos);
    }

    public BiomeGenerationSettings getGenerationSettings() {
        return this.generationSettings;
    }

    public int getGrassColor(double d, double e) {
        int i = this.getBaseGrassColor();
        return this.specialEffects.grassColorModifier().modifyColor(d, e, i);
    }

    private int getBaseGrassColor() {
        Optional<Integer> optional = this.specialEffects.grassColorOverride();
        if (optional.isPresent()) {
            return optional.get();
        }
        return this.getGrassColorFromTexture();
    }

    private int getGrassColorFromTexture() {
        double d = Mth.clamp(this.climateSettings.temperature, 0.0f, 1.0f);
        double e = Mth.clamp(this.climateSettings.downfall, 0.0f, 1.0f);
        return GrassColor.get(d, e);
    }

    public int getFoliageColor() {
        return this.specialEffects.foliageColorOverride().orElseGet(this::getFoliageColorFromTexture);
    }

    private int getFoliageColorFromTexture() {
        double d = Mth.clamp(this.climateSettings.temperature, 0.0f, 1.0f);
        double e = Mth.clamp(this.climateSettings.downfall, 0.0f, 1.0f);
        return FoliageColor.get(d, e);
    }

    public int getDryFoliageColor() {
        return this.specialEffects.dryFoliageColorOverride().orElseGet(this::getDryFoliageColorFromTexture);
    }

    private int getDryFoliageColorFromTexture() {
        double d = Mth.clamp(this.climateSettings.temperature, 0.0f, 1.0f);
        double e = Mth.clamp(this.climateSettings.downfall, 0.0f, 1.0f);
        return DryFoliageColor.get(d, e);
    }

    public float getBaseTemperature() {
        return this.climateSettings.temperature;
    }

    public EnvironmentAttributeMap getAttributes() {
        return this.attributes;
    }

    public BiomeSpecialEffects getSpecialEffects() {
        return this.specialEffects;
    }

    public int getWaterColor() {
        return this.specialEffects.waterColor();
    }

    static final class ClimateSettings
    extends Record {
        private final boolean hasPrecipitation;
        final float temperature;
        final TemperatureModifier temperatureModifier;
        final float downfall;
        public static final MapCodec<ClimateSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.fieldOf("has_precipitation").forGetter(climateSettings -> climateSettings.hasPrecipitation), (App)Codec.FLOAT.fieldOf("temperature").forGetter(climateSettings -> Float.valueOf(climateSettings.temperature)), (App)TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier", (Object)TemperatureModifier.NONE).forGetter(climateSettings -> climateSettings.temperatureModifier), (App)Codec.FLOAT.fieldOf("downfall").forGetter(climateSettings -> Float.valueOf(climateSettings.downfall))).apply((Applicative)instance, ClimateSettings::new));

        ClimateSettings(boolean bl, float f, TemperatureModifier temperatureModifier, float g) {
            this.hasPrecipitation = bl;
            this.temperature = f;
            this.temperatureModifier = temperatureModifier;
            this.downfall = g;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ClimateSettings.class, "hasPrecipitation;temperature;temperatureModifier;downfall", "hasPrecipitation", "temperature", "temperatureModifier", "downfall"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ClimateSettings.class, "hasPrecipitation;temperature;temperatureModifier;downfall", "hasPrecipitation", "temperature", "temperatureModifier", "downfall"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ClimateSettings.class, "hasPrecipitation;temperature;temperatureModifier;downfall", "hasPrecipitation", "temperature", "temperatureModifier", "downfall"}, this, object);
        }

        public boolean hasPrecipitation() {
            return this.hasPrecipitation;
        }

        public float temperature() {
            return this.temperature;
        }

        public TemperatureModifier temperatureModifier() {
            return this.temperatureModifier;
        }

        public float downfall() {
            return this.downfall;
        }
    }

    public static enum Precipitation implements StringRepresentable
    {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        public static final Codec<Precipitation> CODEC;
        private final String name;

        private Precipitation(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Precipitation::values);
        }
    }

    public static enum TemperatureModifier implements StringRepresentable
    {
        NONE("none"){

            @Override
            public float modifyTemperature(BlockPos blockPos, float f) {
                return f;
            }
        }
        ,
        FROZEN("frozen"){

            @Override
            public float modifyTemperature(BlockPos blockPos, float f) {
                double h;
                double e;
                double d = FROZEN_TEMPERATURE_NOISE.getValue((double)blockPos.getX() * 0.05, (double)blockPos.getZ() * 0.05, false) * 7.0;
                double g = d + (e = BIOME_INFO_NOISE.getValue((double)blockPos.getX() * 0.2, (double)blockPos.getZ() * 0.2, false));
                if (g < 0.3 && (h = BIOME_INFO_NOISE.getValue((double)blockPos.getX() * 0.09, (double)blockPos.getZ() * 0.09, false)) < 0.8) {
                    return 0.2f;
                }
                return f;
            }
        };

        private final String name;
        public static final Codec<TemperatureModifier> CODEC;

        public abstract float modifyTemperature(BlockPos var1, float var2);

        TemperatureModifier(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(TemperatureModifier::values);
        }
    }

    public static class BiomeBuilder {
        private boolean hasPrecipitation = true;
        private @Nullable Float temperature;
        private TemperatureModifier temperatureModifier = TemperatureModifier.NONE;
        private @Nullable Float downfall;
        private final EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder();
        private @Nullable BiomeSpecialEffects specialEffects;
        private @Nullable MobSpawnSettings mobSpawnSettings;
        private @Nullable BiomeGenerationSettings generationSettings;

        public BiomeBuilder hasPrecipitation(boolean bl) {
            this.hasPrecipitation = bl;
            return this;
        }

        public BiomeBuilder temperature(float f) {
            this.temperature = Float.valueOf(f);
            return this;
        }

        public BiomeBuilder downfall(float f) {
            this.downfall = Float.valueOf(f);
            return this;
        }

        public BiomeBuilder putAttributes(EnvironmentAttributeMap environmentAttributeMap) {
            this.attributes.putAll(environmentAttributeMap);
            return this;
        }

        public BiomeBuilder putAttributes(EnvironmentAttributeMap.Builder builder) {
            return this.putAttributes(builder.build());
        }

        public <Value> BiomeBuilder setAttribute(EnvironmentAttribute<Value> environmentAttribute, Value object) {
            this.attributes.set(environmentAttribute, object);
            return this;
        }

        public <Value, Parameter> BiomeBuilder modifyAttribute(EnvironmentAttribute<Value> environmentAttribute, AttributeModifier<Value, Parameter> attributeModifier, Parameter object) {
            this.attributes.modify(environmentAttribute, attributeModifier, object);
            return this;
        }

        public BiomeBuilder specialEffects(BiomeSpecialEffects biomeSpecialEffects) {
            this.specialEffects = biomeSpecialEffects;
            return this;
        }

        public BiomeBuilder mobSpawnSettings(MobSpawnSettings mobSpawnSettings) {
            this.mobSpawnSettings = mobSpawnSettings;
            return this;
        }

        public BiomeBuilder generationSettings(BiomeGenerationSettings biomeGenerationSettings) {
            this.generationSettings = biomeGenerationSettings;
            return this;
        }

        public BiomeBuilder temperatureAdjustment(TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return this;
        }

        public Biome build() {
            if (this.temperature == null || this.downfall == null || this.specialEffects == null || this.mobSpawnSettings == null || this.generationSettings == null) {
                throw new IllegalStateException("You are missing parameters to build a proper biome\n" + String.valueOf(this));
            }
            return new Biome(new ClimateSettings(this.hasPrecipitation, this.temperature.floatValue(), this.temperatureModifier, this.downfall.floatValue()), this.attributes.build(), this.specialEffects, this.generationSettings, this.mobSpawnSettings);
        }

        public String toString() {
            return "BiomeBuilder{\nhasPrecipitation=" + this.hasPrecipitation + ",\ntemperature=" + this.temperature + ",\ntemperatureModifier=" + String.valueOf(this.temperatureModifier) + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + String.valueOf((Object)this.specialEffects) + ",\nmobSpawnSettings=" + String.valueOf(this.mobSpawnSettings) + ",\ngenerationSettings=" + String.valueOf(this.generationSettings) + ",\n}";
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;

public record BiomeSpecialEffects(int waterColor, Optional<Integer> foliageColorOverride, Optional<Integer> dryFoliageColorOverride, Optional<Integer> grassColorOverride, GrassColorModifier grassColorModifier) {
    public static final Codec<BiomeSpecialEffects> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.STRING_RGB_COLOR.fieldOf("water_color").forGetter(BiomeSpecialEffects::waterColor), (App)ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("foliage_color").forGetter(BiomeSpecialEffects::foliageColorOverride), (App)ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("dry_foliage_color").forGetter(BiomeSpecialEffects::dryFoliageColorOverride), (App)ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("grass_color").forGetter(BiomeSpecialEffects::grassColorOverride), (App)GrassColorModifier.CODEC.optionalFieldOf("grass_color_modifier", (Object)GrassColorModifier.NONE).forGetter(BiomeSpecialEffects::grassColorModifier)).apply((Applicative)instance, BiomeSpecialEffects::new));

    public static enum GrassColorModifier implements StringRepresentable
    {
        NONE("none"){

            @Override
            public int modifyColor(double d, double e, int i) {
                return i;
            }
        }
        ,
        DARK_FOREST("dark_forest"){

            @Override
            public int modifyColor(double d, double e, int i) {
                return (i & 0xFEFEFE) + 2634762 >> 1;
            }
        }
        ,
        SWAMP("swamp"){

            @Override
            public int modifyColor(double d, double e, int i) {
                double f = Biome.BIOME_INFO_NOISE.getValue(d * 0.0225, e * 0.0225, false);
                if (f < -0.1) {
                    return 5011004;
                }
                return 6975545;
            }
        };

        private final String name;
        public static final Codec<GrassColorModifier> CODEC;

        public abstract int modifyColor(double var1, double var3, int var5);

        GrassColorModifier(String string2) {
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
            CODEC = StringRepresentable.fromEnum(GrassColorModifier::values);
        }
    }

    public static class Builder {
        private OptionalInt waterColor = OptionalInt.empty();
        private Optional<Integer> foliageColorOverride = Optional.empty();
        private Optional<Integer> dryFoliageColorOverride = Optional.empty();
        private Optional<Integer> grassColorOverride = Optional.empty();
        private GrassColorModifier grassColorModifier = GrassColorModifier.NONE;

        public Builder waterColor(int i) {
            this.waterColor = OptionalInt.of(i);
            return this;
        }

        public Builder foliageColorOverride(int i) {
            this.foliageColorOverride = Optional.of(i);
            return this;
        }

        public Builder dryFoliageColorOverride(int i) {
            this.dryFoliageColorOverride = Optional.of(i);
            return this;
        }

        public Builder grassColorOverride(int i) {
            this.grassColorOverride = Optional.of(i);
            return this;
        }

        public Builder grassColorModifier(GrassColorModifier grassColorModifier) {
            this.grassColorModifier = grassColorModifier;
            return this;
        }

        public BiomeSpecialEffects build() {
            return new BiomeSpecialEffects(this.waterColor.orElseThrow(() -> new IllegalStateException("Missing 'water' color.")), this.foliageColorOverride, this.dryFoliageColorOverride, this.grassColorOverride, this.grassColorModifier);
        }
    }
}


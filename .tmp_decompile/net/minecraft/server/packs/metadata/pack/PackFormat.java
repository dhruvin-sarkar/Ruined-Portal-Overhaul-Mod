/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.MatchException
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.metadata.pack;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.InclusiveRange;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record PackFormat(int major, int minor) implements Comparable<PackFormat>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<PackFormat> BOTTOM_CODEC = PackFormat.fullCodec(0);
    public static final Codec<PackFormat> TOP_CODEC = PackFormat.fullCodec(Integer.MAX_VALUE);

    private static Codec<PackFormat> fullCodec(int i) {
        return ExtraCodecs.compactListCodec(ExtraCodecs.NON_NEGATIVE_INT, ExtraCodecs.NON_NEGATIVE_INT.listOf(1, 256)).xmap(list -> list.size() > 1 ? PackFormat.of((Integer)list.getFirst(), (Integer)list.get(1)) : PackFormat.of((Integer)list.getFirst(), i), packFormat -> packFormat.minor != i ? List.of((Object)packFormat.major(), (Object)packFormat.minor()) : List.of((Object)packFormat.major()));
    }

    public static <ResultType, HolderType extends IntermediaryFormatHolder> DataResult<List<ResultType>> validateHolderList(List<HolderType> list, int i, BiFunction<HolderType, InclusiveRange<PackFormat>, ResultType> biFunction) {
        int j = list.stream().map(IntermediaryFormatHolder::format).mapToInt(IntermediaryFormat::effectiveMinMajorVersion).min().orElse(Integer.MAX_VALUE);
        ArrayList<ResultType> list2 = new ArrayList<ResultType>(list.size());
        for (IntermediaryFormatHolder intermediaryFormatHolder : list) {
            IntermediaryFormat intermediaryFormat = intermediaryFormatHolder.format();
            if (intermediaryFormat.min().isEmpty() && intermediaryFormat.max().isEmpty() && intermediaryFormat.supported().isEmpty()) {
                LOGGER.warn("Unknown or broken overlay entry {}", (Object)intermediaryFormatHolder);
                continue;
            }
            DataResult<InclusiveRange<PackFormat>> dataResult = intermediaryFormat.validate(i, false, j <= i, "Overlay \"" + String.valueOf(intermediaryFormatHolder) + "\"", "formats");
            if (dataResult.isSuccess()) {
                list2.add(biFunction.apply(intermediaryFormatHolder, (InclusiveRange)((Object)dataResult.getOrThrow())));
                continue;
            }
            return DataResult.error(() -> ((DataResult.Error)((DataResult.Error)dataResult.error().get())).message());
        }
        return DataResult.success((Object)List.copyOf(list2));
    }

    @VisibleForTesting
    public static int lastPreMinorVersion(PackType packType) {
        return switch (packType) {
            default -> throw new MatchException(null, null);
            case PackType.CLIENT_RESOURCES -> 64;
            case PackType.SERVER_DATA -> 81;
        };
    }

    public static MapCodec<InclusiveRange<PackFormat>> packCodec(PackType packType) {
        int i = PackFormat.lastPreMinorVersion(packType);
        return IntermediaryFormat.PACK_CODEC.flatXmap(intermediaryFormat -> intermediaryFormat.validate(i, true, false, "Pack", "supported_formats"), inclusiveRange -> DataResult.success((Object)((Object)IntermediaryFormat.fromRange(inclusiveRange, i))));
    }

    public static PackFormat of(int i, int j) {
        return new PackFormat(i, j);
    }

    public static PackFormat of(int i) {
        return new PackFormat(i, 0);
    }

    public InclusiveRange<PackFormat> minorRange() {
        return new InclusiveRange<PackFormat>(this, PackFormat.of(this.major, Integer.MAX_VALUE));
    }

    @Override
    public int compareTo(PackFormat packFormat) {
        int i = Integer.compare(this.major(), packFormat.major());
        if (i != 0) {
            return i;
        }
        return Integer.compare(this.minor(), packFormat.minor());
    }

    public String toString() {
        if (this.minor == Integer.MAX_VALUE) {
            return String.format(Locale.ROOT, "%d.*", this.major());
        }
        return String.format(Locale.ROOT, "%d.%d", this.major(), this.minor());
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((PackFormat)object);
    }

    public static interface IntermediaryFormatHolder {
        public IntermediaryFormat format();
    }

    public record IntermediaryFormat(Optional<PackFormat> min, Optional<PackFormat> max, Optional<Integer> format, Optional<InclusiveRange<Integer>> supported) {
        static final MapCodec<IntermediaryFormat> PACK_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BOTTOM_CODEC.optionalFieldOf("min_format").forGetter(IntermediaryFormat::min), (App)TOP_CODEC.optionalFieldOf("max_format").forGetter(IntermediaryFormat::max), (App)Codec.INT.optionalFieldOf("pack_format").forGetter(IntermediaryFormat::format), (App)InclusiveRange.codec(Codec.INT).optionalFieldOf("supported_formats").forGetter(IntermediaryFormat::supported)).apply((Applicative)instance, IntermediaryFormat::new));
        public static final MapCodec<IntermediaryFormat> OVERLAY_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BOTTOM_CODEC.optionalFieldOf("min_format").forGetter(IntermediaryFormat::min), (App)TOP_CODEC.optionalFieldOf("max_format").forGetter(IntermediaryFormat::max), (App)InclusiveRange.codec(Codec.INT).optionalFieldOf("formats").forGetter(IntermediaryFormat::supported)).apply((Applicative)instance, (optional, optional2, optional3) -> new IntermediaryFormat((Optional<PackFormat>)optional, (Optional<PackFormat>)optional2, optional.map(PackFormat::major), (Optional<InclusiveRange<Integer>>)optional3)));

        public static IntermediaryFormat fromRange(InclusiveRange<PackFormat> inclusiveRange, int i) {
            InclusiveRange<Integer> inclusiveRange2 = inclusiveRange.map(PackFormat::major);
            return new IntermediaryFormat(Optional.of(inclusiveRange.minInclusive()), Optional.of(inclusiveRange.maxInclusive()), inclusiveRange2.isValueInRange(i) ? Optional.of(inclusiveRange2.minInclusive()) : Optional.empty(), inclusiveRange2.isValueInRange(i) ? Optional.of(new InclusiveRange<Integer>(inclusiveRange2.minInclusive(), inclusiveRange2.maxInclusive())) : Optional.empty());
        }

        public int effectiveMinMajorVersion() {
            if (this.min.isPresent()) {
                if (this.supported.isPresent()) {
                    return Math.min(this.min.get().major(), this.supported.get().minInclusive());
                }
                return this.min.get().major();
            }
            if (this.supported.isPresent()) {
                return this.supported.get().minInclusive();
            }
            return Integer.MAX_VALUE;
        }

        public DataResult<InclusiveRange<PackFormat>> validate(int i, boolean bl, boolean bl2, String string, String string2) {
            if (this.min.isPresent() != this.max.isPresent()) {
                return DataResult.error(() -> string + " missing field, must declare both min_format and max_format");
            }
            if (bl2 && this.supported.isEmpty()) {
                return DataResult.error(() -> string + " missing required field " + string2 + ", must be present in all overlays for any overlays to work across game versions");
            }
            if (this.min.isPresent()) {
                return this.validateNewFormat(i, bl, bl2, string, string2);
            }
            if (this.supported.isPresent()) {
                return this.validateOldFormat(i, bl, string, string2);
            }
            if (bl && this.format.isPresent()) {
                int j = this.format.get();
                if (j > i) {
                    return DataResult.error(() -> string + " declares support for version newer than " + i + ", but is missing mandatory fields min_format and max_format");
                }
                return DataResult.success(new InclusiveRange<PackFormat>(PackFormat.of(j)));
            }
            return DataResult.error(() -> string + " could not be parsed, missing format version information");
        }

        private DataResult<InclusiveRange<PackFormat>> validateNewFormat(int i, boolean bl, boolean bl2, String string, String string2) {
            int j = this.min.get().major();
            int k = this.max.get().major();
            if (this.min.get().compareTo(this.max.get()) > 0) {
                return DataResult.error(() -> string + " min_format (" + String.valueOf(this.min.get()) + ") is greater than max_format (" + String.valueOf(this.max.get()) + ")");
            }
            if (j > i && !bl2) {
                String string3;
                if (this.supported.isPresent()) {
                    return DataResult.error(() -> string + " key " + string2 + " is deprecated starting from pack format " + (i + 1) + ". Remove " + string2 + " from your pack.mcmeta.");
                }
                if (bl && this.format.isPresent() && (string3 = this.validatePackFormatForRange(j, k)) != null) {
                    return DataResult.error(() -> string3);
                }
            } else {
                if (this.supported.isPresent()) {
                    InclusiveRange<Integer> inclusiveRange = this.supported.get();
                    if (inclusiveRange.minInclusive() != j) {
                        return DataResult.error(() -> string + " version declaration mismatch between " + string2 + " (from " + String.valueOf(inclusiveRange.minInclusive()) + ") and min_format (" + String.valueOf(this.min.get()) + ")");
                    }
                    if (inclusiveRange.maxInclusive() != k && inclusiveRange.maxInclusive() != i) {
                        return DataResult.error(() -> string + " version declaration mismatch between " + string2 + " (up to " + String.valueOf(inclusiveRange.maxInclusive()) + ") and max_format (" + String.valueOf(this.max.get()) + ")");
                    }
                } else {
                    return DataResult.error(() -> string + " declares support for format " + j + ", but game versions supporting formats 17 to " + i + " require a " + string2 + " field. Add \"" + string2 + "\": [" + j + ", " + i + "] or require a version greater or equal to " + (i + 1) + ".0.");
                }
                if (bl) {
                    if (this.format.isPresent()) {
                        String string3 = this.validatePackFormatForRange(j, k);
                        if (string3 != null) {
                            return DataResult.error(() -> string3);
                        }
                    } else {
                        return DataResult.error(() -> string + " declares support for formats up to " + i + ", but game versions supporting formats 17 to " + i + " require a pack_format field. Add \"pack_format\": " + j + " or require a version greater or equal to " + (i + 1) + ".0.");
                    }
                }
            }
            return DataResult.success(new InclusiveRange<PackFormat>(this.min.get(), this.max.get()));
        }

        private DataResult<InclusiveRange<PackFormat>> validateOldFormat(int i, boolean bl, String string, String string2) {
            InclusiveRange<Integer> inclusiveRange = this.supported.get();
            int j = inclusiveRange.minInclusive();
            int k = inclusiveRange.maxInclusive();
            if (k > i) {
                return DataResult.error(() -> string + " declares support for version newer than " + i + ", but is missing mandatory fields min_format and max_format");
            }
            if (bl) {
                if (this.format.isPresent()) {
                    String string3 = this.validatePackFormatForRange(j, k);
                    if (string3 != null) {
                        return DataResult.error(() -> string3);
                    }
                } else {
                    return DataResult.error(() -> string + " declares support for formats up to " + i + ", but game versions supporting formats 17 to " + i + " require a pack_format field. Add \"pack_format\": " + j + " or require a version greater or equal to " + (i + 1) + ".0.");
                }
            }
            return DataResult.success(new InclusiveRange<Integer>(j, k).map(PackFormat::of));
        }

        private @Nullable String validatePackFormatForRange(int i, int j) {
            int k = this.format.get();
            if (k < i || k > j) {
                return "Pack declared support for versions " + i + " to " + j + " but declared main format is " + k;
            }
            if (k < 15) {
                return "Multi-version packs cannot support minimum version of less than 15, since this will leave versions in range unable to load pack.";
            }
            return null;
        }
    }
}


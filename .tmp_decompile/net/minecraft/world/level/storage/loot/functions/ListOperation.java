/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

public interface ListOperation {
    public static final MapCodec<ListOperation> UNLIMITED_CODEC = ListOperation.codec(Integer.MAX_VALUE);

    public static MapCodec<ListOperation> codec(int i) {
        return Type.CODEC.dispatchMap("mode", ListOperation::mode, type -> type.mapCodec).validate(listOperation -> {
            int j;
            ReplaceSection replaceSection;
            if (listOperation instanceof ReplaceSection && (replaceSection = (ReplaceSection)listOperation).size().isPresent() && (j = replaceSection.size().get().intValue()) > i) {
                return DataResult.error(() -> "Size value too large: " + j + ", max size is " + i);
            }
            return DataResult.success((Object)listOperation);
        });
    }

    public Type mode();

    default public <T> List<T> apply(List<T> list, List<T> list2) {
        return this.apply(list, list2, Integer.MAX_VALUE);
    }

    public <T> List<T> apply(List<T> var1, List<T> var2, int var3);

    public static enum Type implements StringRepresentable
    {
        REPLACE_ALL("replace_all", ReplaceAll.MAP_CODEC),
        REPLACE_SECTION("replace_section", ReplaceSection.MAP_CODEC),
        INSERT("insert", Insert.MAP_CODEC),
        APPEND("append", Append.MAP_CODEC);

        public static final Codec<Type> CODEC;
        private final String id;
        final MapCodec<? extends ListOperation> mapCodec;

        private Type(String string2, MapCodec<? extends ListOperation> mapCodec) {
            this.id = string2;
            this.mapCodec = mapCodec;
        }

        public MapCodec<? extends ListOperation> mapCodec() {
            return this.mapCodec;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Type::values);
        }
    }

    public record ReplaceSection(int offset, Optional<Integer> size) implements ListOperation
    {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<ReplaceSection> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", (Object)0).forGetter(ReplaceSection::offset), (App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("size").forGetter(ReplaceSection::size)).apply((Applicative)instance, ReplaceSection::new));

        public ReplaceSection(int i) {
            this(i, Optional.empty());
        }

        @Override
        public Type mode() {
            return Type.REPLACE_SECTION;
        }

        @Override
        public <T> List<T> apply(List<T> list, List<T> list2, int i) {
            ImmutableList list3;
            int j = list.size();
            if (this.offset > j) {
                LOGGER.error("Cannot replace when offset is out of bounds");
                return list;
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            builder.addAll(list.subList(0, this.offset));
            builder.addAll(list2);
            int k = this.offset + this.size.orElse(list2.size());
            if (k < j) {
                builder.addAll(list.subList(k, j));
            }
            if ((list3 = builder.build()).size() > i) {
                LOGGER.error("Contents overflow in section replacement");
                return list;
            }
            return list3;
        }
    }

    public record StandAlone<T>(List<T> value, ListOperation operation) {
        public static <T> Codec<StandAlone<T>> codec(Codec<T> codec, int i) {
            return RecordCodecBuilder.create(instance -> instance.group((App)codec.sizeLimitedListOf(i).fieldOf("values").forGetter(standAlone -> standAlone.value), (App)ListOperation.codec(i).forGetter(standAlone -> standAlone.operation)).apply((Applicative)instance, StandAlone::new));
        }

        public List<T> apply(List<T> list) {
            return this.operation.apply(list, this.value);
        }
    }

    public static class Append
    implements ListOperation {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final Append INSTANCE = new Append();
        public static final MapCodec<Append> MAP_CODEC = MapCodec.unit(() -> INSTANCE);

        private Append() {
        }

        @Override
        public Type mode() {
            return Type.APPEND;
        }

        @Override
        public <T> List<T> apply(List<T> list, List<T> list2, int i) {
            if (list.size() + list2.size() > i) {
                LOGGER.error("Contents overflow in section append");
                return list;
            }
            return Stream.concat(list.stream(), list2.stream()).toList();
        }
    }

    public record Insert(int offset) implements ListOperation
    {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<Insert> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", (Object)0).forGetter(Insert::offset)).apply((Applicative)instance, Insert::new));

        @Override
        public Type mode() {
            return Type.INSERT;
        }

        @Override
        public <T> List<T> apply(List<T> list, List<T> list2, int i) {
            int j = list.size();
            if (this.offset > j) {
                LOGGER.error("Cannot insert when offset is out of bounds");
                return list;
            }
            if (j + list2.size() > i) {
                LOGGER.error("Contents overflow in section insertion");
                return list;
            }
            ImmutableList.Builder builder = ImmutableList.builder();
            builder.addAll(list.subList(0, this.offset));
            builder.addAll(list2);
            builder.addAll(list.subList(this.offset, j));
            return builder.build();
        }
    }

    public static class ReplaceAll
    implements ListOperation {
        public static final ReplaceAll INSTANCE = new ReplaceAll();
        public static final MapCodec<ReplaceAll> MAP_CODEC = MapCodec.unit(() -> INSTANCE);

        private ReplaceAll() {
        }

        @Override
        public Type mode() {
            return Type.REPLACE_ALL;
        }

        @Override
        public <T> List<T> apply(List<T> list, List<T> list2, int i) {
            return list2;
        }
    }
}


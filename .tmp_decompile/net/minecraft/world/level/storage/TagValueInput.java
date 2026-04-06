/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.google.common.collect.Streams
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.DataResult$Success
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  java.lang.MatchException
 *  java.lang.runtime.SwitchBootstraps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.lang.runtime.SwitchBootstraps;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInputContextHelper;
import org.jspecify.annotations.Nullable;

public class TagValueInput
implements ValueInput {
    private final ProblemReporter problemReporter;
    private final ValueInputContextHelper context;
    private final CompoundTag input;

    private TagValueInput(ProblemReporter problemReporter, ValueInputContextHelper valueInputContextHelper, CompoundTag compoundTag) {
        this.problemReporter = problemReporter;
        this.context = valueInputContextHelper;
        this.input = compoundTag;
    }

    public static ValueInput create(ProblemReporter problemReporter, HolderLookup.Provider provider, CompoundTag compoundTag) {
        return new TagValueInput(problemReporter, new ValueInputContextHelper(provider, NbtOps.INSTANCE), compoundTag);
    }

    public static ValueInput.ValueInputList create(ProblemReporter problemReporter, HolderLookup.Provider provider, List<CompoundTag> list) {
        return new CompoundListWrapper(problemReporter, new ValueInputContextHelper(provider, NbtOps.INSTANCE), list);
    }

    @Override
    public <T> Optional<T> read(String string, Codec<T> codec) {
        Tag tag = this.input.get(string);
        if (tag == null) {
            return Optional.empty();
        }
        DataResult dataResult = codec.parse(this.context.ops(), (Object)tag);
        Objects.requireNonNull(dataResult);
        DataResult dataResult2 = dataResult;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult2, (int)n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                DataResult.Success success = (DataResult.Success)dataResult2;
                yield Optional.of(success.value());
            }
            case 1 -> {
                DataResult.Error error = (DataResult.Error)dataResult2;
                this.problemReporter.report(new DecodeFromFieldFailedProblem(string, tag, error));
                yield error.partialValue();
            }
        };
    }

    @Override
    public <T> Optional<T> read(MapCodec<T> mapCodec) {
        DynamicOps<Tag> dynamicOps = this.context.ops();
        DataResult dataResult = dynamicOps.getMap((Object)this.input).flatMap(mapLike -> mapCodec.decode(dynamicOps, mapLike));
        Objects.requireNonNull(dataResult);
        DataResult dataResult2 = dataResult;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult2, (int)n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                DataResult.Success success = (DataResult.Success)dataResult2;
                yield Optional.of(success.value());
            }
            case 1 -> {
                DataResult.Error error = (DataResult.Error)dataResult2;
                this.problemReporter.report(new DecodeFromMapFailedProblem(error));
                yield error.partialValue();
            }
        };
    }

    private <T extends Tag> @Nullable T getOptionalTypedTag(String string, TagType<T> tagType) {
        Tag tag = this.input.get(string);
        if (tag == null) {
            return null;
        }
        TagType<?> tagType2 = tag.getType();
        if (tagType2 != tagType) {
            this.problemReporter.report(new UnexpectedTypeProblem(string, tagType, tagType2));
            return null;
        }
        return (T)tag;
    }

    private @Nullable NumericTag getNumericTag(String string) {
        Tag tag = this.input.get(string);
        if (tag == null) {
            return null;
        }
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            return numericTag;
        }
        this.problemReporter.report(new UnexpectedNonNumberProblem(string, tag.getType()));
        return null;
    }

    @Override
    public Optional<ValueInput> child(String string) {
        CompoundTag compoundTag = this.getOptionalTypedTag(string, CompoundTag.TYPE);
        return compoundTag != null ? Optional.of(this.wrapChild(string, compoundTag)) : Optional.empty();
    }

    @Override
    public ValueInput childOrEmpty(String string) {
        CompoundTag compoundTag = this.getOptionalTypedTag(string, CompoundTag.TYPE);
        return compoundTag != null ? this.wrapChild(string, compoundTag) : this.context.empty();
    }

    @Override
    public Optional<ValueInput.ValueInputList> childrenList(String string) {
        ListTag listTag = this.getOptionalTypedTag(string, ListTag.TYPE);
        return listTag != null ? Optional.of(this.wrapList(string, this.context, listTag)) : Optional.empty();
    }

    @Override
    public ValueInput.ValueInputList childrenListOrEmpty(String string) {
        ListTag listTag = this.getOptionalTypedTag(string, ListTag.TYPE);
        return listTag != null ? this.wrapList(string, this.context, listTag) : this.context.emptyList();
    }

    @Override
    public <T> Optional<ValueInput.TypedInputList<T>> list(String string, Codec<T> codec) {
        ListTag listTag = this.getOptionalTypedTag(string, ListTag.TYPE);
        return listTag != null ? Optional.of(this.wrapTypedList(string, listTag, codec)) : Optional.empty();
    }

    @Override
    public <T> ValueInput.TypedInputList<T> listOrEmpty(String string, Codec<T> codec) {
        ListTag listTag = this.getOptionalTypedTag(string, ListTag.TYPE);
        return listTag != null ? this.wrapTypedList(string, listTag, codec) : this.context.emptyTypedList();
    }

    @Override
    public boolean getBooleanOr(String string, boolean bl) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.byteValue() != 0 : bl;
    }

    @Override
    public byte getByteOr(String string, byte b) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.byteValue() : b;
    }

    @Override
    public int getShortOr(String string, short s) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.shortValue() : s;
    }

    @Override
    public Optional<Integer> getInt(String string) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? Optional.of(numericTag.intValue()) : Optional.empty();
    }

    @Override
    public int getIntOr(String string, int i) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.intValue() : i;
    }

    @Override
    public long getLongOr(String string, long l) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.longValue() : l;
    }

    @Override
    public Optional<Long> getLong(String string) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? Optional.of(numericTag.longValue()) : Optional.empty();
    }

    @Override
    public float getFloatOr(String string, float f) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.floatValue() : f;
    }

    @Override
    public double getDoubleOr(String string, double d) {
        NumericTag numericTag = this.getNumericTag(string);
        return numericTag != null ? numericTag.doubleValue() : d;
    }

    @Override
    public Optional<String> getString(String string) {
        StringTag stringTag = this.getOptionalTypedTag(string, StringTag.TYPE);
        return stringTag != null ? Optional.of(stringTag.value()) : Optional.empty();
    }

    @Override
    public String getStringOr(String string, String string2) {
        StringTag stringTag = this.getOptionalTypedTag(string, StringTag.TYPE);
        return stringTag != null ? stringTag.value() : string2;
    }

    @Override
    public Optional<int[]> getIntArray(String string) {
        IntArrayTag intArrayTag = this.getOptionalTypedTag(string, IntArrayTag.TYPE);
        return intArrayTag != null ? Optional.of(intArrayTag.getAsIntArray()) : Optional.empty();
    }

    @Override
    public HolderLookup.Provider lookup() {
        return this.context.lookup();
    }

    private ValueInput wrapChild(String string, CompoundTag compoundTag) {
        return compoundTag.isEmpty() ? this.context.empty() : new TagValueInput(this.problemReporter.forChild(new ProblemReporter.FieldPathElement(string)), this.context, compoundTag);
    }

    static ValueInput wrapChild(ProblemReporter problemReporter, ValueInputContextHelper valueInputContextHelper, CompoundTag compoundTag) {
        return compoundTag.isEmpty() ? valueInputContextHelper.empty() : new TagValueInput(problemReporter, valueInputContextHelper, compoundTag);
    }

    private ValueInput.ValueInputList wrapList(String string, ValueInputContextHelper valueInputContextHelper, ListTag listTag) {
        return listTag.isEmpty() ? valueInputContextHelper.emptyList() : new ListWrapper(this.problemReporter, string, valueInputContextHelper, listTag);
    }

    private <T> ValueInput.TypedInputList<T> wrapTypedList(String string, ListTag listTag, Codec<T> codec) {
        return listTag.isEmpty() ? this.context.emptyTypedList() : new TypedListWrapper<T>(this.problemReporter, string, this.context, codec, listTag);
    }

    static class CompoundListWrapper
    implements ValueInput.ValueInputList {
        private final ProblemReporter problemReporter;
        private final ValueInputContextHelper context;
        private final List<CompoundTag> list;

        public CompoundListWrapper(ProblemReporter problemReporter, ValueInputContextHelper valueInputContextHelper, List<CompoundTag> list) {
            this.problemReporter = problemReporter;
            this.context = valueInputContextHelper;
            this.list = list;
        }

        ValueInput wrapChild(int i, CompoundTag compoundTag) {
            return TagValueInput.wrapChild(this.problemReporter.forChild(new ProblemReporter.IndexedPathElement(i)), this.context, compoundTag);
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        @Override
        public Stream<ValueInput> stream() {
            return Streams.mapWithIndex(this.list.stream(), (compoundTag, l) -> this.wrapChild((int)l, (CompoundTag)compoundTag));
        }

        @Override
        public Iterator<ValueInput> iterator() {
            final ListIterator<CompoundTag> listIterator = this.list.listIterator();
            return new AbstractIterator<ValueInput>(){

                protected @Nullable ValueInput computeNext() {
                    if (listIterator.hasNext()) {
                        int i = listIterator.nextIndex();
                        CompoundTag compoundTag = (CompoundTag)listIterator.next();
                        return this.wrapChild(i, compoundTag);
                    }
                    return (ValueInput)this.endOfData();
                }

                protected /* synthetic */ @Nullable Object computeNext() {
                    return this.computeNext();
                }
            };
        }
    }

    public record DecodeFromFieldFailedProblem(String name, Tag tag, DataResult.Error<?> error) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Failed to decode value '" + String.valueOf(this.tag) + "' from field '" + this.name + "': " + this.error.message();
        }
    }

    public record DecodeFromMapFailedProblem(DataResult.Error<?> error) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Failed to decode from map: " + this.error.message();
        }
    }

    public record UnexpectedTypeProblem(String name, TagType<?> expected, TagType<?> actual) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Expected field '" + this.name + "' to contain value of type " + this.expected.getName() + ", but got " + this.actual.getName();
        }
    }

    public record UnexpectedNonNumberProblem(String name, TagType<?> actual) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Expected field '" + this.name + "' to contain number, but got " + this.actual.getName();
        }
    }

    static class ListWrapper
    implements ValueInput.ValueInputList {
        private final ProblemReporter problemReporter;
        private final String name;
        final ValueInputContextHelper context;
        private final ListTag list;

        ListWrapper(ProblemReporter problemReporter, String string, ValueInputContextHelper valueInputContextHelper, ListTag listTag) {
            this.problemReporter = problemReporter;
            this.name = string;
            this.context = valueInputContextHelper;
            this.list = listTag;
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        ProblemReporter reporterForChild(int i) {
            return this.problemReporter.forChild(new ProblemReporter.IndexedFieldPathElement(this.name, i));
        }

        void reportIndexUnwrapProblem(int i, Tag tag) {
            this.problemReporter.report(new UnexpectedListElementTypeProblem(this.name, i, CompoundTag.TYPE, tag.getType()));
        }

        @Override
        public Stream<ValueInput> stream() {
            return Streams.mapWithIndex(this.list.stream(), (tag, l) -> {
                if (tag instanceof CompoundTag) {
                    CompoundTag compoundTag = (CompoundTag)tag;
                    return TagValueInput.wrapChild(this.reporterForChild((int)l), this.context, compoundTag);
                }
                this.reportIndexUnwrapProblem((int)l, (Tag)tag);
                return null;
            }).filter(Objects::nonNull);
        }

        @Override
        public Iterator<ValueInput> iterator() {
            final Iterator iterator = this.list.iterator();
            return new AbstractIterator<ValueInput>(){
                private int index;

                protected @Nullable ValueInput computeNext() {
                    while (iterator.hasNext()) {
                        int i;
                        Tag tag = (Tag)iterator.next();
                        ++this.index;
                        if (tag instanceof CompoundTag) {
                            CompoundTag compoundTag = (CompoundTag)tag;
                            return TagValueInput.wrapChild(this.reporterForChild(i), context, compoundTag);
                        }
                        this.reportIndexUnwrapProblem(i, tag);
                    }
                    return (ValueInput)this.endOfData();
                }

                protected /* synthetic */ @Nullable Object computeNext() {
                    return this.computeNext();
                }
            };
        }
    }

    static class TypedListWrapper<T>
    implements ValueInput.TypedInputList<T> {
        private final ProblemReporter problemReporter;
        private final String name;
        final ValueInputContextHelper context;
        final Codec<T> codec;
        private final ListTag list;

        TypedListWrapper(ProblemReporter problemReporter, String string, ValueInputContextHelper valueInputContextHelper, Codec<T> codec, ListTag listTag) {
            this.problemReporter = problemReporter;
            this.name = string;
            this.context = valueInputContextHelper;
            this.codec = codec;
            this.list = listTag;
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }

        void reportIndexUnwrapProblem(int i, Tag tag, DataResult.Error<?> error) {
            this.problemReporter.report(new DecodeFromListFailedProblem(this.name, i, tag, error));
        }

        @Override
        public Stream<T> stream() {
            return Streams.mapWithIndex(this.list.stream(), (tag, l) -> {
                DataResult dataResult = this.codec.parse(this.context.ops(), tag);
                Objects.requireNonNull(dataResult);
                DataResult dataResult2 = dataResult;
                int i = 0;
                return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult2, (int)i)) {
                    default -> throw new MatchException(null, null);
                    case 0 -> {
                        DataResult.Success success = (DataResult.Success)dataResult2;
                        yield success.value();
                    }
                    case 1 -> {
                        DataResult.Error error = (DataResult.Error)dataResult2;
                        this.reportIndexUnwrapProblem((int)l, (Tag)tag, (DataResult.Error<?>)error);
                        yield error.partialValue().orElse(null);
                    }
                };
            }).filter(Objects::nonNull);
        }

        @Override
        public Iterator<T> iterator() {
            final ListIterator listIterator = this.list.listIterator();
            return new AbstractIterator<T>(){

                protected @Nullable T computeNext() {
                    while (listIterator.hasNext()) {
                        DataResult dataResult;
                        int i = listIterator.nextIndex();
                        Tag tag = (Tag)listIterator.next();
                        Objects.requireNonNull(codec.parse(context.ops(), (Object)tag));
                        int n = 0;
                        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (Object)dataResult, (int)n)) {
                            default: {
                                throw new MatchException(null, null);
                            }
                            case 0: {
                                DataResult.Success success = (DataResult.Success)dataResult;
                                return success.value();
                            }
                            case 1: 
                        }
                        DataResult.Error error = (DataResult.Error)dataResult;
                        this.reportIndexUnwrapProblem(i, tag, error);
                        if (!error.partialValue().isPresent()) continue;
                        return error.partialValue().get();
                    }
                    return this.endOfData();
                }
            };
        }
    }

    public record UnexpectedListElementTypeProblem(String name, int index, TagType<?> expected, TagType<?> actual) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Expected list '" + this.name + "' to contain at index " + this.index + " value of type " + this.expected.getName() + ", but got " + this.actual.getName();
        }
    }

    public record DecodeFromListFailedProblem(String name, int index, Tag tag, DataResult.Error<?> error) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Failed to decode value '" + String.valueOf(this.tag) + "' from field '" + this.name + "' at index " + this.index + "': " + this.error.message();
        }
    }
}


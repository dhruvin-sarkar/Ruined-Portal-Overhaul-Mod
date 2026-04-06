/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Splitter
 *  com.google.common.base.Strings
 *  com.google.common.collect.Comparators
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  java.lang.MatchException
 *  java.lang.runtime.SwitchBootstraps
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.PrimitiveTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class NbtUtils {
    private static final Comparator<ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.comparingInt(listTag -> listTag.getIntOr(1, 0)).thenComparingInt(listTag -> listTag.getIntOr(0, 0)).thenComparingInt(listTag -> listTag.getIntOr(2, 0));
    private static final Comparator<ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.comparingDouble(listTag -> listTag.getDoubleOr(1, 0.0)).thenComparingDouble(listTag -> listTag.getDoubleOr(0, 0.0)).thenComparingDouble(listTag -> listTag.getDoubleOr(2, 0.0));
    private static final Codec<ResourceKey<Block>> BLOCK_NAME_CODEC = ResourceKey.codec(Registries.BLOCK);
    public static final String SNBT_DATA_TAG = "data";
    private static final char PROPERTIES_START = '{';
    private static final char PROPERTIES_END = '}';
    private static final String ELEMENT_SEPARATOR = ",";
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final Splitter COMMA_SPLITTER = Splitter.on((String)",");
    private static final Splitter COLON_SPLITTER = Splitter.on((char)':').limit(2);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INDENT = 2;
    private static final int NOT_FOUND = -1;

    private NbtUtils() {
    }

    @VisibleForTesting
    public static boolean compareNbt(@Nullable Tag tag, @Nullable Tag tag2, boolean bl) {
        if (tag == tag2) {
            return true;
        }
        if (tag == null) {
            return true;
        }
        if (tag2 == null) {
            return false;
        }
        if (!tag.getClass().equals(tag2.getClass())) {
            return false;
        }
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            CompoundTag compoundTag2 = (CompoundTag)tag2;
            if (compoundTag2.size() < compoundTag.size()) {
                return false;
            }
            for (Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                Tag tag3 = entry.getValue();
                if (NbtUtils.compareNbt(tag3, compoundTag2.get(entry.getKey()), bl)) continue;
                return false;
            }
            return true;
        }
        if (tag instanceof ListTag) {
            ListTag listTag = (ListTag)tag;
            if (bl) {
                ListTag listTag2 = (ListTag)tag2;
                if (listTag.isEmpty()) {
                    return listTag2.isEmpty();
                }
                if (listTag2.size() < listTag.size()) {
                    return false;
                }
                for (Tag tag4 : listTag) {
                    boolean bl2 = false;
                    for (Tag tag5 : listTag2) {
                        if (!NbtUtils.compareNbt(tag4, tag5, bl)) continue;
                        bl2 = true;
                        break;
                    }
                    if (bl2) continue;
                    return false;
                }
                return true;
            }
        }
        return tag.equals(tag2);
    }

    public static BlockState readBlockState(HolderGetter<Block> holderGetter, CompoundTag compoundTag) {
        Optional optional = compoundTag.read("Name", BLOCK_NAME_CODEC).flatMap(holderGetter::get);
        if (optional.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = (Block)((Holder)optional.get()).value();
        BlockState blockState = block.defaultBlockState();
        Optional<CompoundTag> optional2 = compoundTag.getCompound("Properties");
        if (optional2.isPresent()) {
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
            for (String string : optional2.get().keySet()) {
                Property<?> property = stateDefinition.getProperty(string);
                if (property == null) continue;
                blockState = NbtUtils.setValueHelper(blockState, property, string, optional2.get(), compoundTag);
            }
        }
        return blockState;
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S stateHolder, Property<T> property, String string, CompoundTag compoundTag, CompoundTag compoundTag2) {
        Optional optional = compoundTag.getString(string).flatMap(property::getValue);
        if (optional.isPresent()) {
            return (S)((StateHolder)stateHolder.setValue(property, (Comparable)((Comparable)optional.get())));
        }
        LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", new Object[]{string, compoundTag.get(string), compoundTag2});
        return stateHolder;
    }

    public static CompoundTag writeBlockState(BlockState blockState) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString());
        Map<Property<?>, Comparable<?>> map = blockState.getValues();
        if (!map.isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> property = entry.getKey();
                compoundTag2.putString(property.getName(), NbtUtils.getName(property, entry.getValue()));
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    public static CompoundTag writeFluidState(FluidState fluidState) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", BuiltInRegistries.FLUID.getKey(fluidState.getType()).toString());
        Map<Property<?>, Comparable<?>> map = fluidState.getValues();
        if (!map.isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> property = entry.getKey();
                compoundTag2.putString(property.getName(), NbtUtils.getName(property, entry.getValue()));
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
        return property.getName(comparable);
    }

    public static String prettyPrint(Tag tag) {
        return NbtUtils.prettyPrint(tag, false);
    }

    public static String prettyPrint(Tag tag, boolean bl) {
        return NbtUtils.prettyPrint(new StringBuilder(), tag, 0, bl).toString();
    }

    public static StringBuilder prettyPrint(StringBuilder stringBuilder, Tag tag, int i, boolean bl) {
        Tag tag2 = tag;
        Objects.requireNonNull(tag2);
        Tag tag3 = tag2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PrimitiveTag.class, EndTag.class, ByteArrayTag.class, ListTag.class, IntArrayTag.class, CompoundTag.class, LongArrayTag.class}, (Object)tag3, (int)n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                PrimitiveTag primitiveTag = (PrimitiveTag)tag3;
                yield stringBuilder.append(primitiveTag);
            }
            case 1 -> {
                EndTag endTag = (EndTag)tag3;
                yield stringBuilder;
            }
            case 2 -> {
                ByteArrayTag byteArrayTag = (ByteArrayTag)tag3;
                byte[] bs = byteArrayTag.getAsByteArray();
                int j = bs.length;
                NbtUtils.indent(i, stringBuilder).append("byte[").append(j).append("] {\n");
                if (bl) {
                    NbtUtils.indent(i + 1, stringBuilder);
                    for (int k = 0; k < bs.length; ++k) {
                        if (k != 0) {
                            stringBuilder.append(',');
                        }
                        if (k % 16 == 0 && k / 16 > 0) {
                            stringBuilder.append('\n');
                            if (k < bs.length) {
                                NbtUtils.indent(i + 1, stringBuilder);
                            }
                        } else if (k != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%02X", bs[k] & 0xFF));
                    }
                } else {
                    NbtUtils.indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(i, stringBuilder).append('}');
                yield stringBuilder;
            }
            case 3 -> {
                ListTag listTag = (ListTag)tag3;
                int j = listTag.size();
                NbtUtils.indent(i, stringBuilder).append("list").append("[").append(j).append("] [");
                if (j != 0) {
                    stringBuilder.append('\n');
                }
                for (int k = 0; k < j; ++k) {
                    if (k != 0) {
                        stringBuilder.append(",\n");
                    }
                    NbtUtils.indent(i + 1, stringBuilder);
                    NbtUtils.prettyPrint(stringBuilder, listTag.get(k), i + 1, bl);
                }
                if (j != 0) {
                    stringBuilder.append('\n');
                }
                NbtUtils.indent(i, stringBuilder).append(']');
                yield stringBuilder;
            }
            case 4 -> {
                IntArrayTag intArrayTag = (IntArrayTag)tag3;
                int[] is = intArrayTag.getAsIntArray();
                int l = 0;
                for (int m : is) {
                    l = Math.max(l, String.format(Locale.ROOT, "%X", m).length());
                }
                int n = is.length;
                NbtUtils.indent(i, stringBuilder).append("int[").append(n).append("] {\n");
                if (bl) {
                    NbtUtils.indent(i + 1, stringBuilder);
                    for (int o = 0; o < is.length; ++o) {
                        if (o != 0) {
                            stringBuilder.append(',');
                        }
                        if (o % 16 == 0 && o / 16 > 0) {
                            stringBuilder.append('\n');
                            if (o < is.length) {
                                NbtUtils.indent(i + 1, stringBuilder);
                            }
                        } else if (o != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%0" + l + "X", is[o]));
                    }
                } else {
                    NbtUtils.indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(i, stringBuilder).append('}');
                yield stringBuilder;
            }
            case 5 -> {
                CompoundTag compoundTag = (CompoundTag)tag3;
                ArrayList list = Lists.newArrayList(compoundTag.keySet());
                Collections.sort(list);
                NbtUtils.indent(i, stringBuilder).append('{');
                if (stringBuilder.length() - stringBuilder.lastIndexOf("\n") > 2 * (i + 1)) {
                    stringBuilder.append('\n');
                    NbtUtils.indent(i + 1, stringBuilder);
                }
                int n = list.stream().mapToInt(String::length).max().orElse(0);
                String string = Strings.repeat((String)" ", (int)n);
                for (int p = 0; p < list.size(); ++p) {
                    if (p != 0) {
                        stringBuilder.append(",\n");
                    }
                    String string2 = (String)list.get(p);
                    NbtUtils.indent(i + 1, stringBuilder).append('\"').append(string2).append('\"').append(string, 0, string.length() - string2.length()).append(": ");
                    NbtUtils.prettyPrint(stringBuilder, compoundTag.get(string2), i + 1, bl);
                }
                if (!list.isEmpty()) {
                    stringBuilder.append('\n');
                }
                NbtUtils.indent(i, stringBuilder).append('}');
                yield stringBuilder;
            }
            case 6 -> {
                LongArrayTag longArrayTag = (LongArrayTag)tag3;
                long[] ls = longArrayTag.getAsLongArray();
                long q = 0L;
                for (long r : ls) {
                    q = Math.max(q, (long)String.format(Locale.ROOT, "%X", r).length());
                }
                long s = ls.length;
                NbtUtils.indent(i, stringBuilder).append("long[").append(s).append("] {\n");
                if (bl) {
                    NbtUtils.indent(i + 1, stringBuilder);
                    for (int t = 0; t < ls.length; ++t) {
                        if (t != 0) {
                            stringBuilder.append(',');
                        }
                        if (t % 16 == 0 && t / 16 > 0) {
                            stringBuilder.append('\n');
                            if (t < ls.length) {
                                NbtUtils.indent(i + 1, stringBuilder);
                            }
                        } else if (t != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%0" + q + "X", ls[t]));
                    }
                } else {
                    NbtUtils.indent(i + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(i, stringBuilder).append('}');
                yield stringBuilder;
            }
        };
    }

    private static StringBuilder indent(int i, StringBuilder stringBuilder) {
        int j = stringBuilder.lastIndexOf("\n") + 1;
        int k = stringBuilder.length() - j;
        for (int l = 0; l < 2 * i - k; ++l) {
            stringBuilder.append(' ');
        }
        return stringBuilder;
    }

    public static Component toPrettyComponent(Tag tag) {
        return new TextComponentTagVisitor("").visit(tag);
    }

    public static String structureToSnbt(CompoundTag compoundTag) {
        return new SnbtPrinterTagVisitor().visit(NbtUtils.packStructureTemplate(compoundTag));
    }

    public static CompoundTag snbtToStructure(String string) throws CommandSyntaxException {
        return NbtUtils.unpackStructureTemplate(TagParser.parseCompoundFully(string));
    }

    @VisibleForTesting
    static CompoundTag packStructureTemplate(CompoundTag compoundTag2) {
        ListTag listTag4;
        Optional<ListTag> optional2;
        Optional<ListTag> optional = compoundTag2.getList("palettes");
        ListTag listTag = optional.isPresent() ? optional.get().getListOrEmpty(0) : compoundTag2.getListOrEmpty("palette");
        ListTag listTag2 = listTag.compoundStream().map(NbtUtils::packBlockState).map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new));
        compoundTag2.put("palette", listTag2);
        if (optional.isPresent()) {
            ListTag listTag32 = new ListTag();
            optional.get().stream().flatMap(tag -> tag.asList().stream()).forEach(listTag3 -> {
                CompoundTag compoundTag = new CompoundTag();
                for (int i = 0; i < listTag3.size(); ++i) {
                    compoundTag.putString((String)listTag2.getString(i).orElseThrow(), NbtUtils.packBlockState((CompoundTag)listTag3.getCompound(i).orElseThrow()));
                }
                listTag32.add(compoundTag);
            });
            compoundTag2.put("palettes", listTag32);
        }
        if ((optional2 = compoundTag2.getList("entities")).isPresent()) {
            listTag4 = optional2.get().compoundStream().sorted(Comparator.comparing(compoundTag -> compoundTag.getList("pos"), Comparators.emptiesLast(YXZ_LISTTAG_DOUBLE_COMPARATOR))).collect(Collectors.toCollection(ListTag::new));
            compoundTag2.put("entities", listTag4);
        }
        listTag4 = compoundTag2.getList("blocks").stream().flatMap(ListTag::compoundStream).sorted(Comparator.comparing(compoundTag -> compoundTag.getList("pos"), Comparators.emptiesLast(YXZ_LISTTAG_INT_COMPARATOR))).peek(compoundTag -> compoundTag.putString("state", (String)listTag2.getString(compoundTag.getIntOr("state", 0)).orElseThrow())).collect(Collectors.toCollection(ListTag::new));
        compoundTag2.put(SNBT_DATA_TAG, listTag4);
        compoundTag2.remove("blocks");
        return compoundTag2;
    }

    @VisibleForTesting
    static CompoundTag unpackStructureTemplate(CompoundTag compoundTag2) {
        ListTag listTag = compoundTag2.getListOrEmpty("palette");
        Map map = (Map)listTag.stream().flatMap(tag -> tag.asString().stream()).collect(ImmutableMap.toImmutableMap(Function.identity(), NbtUtils::unpackBlockState));
        Optional<ListTag> optional = compoundTag2.getList("palettes");
        if (optional.isPresent()) {
            compoundTag2.put("palettes", optional.get().compoundStream().map(compoundTag -> map.keySet().stream().map(string -> (String)compoundTag.getString((String)string).orElseThrow()).map(NbtUtils::unpackBlockState).collect(Collectors.toCollection(ListTag::new))).collect(Collectors.toCollection(ListTag::new)));
            compoundTag2.remove("palette");
        } else {
            compoundTag2.put("palette", map.values().stream().collect(Collectors.toCollection(ListTag::new)));
        }
        Optional<ListTag> optional2 = compoundTag2.getList(SNBT_DATA_TAG);
        if (optional2.isPresent()) {
            Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
            object2IntMap.defaultReturnValue(-1);
            for (int i = 0; i < listTag.size(); ++i) {
                object2IntMap.put((Object)((String)listTag.getString(i).orElseThrow()), i);
            }
            ListTag listTag2 = optional2.get();
            for (int j = 0; j < listTag2.size(); ++j) {
                CompoundTag compoundTag22 = (CompoundTag)listTag2.getCompound(j).orElseThrow();
                String string = (String)compoundTag22.getString("state").orElseThrow();
                int k = object2IntMap.getInt((Object)string);
                if (k == -1) {
                    throw new IllegalStateException("Entry " + string + " missing from palette");
                }
                compoundTag22.putInt("state", k);
            }
            compoundTag2.put("blocks", listTag2);
            compoundTag2.remove(SNBT_DATA_TAG);
        }
        return compoundTag2;
    }

    @VisibleForTesting
    static String packBlockState(CompoundTag compoundTag2) {
        StringBuilder stringBuilder = new StringBuilder((String)compoundTag2.getString("Name").orElseThrow());
        compoundTag2.getCompound("Properties").ifPresent(compoundTag -> {
            String string = compoundTag.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> (String)entry.getKey() + ":" + (String)((Tag)entry.getValue()).asString().orElseThrow()).collect(Collectors.joining(ELEMENT_SEPARATOR));
            stringBuilder.append('{').append(string).append('}');
        });
        return stringBuilder.toString();
    }

    @VisibleForTesting
    static CompoundTag unpackBlockState(String string) {
        String string22;
        CompoundTag compoundTag = new CompoundTag();
        int i = string.indexOf(123);
        if (i >= 0) {
            string22 = string.substring(0, i);
            CompoundTag compoundTag2 = new CompoundTag();
            if (i + 2 <= string.length()) {
                String string3 = string.substring(i + 1, string.indexOf(125, i));
                COMMA_SPLITTER.split((CharSequence)string3).forEach(string2 -> {
                    List list = COLON_SPLITTER.splitToList((CharSequence)string2);
                    if (list.size() == 2) {
                        compoundTag2.putString((String)list.get(0), (String)list.get(1));
                    } else {
                        LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", (Object)string);
                    }
                });
                compoundTag.put("Properties", compoundTag2);
            }
        } else {
            string22 = string;
        }
        compoundTag.putString("Name", string22);
        return compoundTag;
    }

    public static CompoundTag addCurrentDataVersion(CompoundTag compoundTag) {
        int i = SharedConstants.getCurrentVersion().dataVersion().version();
        return NbtUtils.addDataVersion(compoundTag, i);
    }

    public static CompoundTag addDataVersion(CompoundTag compoundTag, int i) {
        compoundTag.putInt("DataVersion", i);
        return compoundTag;
    }

    public static Dynamic<Tag> addCurrentDataVersion(Dynamic<Tag> dynamic) {
        int i = SharedConstants.getCurrentVersion().dataVersion().version();
        return NbtUtils.addDataVersion(dynamic, i);
    }

    public static Dynamic<Tag> addDataVersion(Dynamic<Tag> dynamic, int i) {
        return dynamic.set("DataVersion", dynamic.createInt(i));
    }

    public static void addCurrentDataVersion(ValueOutput valueOutput) {
        int i = SharedConstants.getCurrentVersion().dataVersion().version();
        NbtUtils.addDataVersion(valueOutput, i);
    }

    public static void addDataVersion(ValueOutput valueOutput, int i) {
        valueOutput.putInt("DataVersion", i);
    }

    public static int getDataVersion(CompoundTag compoundTag) {
        return NbtUtils.getDataVersion(compoundTag, -1);
    }

    public static int getDataVersion(CompoundTag compoundTag, int i) {
        return compoundTag.getIntOr("DataVersion", i);
    }

    public static int getDataVersion(Dynamic<?> dynamic, int i) {
        return dynamic.get("DataVersion").asInt(i);
    }
}


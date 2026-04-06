/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntLists
 *  it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
 *  java.lang.MatchException
 *  java.lang.runtime.SwitchBootstraps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.commands.functions.PlainTextFunction;
import net.minecraft.commands.functions.StringTemplate;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class MacroFunction<T extends ExecutionCommandSource<T>>
implements CommandFunction<T> {
    private static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.ROOT)), decimalFormat -> decimalFormat.setMaximumFractionDigits(15));
    private static final int MAX_CACHE_ENTRIES = 8;
    private final List<String> parameters;
    private final Object2ObjectLinkedOpenHashMap<List<String>, InstantiatedFunction<T>> cache = new Object2ObjectLinkedOpenHashMap(8, 0.25f);
    private final Identifier id;
    private final List<Entry<T>> entries;

    public MacroFunction(Identifier identifier, List<Entry<T>> list, List<String> list2) {
        this.id = identifier;
        this.entries = list;
        this.parameters = list2;
    }

    @Override
    public Identifier id() {
        return this.id;
    }

    @Override
    public InstantiatedFunction<T> instantiate(@Nullable CompoundTag compoundTag, CommandDispatcher<T> commandDispatcher) throws FunctionInstantiationException {
        if (compoundTag == null) {
            throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_arguments", Component.translationArg(this.id())));
        }
        ArrayList<String> list = new ArrayList<String>(this.parameters.size());
        for (String string : this.parameters) {
            Tag tag = compoundTag.get(string);
            if (tag == null) {
                throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_argument", Component.translationArg(this.id()), string));
            }
            list.add(MacroFunction.stringify(tag));
        }
        InstantiatedFunction instantiatedFunction = (InstantiatedFunction)this.cache.getAndMoveToLast(list);
        if (instantiatedFunction != null) {
            return instantiatedFunction;
        }
        if (this.cache.size() >= 8) {
            this.cache.removeFirst();
        }
        InstantiatedFunction<T> instantiatedFunction2 = this.substituteAndParse(this.parameters, list, commandDispatcher);
        this.cache.put(list, instantiatedFunction2);
        return instantiatedFunction2;
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String stringify(Tag tag) {
        String string;
        Tag tag2 = tag;
        Objects.requireNonNull(tag2);
        Tag tag3 = tag2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{FloatTag.class, DoubleTag.class, ByteTag.class, ShortTag.class, LongTag.class, StringTag.class}, (Object)tag3, (int)n)) {
            case 0: {
                float f2;
                FloatTag floatTag = (FloatTag)tag3;
                try {
                    float f;
                    f2 = f = floatTag.value();
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
                string = DECIMAL_FORMAT.format(f2);
                return string;
            }
            case 1: {
                double d2;
                DoubleTag doubleTag = (DoubleTag)tag3;
                {
                    double d;
                    d2 = d = doubleTag.value();
                }
                string = DECIMAL_FORMAT.format(d2);
                return string;
            }
            case 2: {
                byte b;
                ByteTag byteTag = (ByteTag)tag3;
                {
                    byte by;
                    b = by = byteTag.value();
                }
                string = String.valueOf(b);
                return string;
            }
            case 3: {
                short s2;
                ShortTag shortTag = (ShortTag)tag3;
                {
                    short s;
                    s2 = s = shortTag.value();
                }
                string = String.valueOf(s2);
                return string;
            }
            case 4: {
                long l2;
                LongTag longTag = (LongTag)tag3;
                {
                    long l;
                    l2 = l = longTag.value();
                }
                string = String.valueOf(l2);
                return string;
            }
            case 5: {
                StringTag stringTag = (StringTag)tag3;
                {
                    String string2;
                    String string3;
                    string = string3 = (string2 = stringTag.value());
                    return string;
                }
            }
        }
        string = tag.toString();
        return string;
    }

    private static void lookupValues(List<String> list, IntList intList, List<String> list2) {
        list2.clear();
        intList.forEach(i -> list2.add((String)list.get(i)));
    }

    private InstantiatedFunction<T> substituteAndParse(List<String> list, List<String> list2, CommandDispatcher<T> commandDispatcher) throws FunctionInstantiationException {
        ArrayList list3 = new ArrayList(this.entries.size());
        ArrayList<String> list4 = new ArrayList<String>(list2.size());
        for (Entry<T> entry : this.entries) {
            MacroFunction.lookupValues(list2, entry.parameters(), list4);
            list3.add(entry.instantiate(list4, commandDispatcher, this.id));
        }
        return new PlainTextFunction(this.id().withPath(string -> string + "/" + list.hashCode()), list3);
    }

    static interface Entry<T> {
        public IntList parameters();

        public UnboundEntryAction<T> instantiate(List<String> var1, CommandDispatcher<T> var2, Identifier var3) throws FunctionInstantiationException;
    }

    static class MacroEntry<T extends ExecutionCommandSource<T>>
    implements Entry<T> {
        private final StringTemplate template;
        private final IntList parameters;
        private final T compilationContext;

        public MacroEntry(StringTemplate stringTemplate, IntList intList, T executionCommandSource) {
            this.template = stringTemplate;
            this.parameters = intList;
            this.compilationContext = executionCommandSource;
        }

        @Override
        public IntList parameters() {
            return this.parameters;
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commandDispatcher, Identifier identifier) throws FunctionInstantiationException {
            String string = this.template.substitute(list);
            try {
                return CommandFunction.parseCommand(commandDispatcher, this.compilationContext, new StringReader(string));
            }
            catch (CommandSyntaxException commandSyntaxException) {
                throw new FunctionInstantiationException(Component.translatable("commands.function.error.parse", Component.translationArg(identifier), string, commandSyntaxException.getMessage()));
            }
        }
    }

    static class PlainTextEntry<T>
    implements Entry<T> {
        private final UnboundEntryAction<T> compiledAction;

        public PlainTextEntry(UnboundEntryAction<T> unboundEntryAction) {
            this.compiledAction = unboundEntryAction;
        }

        @Override
        public IntList parameters() {
            return IntLists.emptyList();
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commandDispatcher, Identifier identifier) {
            return this.compiledAction;
        }
    }
}


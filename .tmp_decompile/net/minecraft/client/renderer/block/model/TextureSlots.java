/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  java.lang.MatchException
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  java.lang.runtime.SwitchBootstraps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TextureSlots {
    public static final TextureSlots EMPTY = new TextureSlots(Map.of());
    private static final char REFERENCE_CHAR = '#';
    private final Map<String, Material> resolvedValues;

    TextureSlots(Map<String, Material> map) {
        this.resolvedValues = map;
    }

    public @Nullable Material getMaterial(String string) {
        if (TextureSlots.isTextureReference(string)) {
            string = string.substring(1);
        }
        return this.resolvedValues.get(string);
    }

    private static boolean isTextureReference(String string) {
        return string.charAt(0) == '#';
    }

    public static Data parseTextureMap(JsonObject jsonObject) {
        Data.Builder builder = new Data.Builder();
        for (Map.Entry entry : jsonObject.entrySet()) {
            TextureSlots.parseEntry((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsString(), builder);
        }
        return builder.build();
    }

    private static void parseEntry(String string, String string2, Data.Builder builder) {
        if (TextureSlots.isTextureReference(string2)) {
            builder.addReference(string, string2.substring(1));
        } else {
            Identifier identifier = Identifier.tryParse(string2);
            if (identifier == null) {
                throw new JsonParseException(string2 + " is not valid resource location");
            }
            builder.addTexture(string, new Material(ModelManager.BLOCK_OR_ITEM, identifier));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Data
    extends Record {
        final Map<String, SlotContents> values;
        public static final Data EMPTY = new Data(Map.of());

        public Data(Map<String, SlotContents> map) {
            this.values = map;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Data.class, "values", "values"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Data.class, "values", "values"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Data.class, "values", "values"}, this, object);
        }

        public Map<String, SlotContents> values() {
            return this.values;
        }

        @Environment(value=EnvType.CLIENT)
        public static class Builder {
            private final Map<String, SlotContents> textureMap = new HashMap<String, SlotContents>();

            public Builder addReference(String string, String string2) {
                this.textureMap.put(string, new Reference(string2));
                return this;
            }

            public Builder addTexture(String string, Material material) {
                this.textureMap.put(string, new Value(material));
                return this;
            }

            public Data build() {
                if (this.textureMap.isEmpty()) {
                    return EMPTY;
                }
                return new Data(Map.copyOf(this.textureMap));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Resolver {
        private static final Logger LOGGER = LogUtils.getLogger();
        private final List<Data> entries = new ArrayList<Data>();

        public Resolver addLast(Data data) {
            this.entries.addLast((Object)data);
            return this;
        }

        public Resolver addFirst(Data data) {
            this.entries.addFirst((Object)data);
            return this;
        }

        public TextureSlots resolve(ModelDebugName modelDebugName) {
            if (this.entries.isEmpty()) {
                return EMPTY;
            }
            Object2ObjectArrayMap object2ObjectMap = new Object2ObjectArrayMap();
            Object2ObjectArrayMap object2ObjectMap2 = new Object2ObjectArrayMap();
            for (Data data : Lists.reverse(this.entries)) {
                data.values.forEach((arg_0, arg_1) -> Resolver.method_65552((Object2ObjectMap)object2ObjectMap2, (Object2ObjectMap)object2ObjectMap, arg_0, arg_1));
            }
            if (object2ObjectMap2.isEmpty()) {
                return new TextureSlots((Map<String, Material>)object2ObjectMap);
            }
            boolean bl = true;
            while (bl) {
                bl = false;
                ObjectIterator objectIterator = Object2ObjectMaps.fastIterator((Object2ObjectMap)object2ObjectMap2);
                while (objectIterator.hasNext()) {
                    Object2ObjectMap.Entry entry2 = (Object2ObjectMap.Entry)objectIterator.next();
                    Material material = (Material)object2ObjectMap.get((Object)((Reference)entry2.getValue()).target);
                    if (material == null) continue;
                    object2ObjectMap.put((Object)((String)entry2.getKey()), (Object)material);
                    objectIterator.remove();
                    bl = true;
                }
            }
            if (!object2ObjectMap2.isEmpty()) {
                LOGGER.warn("Unresolved texture references in {}:\n{}", (Object)modelDebugName.debugName(), (Object)object2ObjectMap2.entrySet().stream().map(entry -> "\t#" + (String)entry.getKey() + "-> #" + ((Reference)entry.getValue()).target + "\n").collect(Collectors.joining()));
            }
            return new TextureSlots((Map<String, Material>)object2ObjectMap);
        }

        private static /* synthetic */ void method_65552(Object2ObjectMap object2ObjectMap, Object2ObjectMap object2ObjectMap2, String string, SlotContents slotContents) {
            SlotContents slotContents2 = slotContents;
            Objects.requireNonNull(slotContents2);
            SlotContents slotContents22 = slotContents2;
            int i = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Value.class, Reference.class}, (Object)slotContents22, (int)i)) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    Value value = (Value)slotContents22;
                    object2ObjectMap.remove((Object)string);
                    object2ObjectMap2.put((Object)string, (Object)value.material());
                    break;
                }
                case 1: {
                    Reference reference = (Reference)slotContents22;
                    object2ObjectMap2.remove((Object)string);
                    object2ObjectMap.put((Object)string, (Object)reference);
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class Reference
    extends Record
    implements SlotContents {
        final String target;

        Reference(String string) {
            this.target = string;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Reference.class, "target", "target"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Reference.class, "target", "target"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Reference.class, "target", "target"}, this, object);
        }

        public String target() {
            return this.target;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Value(Material material) implements SlotContents
    {
    }

    @Environment(value=EnvType.CLIENT)
    public static sealed interface SlotContents
    permits Value, Reference {
    }
}


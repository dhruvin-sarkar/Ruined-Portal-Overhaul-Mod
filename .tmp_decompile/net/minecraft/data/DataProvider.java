/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.Hashing
 *  com.google.common.hash.HashingOutputStream
 *  com.google.gson.JsonElement
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.slf4j.Logger
 */
package net.minecraft.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public interface DataProvider {
    public static final ToIntFunction<String> FIXED_ORDER_FIELDS = (ToIntFunction)Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> {
        object2IntOpenHashMap.put((Object)"type", 0);
        object2IntOpenHashMap.put((Object)"parent", 1);
        object2IntOpenHashMap.defaultReturnValue(2);
    });
    public static final Comparator<String> KEY_COMPARATOR = Comparator.comparingInt(FIXED_ORDER_FIELDS).thenComparing(string -> string);
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompletableFuture<?> run(CachedOutput var1);

    public String getName();

    public static <T> CompletableFuture<?> saveAll(CachedOutput cachedOutput, Codec<T> codec, PackOutput.PathProvider pathProvider, Map<Identifier, T> map) {
        return DataProvider.saveAll(cachedOutput, codec, pathProvider::json, map);
    }

    public static <T, E> CompletableFuture<?> saveAll(CachedOutput cachedOutput, Codec<E> codec, Function<T, Path> function, Map<T, E> map) {
        return DataProvider.saveAll(cachedOutput, (E object) -> (JsonElement)codec.encodeStart((DynamicOps)JsonOps.INSTANCE, object).getOrThrow(), function, map);
    }

    public static <T, E> CompletableFuture<?> saveAll(CachedOutput cachedOutput, Function<E, JsonElement> function, Function<T, Path> function2, Map<T, E> map) {
        return CompletableFuture.allOf((CompletableFuture[])map.entrySet().stream().map(entry -> {
            Path path = (Path)function2.apply(entry.getKey());
            JsonElement jsonElement = (JsonElement)function.apply(entry.getValue());
            return DataProvider.saveStable(cachedOutput, jsonElement, path);
        }).toArray(CompletableFuture[]::new));
    }

    public static <T> CompletableFuture<?> saveStable(CachedOutput cachedOutput, HolderLookup.Provider provider, Codec<T> codec, T object, Path path) {
        RegistryOps<JsonElement> registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
        return DataProvider.saveStable(cachedOutput, registryOps, codec, object, path);
    }

    public static <T> CompletableFuture<?> saveStable(CachedOutput cachedOutput, Codec<T> codec, T object, Path path) {
        return DataProvider.saveStable(cachedOutput, (DynamicOps<JsonElement>)JsonOps.INSTANCE, codec, object, path);
    }

    private static <T> CompletableFuture<?> saveStable(CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, Codec<T> codec, T object, Path path) {
        JsonElement jsonElement = (JsonElement)codec.encodeStart(dynamicOps, object).getOrThrow();
        return DataProvider.saveStable(cachedOutput, jsonElement, path);
    }

    public static CompletableFuture<?> saveStable(CachedOutput cachedOutput, JsonElement jsonElement, Path path) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), (OutputStream)byteArrayOutputStream);
                try (JsonWriter jsonWriter = new JsonWriter((Writer)new OutputStreamWriter((OutputStream)hashingOutputStream, StandardCharsets.UTF_8));){
                    jsonWriter.setSerializeNulls(false);
                    jsonWriter.setIndent("  ");
                    GsonHelper.writeValue(jsonWriter, jsonElement, KEY_COMPARATOR);
                }
                cachedOutput.writeIfNeeded(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
            }
            catch (IOException iOException) {
                LOGGER.error("Failed to save file to {}", (Object)path, (Object)iOException);
            }
        }, Util.backgroundExecutor().forName("saveStable"));
    }

    @FunctionalInterface
    public static interface Factory<T extends DataProvider> {
        public T create(PackOutput var1);
    }
}


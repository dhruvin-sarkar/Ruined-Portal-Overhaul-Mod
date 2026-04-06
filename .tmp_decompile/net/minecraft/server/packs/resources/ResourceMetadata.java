/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.server.packs.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.LambdaMetafactory;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;

public interface ResourceMetadata {
    public static final ResourceMetadata EMPTY = new ResourceMetadata(){

        @Override
        public <T> Optional<T> getSection(MetadataSectionType<T> metadataSectionType) {
            return Optional.empty();
        }
    };
    public static final IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> EMPTY;

    public static ResourceMetadata fromJsonStream(InputStream inputStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
            final JsonObject jsonObject = GsonHelper.parse(bufferedReader);
            ResourceMetadata resourceMetadata = new ResourceMetadata(){

                @Override
                public <T> Optional<T> getSection(MetadataSectionType<T> metadataSectionType) {
                    String string = metadataSectionType.name();
                    if (jsonObject.has(string)) {
                        Object object = metadataSectionType.codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonObject.get(string)).getOrThrow(JsonParseException::new);
                        return Optional.of(object);
                    }
                    return Optional.empty();
                }
            };
            return resourceMetadata;
        }
    }

    public <T> Optional<T> getSection(MetadataSectionType<T> var1);

    default public <T> Optional<MetadataSectionType.WithValue<T>> getTypedSection(MetadataSectionType<T> metadataSectionType) {
        return this.getSection(metadataSectionType).map(metadataSectionType::withValue);
    }

    default public List<MetadataSectionType.WithValue<?>> getTypedSections(Collection<MetadataSectionType<?>> collection) {
        return (List)collection.stream().map(this::getTypedSection).flatMap((Function<Optional, Stream>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, stream(), (Ljava/util/Optional;)Ljava/util/stream/Stream;)()).collect(Collectors.toUnmodifiableList());
    }
}


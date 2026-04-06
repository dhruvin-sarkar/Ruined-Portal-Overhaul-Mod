/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.telemetry;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryProperty;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TelemetryPropertyMap {
    final Map<TelemetryProperty<?>, Object> entries;

    TelemetryPropertyMap(Map<TelemetryProperty<?>, Object> map) {
        this.entries = map;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MapCodec<TelemetryPropertyMap> createCodec(final List<TelemetryProperty<?>> list) {
        return new MapCodec<TelemetryPropertyMap>(){

            public <T> RecordBuilder<T> encode(TelemetryPropertyMap telemetryPropertyMap, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                RecordBuilder<T> recordBuilder2 = recordBuilder;
                for (TelemetryProperty telemetryProperty : list) {
                    recordBuilder2 = this.encodeProperty(telemetryPropertyMap, recordBuilder2, telemetryProperty);
                }
                return recordBuilder2;
            }

            private <T, V> RecordBuilder<T> encodeProperty(TelemetryPropertyMap telemetryPropertyMap, RecordBuilder<T> recordBuilder, TelemetryProperty<V> telemetryProperty) {
                V object = telemetryPropertyMap.get(telemetryProperty);
                if (object != null) {
                    return recordBuilder.add(telemetryProperty.id(), object, telemetryProperty.codec());
                }
                return recordBuilder;
            }

            public <T> DataResult<TelemetryPropertyMap> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
                DataResult<Builder> dataResult = DataResult.success((Object)new Builder());
                for (TelemetryProperty telemetryProperty : list) {
                    dataResult = this.decodeProperty(dataResult, dynamicOps, mapLike, telemetryProperty);
                }
                return dataResult.map(Builder::build);
            }

            private <T, V> DataResult<Builder> decodeProperty(DataResult<Builder> dataResult, DynamicOps<T> dynamicOps, MapLike<T> mapLike, TelemetryProperty<V> telemetryProperty) {
                Object object2 = mapLike.get(telemetryProperty.id());
                if (object2 != null) {
                    DataResult dataResult2 = telemetryProperty.codec().parse(dynamicOps, object2);
                    return dataResult.apply2stable((builder, object) -> builder.put(telemetryProperty, object), dataResult2);
                }
                return dataResult;
            }

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return list.stream().map(TelemetryProperty::id).map(arg_0 -> dynamicOps.createString(arg_0));
            }

            public /* synthetic */ RecordBuilder encode(Object object, DynamicOps dynamicOps, RecordBuilder recordBuilder) {
                return this.encode((TelemetryPropertyMap)object, dynamicOps, recordBuilder);
            }
        };
    }

    public <T> @Nullable T get(TelemetryProperty<T> telemetryProperty) {
        return (T)this.entries.get(telemetryProperty);
    }

    public String toString() {
        return this.entries.toString();
    }

    public Set<TelemetryProperty<?>> propertySet() {
        return this.entries.keySet();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Map<TelemetryProperty<?>, Object> entries = new Reference2ObjectOpenHashMap();

        Builder() {
        }

        public <T> Builder put(TelemetryProperty<T> telemetryProperty, T object) {
            this.entries.put(telemetryProperty, object);
            return this;
        }

        public <T> Builder putIfNotNull(TelemetryProperty<T> telemetryProperty, @Nullable T object) {
            if (object != null) {
                this.entries.put(telemetryProperty, object);
            }
            return this;
        }

        public Builder putAll(TelemetryPropertyMap telemetryPropertyMap) {
            this.entries.putAll(telemetryPropertyMap.entries);
            return this;
        }

        public TelemetryPropertyMap build() {
            return new TelemetryPropertyMap(this.entries);
        }
    }
}


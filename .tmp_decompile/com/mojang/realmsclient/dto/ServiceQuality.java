/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public enum ServiceQuality {
    GREAT(1, "icon/ping_5"),
    GOOD(2, "icon/ping_4"),
    OKAY(3, "icon/ping_3"),
    POOR(4, "icon/ping_2"),
    UNKNOWN(5, "icon/ping_unknown");

    final int value;
    private final Identifier icon;

    private ServiceQuality(int j, String string2) {
        this.value = j;
        this.icon = Identifier.withDefaultNamespace(string2);
    }

    public static @Nullable ServiceQuality byValue(int i) {
        for (ServiceQuality serviceQuality : ServiceQuality.values()) {
            if (serviceQuality.getValue() != i) continue;
            return serviceQuality;
        }
        return null;
    }

    public int getValue() {
        return this.value;
    }

    public Identifier getIcon() {
        return this.icon;
    }

    @Environment(value=EnvType.CLIENT)
    public static class RealmsServiceQualityJsonAdapter
    extends TypeAdapter<ServiceQuality> {
        private static final Logger LOGGER = LogUtils.getLogger();

        public void write(JsonWriter jsonWriter, ServiceQuality serviceQuality) throws IOException {
            jsonWriter.value((long)serviceQuality.value);
        }

        public ServiceQuality read(JsonReader jsonReader) throws IOException {
            int i = jsonReader.nextInt();
            ServiceQuality serviceQuality = ServiceQuality.byValue(i);
            if (serviceQuality == null) {
                LOGGER.warn("Unsupported ServiceQuality {}", (Object)i);
                return UNKNOWN;
            }
            return serviceQuality;
        }

        public /* synthetic */ Object read(JsonReader jsonReader) throws IOException {
            return this.read(jsonReader);
        }

        public /* synthetic */ void write(JsonWriter jsonWriter, Object object) throws IOException {
            this.write(jsonWriter, (ServiceQuality)((Object)object));
        }
    }
}


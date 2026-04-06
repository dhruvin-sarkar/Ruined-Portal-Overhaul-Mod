/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Backup
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public final String backupId;
    public final Instant lastModified;
    public final long size;
    public boolean uploadedVersion;
    public final Map<String, String> metadata;
    public final Map<String, String> changeList = new HashMap<String, String>();

    private Backup(String string, Instant instant, long l, Map<String, String> map) {
        this.backupId = string;
        this.lastModified = instant;
        this.size = l;
        this.metadata = map;
    }

    public ZonedDateTime lastModifiedDate() {
        return ZonedDateTime.ofInstant(this.lastModified, ZoneId.systemDefault());
    }

    public static @Nullable Backup parse(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        try {
            String string = JsonUtils.getStringOr("backupId", jsonObject, "");
            Instant instant = JsonUtils.getDateOr("lastModifiedDate", jsonObject);
            long l = JsonUtils.getLongOr("size", jsonObject, 0L);
            HashMap<String, String> map = new HashMap<String, String>();
            if (jsonObject.has("metadata")) {
                JsonObject jsonObject2 = jsonObject.getAsJsonObject("metadata");
                Set set = jsonObject2.entrySet();
                for (Map.Entry entry : set) {
                    if (((JsonElement)entry.getValue()).isJsonNull()) continue;
                    map.put((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsString());
                }
            }
            return new Backup(string, instant, l, map);
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse Backup", (Throwable)exception);
            return null;
        }
    }
}


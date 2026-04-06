/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.TypeAdapter
 *  com.google.gson.annotations.JsonAdapter
 *  com.google.gson.annotations.SerializedName
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonWriter
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.realmsclient.dto;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.RealmsSetting;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public final class RealmsSlot
implements ReflectionBasedSerialization {
    @SerializedName(value="slotId")
    public int slotId;
    @SerializedName(value="options")
    @JsonAdapter(value=RealmsWorldOptionsJsonAdapter.class)
    public RealmsWorldOptions options;
    @SerializedName(value="settings")
    public List<RealmsSetting> settings;

    public RealmsSlot(int i, RealmsWorldOptions realmsWorldOptions, List<RealmsSetting> list) {
        this.slotId = i;
        this.options = realmsWorldOptions;
        this.settings = list;
    }

    public static RealmsSlot defaults(int i) {
        return new RealmsSlot(i, RealmsWorldOptions.createEmptyDefaults(), List.of((Object)RealmsSetting.hardcoreSetting(false)));
    }

    public RealmsSlot copy() {
        return new RealmsSlot(this.slotId, this.options.copy(), new ArrayList<RealmsSetting>(this.settings));
    }

    public boolean isHardcore() {
        return RealmsSetting.isHardcore(this.settings);
    }

    @Environment(value=EnvType.CLIENT)
    static class RealmsWorldOptionsJsonAdapter
    extends TypeAdapter<RealmsWorldOptions> {
        private RealmsWorldOptionsJsonAdapter() {
        }

        public void write(JsonWriter jsonWriter, RealmsWorldOptions realmsWorldOptions) throws IOException {
            jsonWriter.jsonValue(new GuardedSerializer().toJson(realmsWorldOptions));
        }

        public RealmsWorldOptions read(JsonReader jsonReader) throws IOException {
            String string = jsonReader.nextString();
            return RealmsWorldOptions.parse(new GuardedSerializer(), string);
        }

        public /* synthetic */ Object read(JsonReader jsonReader) throws IOException {
            return this.read(jsonReader);
        }

        public /* synthetic */ void write(JsonWriter jsonWriter, Object object) throws IOException {
            this.write(jsonWriter, (RealmsWorldOptions)object);
        }
    }
}


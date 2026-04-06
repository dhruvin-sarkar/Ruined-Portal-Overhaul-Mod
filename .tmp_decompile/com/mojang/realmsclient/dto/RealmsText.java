/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsText {
    private static final String TRANSLATION_KEY = "translationKey";
    private static final String ARGS = "args";
    private final String translationKey;
    private final String @Nullable [] args;

    private RealmsText(String string, String @Nullable [] strings) {
        this.translationKey = string;
        this.args = strings;
    }

    public Component createComponent(Component component) {
        return (Component)Objects.requireNonNullElse((Object)this.createComponent(), (Object)component);
    }

    public @Nullable Component createComponent() {
        if (!I18n.exists(this.translationKey)) {
            return null;
        }
        if (this.args == null) {
            return Component.translatable(this.translationKey);
        }
        return Component.translatable(this.translationKey, this.args);
    }

    public static RealmsText parse(JsonObject jsonObject) {
        String[] strings;
        String string = JsonUtils.getRequiredString(TRANSLATION_KEY, jsonObject);
        JsonElement jsonElement = jsonObject.get(ARGS);
        if (jsonElement == null || jsonElement.isJsonNull()) {
            strings = null;
        } else {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            strings = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); ++i) {
                strings[i] = jsonArray.get(i).getAsString();
            }
        }
        return new RealmsText(string, strings);
    }

    public String toString() {
        return this.translationKey;
    }
}


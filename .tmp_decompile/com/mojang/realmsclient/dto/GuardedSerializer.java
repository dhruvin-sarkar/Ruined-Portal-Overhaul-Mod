/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.ExclusionStrategy
 *  com.google.gson.FieldAttributes
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.dto;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.realmsclient.dto.Exclude;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GuardedSerializer {
    ExclusionStrategy strategy = new ExclusionStrategy(this){

        public boolean shouldSkipClass(Class<?> class_) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Exclude.class) != null;
        }
    };
    private final Gson gson = new GsonBuilder().addSerializationExclusionStrategy(this.strategy).addDeserializationExclusionStrategy(this.strategy).create();

    public String toJson(ReflectionBasedSerialization reflectionBasedSerialization) {
        return this.gson.toJson((Object)reflectionBasedSerialization);
    }

    public String toJson(JsonElement jsonElement) {
        return this.gson.toJson(jsonElement);
    }

    public <T extends ReflectionBasedSerialization> @Nullable T fromJson(String string, Class<T> class_) {
        return (T)((ReflectionBasedSerialization)this.gson.fromJson(string, class_));
    }
}


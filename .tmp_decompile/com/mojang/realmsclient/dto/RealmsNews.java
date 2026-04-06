/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.LenientJsonParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record RealmsNews(@Nullable String newsLink) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static RealmsNews parse(String string) {
        String string2 = null;
        try {
            JsonObject jsonObject = LenientJsonParser.parse(string).getAsJsonObject();
            string2 = JsonUtils.getStringOr("newsLink", jsonObject, null);
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse RealmsNews", (Throwable)exception);
        }
        return new RealmsNews(string2);
    }
}


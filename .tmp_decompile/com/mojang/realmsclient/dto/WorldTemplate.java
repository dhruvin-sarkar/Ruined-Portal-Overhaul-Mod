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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record WorldTemplate(String id, String name, String version, String author, String link, @Nullable String image, String trailer, String recommendedPlayers, WorldTemplateType type) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static @Nullable WorldTemplate parse(JsonObject jsonObject) {
        try {
            String string = JsonUtils.getStringOr("type", jsonObject, null);
            return new WorldTemplate(JsonUtils.getStringOr("id", jsonObject, ""), JsonUtils.getStringOr("name", jsonObject, ""), JsonUtils.getStringOr("version", jsonObject, ""), JsonUtils.getStringOr("author", jsonObject, ""), JsonUtils.getStringOr("link", jsonObject, ""), JsonUtils.getStringOr("image", jsonObject, null), JsonUtils.getStringOr("trailer", jsonObject, ""), JsonUtils.getStringOr("recommendedPlayers", jsonObject, ""), string == null ? WorldTemplateType.WORLD_TEMPLATE : WorldTemplateType.valueOf(string));
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse WorldTemplate", (Throwable)exception);
            return null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum WorldTemplateType {
        WORLD_TEMPLATE,
        MINIGAME,
        ADVENTUREMAP,
        EXPERIENCE,
        INSPIRATION;

    }
}


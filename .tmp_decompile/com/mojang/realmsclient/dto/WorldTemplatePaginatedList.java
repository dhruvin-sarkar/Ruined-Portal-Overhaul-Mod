/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.LenientJsonParser;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record WorldTemplatePaginatedList(List<WorldTemplate> templates, int page, int size, int total) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public WorldTemplatePaginatedList(int i) {
        this(List.of(), 0, i, -1);
    }

    public boolean isLastPage() {
        return this.page * this.size >= this.total && this.page > 0 && this.total > 0 && this.size > 0;
    }

    public static WorldTemplatePaginatedList parse(String string) {
        ArrayList<WorldTemplate> list = new ArrayList<WorldTemplate>();
        int i = 0;
        int j = 0;
        int k = 0;
        try {
            JsonObject jsonObject = LenientJsonParser.parse(string).getAsJsonObject();
            if (jsonObject.get("templates").isJsonArray()) {
                for (JsonElement jsonElement : jsonObject.get("templates").getAsJsonArray()) {
                    WorldTemplate worldTemplate = WorldTemplate.parse(jsonElement.getAsJsonObject());
                    if (worldTemplate == null) continue;
                    list.add(worldTemplate);
                }
            }
            i = JsonUtils.getIntOr("page", jsonObject, 0);
            j = JsonUtils.getIntOr("size", jsonObject, 0);
            k = JsonUtils.getIntOr("total", jsonObject, 0);
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse WorldTemplatePaginatedList", (Throwable)exception);
        }
        return new WorldTemplatePaginatedList(list, i, j, k);
    }
}


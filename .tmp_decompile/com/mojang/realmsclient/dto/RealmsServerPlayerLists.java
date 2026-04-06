/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.world.item.component.ResolvableProfile;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record RealmsServerPlayerLists(Map<Long, List<ResolvableProfile>> servers) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static RealmsServerPlayerLists parse(String string) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        try {
            JsonObject jsonObject = GsonHelper.parse(string);
            if (GsonHelper.isArrayNode(jsonObject, "lists")) {
                JsonArray jsonArray = jsonObject.getAsJsonArray("lists");
                for (JsonElement jsonElement : jsonArray) {
                    JsonElement jsonElement2;
                    JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                    String string2 = JsonUtils.getStringOr("playerList", jsonObject2, null);
                    List<Object> list = string2 != null ? ((jsonElement2 = LenientJsonParser.parse(string2)).isJsonArray() ? RealmsServerPlayerLists.parsePlayers(jsonElement2.getAsJsonArray()) : Lists.newArrayList()) : Lists.newArrayList();
                    builder.put((Object)JsonUtils.getLongOr("serverId", jsonObject2, -1L), (Object)list);
                }
            }
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse RealmsServerPlayerLists", (Throwable)exception);
        }
        return new RealmsServerPlayerLists((Map<Long, List<ResolvableProfile>>)builder.build());
    }

    private static List<ResolvableProfile> parsePlayers(JsonArray jsonArray) {
        ArrayList<ResolvableProfile> list = new ArrayList<ResolvableProfile>(jsonArray.size());
        for (JsonElement jsonElement : jsonArray) {
            UUID uUID;
            if (!jsonElement.isJsonObject() || (uUID = JsonUtils.getUuidOr("playerId", jsonElement.getAsJsonObject(), null)) == null || Minecraft.getInstance().isLocalPlayer(uUID)) continue;
            list.add(ResolvableProfile.createUnresolved(uUID));
        }
        return list;
    }

    public List<ResolvableProfile> getProfileResultsFor(long l) {
        List<ResolvableProfile> list = this.servers.get(l);
        if (list != null) {
            return list;
        }
        return List.of();
    }
}


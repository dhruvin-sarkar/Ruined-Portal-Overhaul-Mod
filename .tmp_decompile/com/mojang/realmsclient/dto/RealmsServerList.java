/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record RealmsServerList(@SerializedName(value="servers") List<RealmsServer> servers) implements ReflectionBasedSerialization
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static RealmsServerList parse(GuardedSerializer guardedSerializer, String string) {
        try {
            RealmsServerList realmsServerList = guardedSerializer.fromJson(string, RealmsServerList.class);
            if (realmsServerList != null) {
                realmsServerList.servers.forEach(RealmsServer::finalize);
                return realmsServerList;
            }
            LOGGER.error("Could not parse McoServerList: {}", (Object)string);
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse McoServerList", (Throwable)exception);
        }
        return new RealmsServerList(List.of());
    }
}


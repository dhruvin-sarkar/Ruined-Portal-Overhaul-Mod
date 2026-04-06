/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractPackResources
implements PackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;

    protected AbstractPackResources(PackLocationInfo packLocationInfo) {
        this.location = packLocationInfo;
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> metadataSectionType) throws IOException {
        IoSupplier<InputStream> ioSupplier = this.getRootResource("pack.mcmeta");
        if (ioSupplier == null) {
            return null;
        }
        try (InputStream inputStream = ioSupplier.get();){
            T t = AbstractPackResources.getMetadataFromStream(metadataSectionType, inputStream, this.location);
            return t;
        }
    }

    public static <T> @Nullable T getMetadataFromStream(MetadataSectionType<T> metadataSectionType, InputStream inputStream, PackLocationInfo packLocationInfo) {
        JsonObject jsonObject;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
            jsonObject = GsonHelper.parse(bufferedReader);
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't load {} {} metadata: {}", new Object[]{packLocationInfo.id(), metadataSectionType.name(), exception.getMessage()});
            return null;
        }
        if (!jsonObject.has(metadataSectionType.name())) {
            return null;
        }
        return metadataSectionType.codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonObject.get(metadataSectionType.name())).ifError(error -> LOGGER.error("Couldn't load {} {} metadata: {}", new Object[]{packLocationInfo.id(), metadataSectionType.name(), error.message()})).result().orElse(null);
    }

    @Override
    public PackLocationInfo location() {
        return this.location;
    }
}


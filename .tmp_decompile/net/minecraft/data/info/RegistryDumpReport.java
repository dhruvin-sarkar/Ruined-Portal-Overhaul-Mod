/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public class RegistryDumpReport
implements DataProvider {
    private final PackOutput output;

    public RegistryDumpReport(PackOutput packOutput) {
        this.output = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject jsonObject = new JsonObject();
        BuiltInRegistries.REGISTRY.listElements().forEach(reference -> jsonObject.add(reference.key().identifier().toString(), RegistryDumpReport.dumpRegistry((Registry)reference.value())));
        Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("registries.json");
        return DataProvider.saveStable(cachedOutput, (JsonElement)jsonObject, path);
    }

    private static <T> JsonElement dumpRegistry(Registry<T> registry) {
        JsonObject jsonObject = new JsonObject();
        if (registry instanceof DefaultedRegistry) {
            Identifier identifier = ((DefaultedRegistry)registry).getDefaultKey();
            jsonObject.addProperty("default", identifier.toString());
        }
        int i = BuiltInRegistries.REGISTRY.getId(registry);
        jsonObject.addProperty("protocol_id", (Number)i);
        JsonObject jsonObject2 = new JsonObject();
        registry.listElements().forEach(reference -> {
            Object object = reference.value();
            int i = registry.getId(object);
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("protocol_id", (Number)i);
            jsonObject2.add(reference.key().identifier().toString(), (JsonElement)jsonObject2);
        });
        jsonObject.add("entries", (JsonElement)jsonObject2);
        return jsonObject;
    }

    @Override
    public final String getName() {
        return "Registry Dump";
    }
}


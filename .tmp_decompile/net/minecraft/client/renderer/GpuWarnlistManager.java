/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GpuWarnlistManager
extends SimplePreparableReloadListener<Preparations> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier GPU_WARNLIST_LOCATION = Identifier.withDefaultNamespace("gpu_warnlist.json");
    private ImmutableMap<String, String> warnings = ImmutableMap.of();
    private boolean showWarning;
    private boolean warningDismissed;

    public boolean hasWarnings() {
        return !this.warnings.isEmpty();
    }

    public boolean willShowWarning() {
        return this.hasWarnings() && !this.warningDismissed;
    }

    public void showWarning() {
        this.showWarning = true;
    }

    public void dismissWarning() {
        this.warningDismissed = true;
    }

    public boolean isShowingWarning() {
        return this.showWarning && !this.warningDismissed;
    }

    public void resetWarnings() {
        this.showWarning = false;
        this.warningDismissed = false;
    }

    public @Nullable String getRendererWarnings() {
        return (String)this.warnings.get((Object)"renderer");
    }

    public @Nullable String getVersionWarnings() {
        return (String)this.warnings.get((Object)"version");
    }

    public @Nullable String getVendorWarnings() {
        return (String)this.warnings.get((Object)"vendor");
    }

    public @Nullable String getAllWarnings() {
        StringBuilder stringBuilder = new StringBuilder();
        this.warnings.forEach((string, string2) -> stringBuilder.append((String)string).append(": ").append((String)string2));
        return stringBuilder.isEmpty() ? null : stringBuilder.toString();
    }

    @Override
    protected Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ArrayList list = Lists.newArrayList();
        ArrayList list2 = Lists.newArrayList();
        ArrayList list3 = Lists.newArrayList();
        JsonObject jsonObject = GpuWarnlistManager.parseJson(resourceManager, profilerFiller);
        if (jsonObject != null) {
            try (Zone zone = profilerFiller.zone("compile_regex");){
                GpuWarnlistManager.compilePatterns(jsonObject.getAsJsonArray("renderer"), list);
                GpuWarnlistManager.compilePatterns(jsonObject.getAsJsonArray("version"), list2);
                GpuWarnlistManager.compilePatterns(jsonObject.getAsJsonArray("vendor"), list3);
            }
        }
        return new Preparations(list, list2, list3);
    }

    @Override
    protected void apply(Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.warnings = preparations.apply();
    }

    private static void compilePatterns(JsonArray jsonArray, List<Pattern> list) {
        jsonArray.forEach(jsonElement -> list.add(Pattern.compile(jsonElement.getAsString(), 2)));
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static @Nullable JsonObject parseJson(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        try (Zone zone = profilerFiller.zone("parse_json");){
            JsonObject jsonObject;
            block14: {
                BufferedReader reader = resourceManager.openAsReader(GPU_WARNLIST_LOCATION);
                try {
                    jsonObject = StrictJsonParser.parse(reader).getAsJsonObject();
                    if (reader == null) break block14;
                }
                catch (Throwable throwable) {
                    if (reader != null) {
                        try {
                            ((Reader)reader).close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                ((Reader)reader).close();
            }
            return jsonObject;
        }
        catch (JsonSyntaxException | IOException exception) {
            LOGGER.warn("Failed to load GPU warnlist", exception);
            return null;
        }
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    @Environment(value=EnvType.CLIENT)
    protected static final class Preparations {
        private final List<Pattern> rendererPatterns;
        private final List<Pattern> versionPatterns;
        private final List<Pattern> vendorPatterns;

        Preparations(List<Pattern> list, List<Pattern> list2, List<Pattern> list3) {
            this.rendererPatterns = list;
            this.versionPatterns = list2;
            this.vendorPatterns = list3;
        }

        private static String matchAny(List<Pattern> list, String string) {
            ArrayList list2 = Lists.newArrayList();
            for (Pattern pattern : list) {
                Matcher matcher = pattern.matcher(string);
                while (matcher.find()) {
                    list2.add(matcher.group());
                }
            }
            return String.join((CharSequence)", ", list2);
        }

        ImmutableMap<String, String> apply() {
            ImmutableMap.Builder builder = new ImmutableMap.Builder();
            GpuDevice gpuDevice = RenderSystem.getDevice();
            if (gpuDevice.getBackendName().equals("OpenGL")) {
                String string3;
                String string2;
                String string = Preparations.matchAny(this.rendererPatterns, gpuDevice.getRenderer());
                if (!string.isEmpty()) {
                    builder.put((Object)"renderer", (Object)string);
                }
                if (!(string2 = Preparations.matchAny(this.versionPatterns, gpuDevice.getVersion())).isEmpty()) {
                    builder.put((Object)"version", (Object)string2);
                }
                if (!(string3 = Preparations.matchAny(this.vendorPatterns, gpuDevice.getVendor())).isEmpty()) {
                    builder.put((Object)"vendor", (Object)string3);
                }
            }
            return builder.build();
        }
    }
}


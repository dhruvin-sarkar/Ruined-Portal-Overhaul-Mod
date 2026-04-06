/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.equipment.trim;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public class TrimPatterns {
    public static final ResourceKey<TrimPattern> SENTRY = TrimPatterns.registryKey("sentry");
    public static final ResourceKey<TrimPattern> DUNE = TrimPatterns.registryKey("dune");
    public static final ResourceKey<TrimPattern> COAST = TrimPatterns.registryKey("coast");
    public static final ResourceKey<TrimPattern> WILD = TrimPatterns.registryKey("wild");
    public static final ResourceKey<TrimPattern> WARD = TrimPatterns.registryKey("ward");
    public static final ResourceKey<TrimPattern> EYE = TrimPatterns.registryKey("eye");
    public static final ResourceKey<TrimPattern> VEX = TrimPatterns.registryKey("vex");
    public static final ResourceKey<TrimPattern> TIDE = TrimPatterns.registryKey("tide");
    public static final ResourceKey<TrimPattern> SNOUT = TrimPatterns.registryKey("snout");
    public static final ResourceKey<TrimPattern> RIB = TrimPatterns.registryKey("rib");
    public static final ResourceKey<TrimPattern> SPIRE = TrimPatterns.registryKey("spire");
    public static final ResourceKey<TrimPattern> WAYFINDER = TrimPatterns.registryKey("wayfinder");
    public static final ResourceKey<TrimPattern> SHAPER = TrimPatterns.registryKey("shaper");
    public static final ResourceKey<TrimPattern> SILENCE = TrimPatterns.registryKey("silence");
    public static final ResourceKey<TrimPattern> RAISER = TrimPatterns.registryKey("raiser");
    public static final ResourceKey<TrimPattern> HOST = TrimPatterns.registryKey("host");
    public static final ResourceKey<TrimPattern> FLOW = TrimPatterns.registryKey("flow");
    public static final ResourceKey<TrimPattern> BOLT = TrimPatterns.registryKey("bolt");

    public static void bootstrap(BootstrapContext<TrimPattern> bootstrapContext) {
        TrimPatterns.register(bootstrapContext, SENTRY);
        TrimPatterns.register(bootstrapContext, DUNE);
        TrimPatterns.register(bootstrapContext, COAST);
        TrimPatterns.register(bootstrapContext, WILD);
        TrimPatterns.register(bootstrapContext, WARD);
        TrimPatterns.register(bootstrapContext, EYE);
        TrimPatterns.register(bootstrapContext, VEX);
        TrimPatterns.register(bootstrapContext, TIDE);
        TrimPatterns.register(bootstrapContext, SNOUT);
        TrimPatterns.register(bootstrapContext, RIB);
        TrimPatterns.register(bootstrapContext, SPIRE);
        TrimPatterns.register(bootstrapContext, WAYFINDER);
        TrimPatterns.register(bootstrapContext, SHAPER);
        TrimPatterns.register(bootstrapContext, SILENCE);
        TrimPatterns.register(bootstrapContext, RAISER);
        TrimPatterns.register(bootstrapContext, HOST);
        TrimPatterns.register(bootstrapContext, FLOW);
        TrimPatterns.register(bootstrapContext, BOLT);
    }

    public static void register(BootstrapContext<TrimPattern> bootstrapContext, ResourceKey<TrimPattern> resourceKey) {
        TrimPattern trimPattern = new TrimPattern(TrimPatterns.defaultAssetId(resourceKey), Component.translatable(Util.makeDescriptionId("trim_pattern", resourceKey.identifier())), false);
        bootstrapContext.register(resourceKey, trimPattern);
    }

    private static ResourceKey<TrimPattern> registryKey(String string) {
        return ResourceKey.create(Registries.TRIM_PATTERN, Identifier.withDefaultNamespace(string));
    }

    public static Identifier defaultAssetId(ResourceKey<TrimPattern> resourceKey) {
        return resourceKey.identifier();
    }
}


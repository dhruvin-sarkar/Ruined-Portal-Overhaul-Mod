/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.multiplayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagNetworkSerialization;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RegistryDataCollector {
    private @Nullable ContentsCollector contentsCollector;
    private @Nullable TagCollector tagCollector;

    public void appendContents(ResourceKey<? extends Registry<?>> resourceKey, List<RegistrySynchronization.PackedRegistryEntry> list) {
        if (this.contentsCollector == null) {
            this.contentsCollector = new ContentsCollector();
        }
        this.contentsCollector.append(resourceKey, list);
    }

    public void appendTags(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> map) {
        if (this.tagCollector == null) {
            this.tagCollector = new TagCollector();
        }
        map.forEach(this.tagCollector::append);
    }

    private static <T> Registry.PendingTags<T> resolveRegistryTags(RegistryAccess.Frozen frozen, ResourceKey<? extends Registry<? extends T>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload) {
        HolderLookup.RegistryLookup registry = frozen.lookupOrThrow((ResourceKey)resourceKey);
        return registry.prepareTagReload(networkPayload.resolve(registry));
    }

    private RegistryAccess loadNewElementsAndTags(ResourceProvider resourceProvider, ContentsCollector contentsCollector, boolean bl) {
        RegistryAccess.Frozen frozen2;
        LayeredRegistryAccess<ClientRegistryLayer> layeredRegistryAccess = ClientRegistryLayer.createRegistryAccess();
        RegistryAccess.Frozen frozen = layeredRegistryAccess.getAccessForLoading(ClientRegistryLayer.REMOTE);
        HashMap map = new HashMap();
        contentsCollector.elements.forEach((resourceKey, list) -> map.put((ResourceKey<? extends Registry<?>>)resourceKey, new RegistryDataLoader.NetworkedRegistryData((List<RegistrySynchronization.PackedRegistryEntry>)list, TagNetworkSerialization.NetworkPayload.EMPTY)));
        ArrayList list2 = new ArrayList();
        if (this.tagCollector != null) {
            this.tagCollector.forEach((resourceKey2, networkPayload) -> {
                if (networkPayload.isEmpty()) {
                    return;
                }
                if (RegistrySynchronization.isNetworkable(resourceKey2)) {
                    map.compute((ResourceKey<? extends Registry<?>>)resourceKey2, (resourceKey, networkedRegistryData) -> {
                        List<RegistrySynchronization.PackedRegistryEntry> list = networkedRegistryData != null ? networkedRegistryData.elements() : List.of();
                        return new RegistryDataLoader.NetworkedRegistryData(list, (TagNetworkSerialization.NetworkPayload)networkPayload);
                    });
                } else if (!bl) {
                    list2.add(RegistryDataCollector.resolveRegistryTags(frozen, resourceKey2, networkPayload));
                }
            });
        }
        List<HolderLookup.RegistryLookup<?>> list22 = TagLoader.buildUpdatedLookups(frozen, list2);
        try {
            frozen2 = RegistryDataLoader.load(map, resourceProvider, list22, RegistryDataLoader.SYNCHRONIZED_REGISTRIES).freeze();
        }
        catch (Exception exception) {
            CrashReport crashReport = CrashReport.forThrowable(exception, "Network Registry Load");
            RegistryDataCollector.addCrashDetails(crashReport, map, list2);
            throw new ReportedException(crashReport);
        }
        RegistryAccess.Frozen registryAccess = layeredRegistryAccess.replaceFrom(ClientRegistryLayer.REMOTE, frozen2).compositeAccess();
        list2.forEach(Registry.PendingTags::apply);
        return registryAccess;
    }

    private static void addCrashDetails(CrashReport crashReport, Map<ResourceKey<? extends Registry<?>>, RegistryDataLoader.NetworkedRegistryData> map, List<Registry.PendingTags<?>> list) {
        CrashReportCategory crashReportCategory = crashReport.addCategory("Received Elements and Tags");
        crashReportCategory.setDetail("Dynamic Registries", () -> map.entrySet().stream().sorted(Comparator.comparing(entry -> ((ResourceKey)entry.getKey()).identifier())).map(entry -> String.format(Locale.ROOT, "\n\t\t%s: elements=%d tags=%d", ((ResourceKey)entry.getKey()).identifier(), ((RegistryDataLoader.NetworkedRegistryData)((Object)((Object)((Object)entry.getValue())))).elements().size(), ((RegistryDataLoader.NetworkedRegistryData)((Object)((Object)((Object)entry.getValue())))).tags().size())).collect(Collectors.joining()));
        crashReportCategory.setDetail("Static Registries", () -> list.stream().sorted(Comparator.comparing(pendingTags -> pendingTags.key().identifier())).map(pendingTags -> String.format(Locale.ROOT, "\n\t\t%s: tags=%d", pendingTags.key().identifier(), pendingTags.size())).collect(Collectors.joining()));
    }

    private void loadOnlyTags(TagCollector tagCollector, RegistryAccess.Frozen frozen, boolean bl) {
        tagCollector.forEach((resourceKey, networkPayload) -> {
            if (bl || RegistrySynchronization.isNetworkable(resourceKey)) {
                RegistryDataCollector.resolveRegistryTags(frozen, resourceKey, networkPayload).apply();
            }
        });
    }

    public RegistryAccess.Frozen collectGameRegistries(ResourceProvider resourceProvider, RegistryAccess.Frozen frozen, boolean bl) {
        RegistryAccess registryAccess;
        if (this.contentsCollector != null) {
            registryAccess = this.loadNewElementsAndTags(resourceProvider, this.contentsCollector, bl);
        } else {
            if (this.tagCollector != null) {
                this.loadOnlyTags(this.tagCollector, frozen, !bl);
            }
            registryAccess = frozen;
        }
        return registryAccess.freeze();
    }

    @Environment(value=EnvType.CLIENT)
    static class ContentsCollector {
        final Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> elements = new HashMap();

        ContentsCollector() {
        }

        public void append(ResourceKey<? extends Registry<?>> resourceKey2, List<RegistrySynchronization.PackedRegistryEntry> list) {
            this.elements.computeIfAbsent(resourceKey2, resourceKey -> new ArrayList()).addAll(list);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class TagCollector {
        private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags = new HashMap();

        TagCollector() {
        }

        public void append(ResourceKey<? extends Registry<?>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload) {
            this.tags.put(resourceKey, networkPayload);
        }

        public void forEach(BiConsumer<? super ResourceKey<? extends Registry<?>>, ? super TagNetworkSerialization.NetworkPayload> biConsumer) {
            this.tags.forEach(biConsumer);
        }
    }
}


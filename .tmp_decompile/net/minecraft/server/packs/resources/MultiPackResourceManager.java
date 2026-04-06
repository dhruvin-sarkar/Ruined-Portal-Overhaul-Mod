/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.resources;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceFilterSection;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MultiPackResourceManager
implements CloseableResourceManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, FallbackResourceManager> namespacedManagers;
    private final List<PackResources> packs;

    public MultiPackResourceManager(PackType packType, List<PackResources> list) {
        this.packs = List.copyOf(list);
        HashMap<String, FallbackResourceManager> map = new HashMap<String, FallbackResourceManager>();
        List list2 = list.stream().flatMap(packResources -> packResources.getNamespaces(packType).stream()).distinct().toList();
        for (PackResources packResources2 : list) {
            ResourceFilterSection resourceFilterSection = this.getPackFilterSection(packResources2);
            Set<String> set = packResources2.getNamespaces(packType);
            Predicate<Identifier> predicate = resourceFilterSection != null ? identifier -> resourceFilterSection.isPathFiltered(identifier.getPath()) : null;
            for (String string : list2) {
                boolean bl2;
                boolean bl = set.contains(string);
                boolean bl3 = bl2 = resourceFilterSection != null && resourceFilterSection.isNamespaceFiltered(string);
                if (!bl && !bl2) continue;
                FallbackResourceManager fallbackResourceManager = (FallbackResourceManager)map.get(string);
                if (fallbackResourceManager == null) {
                    fallbackResourceManager = new FallbackResourceManager(packType, string);
                    map.put(string, fallbackResourceManager);
                }
                if (bl && bl2) {
                    fallbackResourceManager.push(packResources2, predicate);
                    continue;
                }
                if (bl) {
                    fallbackResourceManager.push(packResources2);
                    continue;
                }
                fallbackResourceManager.pushFilterOnly(packResources2.packId(), predicate);
            }
        }
        this.namespacedManagers = map;
    }

    private @Nullable ResourceFilterSection getPackFilterSection(PackResources packResources) {
        try {
            return packResources.getMetadataSection(ResourceFilterSection.TYPE);
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to get filter section from pack {}", (Object)packResources.packId());
            return null;
        }
    }

    @Override
    public Set<String> getNamespaces() {
        return this.namespacedManagers.keySet();
    }

    @Override
    public Optional<Resource> getResource(Identifier identifier) {
        ResourceManager resourceManager = this.namespacedManagers.get(identifier.getNamespace());
        if (resourceManager != null) {
            return resourceManager.getResource(identifier);
        }
        return Optional.empty();
    }

    @Override
    public List<Resource> getResourceStack(Identifier identifier) {
        ResourceManager resourceManager = this.namespacedManagers.get(identifier.getNamespace());
        if (resourceManager != null) {
            return resourceManager.getResourceStack(identifier);
        }
        return List.of();
    }

    @Override
    public Map<Identifier, Resource> listResources(String string, Predicate<Identifier> predicate) {
        MultiPackResourceManager.checkTrailingDirectoryPath(string);
        TreeMap<Identifier, Resource> map = new TreeMap<Identifier, Resource>();
        for (FallbackResourceManager fallbackResourceManager : this.namespacedManagers.values()) {
            map.putAll(fallbackResourceManager.listResources(string, predicate));
        }
        return map;
    }

    @Override
    public Map<Identifier, List<Resource>> listResourceStacks(String string, Predicate<Identifier> predicate) {
        MultiPackResourceManager.checkTrailingDirectoryPath(string);
        TreeMap<Identifier, List<Resource>> map = new TreeMap<Identifier, List<Resource>>();
        for (FallbackResourceManager fallbackResourceManager : this.namespacedManagers.values()) {
            map.putAll(fallbackResourceManager.listResourceStacks(string, predicate));
        }
        return map;
    }

    private static void checkTrailingDirectoryPath(String string) {
        if (string.endsWith("/")) {
            throw new IllegalArgumentException("Trailing slash in path " + string);
        }
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.packs.stream();
    }

    @Override
    public void close() {
        this.packs.forEach(PackResources::close);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.packs.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.Util;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jspecify.annotations.Nullable;

public class PackRepository {
    private final Set<RepositorySource> sources;
    private Map<String, Pack> available = ImmutableMap.of();
    private List<Pack> selected = ImmutableList.of();

    public PackRepository(RepositorySource ... repositorySources) {
        this.sources = ImmutableSet.copyOf((Object[])repositorySources);
    }

    public static String displayPackList(Collection<Pack> collection) {
        return collection.stream().map(pack -> pack.getId() + (pack.getCompatibility().isCompatible() ? "" : " (incompatible)")).collect(Collectors.joining(", "));
    }

    public void reload() {
        List list = (List)this.selected.stream().map(Pack::getId).collect(ImmutableList.toImmutableList());
        this.available = this.discoverAvailable();
        this.selected = this.rebuildSelected(list);
    }

    private Map<String, Pack> discoverAvailable() {
        TreeMap map = Maps.newTreeMap();
        for (RepositorySource repositorySource : this.sources) {
            repositorySource.loadPacks(pack -> map.put(pack.getId(), pack));
        }
        return ImmutableMap.copyOf((Map)map);
    }

    public boolean isAbleToClearAnyPack() {
        List<Pack> list = this.rebuildSelected(List.of());
        return !this.selected.equals(list);
    }

    public void setSelected(Collection<String> collection) {
        this.selected = this.rebuildSelected(collection);
    }

    public boolean addPack(String string) {
        Pack pack = this.available.get(string);
        if (pack != null && !this.selected.contains(pack)) {
            ArrayList list = Lists.newArrayList(this.selected);
            list.add(pack);
            this.selected = list;
            return true;
        }
        return false;
    }

    public boolean removePack(String string) {
        Pack pack = this.available.get(string);
        if (pack != null && this.selected.contains(pack)) {
            ArrayList list = Lists.newArrayList(this.selected);
            list.remove(pack);
            this.selected = list;
            return true;
        }
        return false;
    }

    private List<Pack> rebuildSelected(Collection<String> collection) {
        List list = this.getAvailablePacks(collection).collect(Util.toMutableList());
        for (Pack pack : this.available.values()) {
            if (!pack.isRequired() || list.contains(pack)) continue;
            pack.getDefaultPosition().insert(list, pack, Pack::selectionConfig, false);
        }
        return ImmutableList.copyOf(list);
    }

    private Stream<Pack> getAvailablePacks(Collection<String> collection) {
        return collection.stream().map(this.available::get).filter(Objects::nonNull);
    }

    public Collection<String> getAvailableIds() {
        return this.available.keySet();
    }

    public Collection<Pack> getAvailablePacks() {
        return this.available.values();
    }

    public Collection<String> getSelectedIds() {
        return (Collection)this.selected.stream().map(Pack::getId).collect(ImmutableSet.toImmutableSet());
    }

    public FeatureFlagSet getRequestedFeatureFlags() {
        return this.getSelectedPacks().stream().map(Pack::getRequestedFeatures).reduce(FeatureFlagSet::join).orElse(FeatureFlagSet.of());
    }

    public Collection<Pack> getSelectedPacks() {
        return this.selected;
    }

    public @Nullable Pack getPack(String string) {
        return this.available.get(string);
    }

    public boolean isAvailable(String string) {
        return this.available.containsKey(string);
    }

    public List<PackResources> openAllSelected() {
        return (List)this.selected.stream().map(Pack::open).collect(ImmutableList.toImmutableList());
    }
}


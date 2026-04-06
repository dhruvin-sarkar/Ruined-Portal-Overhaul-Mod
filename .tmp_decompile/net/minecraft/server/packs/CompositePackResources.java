/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.packs;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

public class CompositePackResources
implements PackResources {
    private final PackResources primaryPackResources;
    private final List<PackResources> packResourcesStack;

    public CompositePackResources(PackResources packResources, List<PackResources> list) {
        this.primaryPackResources = packResources;
        ArrayList<PackResources> list2 = new ArrayList<PackResources>(list.size() + 1);
        list2.addAll(Lists.reverse(list));
        list2.add(packResources);
        this.packResourcesStack = List.copyOf(list2);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String ... strings) {
        return this.primaryPackResources.getRootResource(strings);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, Identifier identifier) {
        for (PackResources packResources : this.packResourcesStack) {
            IoSupplier<InputStream> ioSupplier = packResources.getResource(packType, identifier);
            if (ioSupplier == null) continue;
            return ioSupplier;
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
        HashMap<Identifier, IoSupplier<InputStream>> map = new HashMap<Identifier, IoSupplier<InputStream>>();
        for (PackResources packResources : this.packResourcesStack) {
            packResources.listResources(packType, string, string2, map::putIfAbsent);
        }
        map.forEach(resourceOutput);
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        HashSet<String> set = new HashSet<String>();
        for (PackResources packResources : this.packResourcesStack) {
            set.addAll(packResources.getNamespaces(packType));
        }
        return set;
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> metadataSectionType) throws IOException {
        return this.primaryPackResources.getMetadataSection(metadataSectionType);
    }

    @Override
    public PackLocationInfo location() {
        return this.primaryPackResources.location();
    }

    @Override
    public void close() {
        this.packResourcesStack.forEach(PackResources::close);
    }
}


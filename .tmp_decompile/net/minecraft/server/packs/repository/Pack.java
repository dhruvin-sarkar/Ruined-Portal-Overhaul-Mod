/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Pack {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;
    private final ResourcesSupplier resources;
    private final Metadata metadata;
    private final PackSelectionConfig selectionConfig;

    public static @Nullable Pack readMetaAndCreate(PackLocationInfo packLocationInfo, ResourcesSupplier resourcesSupplier, PackType packType, PackSelectionConfig packSelectionConfig) {
        PackFormat packFormat = SharedConstants.getCurrentVersion().packVersion(packType);
        Metadata metadata = Pack.readPackMetadata(packLocationInfo, resourcesSupplier, packFormat, packType);
        return metadata != null ? new Pack(packLocationInfo, resourcesSupplier, metadata, packSelectionConfig) : null;
    }

    public Pack(PackLocationInfo packLocationInfo, ResourcesSupplier resourcesSupplier, Metadata metadata, PackSelectionConfig packSelectionConfig) {
        this.location = packLocationInfo;
        this.resources = resourcesSupplier;
        this.metadata = metadata;
        this.selectionConfig = packSelectionConfig;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static @Nullable Metadata readPackMetadata(PackLocationInfo packLocationInfo, ResourcesSupplier resourcesSupplier, PackFormat packFormat, PackType packType) {
        try (PackResources packResources = resourcesSupplier.openPrimary(packLocationInfo);){
            PackMetadataSection packMetadataSection = packResources.getMetadataSection(PackMetadataSection.forPackType(packType));
            if (packMetadataSection == null) {
                packMetadataSection = packResources.getMetadataSection(PackMetadataSection.FALLBACK_TYPE);
            }
            if (packMetadataSection == null) {
                LOGGER.warn("Missing metadata in pack {}", (Object)packLocationInfo.id());
                Metadata metadata = null;
                return metadata;
            }
            FeatureFlagsMetadataSection featureFlagsMetadataSection = packResources.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
            FeatureFlagSet featureFlagSet = featureFlagsMetadataSection != null ? featureFlagsMetadataSection.flags() : FeatureFlagSet.of();
            PackCompatibility packCompatibility = PackCompatibility.forVersion(packMetadataSection.supportedFormats(), packFormat);
            OverlayMetadataSection overlayMetadataSection = packResources.getMetadataSection(OverlayMetadataSection.forPackType(packType));
            List<String> list = overlayMetadataSection != null ? overlayMetadataSection.overlaysForVersion(packFormat) : List.of();
            Metadata metadata = new Metadata(packMetadataSection.description(), packCompatibility, featureFlagSet, list);
            return metadata;
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to read pack {} metadata", (Object)packLocationInfo.id(), (Object)exception);
            return null;
        }
    }

    public PackLocationInfo location() {
        return this.location;
    }

    public Component getTitle() {
        return this.location.title();
    }

    public Component getDescription() {
        return this.metadata.description();
    }

    public Component getChatLink(boolean bl) {
        return this.location.createChatLink(bl, this.metadata.description);
    }

    public PackCompatibility getCompatibility() {
        return this.metadata.compatibility();
    }

    public FeatureFlagSet getRequestedFeatures() {
        return this.metadata.requestedFeatures();
    }

    public PackResources open() {
        return this.resources.openFull(this.location, this.metadata);
    }

    public String getId() {
        return this.location.id();
    }

    public PackSelectionConfig selectionConfig() {
        return this.selectionConfig;
    }

    public boolean isRequired() {
        return this.selectionConfig.required();
    }

    public boolean isFixedPosition() {
        return this.selectionConfig.fixedPosition();
    }

    public Position getDefaultPosition() {
        return this.selectionConfig.defaultPosition();
    }

    public PackSource getPackSource() {
        return this.location.source();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Pack)) {
            return false;
        }
        Pack pack = (Pack)object;
        return this.location.equals((Object)pack.location);
    }

    public int hashCode() {
        return this.location.hashCode();
    }

    public static interface ResourcesSupplier {
        public PackResources openPrimary(PackLocationInfo var1);

        public PackResources openFull(PackLocationInfo var1, Metadata var2);
    }

    public static final class Metadata
    extends Record {
        final Component description;
        private final PackCompatibility compatibility;
        private final FeatureFlagSet requestedFeatures;
        private final List<String> overlays;

        public Metadata(Component component, PackCompatibility packCompatibility, FeatureFlagSet featureFlagSet, List<String> list) {
            this.description = component;
            this.compatibility = packCompatibility;
            this.requestedFeatures = featureFlagSet;
            this.overlays = list;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this, object);
        }

        public Component description() {
            return this.description;
        }

        public PackCompatibility compatibility() {
            return this.compatibility;
        }

        public FeatureFlagSet requestedFeatures() {
            return this.requestedFeatures;
        }

        public List<String> overlays() {
            return this.overlays;
        }
    }

    public static enum Position {
        TOP,
        BOTTOM;


        public <T> int insert(List<T> list, T object, Function<T, PackSelectionConfig> function, boolean bl) {
            PackSelectionConfig packSelectionConfig;
            int i;
            Position position;
            Position position2 = position = bl ? this.opposite() : this;
            if (position == BOTTOM) {
                PackSelectionConfig packSelectionConfig2;
                int i2;
                for (i2 = 0; i2 < list.size() && (packSelectionConfig2 = function.apply(list.get(i2))).fixedPosition() && packSelectionConfig2.defaultPosition() == this; ++i2) {
                }
                list.add(i2, object);
                return i2;
            }
            for (i = list.size() - 1; i >= 0 && (packSelectionConfig = function.apply(list.get(i))).fixedPosition() && packSelectionConfig.defaultPosition() == this; --i) {
            }
            list.add(i + 1, object);
            return i + 1;
        }

        public Position opposite() {
            return this == TOP ? BOTTOM : TOP;
        }
    }
}


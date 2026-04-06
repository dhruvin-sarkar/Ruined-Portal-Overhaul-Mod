/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.runtime.SwitchBootstraps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.apache.commons.io.FileUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.components.debug;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.io.IOException;
import java.lang.runtime.SwitchBootstraps;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.datafix.DataFixTypes;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class DebugScreenEntryList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_DEBUG_PROFILE_VERSION = 4649;
    private Map<Identifier, DebugScreenEntryStatus> allStatuses;
    private final List<Identifier> currentlyEnabled = new ArrayList<Identifier>();
    private boolean isOverlayVisible = false;
    private @Nullable DebugScreenProfile profile;
    private final File debugProfileFile;
    private long currentlyEnabledVersion;
    private final Codec<SerializedOptions> codec;

    public DebugScreenEntryList(File file) {
        this.debugProfileFile = new File(file, "debug-profile.json");
        this.codec = DataFixTypes.DEBUG_PROFILE.wrapCodec(SerializedOptions.CODEC, Minecraft.getInstance().getFixerUpper(), 4649);
        this.load();
    }

    public void load() {
        try {
            if (!this.debugProfileFile.isFile()) {
                this.loadDefaultProfile();
                this.rebuildCurrentList();
                return;
            }
            Dynamic dynamic = new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)StrictJsonParser.parse(FileUtils.readFileToString((File)this.debugProfileFile, (Charset)StandardCharsets.UTF_8)));
            SerializedOptions serializedOptions = (SerializedOptions)((Object)this.codec.parse(dynamic).getOrThrow(string -> new IOException("Could not parse debug profile JSON: " + string)));
            if (serializedOptions.profile().isPresent()) {
                this.loadProfile(serializedOptions.profile().get());
            } else {
                this.allStatuses = new HashMap<Identifier, DebugScreenEntryStatus>();
                if (serializedOptions.custom().isPresent()) {
                    this.allStatuses.putAll(serializedOptions.custom().get());
                }
                this.profile = null;
            }
        }
        catch (JsonSyntaxException | IOException exception) {
            LOGGER.error("Couldn't read debug profile file {}, resetting to default", (Object)this.debugProfileFile, (Object)exception);
            this.loadDefaultProfile();
            this.save();
        }
        this.rebuildCurrentList();
    }

    public void loadProfile(DebugScreenProfile debugScreenProfile) {
        this.profile = debugScreenProfile;
        Map<Identifier, DebugScreenEntryStatus> map = DebugScreenEntries.PROFILES.get(debugScreenProfile);
        this.allStatuses = new HashMap<Identifier, DebugScreenEntryStatus>(map);
        this.rebuildCurrentList();
    }

    private void loadDefaultProfile() {
        this.profile = DebugScreenProfile.DEFAULT;
        this.allStatuses = new HashMap<Identifier, DebugScreenEntryStatus>(DebugScreenEntries.PROFILES.get(DebugScreenProfile.DEFAULT));
    }

    public DebugScreenEntryStatus getStatus(Identifier identifier) {
        DebugScreenEntryStatus debugScreenEntryStatus = this.allStatuses.get(identifier);
        if (debugScreenEntryStatus == null) {
            return DebugScreenEntryStatus.NEVER;
        }
        return debugScreenEntryStatus;
    }

    public boolean isCurrentlyEnabled(Identifier identifier) {
        return this.currentlyEnabled.contains(identifier);
    }

    public void setStatus(Identifier identifier, DebugScreenEntryStatus debugScreenEntryStatus) {
        this.profile = null;
        this.allStatuses.put(identifier, debugScreenEntryStatus);
        this.rebuildCurrentList();
        this.save();
    }

    public boolean toggleStatus(Identifier identifier) {
        DebugScreenEntryStatus debugScreenEntryStatus;
        DebugScreenEntryStatus debugScreenEntryStatus2 = debugScreenEntryStatus = this.allStatuses.get(identifier);
        int n = 0;
        switch (SwitchBootstraps.enumSwitch("enumSwitch", new Object[]{"ALWAYS_ON", "IN_OVERLAY", "NEVER"}, (DebugScreenEntryStatus)debugScreenEntryStatus2, (int)n)) {
            case 0: {
                this.setStatus(identifier, DebugScreenEntryStatus.NEVER);
                return false;
            }
            case 1: {
                if (this.isOverlayVisible) {
                    this.setStatus(identifier, DebugScreenEntryStatus.NEVER);
                    return false;
                }
                this.setStatus(identifier, DebugScreenEntryStatus.ALWAYS_ON);
                return true;
            }
            case 2: {
                if (this.isOverlayVisible) {
                    this.setStatus(identifier, DebugScreenEntryStatus.IN_OVERLAY);
                } else {
                    this.setStatus(identifier, DebugScreenEntryStatus.ALWAYS_ON);
                }
                return true;
            }
        }
        this.setStatus(identifier, DebugScreenEntryStatus.ALWAYS_ON);
        return true;
    }

    public Collection<Identifier> getCurrentlyEnabled() {
        return this.currentlyEnabled;
    }

    public void toggleDebugOverlay() {
        this.setOverlayVisible(!this.isOverlayVisible);
    }

    public void setOverlayVisible(boolean bl) {
        if (this.isOverlayVisible != bl) {
            this.isOverlayVisible = bl;
            this.rebuildCurrentList();
        }
    }

    public boolean isOverlayVisible() {
        return this.isOverlayVisible;
    }

    public void rebuildCurrentList() {
        this.currentlyEnabled.clear();
        boolean bl = Minecraft.getInstance().showOnlyReducedInfo();
        for (Map.Entry<Identifier, DebugScreenEntryStatus> entry : this.allStatuses.entrySet()) {
            DebugScreenEntry debugScreenEntry;
            if (entry.getValue() != DebugScreenEntryStatus.ALWAYS_ON && (!this.isOverlayVisible || entry.getValue() != DebugScreenEntryStatus.IN_OVERLAY) || (debugScreenEntry = DebugScreenEntries.getEntry(entry.getKey())) == null || !debugScreenEntry.isAllowed(bl)) continue;
            this.currentlyEnabled.add(entry.getKey());
        }
        this.currentlyEnabled.sort(Identifier::compareTo);
        ++this.currentlyEnabledVersion;
    }

    public long getCurrentlyEnabledVersion() {
        return this.currentlyEnabledVersion;
    }

    public boolean isUsingProfile(DebugScreenProfile debugScreenProfile) {
        return this.profile == debugScreenProfile;
    }

    public void save() {
        SerializedOptions serializedOptions = new SerializedOptions(Optional.ofNullable(this.profile), this.profile == null ? Optional.of(this.allStatuses) : Optional.empty());
        try {
            FileUtils.writeStringToFile((File)this.debugProfileFile, (String)((JsonElement)this.codec.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)serializedOptions).getOrThrow()).toString(), (Charset)StandardCharsets.UTF_8);
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to save debug profile file {}", (Object)this.debugProfileFile, (Object)iOException);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record SerializedOptions(Optional<DebugScreenProfile> profile, Optional<Map<Identifier, DebugScreenEntryStatus>> custom) {
        private static final Codec<Map<Identifier, DebugScreenEntryStatus>> CUSTOM_ENTRIES_CODEC = Codec.unboundedMap(Identifier.CODEC, DebugScreenEntryStatus.CODEC);
        public static final Codec<SerializedOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)DebugScreenProfile.CODEC.optionalFieldOf("profile").forGetter(SerializedOptions::profile), (App)CUSTOM_ENTRIES_CODEC.optionalFieldOf("custom").forGetter(SerializedOptions::custom)).apply((Applicative)instance, SerializedOptions::new));
    }
}


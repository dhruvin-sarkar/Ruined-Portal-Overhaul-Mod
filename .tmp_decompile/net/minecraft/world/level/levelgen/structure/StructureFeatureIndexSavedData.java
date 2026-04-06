/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.longs.LongCollection
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class StructureFeatureIndexSavedData
extends SavedData {
    private final LongSet all;
    private final LongSet remaining;
    private static final Codec<LongSet> LONG_SET = Codec.LONG_STREAM.xmap(LongOpenHashSet::toSet, LongCollection::longStream);
    public static final Codec<StructureFeatureIndexSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)LONG_SET.fieldOf("All").forGetter(structureFeatureIndexSavedData -> structureFeatureIndexSavedData.all), (App)LONG_SET.fieldOf("Remaining").forGetter(structureFeatureIndexSavedData -> structureFeatureIndexSavedData.remaining)).apply((Applicative)instance, StructureFeatureIndexSavedData::new));

    public static SavedDataType<StructureFeatureIndexSavedData> type(String string) {
        return new SavedDataType<StructureFeatureIndexSavedData>(string, StructureFeatureIndexSavedData::new, CODEC, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES);
    }

    private StructureFeatureIndexSavedData(LongSet longSet, LongSet longSet2) {
        this.all = longSet;
        this.remaining = longSet2;
    }

    public StructureFeatureIndexSavedData() {
        this((LongSet)new LongOpenHashSet(), (LongSet)new LongOpenHashSet());
    }

    public void addIndex(long l) {
        this.all.add(l);
        this.remaining.add(l);
        this.setDirty();
    }

    public boolean hasStartIndex(long l) {
        return this.all.contains(l);
    }

    public boolean hasUnhandledIndex(long l) {
        return this.remaining.contains(l);
    }

    public void removeIndex(long l) {
        if (this.remaining.remove(l)) {
            this.setDirty();
        }
    }

    public LongSet getAll() {
        return this.all;
    }
}


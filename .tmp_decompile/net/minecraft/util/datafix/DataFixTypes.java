/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.References;

public enum DataFixTypes {
    LEVEL(References.LEVEL),
    LEVEL_SUMMARY(References.LIGHTWEIGHT_LEVEL),
    PLAYER(References.PLAYER),
    CHUNK(References.CHUNK),
    HOTBAR(References.HOTBAR),
    OPTIONS(References.OPTIONS),
    STRUCTURE(References.STRUCTURE),
    STATS(References.STATS),
    SAVED_DATA_COMMAND_STORAGE(References.SAVED_DATA_COMMAND_STORAGE),
    SAVED_DATA_FORCED_CHUNKS(References.SAVED_DATA_TICKETS),
    SAVED_DATA_MAP_DATA(References.SAVED_DATA_MAP_DATA),
    SAVED_DATA_MAP_INDEX(References.SAVED_DATA_MAP_INDEX),
    SAVED_DATA_RAIDS(References.SAVED_DATA_RAIDS),
    SAVED_DATA_RANDOM_SEQUENCES(References.SAVED_DATA_RANDOM_SEQUENCES),
    SAVED_DATA_SCOREBOARD(References.SAVED_DATA_SCOREBOARD),
    SAVED_DATA_STOPWATCHES(References.SAVED_DATA_STOPWATCHES),
    SAVED_DATA_STRUCTURE_FEATURE_INDICES(References.SAVED_DATA_STRUCTURE_FEATURE_INDICES),
    SAVED_DATA_WORLD_BORDER(References.SAVED_DATA_WORLD_BORDER),
    ADVANCEMENTS(References.ADVANCEMENTS),
    POI_CHUNK(References.POI_CHUNK),
    WORLD_GEN_SETTINGS(References.WORLD_GEN_SETTINGS),
    ENTITY_CHUNK(References.ENTITY_CHUNK),
    DEBUG_PROFILE(References.DEBUG_PROFILE);

    public static final Set<DSL.TypeReference> TYPES_FOR_LEVEL_LIST;
    private final DSL.TypeReference type;

    private DataFixTypes(DSL.TypeReference typeReference) {
        this.type = typeReference;
    }

    static int currentVersion() {
        return SharedConstants.getCurrentVersion().dataVersion().version();
    }

    public <A> Codec<A> wrapCodec(final Codec<A> codec, final DataFixer dataFixer, final int i) {
        return new Codec<A>(){

            public <T> DataResult<T> encode(A object2, DynamicOps<T> dynamicOps, T object22) {
                return codec.encode(object2, dynamicOps, object22).flatMap(object -> dynamicOps.mergeToMap(object, dynamicOps.createString("DataVersion"), dynamicOps.createInt(DataFixTypes.currentVersion())));
            }

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicOps, T object) {
                int i2 = dynamicOps.get(object, "DataVersion").flatMap(arg_0 -> dynamicOps.getNumberValue(arg_0)).map(Number::intValue).result().orElse(i);
                Dynamic dynamic = new Dynamic(dynamicOps, dynamicOps.remove(object, "DataVersion"));
                Dynamic dynamic2 = DataFixTypes.this.updateToCurrentVersion(dataFixer, dynamic, i2);
                return codec.decode(dynamic2);
            }
        };
    }

    public <T> Dynamic<T> update(DataFixer dataFixer, Dynamic<T> dynamic, int i, int j) {
        return dataFixer.update(this.type, dynamic, i, j);
    }

    public <T> Dynamic<T> updateToCurrentVersion(DataFixer dataFixer, Dynamic<T> dynamic, int i) {
        return this.update(dataFixer, dynamic, i, DataFixTypes.currentVersion());
    }

    public CompoundTag update(DataFixer dataFixer, CompoundTag compoundTag, int i, int j) {
        return (CompoundTag)this.update(dataFixer, new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag), i, j).getValue();
    }

    public CompoundTag updateToCurrentVersion(DataFixer dataFixer, CompoundTag compoundTag, int i) {
        return this.update(dataFixer, compoundTag, i, DataFixTypes.currentVersion());
    }

    static {
        TYPES_FOR_LEVEL_LIST = Set.of((Object)DataFixTypes.LEVEL_SUMMARY.type);
    }
}


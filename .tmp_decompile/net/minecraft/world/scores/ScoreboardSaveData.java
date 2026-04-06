/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.scores;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class ScoreboardSaveData
extends SavedData {
    public static final SavedDataType<ScoreboardSaveData> TYPE = new SavedDataType<ScoreboardSaveData>("scoreboard", ScoreboardSaveData::new, Packed.CODEC.xmap(ScoreboardSaveData::new, ScoreboardSaveData::getData), DataFixTypes.SAVED_DATA_SCOREBOARD);
    private Packed data;

    private ScoreboardSaveData() {
        this(Packed.EMPTY);
    }

    public ScoreboardSaveData(Packed packed) {
        this.data = packed;
    }

    public Packed getData() {
        return this.data;
    }

    public void setData(Packed packed) {
        if (!packed.equals((Object)this.data)) {
            this.data = packed;
            this.setDirty();
        }
    }

    public record Packed(List<Objective.Packed> objectives, List<Scoreboard.PackedScore> scores, Map<DisplaySlot, String> displaySlots, List<PlayerTeam.Packed> teams) {
        public static final Packed EMPTY = new Packed(List.of(), List.of(), Map.of(), List.of());
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Objective.Packed.CODEC.listOf().optionalFieldOf("Objectives", (Object)List.of()).forGetter(Packed::objectives), (App)Scoreboard.PackedScore.CODEC.listOf().optionalFieldOf("PlayerScores", (Object)List.of()).forGetter(Packed::scores), (App)Codec.unboundedMap(DisplaySlot.CODEC, (Codec)Codec.STRING).optionalFieldOf("DisplaySlots", (Object)Map.of()).forGetter(Packed::displaySlots), (App)PlayerTeam.Packed.CODEC.listOf().optionalFieldOf("Teams", (Object)List.of()).forGetter(Packed::teams)).apply((Applicative)instance, Packed::new));
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.resources;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public record WaypointStyle(int nearDistance, int farDistance, List<Identifier> sprites, List<Identifier> spriteLocations) {
    @VisibleForTesting
    public static final String ICON_LOCATION_PREFIX = "hud/locator_bar_dot/";
    public static final int DEFAULT_NEAR_DISTANCE = 128;
    public static final int DEFAULT_FAR_DISTANCE = 332;
    private static final Codec<Integer> DISTANCE_CODEC = Codec.intRange((int)0, (int)60000000);
    public static final Codec<WaypointStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)DISTANCE_CODEC.optionalFieldOf("near_distance", (Object)128).forGetter(WaypointStyle::nearDistance), (App)DISTANCE_CODEC.optionalFieldOf("far_distance", (Object)332).forGetter(WaypointStyle::farDistance), (App)ExtraCodecs.nonEmptyList(Identifier.CODEC.listOf()).fieldOf("sprites").forGetter(WaypointStyle::sprites)).apply((Applicative)instance, WaypointStyle::new)).validate(WaypointStyle::validate);

    public WaypointStyle(int i, int j, List<Identifier> list) {
        this(i, j, list, list.stream().map(identifier -> identifier.withPrefix(ICON_LOCATION_PREFIX)).toList());
    }

    @VisibleForTesting
    public DataResult<WaypointStyle> validate() {
        if (this.sprites.isEmpty()) {
            return DataResult.error(() -> "Must have at least one sprite icon");
        }
        if (this.nearDistance <= 0) {
            return DataResult.error(() -> "Near distance (" + this.nearDistance + ") must be greater than zero");
        }
        if (this.nearDistance >= this.farDistance) {
            return DataResult.error(() -> "Far distance (" + this.farDistance + ") cannot be closer or equal to near distance (" + this.nearDistance + ")");
        }
        return DataResult.success((Object)((Object)this));
    }

    public Identifier sprite(float f) {
        if (f < (float)this.nearDistance) {
            return (Identifier)this.spriteLocations.getFirst();
        }
        if (f >= (float)this.farDistance) {
            return (Identifier)this.spriteLocations.getLast();
        }
        if (this.spriteLocations.size() == 1) {
            return (Identifier)this.spriteLocations.getFirst();
        }
        if (this.spriteLocations.size() == 3) {
            return this.spriteLocations.get(1);
        }
        int i = Mth.lerpInt((f - (float)this.nearDistance) / (float)(this.farDistance - this.nearDistance), 1, this.spriteLocations.size() - 1);
        return this.spriteLocations.get(i);
    }
}


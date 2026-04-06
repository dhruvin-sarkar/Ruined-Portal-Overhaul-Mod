/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.saveddata.maps;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record MapFrame(BlockPos pos, int rotation, int entityId) {
    public static final Codec<MapFrame> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockPos.CODEC.fieldOf("pos").forGetter(MapFrame::pos), (App)Codec.INT.fieldOf("rotation").forGetter(MapFrame::rotation), (App)Codec.INT.fieldOf("entity_id").forGetter(MapFrame::entityId)).apply((Applicative)instance, MapFrame::new));

    public String getId() {
        return MapFrame.frameId(this.pos);
    }

    public static String frameId(BlockPos blockPos) {
        return "frame-" + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
    }
}


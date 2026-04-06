/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.level.storage;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

public interface LevelData {
    public RespawnData getRespawnData();

    public long getGameTime();

    public long getDayTime();

    public boolean isThundering();

    public boolean isRaining();

    public void setRaining(boolean var1);

    public boolean isHardcore();

    public Difficulty getDifficulty();

    public boolean isDifficultyLocked();

    default public void fillCrashReportCategory(CrashReportCategory crashReportCategory, LevelHeightAccessor levelHeightAccessor) {
        crashReportCategory.setDetail("Level spawn location", () -> CrashReportCategory.formatLocation(levelHeightAccessor, this.getRespawnData().pos()));
        crashReportCategory.setDetail("Level time", () -> String.format(Locale.ROOT, "%d game time, %d day time", this.getGameTime(), this.getDayTime()));
    }

    public record RespawnData(GlobalPos globalPos, float yaw, float pitch) {
        public static final RespawnData DEFAULT = new RespawnData(GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO), 0.0f, 0.0f);
        public static final MapCodec<RespawnData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)GlobalPos.MAP_CODEC.forGetter(RespawnData::globalPos), (App)Codec.floatRange((float)-180.0f, (float)180.0f).fieldOf("yaw").forGetter(RespawnData::yaw), (App)Codec.floatRange((float)-90.0f, (float)90.0f).fieldOf("pitch").forGetter(RespawnData::pitch)).apply((Applicative)instance, RespawnData::new));
        public static final Codec<RespawnData> CODEC = MAP_CODEC.codec();
        public static final StreamCodec<ByteBuf, RespawnData> STREAM_CODEC = StreamCodec.composite(GlobalPos.STREAM_CODEC, RespawnData::globalPos, ByteBufCodecs.FLOAT, RespawnData::yaw, ByteBufCodecs.FLOAT, RespawnData::pitch, RespawnData::new);

        public static RespawnData of(ResourceKey<Level> resourceKey, BlockPos blockPos, float f, float g) {
            return new RespawnData(GlobalPos.of(resourceKey, blockPos.immutable()), Mth.wrapDegrees(f), Mth.clamp(g, -90.0f, 90.0f));
        }

        public ResourceKey<Level> dimension() {
            return this.globalPos.dimension();
        }

        public BlockPos pos() {
            return this.globalPos.pos();
        }
    }
}


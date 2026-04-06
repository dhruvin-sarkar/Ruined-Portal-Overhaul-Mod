/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ibm.icu.text.DateFormat
 *  com.ibm.icu.text.SimpleDateFormat
 *  com.ibm.icu.util.Calendar
 *  com.ibm.icu.util.TimeZone
 *  com.ibm.icu.util.ULocale
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item.properties.select;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LocalTime
implements SelectItemModelProperty<String> {
    public static final String ROOT_LOCALE = "";
    private static final long UPDATE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1L);
    public static final Codec<String> VALUE_CODEC = Codec.STRING;
    private static final Codec<TimeZone> TIME_ZONE_CODEC = VALUE_CODEC.comapFlatMap(string -> {
        TimeZone timeZone = TimeZone.getTimeZone((String)string);
        if (timeZone.equals((Object)TimeZone.UNKNOWN_ZONE)) {
            return DataResult.error(() -> "Unknown timezone: " + string);
        }
        return DataResult.success((Object)timeZone);
    }, TimeZone::getID);
    private static final MapCodec<Data> DATA_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("pattern").forGetter(data -> data.format), (App)Codec.STRING.optionalFieldOf("locale", (Object)ROOT_LOCALE).forGetter(data -> data.localeId), (App)TIME_ZONE_CODEC.optionalFieldOf("time_zone").forGetter(data -> data.timeZone)).apply((Applicative)instance, Data::new));
    public static final SelectItemModelProperty.Type<LocalTime, String> TYPE = SelectItemModelProperty.Type.create(DATA_MAP_CODEC.flatXmap(LocalTime::create, localTime -> DataResult.success((Object)((Object)localTime.data))), VALUE_CODEC);
    private final Data data;
    private final DateFormat parsedFormat;
    private long nextUpdateTimeMs;
    private String lastResult = "";

    private LocalTime(Data data, DateFormat dateFormat) {
        this.data = data;
        this.parsedFormat = dateFormat;
    }

    public static LocalTime create(String string2, String string22, Optional<TimeZone> optional) {
        return (LocalTime)LocalTime.create(new Data(string2, string22, optional)).getOrThrow(string -> new IllegalStateException("Failed to validate format: " + string));
    }

    private static DataResult<LocalTime> create(Data data) {
        ULocale uLocale = new ULocale(data.localeId);
        Calendar calendar = data.timeZone.map(timeZone -> Calendar.getInstance((TimeZone)timeZone, (ULocale)uLocale)).orElseGet(() -> Calendar.getInstance((ULocale)uLocale));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(data.format, uLocale);
        simpleDateFormat.setCalendar(calendar);
        try {
            simpleDateFormat.format(new Date());
        }
        catch (Exception exception) {
            return DataResult.error(() -> "Invalid time format '" + String.valueOf(simpleDateFormat) + "': " + exception.getMessage());
        }
        return DataResult.success((Object)new LocalTime(data, (DateFormat)simpleDateFormat));
    }

    @Override
    public @Nullable String get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext) {
        long l = Util.getMillis();
        if (l > this.nextUpdateTimeMs) {
            this.lastResult = this.update();
            this.nextUpdateTimeMs = l + UPDATE_INTERVAL_MS;
        }
        return this.lastResult;
    }

    private String update() {
        return this.parsedFormat.format(new Date());
    }

    @Override
    public SelectItemModelProperty.Type<LocalTime, String> type() {
        return TYPE;
    }

    @Override
    public Codec<String> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    public /* synthetic */ @Nullable Object get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext) {
        return this.get(itemStack, clientLevel, livingEntity, i, itemDisplayContext);
    }

    @Environment(value=EnvType.CLIENT)
    static final class Data
    extends Record {
        final String format;
        final String localeId;
        final Optional<TimeZone> timeZone;

        Data(String string, String string2, Optional<TimeZone> optional) {
            this.format = string;
            this.localeId = string2;
            this.timeZone = optional;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Data.class, "format;localeId;timeZone", "format", "localeId", "timeZone"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Data.class, "format;localeId;timeZone", "format", "localeId", "timeZone"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Data.class, "format;localeId;timeZone", "format", "localeId", "timeZone"}, this, object);
        }

        public String format() {
            return this.format;
        }

        public String localeId() {
            return this.localeId;
        }

        public Optional<TimeZone> timeZone() {
            return this.timeZone;
        }
    }
}


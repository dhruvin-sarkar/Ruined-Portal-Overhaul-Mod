/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.item.Instrument;

public interface Instruments {
    public static final int GOAT_HORN_RANGE_BLOCKS = 256;
    public static final float GOAT_HORN_DURATION = 7.0f;
    public static final ResourceKey<Instrument> PONDER_GOAT_HORN = Instruments.create("ponder_goat_horn");
    public static final ResourceKey<Instrument> SING_GOAT_HORN = Instruments.create("sing_goat_horn");
    public static final ResourceKey<Instrument> SEEK_GOAT_HORN = Instruments.create("seek_goat_horn");
    public static final ResourceKey<Instrument> FEEL_GOAT_HORN = Instruments.create("feel_goat_horn");
    public static final ResourceKey<Instrument> ADMIRE_GOAT_HORN = Instruments.create("admire_goat_horn");
    public static final ResourceKey<Instrument> CALL_GOAT_HORN = Instruments.create("call_goat_horn");
    public static final ResourceKey<Instrument> YEARN_GOAT_HORN = Instruments.create("yearn_goat_horn");
    public static final ResourceKey<Instrument> DREAM_GOAT_HORN = Instruments.create("dream_goat_horn");

    private static ResourceKey<Instrument> create(String string) {
        return ResourceKey.create(Registries.INSTRUMENT, Identifier.withDefaultNamespace(string));
    }

    public static void bootstrap(BootstrapContext<Instrument> bootstrapContext) {
        Instruments.register(bootstrapContext, PONDER_GOAT_HORN, (Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0), 7.0f, 256.0f);
        Instruments.register(bootstrapContext, SING_GOAT_HORN, (Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(1), 7.0f, 256.0f);
        Instruments.register(bootstrapContext, SEEK_GOAT_HORN, (Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(2), 7.0f, 256.0f);
        Instruments.register(bootstrapContext, FEEL_GOAT_HORN, (Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(3), 7.0f, 256.0f);
        Instruments.register(bootstrapContext, ADMIRE_GOAT_HORN, (Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(4), 7.0f, 256.0f);
        Instruments.register(bootstrapContext, CALL_GOAT_HORN, (Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(5), 7.0f, 256.0f);
        Instruments.register(bootstrapContext, YEARN_GOAT_HORN, (Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(6), 7.0f, 256.0f);
        Instruments.register(bootstrapContext, DREAM_GOAT_HORN, (Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(7), 7.0f, 256.0f);
    }

    public static void register(BootstrapContext<Instrument> bootstrapContext, ResourceKey<Instrument> resourceKey, Holder<SoundEvent> holder, float f, float g) {
        MutableComponent mutableComponent = Component.translatable(Util.makeDescriptionId("instrument", resourceKey.identifier()));
        bootstrapContext.register(resourceKey, new Instrument(holder, f, g, mutableComponent));
    }
}


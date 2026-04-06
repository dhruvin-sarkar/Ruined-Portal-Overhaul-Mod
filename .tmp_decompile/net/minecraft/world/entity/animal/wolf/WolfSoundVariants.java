/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.wolf;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;

public class WolfSoundVariants {
    public static final ResourceKey<WolfSoundVariant> CLASSIC = WolfSoundVariants.createKey(SoundSet.CLASSIC);
    public static final ResourceKey<WolfSoundVariant> PUGLIN = WolfSoundVariants.createKey(SoundSet.PUGLIN);
    public static final ResourceKey<WolfSoundVariant> SAD = WolfSoundVariants.createKey(SoundSet.SAD);
    public static final ResourceKey<WolfSoundVariant> ANGRY = WolfSoundVariants.createKey(SoundSet.ANGRY);
    public static final ResourceKey<WolfSoundVariant> GRUMPY = WolfSoundVariants.createKey(SoundSet.GRUMPY);
    public static final ResourceKey<WolfSoundVariant> BIG = WolfSoundVariants.createKey(SoundSet.BIG);
    public static final ResourceKey<WolfSoundVariant> CUTE = WolfSoundVariants.createKey(SoundSet.CUTE);

    private static ResourceKey<WolfSoundVariant> createKey(SoundSet soundSet) {
        return ResourceKey.create(Registries.WOLF_SOUND_VARIANT, Identifier.withDefaultNamespace(soundSet.getIdentifier()));
    }

    public static void bootstrap(BootstrapContext<WolfSoundVariant> bootstrapContext) {
        WolfSoundVariants.register(bootstrapContext, CLASSIC, SoundSet.CLASSIC);
        WolfSoundVariants.register(bootstrapContext, PUGLIN, SoundSet.PUGLIN);
        WolfSoundVariants.register(bootstrapContext, SAD, SoundSet.SAD);
        WolfSoundVariants.register(bootstrapContext, ANGRY, SoundSet.ANGRY);
        WolfSoundVariants.register(bootstrapContext, GRUMPY, SoundSet.GRUMPY);
        WolfSoundVariants.register(bootstrapContext, BIG, SoundSet.BIG);
        WolfSoundVariants.register(bootstrapContext, CUTE, SoundSet.CUTE);
    }

    private static void register(BootstrapContext<WolfSoundVariant> bootstrapContext, ResourceKey<WolfSoundVariant> resourceKey, SoundSet soundSet) {
        bootstrapContext.register(resourceKey, SoundEvents.WOLF_SOUNDS.get((Object)soundSet));
    }

    public static Holder<WolfSoundVariant> pickRandomSoundVariant(RegistryAccess registryAccess, RandomSource randomSource) {
        return (Holder)registryAccess.lookupOrThrow(Registries.WOLF_SOUND_VARIANT).getRandom(randomSource).orElseThrow();
    }

    public static enum SoundSet {
        CLASSIC("classic", ""),
        PUGLIN("puglin", "_puglin"),
        SAD("sad", "_sad"),
        ANGRY("angry", "_angry"),
        GRUMPY("grumpy", "_grumpy"),
        BIG("big", "_big"),
        CUTE("cute", "_cute");

        private final String identifier;
        private final String soundEventSuffix;

        private SoundSet(String string2, String string3) {
            this.identifier = string2;
            this.soundEventSuffix = string3;
        }

        public String getIdentifier() {
            return this.identifier;
        }

        public String getSoundEventSuffix() {
            return this.soundEventSuffix;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.core.component.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;

public record JukeboxPlayablePredicate(Optional<HolderSet<JukeboxSong>> song) implements SingleComponentItemPredicate<JukeboxPlayable>
{
    public static final Codec<JukeboxPlayablePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.JUKEBOX_SONG).optionalFieldOf("song").forGetter(JukeboxPlayablePredicate::song)).apply((Applicative)instance, JukeboxPlayablePredicate::new));

    @Override
    public DataComponentType<JukeboxPlayable> componentType() {
        return DataComponents.JUKEBOX_PLAYABLE;
    }

    @Override
    public boolean matches(JukeboxPlayable jukeboxPlayable) {
        if (this.song.isPresent()) {
            boolean bl = false;
            for (Holder holder : this.song.get()) {
                Optional optional = holder.unwrapKey();
                if (optional.isEmpty() || !optional.equals(jukeboxPlayable.song().key())) continue;
                bl = true;
                break;
            }
            return bl;
        }
        return true;
    }

    public static JukeboxPlayablePredicate any() {
        return new JukeboxPlayablePredicate(Optional.empty());
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record LightningBoltPredicate(MinMaxBounds.Ints blocksSetOnFire, Optional<EntityPredicate> entityStruck) implements EntitySubPredicate
{
    public static final MapCodec<LightningBoltPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("blocks_set_on_fire", (Object)MinMaxBounds.Ints.ANY).forGetter(LightningBoltPredicate::blocksSetOnFire), (App)EntityPredicate.CODEC.optionalFieldOf("entity_struck").forGetter(LightningBoltPredicate::entityStruck)).apply((Applicative)instance, LightningBoltPredicate::new));

    public static LightningBoltPredicate blockSetOnFire(MinMaxBounds.Ints ints) {
        return new LightningBoltPredicate(ints, Optional.empty());
    }

    public MapCodec<LightningBoltPredicate> codec() {
        return EntitySubPredicates.LIGHTNING;
    }

    @Override
    public boolean matches(Entity entity2, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if (!(entity2 instanceof LightningBolt)) {
            return false;
        }
        LightningBolt lightningBolt = (LightningBolt)entity2;
        return this.blocksSetOnFire.matches(lightningBolt.getBlocksSetOnFire()) && (this.entityStruck.isEmpty() || lightningBolt.getHitEntities().anyMatch(entity -> this.entityStruck.get().matches(serverLevel, vec3, (Entity)entity)));
    }
}


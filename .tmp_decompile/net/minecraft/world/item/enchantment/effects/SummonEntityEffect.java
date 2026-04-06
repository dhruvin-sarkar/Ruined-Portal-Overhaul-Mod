/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record SummonEntityEffect(HolderSet<EntityType<?>> entityTypes, boolean joinTeam) implements EnchantmentEntityEffect
{
    public static final MapCodec<SummonEntityEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity").forGetter(SummonEntityEffect::entityTypes), (App)Codec.BOOL.optionalFieldOf("join_team", (Object)false).forGetter(SummonEntityEffect::joinTeam)).apply((Applicative)instance, SummonEntityEffect::new));

    @Override
    public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
        BlockPos blockPos = BlockPos.containing(vec3);
        if (!Level.isInSpawnableBounds(blockPos)) {
            return;
        }
        Optional<Holder<EntityType<?>>> optional = this.entityTypes().getRandomElement(serverLevel.getRandom());
        if (optional.isEmpty()) {
            return;
        }
        Object entity2 = optional.get().value().spawn(serverLevel, blockPos, EntitySpawnReason.TRIGGERED);
        if (entity2 == null) {
            return;
        }
        if (entity2 instanceof LightningBolt) {
            LightningBolt lightningBolt = (LightningBolt)entity2;
            LivingEntity livingEntity = enchantedItemInUse.owner();
            if (livingEntity instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                lightningBolt.setCause(serverPlayer);
            }
        }
        if (this.joinTeam && entity.getTeam() != null) {
            serverLevel.getScoreboard().addPlayerToTeam(((Entity)entity2).getScoreboardName(), entity.getTeam());
        }
        ((Entity)entity2).snapTo(vec3.x, vec3.y, vec3.z, ((Entity)entity2).getYRot(), ((Entity)entity2).getXRot());
    }

    public MapCodec<SummonEntityEffect> codec() {
        return CODEC;
    }
}


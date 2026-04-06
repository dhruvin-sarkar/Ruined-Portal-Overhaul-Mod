/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MobEffectsPredicate;
import net.minecraft.advancements.criterion.MovementPredicate;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.advancements.criterion.SlotsPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jspecify.annotations.Nullable;

public record EntityPredicate(Optional<EntityTypePredicate> entityType, Optional<DistancePredicate> distanceToPlayer, Optional<MovementPredicate> movement, LocationWrapper location, Optional<MobEffectsPredicate> effects, Optional<NbtPredicate> nbt, Optional<EntityFlagsPredicate> flags, Optional<EntityEquipmentPredicate> equipment, Optional<EntitySubPredicate> subPredicate, Optional<Integer> periodicTick, Optional<EntityPredicate> vehicle, Optional<EntityPredicate> passenger, Optional<EntityPredicate> targetedEntity, Optional<String> team, Optional<SlotsPredicate> slots, DataComponentMatchers components) {
    public static final Codec<EntityPredicate> CODEC = Codec.recursive((String)"EntityPredicate", codec -> RecordCodecBuilder.create(instance -> instance.group((App)EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(EntityPredicate::entityType), (App)DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(EntityPredicate::distanceToPlayer), (App)MovementPredicate.CODEC.optionalFieldOf("movement").forGetter(EntityPredicate::movement), (App)LocationWrapper.CODEC.forGetter(EntityPredicate::location), (App)MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(EntityPredicate::effects), (App)NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(EntityPredicate::nbt), (App)EntityFlagsPredicate.CODEC.optionalFieldOf("flags").forGetter(EntityPredicate::flags), (App)EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(EntityPredicate::equipment), (App)EntitySubPredicate.CODEC.optionalFieldOf("type_specific").forGetter(EntityPredicate::subPredicate), (App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("periodic_tick").forGetter(EntityPredicate::periodicTick), (App)codec.optionalFieldOf("vehicle").forGetter(EntityPredicate::vehicle), (App)codec.optionalFieldOf("passenger").forGetter(EntityPredicate::passenger), (App)codec.optionalFieldOf("targeted_entity").forGetter(EntityPredicate::targetedEntity), (App)Codec.STRING.optionalFieldOf("team").forGetter(EntityPredicate::team), (App)SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(EntityPredicate::slots), (App)DataComponentMatchers.CODEC.forGetter(EntityPredicate::components)).apply((Applicative)instance, EntityPredicate::new)));
    public static final Codec<ContextAwarePredicate> ADVANCEMENT_CODEC = Codec.withAlternative(ContextAwarePredicate.CODEC, CODEC, EntityPredicate::wrap);

    public static ContextAwarePredicate wrap(Builder builder) {
        return EntityPredicate.wrap(builder.build());
    }

    public static Optional<ContextAwarePredicate> wrap(Optional<EntityPredicate> optional) {
        return optional.map(EntityPredicate::wrap);
    }

    public static List<ContextAwarePredicate> wrap(Builder ... builders) {
        return Stream.of(builders).map(EntityPredicate::wrap).toList();
    }

    public static ContextAwarePredicate wrap(EntityPredicate entityPredicate) {
        LootItemCondition lootItemCondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, entityPredicate).build();
        return new ContextAwarePredicate(List.of((Object)lootItemCondition));
    }

    public boolean matches(ServerPlayer serverPlayer, @Nullable Entity entity) {
        return this.matches(serverPlayer.level(), serverPlayer.position(), entity);
    }

    public boolean matches(ServerLevel serverLevel, @Nullable Vec3 vec3, @Nullable Entity entity2) {
        PlayerTeam team;
        Vec3 vec32;
        if (entity2 == null) {
            return false;
        }
        if (this.entityType.isPresent() && !this.entityType.get().matches(entity2.getType())) {
            return false;
        }
        if (vec3 == null ? this.distanceToPlayer.isPresent() : this.distanceToPlayer.isPresent() && !this.distanceToPlayer.get().matches(vec3.x, vec3.y, vec3.z, entity2.getX(), entity2.getY(), entity2.getZ())) {
            return false;
        }
        if (this.movement.isPresent()) {
            vec32 = entity2.getKnownMovement();
            Vec3 vec33 = vec32.scale(20.0);
            if (!this.movement.get().matches(vec33.x, vec33.y, vec33.z, entity2.fallDistance)) {
                return false;
            }
        }
        if (this.location.located.isPresent() && !this.location.located.get().matches(serverLevel, entity2.getX(), entity2.getY(), entity2.getZ())) {
            return false;
        }
        if (this.location.steppingOn.isPresent()) {
            vec32 = Vec3.atCenterOf(entity2.getOnPos());
            if (!entity2.onGround() || !this.location.steppingOn.get().matches(serverLevel, vec32.x(), vec32.y(), vec32.z())) {
                return false;
            }
        }
        if (this.location.affectsMovement.isPresent()) {
            vec32 = Vec3.atCenterOf(entity2.getBlockPosBelowThatAffectsMyMovement());
            if (!this.location.affectsMovement.get().matches(serverLevel, vec32.x(), vec32.y(), vec32.z())) {
                return false;
            }
        }
        if (this.effects.isPresent() && !this.effects.get().matches(entity2)) {
            return false;
        }
        if (this.flags.isPresent() && !this.flags.get().matches(entity2)) {
            return false;
        }
        if (this.equipment.isPresent() && !this.equipment.get().matches(entity2)) {
            return false;
        }
        if (this.subPredicate.isPresent() && !this.subPredicate.get().matches(entity2, serverLevel, vec3)) {
            return false;
        }
        if (this.vehicle.isPresent() && !this.vehicle.get().matches(serverLevel, vec3, entity2.getVehicle())) {
            return false;
        }
        if (this.passenger.isPresent() && entity2.getPassengers().stream().noneMatch(entity -> this.passenger.get().matches(serverLevel, vec3, (Entity)entity))) {
            return false;
        }
        if (this.targetedEntity.isPresent() && !this.targetedEntity.get().matches(serverLevel, vec3, entity2 instanceof Mob ? ((Mob)entity2).getTarget() : null)) {
            return false;
        }
        if (this.periodicTick.isPresent() && entity2.tickCount % this.periodicTick.get() != 0) {
            return false;
        }
        if (this.team.isPresent() && ((team = entity2.getTeam()) == null || !this.team.get().equals(((Team)team).getName()))) {
            return false;
        }
        if (this.slots.isPresent() && !this.slots.get().matches(entity2)) {
            return false;
        }
        if (!this.components.test(entity2)) {
            return false;
        }
        return this.nbt.isEmpty() || this.nbt.get().matches(entity2);
    }

    public static LootContext createContext(ServerPlayer serverPlayer, Entity entity) {
        LootParams lootParams = new LootParams.Builder(serverPlayer.level()).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, serverPlayer.position()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
        return new LootContext.Builder(lootParams).create(Optional.empty());
    }

    public static final class LocationWrapper
    extends Record {
        final Optional<LocationPredicate> located;
        final Optional<LocationPredicate> steppingOn;
        final Optional<LocationPredicate> affectsMovement;
        public static final MapCodec<LocationWrapper> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LocationPredicate.CODEC.optionalFieldOf("location").forGetter(LocationWrapper::located), (App)LocationPredicate.CODEC.optionalFieldOf("stepping_on").forGetter(LocationWrapper::steppingOn), (App)LocationPredicate.CODEC.optionalFieldOf("movement_affected_by").forGetter(LocationWrapper::affectsMovement)).apply((Applicative)instance, LocationWrapper::new));

        public LocationWrapper(Optional<LocationPredicate> optional, Optional<LocationPredicate> optional2, Optional<LocationPredicate> optional3) {
            this.located = optional;
            this.steppingOn = optional2;
            this.affectsMovement = optional3;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LocationWrapper.class, "located;steppingOn;affectsMovement", "located", "steppingOn", "affectsMovement"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LocationWrapper.class, "located;steppingOn;affectsMovement", "located", "steppingOn", "affectsMovement"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LocationWrapper.class, "located;steppingOn;affectsMovement", "located", "steppingOn", "affectsMovement"}, this, object);
        }

        public Optional<LocationPredicate> located() {
            return this.located;
        }

        public Optional<LocationPredicate> steppingOn() {
            return this.steppingOn;
        }

        public Optional<LocationPredicate> affectsMovement() {
            return this.affectsMovement;
        }
    }

    public static class Builder {
        private Optional<EntityTypePredicate> entityType = Optional.empty();
        private Optional<DistancePredicate> distanceToPlayer = Optional.empty();
        private Optional<MovementPredicate> movement = Optional.empty();
        private Optional<LocationPredicate> located = Optional.empty();
        private Optional<LocationPredicate> steppingOnLocation = Optional.empty();
        private Optional<LocationPredicate> movementAffectedBy = Optional.empty();
        private Optional<MobEffectsPredicate> effects = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();
        private Optional<EntityFlagsPredicate> flags = Optional.empty();
        private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
        private Optional<EntitySubPredicate> subPredicate = Optional.empty();
        private Optional<Integer> periodicTick = Optional.empty();
        private Optional<EntityPredicate> vehicle = Optional.empty();
        private Optional<EntityPredicate> passenger = Optional.empty();
        private Optional<EntityPredicate> targetedEntity = Optional.empty();
        private Optional<String> team = Optional.empty();
        private Optional<SlotsPredicate> slots = Optional.empty();
        private DataComponentMatchers components = DataComponentMatchers.ANY;

        public static Builder entity() {
            return new Builder();
        }

        public Builder of(HolderGetter<EntityType<?>> holderGetter, EntityType<?> entityType) {
            this.entityType = Optional.of(EntityTypePredicate.of(holderGetter, entityType));
            return this;
        }

        public Builder of(HolderGetter<EntityType<?>> holderGetter, TagKey<EntityType<?>> tagKey) {
            this.entityType = Optional.of(EntityTypePredicate.of(holderGetter, tagKey));
            return this;
        }

        public Builder entityType(EntityTypePredicate entityTypePredicate) {
            this.entityType = Optional.of(entityTypePredicate);
            return this;
        }

        public Builder distance(DistancePredicate distancePredicate) {
            this.distanceToPlayer = Optional.of(distancePredicate);
            return this;
        }

        public Builder moving(MovementPredicate movementPredicate) {
            this.movement = Optional.of(movementPredicate);
            return this;
        }

        public Builder located(LocationPredicate.Builder builder) {
            this.located = Optional.of(builder.build());
            return this;
        }

        public Builder steppingOn(LocationPredicate.Builder builder) {
            this.steppingOnLocation = Optional.of(builder.build());
            return this;
        }

        public Builder movementAffectedBy(LocationPredicate.Builder builder) {
            this.movementAffectedBy = Optional.of(builder.build());
            return this;
        }

        public Builder effects(MobEffectsPredicate.Builder builder) {
            this.effects = builder.build();
            return this;
        }

        public Builder nbt(NbtPredicate nbtPredicate) {
            this.nbt = Optional.of(nbtPredicate);
            return this;
        }

        public Builder flags(EntityFlagsPredicate.Builder builder) {
            this.flags = Optional.of(builder.build());
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate.Builder builder) {
            this.equipment = Optional.of(builder.build());
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate entityEquipmentPredicate) {
            this.equipment = Optional.of(entityEquipmentPredicate);
            return this;
        }

        public Builder subPredicate(EntitySubPredicate entitySubPredicate) {
            this.subPredicate = Optional.of(entitySubPredicate);
            return this;
        }

        public Builder periodicTick(int i) {
            this.periodicTick = Optional.of(i);
            return this;
        }

        public Builder vehicle(Builder builder) {
            this.vehicle = Optional.of(builder.build());
            return this;
        }

        public Builder passenger(Builder builder) {
            this.passenger = Optional.of(builder.build());
            return this;
        }

        public Builder targetedEntity(Builder builder) {
            this.targetedEntity = Optional.of(builder.build());
            return this;
        }

        public Builder team(String string) {
            this.team = Optional.of(string);
            return this;
        }

        public Builder slots(SlotsPredicate slotsPredicate) {
            this.slots = Optional.of(slotsPredicate);
            return this;
        }

        public Builder components(DataComponentMatchers dataComponentMatchers) {
            this.components = dataComponentMatchers;
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(this.entityType, this.distanceToPlayer, this.movement, new LocationWrapper(this.located, this.steppingOnLocation, this.movementAffectedBy), this.effects, this.nbt, this.flags, this.equipment, this.subPredicate, this.periodicTick, this.vehicle, this.passenger, this.targetedEntity, this.team, this.slots, this.components);
        }
    }
}


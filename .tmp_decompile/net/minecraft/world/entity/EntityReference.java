/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.UUIDLookup;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class EntityReference<StoredEntityType extends UniquelyIdentifyable> {
    private static final Codec<? extends EntityReference<?>> CODEC = UUIDUtil.CODEC.xmap(EntityReference::new, EntityReference::getUUID);
    private static final StreamCodec<ByteBuf, ? extends EntityReference<?>> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(EntityReference::new, EntityReference::getUUID);
    private Either<UUID, StoredEntityType> entity;

    public static <Type extends UniquelyIdentifyable> Codec<EntityReference<Type>> codec() {
        return CODEC;
    }

    public static <Type extends UniquelyIdentifyable> StreamCodec<ByteBuf, EntityReference<Type>> streamCodec() {
        return STREAM_CODEC;
    }

    private EntityReference(StoredEntityType uniquelyIdentifyable) {
        this.entity = Either.right(uniquelyIdentifyable);
    }

    private EntityReference(UUID uUID) {
        this.entity = Either.left((Object)uUID);
    }

    public static <T extends UniquelyIdentifyable> @Nullable EntityReference<T> of(@Nullable T uniquelyIdentifyable) {
        return uniquelyIdentifyable != null ? new EntityReference<T>(uniquelyIdentifyable) : null;
    }

    public static <T extends UniquelyIdentifyable> EntityReference<T> of(UUID uUID) {
        return new EntityReference(uUID);
    }

    public UUID getUUID() {
        return (UUID)this.entity.map(uUID -> uUID, UniquelyIdentifyable::getUUID);
    }

    public @Nullable StoredEntityType getEntity(UUIDLookup<? extends UniquelyIdentifyable> uUIDLookup, Class<StoredEntityType> class_) {
        StoredEntityType uniquelyIdentifyable2;
        Optional optional2;
        Optional optional = this.entity.right();
        if (optional.isPresent()) {
            UniquelyIdentifyable uniquelyIdentifyable = (UniquelyIdentifyable)optional.get();
            if (uniquelyIdentifyable.isRemoved()) {
                this.entity = Either.left((Object)uniquelyIdentifyable.getUUID());
            } else {
                return (StoredEntityType)uniquelyIdentifyable;
            }
        }
        if ((optional2 = this.entity.left()).isPresent() && (uniquelyIdentifyable2 = this.resolve(uUIDLookup.lookup((UUID)optional2.get()), class_)) != null && !uniquelyIdentifyable2.isRemoved()) {
            this.entity = Either.right(uniquelyIdentifyable2);
            return uniquelyIdentifyable2;
        }
        return null;
    }

    public @Nullable StoredEntityType getEntity(Level level, Class<StoredEntityType> class_) {
        if (Player.class.isAssignableFrom(class_)) {
            return this.getEntity(level::getPlayerInAnyDimension, class_);
        }
        return this.getEntity(level::getEntityInAnyDimension, class_);
    }

    private @Nullable StoredEntityType resolve(@Nullable UniquelyIdentifyable uniquelyIdentifyable, Class<StoredEntityType> class_) {
        if (uniquelyIdentifyable != null && class_.isAssignableFrom(uniquelyIdentifyable.getClass())) {
            return (StoredEntityType)((UniquelyIdentifyable)class_.cast(uniquelyIdentifyable));
        }
        return null;
    }

    public boolean matches(StoredEntityType uniquelyIdentifyable) {
        return this.getUUID().equals(uniquelyIdentifyable.getUUID());
    }

    public void store(ValueOutput valueOutput, String string) {
        valueOutput.store(string, UUIDUtil.CODEC, this.getUUID());
    }

    public static void store(@Nullable EntityReference<?> entityReference, ValueOutput valueOutput, String string) {
        if (entityReference != null) {
            entityReference.store(valueOutput, string);
        }
    }

    public static <StoredEntityType extends UniquelyIdentifyable> @Nullable StoredEntityType get(@Nullable EntityReference<StoredEntityType> entityReference, Level level, Class<StoredEntityType> class_) {
        return entityReference != null ? (StoredEntityType)entityReference.getEntity(level, class_) : null;
    }

    public static @Nullable Entity getEntity(@Nullable EntityReference<Entity> entityReference, Level level) {
        return EntityReference.get(entityReference, level, Entity.class);
    }

    public static @Nullable LivingEntity getLivingEntity(@Nullable EntityReference<LivingEntity> entityReference, Level level) {
        return EntityReference.get(entityReference, level, LivingEntity.class);
    }

    public static @Nullable Player getPlayer(@Nullable EntityReference<Player> entityReference, Level level) {
        return EntityReference.get(entityReference, level, Player.class);
    }

    public static <StoredEntityType extends UniquelyIdentifyable> @Nullable EntityReference<StoredEntityType> read(ValueInput valueInput, String string) {
        return valueInput.read(string, EntityReference.codec()).orElse(null);
    }

    public static <StoredEntityType extends UniquelyIdentifyable> @Nullable EntityReference<StoredEntityType> readWithOldOwnerConversion(ValueInput valueInput, String string2, Level level) {
        Optional<UUID> optional = valueInput.read(string2, UUIDUtil.CODEC);
        if (optional.isPresent()) {
            return EntityReference.of(optional.get());
        }
        return valueInput.getString(string2).map(string -> OldUsersConverter.convertMobOwnerIfNecessary(level.getServer(), string)).map(EntityReference::new).orElse(null);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof EntityReference)) return false;
        EntityReference entityReference = (EntityReference)object;
        if (!this.getUUID().equals(entityReference.getUUID())) return false;
        return true;
    }

    public int hashCode() {
        return this.getUUID().hashCode();
    }
}


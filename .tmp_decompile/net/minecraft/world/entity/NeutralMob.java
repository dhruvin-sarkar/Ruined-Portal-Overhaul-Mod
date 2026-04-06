/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public interface NeutralMob {
    public static final String TAG_ANGER_END_TIME = "anger_end_time";
    public static final String TAG_ANGRY_AT = "angry_at";
    public static final long NO_ANGER_END_TIME = -1L;

    public long getPersistentAngerEndTime();

    default public void setTimeToRemainAngry(long l) {
        this.setPersistentAngerEndTime(this.level().getGameTime() + l);
    }

    public void setPersistentAngerEndTime(long var1);

    public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget();

    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> var1);

    public void startPersistentAngerTimer();

    public Level level();

    default public void addPersistentAngerSaveData(ValueOutput valueOutput) {
        valueOutput.putLong(TAG_ANGER_END_TIME, this.getPersistentAngerEndTime());
        valueOutput.storeNullable(TAG_ANGRY_AT, EntityReference.codec(), this.getPersistentAngerTarget());
    }

    default public void readPersistentAngerSaveData(Level level, ValueInput valueInput) {
        Optional<Long> optional = valueInput.getLong(TAG_ANGER_END_TIME);
        if (optional.isPresent()) {
            this.setPersistentAngerEndTime(optional.get());
        } else {
            Optional<Integer> optional2 = valueInput.getInt("AngerTime");
            if (optional2.isPresent()) {
                this.setTimeToRemainAngry(optional2.get().intValue());
            } else {
                this.setPersistentAngerEndTime(-1L);
            }
        }
        if (!(level instanceof ServerLevel)) {
            return;
        }
        this.setPersistentAngerTarget(EntityReference.read(valueInput, TAG_ANGRY_AT));
        this.setTarget(EntityReference.getLivingEntity(this.getPersistentAngerTarget(), level));
    }

    default public void updatePersistentAnger(ServerLevel serverLevel, boolean bl) {
        LivingEntity livingEntity = this.getTarget();
        EntityReference<LivingEntity> entityReference = this.getPersistentAngerTarget();
        if (livingEntity != null && livingEntity.isDeadOrDying() && entityReference != null && entityReference.matches(livingEntity) && livingEntity instanceof Mob) {
            this.stopBeingAngry();
            return;
        }
        if (livingEntity != null) {
            if (entityReference == null || !entityReference.matches(livingEntity)) {
                this.setPersistentAngerTarget(EntityReference.of(livingEntity));
            }
            this.startPersistentAngerTimer();
        }
        if (!(entityReference == null || this.isAngry() || livingEntity != null && NeutralMob.isValidPlayerTarget(livingEntity) && bl)) {
            this.stopBeingAngry();
        }
    }

    private static boolean isValidPlayerTarget(LivingEntity livingEntity) {
        Player player;
        return livingEntity instanceof Player && !(player = (Player)livingEntity).isCreative() && !player.isSpectator();
    }

    default public boolean isAngryAt(LivingEntity livingEntity, ServerLevel serverLevel) {
        if (!this.canAttack(livingEntity)) {
            return false;
        }
        if (NeutralMob.isValidPlayerTarget(livingEntity) && this.isAngryAtAllPlayers(serverLevel)) {
            return true;
        }
        EntityReference<LivingEntity> entityReference = this.getPersistentAngerTarget();
        return entityReference != null && entityReference.matches(livingEntity);
    }

    default public boolean isAngryAtAllPlayers(ServerLevel serverLevel) {
        return serverLevel.getGameRules().get(GameRules.UNIVERSAL_ANGER) != false && this.isAngry() && this.getPersistentAngerTarget() == null;
    }

    default public boolean isAngry() {
        long l = this.getPersistentAngerEndTime();
        if (l > 0L) {
            long m = l - this.level().getGameTime();
            return m > 0L;
        }
        return false;
    }

    default public void playerDied(ServerLevel serverLevel, Player player) {
        if (!serverLevel.getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS).booleanValue()) {
            return;
        }
        EntityReference<LivingEntity> entityReference = this.getPersistentAngerTarget();
        if (entityReference == null || !entityReference.matches(player)) {
            return;
        }
        this.stopBeingAngry();
    }

    default public void forgetCurrentTargetAndRefreshUniversalAnger() {
        this.stopBeingAngry();
        this.startPersistentAngerTimer();
    }

    default public void stopBeingAngry() {
        this.setLastHurtByMob(null);
        this.setPersistentAngerTarget(null);
        this.setTarget(null);
        this.setPersistentAngerEndTime(-1L);
    }

    public @Nullable LivingEntity getLastHurtByMob();

    public void setLastHurtByMob(@Nullable LivingEntity var1);

    public void setTarget(@Nullable LivingEntity var1);

    public boolean canAttack(LivingEntity var1);

    public @Nullable LivingEntity getTarget();
}


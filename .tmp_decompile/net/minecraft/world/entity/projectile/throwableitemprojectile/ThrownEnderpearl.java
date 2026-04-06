/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile.throwableitemprojectile;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ThrownEnderpearl
extends ThrowableItemProjectile {
    private long ticketTimer = 0L;

    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public ThrownEnderpearl(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(EntityType.ENDER_PEARL, livingEntity, level, itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void setOwner(@Nullable EntityReference<Entity> entityReference) {
        this.deregisterFromCurrentOwner();
        super.setOwner(entityReference);
        this.registerToCurrentOwner();
    }

    private void deregisterFromCurrentOwner() {
        Entity entity = this.getOwner();
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.deregisterEnderPearl(this);
        }
    }

    private void registerToCurrentOwner() {
        Entity entity = this.getOwner();
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.registerEnderPearl(this);
        }
    }

    @Override
    public @Nullable Entity getOwner() {
        Level level;
        if (this.owner == null || !((level = this.level()) instanceof ServerLevel)) {
            return super.getOwner();
        }
        ServerLevel serverLevel = (ServerLevel)level;
        return this.owner.getEntity(serverLevel, Entity.class);
    }

    private static @Nullable Entity findOwnerIncludingDeadPlayer(ServerLevel serverLevel, UUID uUID) {
        Entity entity = serverLevel.getEntityInAnyDimension(uUID);
        if (entity != null) {
            return entity;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(uUID);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        entityHitResult.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0f);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        ServerLevel serverLevel;
        block14: {
            block13: {
                super.onHit(hitResult);
                for (int i = 0; i < 32; ++i) {
                    this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian());
                }
                Level level = this.level();
                if (!(level instanceof ServerLevel)) break block13;
                serverLevel = (ServerLevel)level;
                if (!this.isRemoved()) break block14;
            }
            return;
        }
        Entity entity = this.getOwner();
        if (entity == null || !ThrownEnderpearl.isAllowedToTeleportOwner(entity, serverLevel)) {
            this.discard();
            return;
        }
        Vec3 vec3 = this.oldPosition();
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            if (serverPlayer.connection.isAcceptingMessages()) {
                ServerPlayer serverPlayer2;
                Endermite endermite;
                if (this.random.nextFloat() < 0.05f && serverLevel.isSpawningMonsters() && (endermite = EntityType.ENDERMITE.create(serverLevel, EntitySpawnReason.TRIGGERED)) != null) {
                    endermite.snapTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                    serverLevel.addFreshEntity(endermite);
                }
                if (this.isOnPortalCooldown()) {
                    entity.setPortalCooldown();
                }
                if ((serverPlayer2 = serverPlayer.teleport(new TeleportTransition(serverLevel, vec3, Vec3.ZERO, 0.0f, 0.0f, Relative.union(Relative.ROTATION, Relative.DELTA), TeleportTransition.DO_NOTHING))) != null) {
                    serverPlayer2.resetFallDistance();
                    serverPlayer2.resetCurrentImpulseContext();
                    serverPlayer2.hurtServer(serverPlayer.level(), this.damageSources().enderPearl(), 5.0f);
                }
                this.playSound(serverLevel, vec3);
            }
        } else {
            Entity entity2 = entity.teleport(new TeleportTransition(serverLevel, vec3, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), TeleportTransition.DO_NOTHING));
            if (entity2 != null) {
                entity2.resetFallDistance();
            }
            this.playSound(serverLevel, vec3);
        }
        this.discard();
    }

    private static boolean isAllowedToTeleportOwner(Entity entity, Level level) {
        if (entity.level().dimension() == level.dimension()) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                return livingEntity.isAlive() && !livingEntity.isSleeping();
            }
            return entity.isAlive();
        }
        return entity.canUsePortal(true);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void tick() {
        var2_1 = this.level();
        if (!(var2_1 instanceof ServerLevel)) {
            super.tick();
            return;
        }
        serverLevel = (ServerLevel)var2_1;
        i = SectionPos.blockToSectionCoord(this.position().x());
        j = SectionPos.blockToSectionCoord(this.position().z());
        v0 = entity = this.owner != null ? ThrownEnderpearl.findOwnerIncludingDeadPlayer(serverLevel, this.owner.getUUID()) : null;
        if (!(entity instanceof ServerPlayer)) ** GOTO lbl-1000
        serverPlayer = (ServerPlayer)entity;
        if (!entity.isAlive() && !serverPlayer.wonGame && serverPlayer.level().getGameRules().get(GameRules.ENDER_PEARLS_VANISH_ON_DEATH).booleanValue()) {
            this.discard();
        } else lbl-1000:
        // 2 sources

        {
            super.tick();
        }
        if (!this.isAlive()) {
            return;
        }
        blockPos = BlockPos.containing(this.position());
        if ((--this.ticketTimer <= 0L || i != SectionPos.blockToSectionCoord(blockPos.getX()) || j != SectionPos.blockToSectionCoord(blockPos.getZ())) && entity instanceof ServerPlayer) {
            serverPlayer2 = (ServerPlayer)entity;
            this.ticketTimer = serverPlayer2.registerAndUpdateEnderPearlTicket(this);
        }
    }

    private void playSound(Level level, Vec3 vec3) {
        level.playSound(null, vec3.x, vec3.y, vec3.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
    }

    @Override
    public @Nullable Entity teleport(TeleportTransition teleportTransition) {
        Entity entity = super.teleport(teleportTransition);
        if (entity != null) {
            entity.placePortalTicket(BlockPos.containing(entity.position()));
        }
        return entity;
    }

    @Override
    public boolean canTeleport(Level level, Level level2) {
        Entity entity;
        if (level.dimension() == Level.END && level2.dimension() == Level.OVERWORLD && (entity = this.getOwner()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            return super.canTeleport(level, level2) && serverPlayer.seenCredits;
        }
        return super.canTeleport(level, level2);
    }

    @Override
    protected void onInsideBlock(BlockState blockState) {
        Entity entity;
        super.onInsideBlock(blockState);
        if (blockState.is(Blocks.END_GATEWAY) && (entity = this.getOwner()) instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.onInsideBlock(blockState);
        }
    }

    @Override
    public void onRemoval(Entity.RemovalReason removalReason) {
        if (removalReason != Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            this.deregisterFromCurrentOwner();
        }
        super.onRemoval(removalReason);
    }

    @Override
    public void onAboveBubbleColumn(boolean bl, BlockPos blockPos) {
        Entity.handleOnAboveBubbleColumn(this, bl, blockPos);
    }

    @Override
    public void onInsideBubbleColumn(boolean bl) {
        Entity.handleOnInsideBubbleColumn(this, bl);
    }
}


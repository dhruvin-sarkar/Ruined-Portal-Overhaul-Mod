/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LlamaSpit
extends Projectile {
    public LlamaSpit(EntityType<? extends LlamaSpit> entityType, Level level) {
        super((EntityType<? extends Projectile>)entityType, level);
    }

    public LlamaSpit(Level level, Llama llama) {
        this((EntityType<? extends LlamaSpit>)EntityType.LLAMA_SPIT, level);
        this.setOwner(llama);
        this.setPos(llama.getX() - (double)(llama.getBbWidth() + 1.0f) * 0.5 * (double)Mth.sin(llama.yBodyRot * ((float)Math.PI / 180)), llama.getEyeY() - (double)0.1f, llama.getZ() + (double)(llama.getBbWidth() + 1.0f) * 0.5 * (double)Mth.cos(llama.yBodyRot * ((float)Math.PI / 180)));
    }

    @Override
    protected double getDefaultGravity() {
        return 0.06;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 vec3 = this.getDeltaMovement();
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        this.hitTargetOrDeflectSelf(hitResult);
        double d = this.getX() + vec3.x;
        double e = this.getY() + vec3.y;
        double f = this.getZ() + vec3.z;
        this.updateRotation();
        float g = 0.99f;
        if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir)) {
            this.discard();
            return;
        }
        if (this.isInWater()) {
            this.discard();
            return;
        }
        this.setDeltaMovement(vec3.scale(0.99f));
        this.applyGravity();
        this.setPos(d, e, f);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            ServerLevel serverLevel;
            LivingEntity livingEntity = (LivingEntity)entity;
            Entity entity2 = entityHitResult.getEntity();
            DamageSource damageSource = this.damageSources().spit(this, livingEntity);
            Level level = this.level();
            if (level instanceof ServerLevel && entity2.hurtServer(serverLevel = (ServerLevel)level, damageSource, 1.0f)) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, entity2, damageSource);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        Vec3 vec3 = clientboundAddEntityPacket.getMovement();
        for (int i = 0; i < 7; ++i) {
            double d = 0.4 + 0.1 * (double)i;
            this.level().addParticle(ParticleTypes.SPIT, this.getX(), this.getY(), this.getZ(), vec3.x * d, vec3.y, vec3.z * d);
        }
        this.setDeltaMovement(vec3);
    }
}


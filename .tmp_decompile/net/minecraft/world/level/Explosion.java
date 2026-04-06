/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level;

import java.lang.runtime.SwitchBootstraps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface Explosion {
    public static DamageSource getDefaultDamageSource(Level level, @Nullable Entity entity) {
        return level.damageSources().explosion(entity, Explosion.getIndirectSourceEntity(entity));
    }

    public static @Nullable LivingEntity getIndirectSourceEntity(@Nullable Entity entity) {
        LivingEntity livingEntity;
        Entity entity2 = entity;
        int n = 0;
        block5: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PrimedTnt.class, LivingEntity.class, Projectile.class}, (Object)entity2, (int)n)) {
                case 0: {
                    PrimedTnt primedTnt = (PrimedTnt)entity2;
                    livingEntity = primedTnt.getOwner();
                    break block5;
                }
                case 1: {
                    LivingEntity livingEntity2;
                    livingEntity = livingEntity2 = (LivingEntity)entity2;
                    break block5;
                }
                case 2: {
                    Projectile projectile = (Projectile)entity2;
                    Entity entity3 = projectile.getOwner();
                    if (!(entity3 instanceof LivingEntity)) {
                        n = 3;
                        continue block5;
                    }
                    LivingEntity livingEntity2 = (LivingEntity)entity3;
                    livingEntity = livingEntity2;
                    break block5;
                }
                default: {
                    livingEntity = null;
                    break block5;
                }
            }
            break;
        }
        return livingEntity;
    }

    public ServerLevel level();

    public BlockInteraction getBlockInteraction();

    public @Nullable LivingEntity getIndirectSourceEntity();

    public @Nullable Entity getDirectSourceEntity();

    public float radius();

    public Vec3 center();

    public boolean canTriggerBlocks();

    public boolean shouldAffectBlocklikeEntities();

    public static enum BlockInteraction {
        KEEP(false),
        DESTROY(true),
        DESTROY_WITH_DECAY(true),
        TRIGGER_BLOCK(false);

        private final boolean shouldAffectBlocklikeEntities;

        private BlockInteraction(boolean bl) {
            this.shouldAffectBlocklikeEntities = bl;
        }

        public boolean shouldAffectBlocklikeEntities() {
            return this.shouldAffectBlocklikeEntities;
        }
    }
}


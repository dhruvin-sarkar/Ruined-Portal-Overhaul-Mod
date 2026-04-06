/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ProjectileUtil {
    public static final float DEFAULT_ENTITY_HIT_RESULT_MARGIN = 0.3f;

    public static HitResult getHitResultOnMoveVector(Entity entity, Predicate<Entity> predicate) {
        Vec3 vec3 = entity.getDeltaMovement();
        Level level = entity.level();
        Vec3 vec32 = entity.position();
        return ProjectileUtil.getHitResult(vec32, entity, predicate, vec3, level, ProjectileUtil.computeMargin(entity), ClipContext.Block.COLLIDER);
    }

    public static Either<BlockHitResult, Collection<EntityHitResult>> getHitEntitiesAlong(Entity entity, AttackRange attackRange, Predicate<Entity> predicate, ClipContext.Block block) {
        Vec3 vec3 = entity.getHeadLookAngle();
        Vec3 vec32 = entity.getEyePosition();
        Vec3 vec33 = vec32.add(vec3.scale(attackRange.effectiveMinRange(entity)));
        double d = entity.getKnownMovement().dot(vec3);
        Vec3 vec34 = vec32.add(vec3.scale((double)attackRange.effectiveMaxRange(entity) + Math.max(0.0, d)));
        return ProjectileUtil.getHitEntitiesAlong(entity, vec32, vec33, predicate, vec34, attackRange.hitboxMargin(), block);
    }

    public static HitResult getHitResultOnMoveVector(Entity entity, Predicate<Entity> predicate, ClipContext.Block block) {
        Vec3 vec3 = entity.getDeltaMovement();
        Level level = entity.level();
        Vec3 vec32 = entity.position();
        return ProjectileUtil.getHitResult(vec32, entity, predicate, vec3, level, ProjectileUtil.computeMargin(entity), block);
    }

    public static HitResult getHitResultOnViewVector(Entity entity, Predicate<Entity> predicate, double d) {
        Vec3 vec3 = entity.getViewVector(0.0f).scale(d);
        Level level = entity.level();
        Vec3 vec32 = entity.getEyePosition();
        return ProjectileUtil.getHitResult(vec32, entity, predicate, vec3, level, 0.0f, ClipContext.Block.COLLIDER);
    }

    private static HitResult getHitResult(Vec3 vec3, Entity entity, Predicate<Entity> predicate, Vec3 vec32, Level level, float f, ClipContext.Block block) {
        EntityHitResult hitResult2;
        Vec3 vec33 = vec3.add(vec32);
        HitResult hitResult = level.clipIncludingBorder(new ClipContext(vec3, vec33, block, ClipContext.Fluid.NONE, entity));
        if (((HitResult)hitResult).getType() != HitResult.Type.MISS) {
            vec33 = hitResult.getLocation();
        }
        if ((hitResult2 = ProjectileUtil.getEntityHitResult(level, entity, vec3, vec33, entity.getBoundingBox().expandTowards(vec32).inflate(1.0), predicate, f)) != null) {
            hitResult = hitResult2;
        }
        return hitResult;
    }

    private static Either<BlockHitResult, Collection<EntityHitResult>> getHitEntitiesAlong(Entity entity, Vec3 vec3, Vec3 vec32, Predicate<Entity> predicate, Vec3 vec33, float f, ClipContext.Block block) {
        Level level = entity.level();
        BlockHitResult blockHitResult = level.clipIncludingBorder(new ClipContext(vec3, vec33, block, ClipContext.Fluid.NONE, entity));
        if (blockHitResult.getType() != HitResult.Type.MISS && vec3.distanceToSqr(vec33 = blockHitResult.getLocation()) < vec3.distanceToSqr(vec32)) {
            return Either.left((Object)blockHitResult);
        }
        AABB aABB = AABB.ofSize(vec32, f, f, f).expandTowards(vec33.subtract(vec32)).inflate(1.0);
        Collection<EntityHitResult> collection = ProjectileUtil.getManyEntityHitResult(level, entity, vec32, vec33, aABB, predicate, f, block, true);
        if (!collection.isEmpty()) {
            return Either.right(collection);
        }
        return Either.left((Object)blockHitResult);
    }

    public static @Nullable EntityHitResult getEntityHitResult(Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate, double d) {
        Level level = entity.level();
        double e = d;
        Entity entity2 = null;
        Vec3 vec33 = null;
        for (Entity entity3 : level.getEntities(entity, aABB, predicate)) {
            Vec3 vec34;
            double f;
            AABB aABB2 = entity3.getBoundingBox().inflate(entity3.getPickRadius());
            Optional<Vec3> optional = aABB2.clip(vec3, vec32);
            if (aABB2.contains(vec3)) {
                if (!(e >= 0.0)) continue;
                entity2 = entity3;
                vec33 = optional.orElse(vec3);
                e = 0.0;
                continue;
            }
            if (!optional.isPresent() || !((f = vec3.distanceToSqr(vec34 = optional.get())) < e) && e != 0.0) continue;
            if (entity3.getRootVehicle() == entity.getRootVehicle()) {
                if (e != 0.0) continue;
                entity2 = entity3;
                vec33 = vec34;
                continue;
            }
            entity2 = entity3;
            vec33 = vec34;
            e = f;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2, vec33);
    }

    public static @Nullable EntityHitResult getEntityHitResult(Level level, Projectile projectile, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate) {
        return ProjectileUtil.getEntityHitResult(level, projectile, vec3, vec32, aABB, predicate, ProjectileUtil.computeMargin(projectile));
    }

    public static float computeMargin(Entity entity) {
        return Math.max(0.0f, Math.min(0.3f, (float)(entity.tickCount - 2) / 20.0f));
    }

    public static @Nullable EntityHitResult getEntityHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate, float f) {
        double d = Double.MAX_VALUE;
        Optional<Object> optional = Optional.empty();
        Entity entity2 = null;
        for (Entity entity3 : level.getEntities(entity, aABB, predicate)) {
            double e;
            AABB aABB2 = entity3.getBoundingBox().inflate(f);
            Optional<Vec3> optional2 = aABB2.clip(vec3, vec32);
            if (!optional2.isPresent() || !((e = vec3.distanceToSqr(optional2.get())) < d)) continue;
            entity2 = entity3;
            d = e;
            optional = optional2;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2, (Vec3)optional.get());
    }

    public static Collection<EntityHitResult> getManyEntityHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate, boolean bl) {
        return ProjectileUtil.getManyEntityHitResult(level, entity, vec3, vec32, aABB, predicate, ProjectileUtil.computeMargin(entity), ClipContext.Block.COLLIDER, bl);
    }

    public static Collection<EntityHitResult> getManyEntityHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate, float f, ClipContext.Block block, boolean bl) {
        ArrayList<EntityHitResult> list = new ArrayList<EntityHitResult>();
        for (Entity entity2 : level.getEntities(entity, aABB, predicate)) {
            Optional<Vec3> optional3;
            Vec3 vec34;
            Optional<Vec3> optional2;
            AABB aABB2 = entity2.getBoundingBox();
            if (bl && aABB2.contains(vec3)) {
                list.add(new EntityHitResult(entity2, vec3));
                continue;
            }
            Optional<Vec3> optional = aABB2.clip(vec3, vec32);
            if (optional.isPresent()) {
                list.add(new EntityHitResult(entity2, optional.get()));
                continue;
            }
            if ((double)f <= 0.0 || (optional2 = aABB2.inflate(f).clip(vec3, vec32)).isEmpty()) continue;
            Vec3 vec33 = optional2.get();
            BlockHitResult blockHitResult = level.clipIncludingBorder(new ClipContext(vec33, vec34 = aABB2.getCenter(), block, ClipContext.Fluid.NONE, entity));
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                vec34 = blockHitResult.getLocation();
            }
            if (!(optional3 = entity2.getBoundingBox().clip(vec33, vec34)).isPresent()) continue;
            list.add(new EntityHitResult(entity2, optional3.get()));
        }
        return list;
    }

    public static void rotateTowardsMovement(Entity entity, float f) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.lengthSqr() == 0.0) {
            return;
        }
        double d = vec3.horizontalDistance();
        entity.setYRot((float)(Mth.atan2(vec3.z, vec3.x) * 57.2957763671875) + 90.0f);
        entity.setXRot((float)(Mth.atan2(d, vec3.y) * 57.2957763671875) - 90.0f);
        while (entity.getXRot() - entity.xRotO < -180.0f) {
            entity.xRotO -= 360.0f;
        }
        while (entity.getXRot() - entity.xRotO >= 180.0f) {
            entity.xRotO += 360.0f;
        }
        while (entity.getYRot() - entity.yRotO < -180.0f) {
            entity.yRotO -= 360.0f;
        }
        while (entity.getYRot() - entity.yRotO >= 180.0f) {
            entity.yRotO += 360.0f;
        }
        entity.setXRot(Mth.lerp(f, entity.xRotO, entity.getXRot()));
        entity.setYRot(Mth.lerp(f, entity.yRotO, entity.getYRot()));
    }

    public static InteractionHand getWeaponHoldingHand(LivingEntity livingEntity, Item item) {
        return livingEntity.getMainHandItem().is(item) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static AbstractArrow getMobArrow(LivingEntity livingEntity, ItemStack itemStack, float f, @Nullable ItemStack itemStack2) {
        ArrowItem arrowItem = (ArrowItem)(itemStack.getItem() instanceof ArrowItem ? itemStack.getItem() : Items.ARROW);
        AbstractArrow abstractArrow = arrowItem.createArrow(livingEntity.level(), itemStack, livingEntity, itemStack2);
        abstractArrow.setBaseDamageFromMob(f);
        return abstractArrow;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.BoatGoals;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FollowBoatGoal
extends Goal {
    private int timeToRecalcPath;
    private final PathfinderMob mob;
    private @Nullable Player following;
    private BoatGoals currentGoal;

    public FollowBoatGoal(PathfinderMob pathfinderMob) {
        this.mob = pathfinderMob;
    }

    @Override
    public boolean canUse() {
        if (this.following != null && this.following.hasMovedHorizontallyRecently()) {
            return true;
        }
        List<AbstractBoat> list = this.mob.level().getEntitiesOfClass(AbstractBoat.class, this.mob.getBoundingBox().inflate(5.0));
        for (AbstractBoat abstractBoat : list) {
            Player player;
            LivingEntity livingEntity = abstractBoat.getControllingPassenger();
            if (!(livingEntity instanceof Player) || !(player = (Player)livingEntity).hasMovedHorizontallyRecently()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.following != null && this.following.isPassenger() && this.following.hasMovedHorizontallyRecently();
    }

    @Override
    public void start() {
        List<AbstractBoat> list = this.mob.level().getEntitiesOfClass(AbstractBoat.class, this.mob.getBoundingBox().inflate(5.0));
        for (AbstractBoat abstractBoat : list) {
            Player player;
            LivingEntity livingEntity = abstractBoat.getControllingPassenger();
            if (!(livingEntity instanceof Player)) continue;
            this.following = player = (Player)livingEntity;
            break;
        }
        this.timeToRecalcPath = 0;
        this.currentGoal = BoatGoals.GO_TO_BOAT;
    }

    @Override
    public void stop() {
        this.following = null;
    }

    @Override
    public void tick() {
        float f = this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION ? 0.01f : 0.015f;
        this.mob.moveRelative(f, new Vec3(this.mob.xxa, this.mob.yya, this.mob.zza));
        this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
        if (--this.timeToRecalcPath > 0) {
            return;
        }
        this.timeToRecalcPath = this.adjustedTickDelay(10);
        if (this.currentGoal == BoatGoals.GO_TO_BOAT) {
            BlockPos blockPos = this.following.blockPosition().relative(this.following.getDirection().getOpposite());
            blockPos = blockPos.offset(0, -1, 0);
            this.mob.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0);
            if (this.mob.distanceTo(this.following) < 4.0f) {
                this.timeToRecalcPath = 0;
                this.currentGoal = BoatGoals.GO_IN_BOAT_DIRECTION;
            }
        } else if (this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION) {
            Direction direction = this.following.getMotionDirection();
            BlockPos blockPos2 = this.following.blockPosition().relative(direction, 10);
            this.mob.getNavigation().moveTo(blockPos2.getX(), blockPos2.getY() - 1, blockPos2.getZ(), 1.0);
            if (this.mob.distanceTo(this.following) > 12.0f) {
                this.timeToRecalcPath = 0;
                this.currentGoal = BoatGoals.GO_TO_BOAT;
            }
        }
    }
}


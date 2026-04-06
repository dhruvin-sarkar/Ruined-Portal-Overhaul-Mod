/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RandomStroll {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;
    private static final int[][] SWIM_XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

    public static OneShot<PathfinderMob> stroll(float f) {
        return RandomStroll.stroll(f, true);
    }

    public static OneShot<PathfinderMob> stroll(float f, boolean bl) {
        return RandomStroll.strollFlyOrSwim(f, pathfinderMob -> LandRandomPos.getPos(pathfinderMob, 10, 7), bl ? pathfinderMob -> true : pathfinderMob -> !pathfinderMob.isInWater());
    }

    public static BehaviorControl<PathfinderMob> stroll(float f, int i, int j) {
        return RandomStroll.strollFlyOrSwim(f, pathfinderMob -> LandRandomPos.getPos(pathfinderMob, i, j), pathfinderMob -> true);
    }

    public static BehaviorControl<PathfinderMob> fly(float f) {
        return RandomStroll.strollFlyOrSwim(f, pathfinderMob -> RandomStroll.getTargetFlyPos(pathfinderMob, 10, 7), pathfinderMob -> true);
    }

    public static BehaviorControl<PathfinderMob> swim(float f) {
        return RandomStroll.strollFlyOrSwim(f, RandomStroll::getTargetSwimPos, Entity::isInWater);
    }

    private static OneShot<PathfinderMob> strollFlyOrSwim(float f, Function<PathfinderMob, Vec3> function, Predicate<PathfinderMob> predicate) {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET)).apply((Applicative)instance, memoryAccessor -> (serverLevel, pathfinderMob, l) -> {
            if (!predicate.test((PathfinderMob)pathfinderMob)) {
                return false;
            }
            Optional<Vec3> optional = Optional.ofNullable((Vec3)function.apply((PathfinderMob)pathfinderMob));
            memoryAccessor.setOrErase(optional.map(vec3 -> new WalkTarget((Vec3)vec3, f, 0)));
            return true;
        }));
    }

    private static @Nullable Vec3 getTargetSwimPos(PathfinderMob pathfinderMob) {
        Vec3 vec3 = null;
        Vec3 vec32 = null;
        for (int[] is : SWIM_XY_DISTANCE_TIERS) {
            vec32 = vec3 == null ? BehaviorUtils.getRandomSwimmablePos(pathfinderMob, is[0], is[1]) : pathfinderMob.position().add(pathfinderMob.position().vectorTo(vec3).normalize().multiply(is[0], is[1], is[0]));
            boolean bl = GoalUtils.mobRestricted(pathfinderMob, is[0]);
            if (vec32 == null || pathfinderMob.level().getFluidState(BlockPos.containing(vec32)).isEmpty() || GoalUtils.isRestricted(bl, pathfinderMob, vec32)) {
                return vec3;
            }
            vec3 = vec32;
        }
        return vec32;
    }

    private static @Nullable Vec3 getTargetFlyPos(PathfinderMob pathfinderMob, int i, int j) {
        Vec3 vec3 = pathfinderMob.getViewVector(0.0f);
        return AirAndWaterRandomPos.getPos(pathfinderMob, i, j, -2, vec3.x, vec3.z, 1.5707963705062866);
    }
}


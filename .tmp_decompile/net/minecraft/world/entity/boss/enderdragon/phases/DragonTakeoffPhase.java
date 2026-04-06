/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DragonTakeoffPhase
extends AbstractDragonPhaseInstance {
    private boolean firstTick;
    private @Nullable Path currentPath;
    private @Nullable Vec3 targetLocation;

    public DragonTakeoffPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doServerTick(ServerLevel serverLevel) {
        if (this.firstTick || this.currentPath == null) {
            this.firstTick = false;
            this.findNewTarget();
        } else {
            BlockPos blockPos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.dragon.getFightOrigin()));
            if (!blockPos.closerToCenterThan(this.dragon.position(), 10.0)) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            }
        }
    }

    @Override
    public void begin() {
        this.firstTick = true;
        this.currentPath = null;
        this.targetLocation = null;
    }

    private void findNewTarget() {
        int i = this.dragon.findClosestNode();
        Vec3 vec3 = this.dragon.getHeadLookVector(1.0f);
        int j = this.dragon.findClosestNode(-vec3.x * 40.0, 105.0, -vec3.z * 40.0);
        if (this.dragon.getDragonFight() == null || this.dragon.getDragonFight().getCrystalsAlive() <= 0) {
            j -= 12;
            j &= 7;
            j += 12;
        } else if ((j %= 12) < 0) {
            j += 12;
        }
        this.currentPath = this.dragon.findPath(i, j, null);
        this.navigateToNextPathNode();
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null) {
            this.currentPath.advance();
            if (!this.currentPath.isDone()) {
                double d;
                BlockPos vec3i = this.currentPath.getNextNodePos();
                this.currentPath.advance();
                while ((d = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0f)) < (double)vec3i.getY()) {
                }
                this.targetLocation = new Vec3(vec3i.getX(), d, vec3i.getZ());
            }
        }
    }

    @Override
    public @Nullable Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    public EnderDragonPhase<DragonTakeoffPhase> getPhase() {
        return EnderDragonPhase.TAKEOFF;
    }
}


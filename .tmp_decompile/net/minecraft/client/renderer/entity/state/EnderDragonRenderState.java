/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.boss.enderdragon.DragonFlightHistory;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EnderDragonRenderState
extends EntityRenderState {
    public float flapTime;
    public float deathTime;
    public boolean hasRedOverlay;
    public @Nullable Vec3 beamOffset;
    public boolean isLandingOrTakingOff;
    public boolean isSitting;
    public double distanceToEgg;
    public float partialTicks;
    public final DragonFlightHistory flightHistory = new DragonFlightHistory();

    public DragonFlightHistory.Sample getHistoricalPos(int i) {
        return this.flightHistory.get(i, this.partialTicks);
    }

    public float getHeadPartYOffset(int i, DragonFlightHistory.Sample sample, DragonFlightHistory.Sample sample2) {
        double d = this.isLandingOrTakingOff ? (double)i / Math.max(this.distanceToEgg / 4.0, 1.0) : (this.isSitting ? (double)i : (i == 6 ? 0.0 : sample2.y() - sample.y()));
        return (float)d;
    }
}


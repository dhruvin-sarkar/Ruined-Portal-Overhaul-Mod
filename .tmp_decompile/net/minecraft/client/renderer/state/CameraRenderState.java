/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionf
 */
package net.minecraft.client.renderer.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class CameraRenderState {
    public BlockPos blockPos = BlockPos.ZERO;
    public Vec3 pos = new Vec3(0.0, 0.0, 0.0);
    public boolean initialized;
    public Vec3 entityPos = new Vec3(0.0, 0.0, 0.0);
    public Quaternionf orientation = new Quaternionf();
}


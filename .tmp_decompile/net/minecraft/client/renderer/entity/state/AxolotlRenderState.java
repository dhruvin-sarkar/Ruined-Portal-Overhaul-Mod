/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

@Environment(value=EnvType.CLIENT)
public class AxolotlRenderState
extends LivingEntityRenderState {
    public Axolotl.Variant variant = Axolotl.Variant.DEFAULT;
    public float playingDeadFactor;
    public float movingFactor;
    public float inWaterFactor = 1.0f;
    public float onGroundFactor;
}


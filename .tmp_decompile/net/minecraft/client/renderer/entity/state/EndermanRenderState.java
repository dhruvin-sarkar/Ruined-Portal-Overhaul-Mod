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
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EndermanRenderState
extends HumanoidRenderState {
    public boolean isCreepy;
    public @Nullable BlockState carriedBlock;
}


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
import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;
import net.minecraft.world.entity.Display;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockDisplayEntityRenderState
extends DisplayEntityRenderState {
    public @Nullable Display.BlockDisplay.BlockRenderState blockRenderState;

    @Override
    public boolean hasSubState() {
        return this.blockRenderState != null;
    }
}


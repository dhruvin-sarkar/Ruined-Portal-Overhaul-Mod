/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.state;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.client.renderer.state.WorldBorderRenderState;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LevelRenderState {
    public CameraRenderState cameraRenderState = new CameraRenderState();
    public final List<EntityRenderState> entityRenderStates = new ArrayList<EntityRenderState>();
    public final List<BlockEntityRenderState> blockEntityRenderStates = new ArrayList<BlockEntityRenderState>();
    public boolean haveGlowingEntities;
    public @Nullable BlockOutlineRenderState blockOutlineRenderState;
    public final List<BlockBreakingRenderState> blockBreakingRenderStates = new ArrayList<BlockBreakingRenderState>();
    public final WeatherRenderState weatherRenderState = new WeatherRenderState();
    public final WorldBorderRenderState worldBorderRenderState = new WorldBorderRenderState();
    public final SkyRenderState skyRenderState = new SkyRenderState();
    public long gameTime;

    public void reset() {
        this.entityRenderStates.clear();
        this.blockEntityRenderStates.clear();
        this.blockBreakingRenderStates.clear();
        this.haveGlowingEntities = false;
        this.blockOutlineRenderState = null;
        this.weatherRenderState.reset();
        this.worldBorderRenderState.reset();
        this.skyRenderState.reset();
        this.gameTime = 0L;
    }
}


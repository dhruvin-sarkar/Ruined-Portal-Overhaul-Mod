/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components.debug;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugEntryLight
implements DebugScreenEntry {
    public static final Identifier GROUP = Identifier.withDefaultNamespace("light");

    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.getCameraEntity();
        if (entity == null || minecraft.level == null) {
            return;
        }
        BlockPos blockPos = entity.blockPosition();
        int i = minecraft.level.getChunkSource().getLightEngine().getRawBrightness(blockPos, 0);
        int j = minecraft.level.getBrightness(LightLayer.SKY, blockPos);
        int k = minecraft.level.getBrightness(LightLayer.BLOCK, blockPos);
        String string = "Client Light: " + i + " (" + j + " sky, " + k + " block)";
        if (SharedConstants.DEBUG_SHOW_SERVER_DEBUG_VALUES) {
            Object string2;
            if (levelChunk2 != null) {
                LevelLightEngine levelLightEngine = levelChunk2.getLevel().getLightEngine();
                string2 = "Server Light: (" + levelLightEngine.getLayerListener(LightLayer.SKY).getLightValue(blockPos) + " sky, " + levelLightEngine.getLayerListener(LightLayer.BLOCK).getLightValue(blockPos) + " block)";
            } else {
                string2 = "Server Light: (?? sky, ?? block)";
            }
            debugScreenDisplayer.addToGroup(GROUP, List.of((Object)string, (Object)string2));
        } else {
            debugScreenDisplayer.addToGroup(GROUP, string);
        }
    }
}


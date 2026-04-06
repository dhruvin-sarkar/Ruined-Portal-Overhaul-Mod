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
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugEntryBiome
implements DebugScreenEntry {
    private static final Identifier GROUP = Identifier.withDefaultNamespace("biome");

    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.getCameraEntity();
        if (entity == null || minecraft.level == null) {
            return;
        }
        BlockPos blockPos = entity.blockPosition();
        if (minecraft.level.isInsideBuildHeight(blockPos.getY())) {
            if (SharedConstants.DEBUG_SHOW_SERVER_DEBUG_VALUES && level instanceof ServerLevel) {
                debugScreenDisplayer.addToGroup(GROUP, List.of((Object)("Biome: " + DebugEntryBiome.printBiome(minecraft.level.getBiome(blockPos))), (Object)("Server Biome: " + DebugEntryBiome.printBiome(level.getBiome(blockPos)))));
            } else {
                debugScreenDisplayer.addLine("Biome: " + DebugEntryBiome.printBiome(minecraft.level.getBiome(blockPos)));
            }
        }
    }

    private static String printBiome(Holder<Biome> holder) {
        return (String)holder.unwrap().map(resourceKey -> resourceKey.identifier().toString(), biome -> "[unregistered " + String.valueOf(biome) + "]");
    }
}


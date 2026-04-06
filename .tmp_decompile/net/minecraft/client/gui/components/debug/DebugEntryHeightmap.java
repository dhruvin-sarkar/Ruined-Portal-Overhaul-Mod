/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components.debug;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugEntryHeightmap
implements DebugScreenEntry {
    private static final Map<Heightmap.Types, String> HEIGHTMAP_NAMES = Maps.newEnumMap((Map)Map.of((Object)Heightmap.Types.WORLD_SURFACE_WG, (Object)"SW", (Object)Heightmap.Types.WORLD_SURFACE, (Object)"S", (Object)Heightmap.Types.OCEAN_FLOOR_WG, (Object)"OW", (Object)Heightmap.Types.OCEAN_FLOOR, (Object)"O", (Object)Heightmap.Types.MOTION_BLOCKING, (Object)"M", (Object)Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (Object)"ML"));
    private static final Identifier GROUP = Identifier.withDefaultNamespace("heightmaps");

    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.getCameraEntity();
        if (entity == null || minecraft.level == null || levelChunk == null) {
            return;
        }
        BlockPos blockPos = entity.blockPosition();
        ArrayList<String> list = new ArrayList<String>();
        StringBuilder stringBuilder = new StringBuilder("CH");
        for (Heightmap.Types types : Heightmap.Types.values()) {
            if (!types.sendToClient()) continue;
            stringBuilder.append(" ").append(HEIGHTMAP_NAMES.get(types)).append(": ").append(levelChunk.getHeight(types, blockPos.getX(), blockPos.getZ()));
        }
        list.add(stringBuilder.toString());
        stringBuilder.setLength(0);
        stringBuilder.append("SH");
        for (Heightmap.Types types : Heightmap.Types.values()) {
            if (!types.keepAfterWorldgen()) continue;
            stringBuilder.append(" ").append(HEIGHTMAP_NAMES.get(types)).append(": ");
            if (levelChunk2 != null) {
                stringBuilder.append(levelChunk2.getHeight(types, blockPos.getX(), blockPos.getZ()));
                continue;
            }
            stringBuilder.append("??");
        }
        list.add(stringBuilder.toString());
        debugScreenDisplayer.addToGroup(GROUP, list);
    }
}


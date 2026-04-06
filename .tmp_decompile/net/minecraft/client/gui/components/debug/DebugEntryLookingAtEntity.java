/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components.debug;

import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugEntryLookingAtEntity
implements DebugScreenEntry {
    private static final Identifier GROUP = Identifier.withDefaultNamespace("looking_at_entity");

    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.crosshairPickEntity;
        ArrayList<String> list = new ArrayList<String>();
        if (entity != null) {
            list.add(String.valueOf(ChatFormatting.UNDERLINE) + "Targeted Entity");
            list.add(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())));
        }
        debugScreenDisplayer.addToGroup(GROUP, list);
    }
}


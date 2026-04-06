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
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugEntryLookingAtBlock
implements DebugScreenEntry {
    private static final Identifier GROUP = Identifier.withDefaultNamespace("looking_at_block");

    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        Level level2;
        Entity entity = Minecraft.getInstance().getCameraEntity();
        Level level3 = level2 = SharedConstants.DEBUG_SHOW_SERVER_DEBUG_VALUES ? level : Minecraft.getInstance().level;
        if (entity == null || level2 == null) {
            return;
        }
        HitResult hitResult = entity.pick(20.0, 0.0f, false);
        ArrayList<String> list = new ArrayList<String>();
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState blockState = level2.getBlockState(blockPos);
            list.add(String.valueOf(ChatFormatting.UNDERLINE) + "Targeted Block: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
            list.add(String.valueOf(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())));
            for (Map.Entry<Property<?>, Comparable<?>> entry : blockState.getValues().entrySet()) {
                list.add(this.getPropertyValueString(entry));
            }
            blockState.getTags().map(tagKey -> "#" + String.valueOf(tagKey.location())).forEach(list::add);
        }
        debugScreenDisplayer.addToGroup(GROUP, list);
    }

    private String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> entry) {
        Property<?> property = entry.getKey();
        Comparable<?> comparable = entry.getValue();
        Object string = Util.getPropertyName(property, comparable);
        if (Boolean.TRUE.equals(comparable)) {
            string = String.valueOf(ChatFormatting.GREEN) + (String)string;
        } else if (Boolean.FALSE.equals(comparable)) {
            string = String.valueOf(ChatFormatting.RED) + (String)string;
        }
        return property.getName() + ": " + (String)string;
    }
}


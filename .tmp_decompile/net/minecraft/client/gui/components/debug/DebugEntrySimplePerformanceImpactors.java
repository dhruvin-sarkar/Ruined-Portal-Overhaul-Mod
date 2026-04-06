/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components.debug;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugEntrySimplePerformanceImpactors
implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        Minecraft minecraft = Minecraft.getInstance();
        Options options = minecraft.options;
        Object[] objectArray = new Object[3];
        Object object = objectArray[0] = options.improvedTransparency().get() != false ? "improved-transparency" : "";
        objectArray[1] = options.cloudStatus().get() == CloudStatus.OFF ? "" : (options.cloudStatus().get() == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds");
        objectArray[2] = options.biomeBlendRadius().get();
        debugScreenDisplayer.addLine(String.format(Locale.ROOT, "%s%s B: %d", objectArray));
        TextureFilteringMethod textureFilteringMethod = options.textureFiltering().get();
        if (textureFilteringMethod == TextureFilteringMethod.ANISOTROPIC) {
            debugScreenDisplayer.addLine(String.format(Locale.ROOT, "Filtering: %s %dx", textureFilteringMethod.caption().getString(), options.maxAnisotropyValue()));
        } else {
            debugScreenDisplayer.addLine(String.format(Locale.ROOT, "Filtering: %s", textureFilteringMethod.caption().getString()));
        }
    }

    @Override
    public boolean isAllowed(boolean bl) {
        return true;
    }
}


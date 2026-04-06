/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.geom.builders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.builders.MeshDefinition;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface MeshTransformer {
    public static final MeshTransformer IDENTITY = meshDefinition -> meshDefinition;

    public static MeshTransformer scaling(float f) {
        float g = 24.016f * (1.0f - f);
        return meshDefinition -> meshDefinition.transformed(partPose -> partPose.scaled(f).translated(0.0f, g, 0.0f));
    }

    public MeshDefinition apply(MeshDefinition var1);
}


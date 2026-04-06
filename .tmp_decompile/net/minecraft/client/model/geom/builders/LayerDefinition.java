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
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.MaterialDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;

@Environment(value=EnvType.CLIENT)
public class LayerDefinition {
    private final MeshDefinition mesh;
    private final MaterialDefinition material;

    private LayerDefinition(MeshDefinition meshDefinition, MaterialDefinition materialDefinition) {
        this.mesh = meshDefinition;
        this.material = materialDefinition;
    }

    public LayerDefinition apply(MeshTransformer meshTransformer) {
        return new LayerDefinition(meshTransformer.apply(this.mesh), this.material);
    }

    public ModelPart bakeRoot() {
        return this.mesh.getRoot().bake(this.material.xTexSize, this.material.yTexSize);
    }

    public static LayerDefinition create(MeshDefinition meshDefinition, int i, int j) {
        return new LayerDefinition(meshDefinition, new MaterialDefinition(i, j));
    }
}


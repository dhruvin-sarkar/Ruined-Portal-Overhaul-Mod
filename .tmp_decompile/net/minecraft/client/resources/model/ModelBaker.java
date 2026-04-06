/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.resources.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public interface ModelBaker {
    public ResolvedModel getModel(Identifier var1);

    public BlockModelPart missingBlockModelPart();

    public SpriteGetter sprites();

    public PartCache parts();

    public <T> T compute(SharedOperationKey<T> var1);

    @Environment(value=EnvType.CLIENT)
    public static interface PartCache {
        default public Vector3fc vector(float f, float g, float h) {
            return this.vector((Vector3fc)new Vector3f(f, g, h));
        }

        public Vector3fc vector(Vector3fc var1);
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface SharedOperationKey<T> {
        public T compute(ModelBaker var1);
    }
}


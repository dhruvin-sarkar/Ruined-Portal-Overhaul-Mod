/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface EnvironmentAttributeReader {
    public static final EnvironmentAttributeReader EMPTY = new EnvironmentAttributeReader(){

        @Override
        public <Value> Value getDimensionValue(EnvironmentAttribute<Value> environmentAttribute) {
            return environmentAttribute.defaultValue();
        }

        @Override
        public <Value> Value getValue(EnvironmentAttribute<Value> environmentAttribute, Vec3 vec3, @Nullable SpatialAttributeInterpolator spatialAttributeInterpolator) {
            return environmentAttribute.defaultValue();
        }
    };

    public <Value> Value getDimensionValue(EnvironmentAttribute<Value> var1);

    default public <Value> Value getValue(EnvironmentAttribute<Value> environmentAttribute, BlockPos blockPos) {
        return this.getValue(environmentAttribute, Vec3.atCenterOf(blockPos));
    }

    default public <Value> Value getValue(EnvironmentAttribute<Value> environmentAttribute, Vec3 vec3) {
        return this.getValue(environmentAttribute, vec3, null);
    }

    public <Value> Value getValue(EnvironmentAttribute<Value> var1, Vec3 var2, @Nullable SpatialAttributeInterpolator var3);
}


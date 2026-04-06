/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.resources.model;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Transformation;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Environment(value=EnvType.CLIENT)
public class BlockModelRotation
implements ModelState {
    private static final Map<OctahedralGroup, BlockModelRotation> BY_GROUP_ORDINAL = Util.makeEnumMap(OctahedralGroup.class, BlockModelRotation::new);
    public static final BlockModelRotation IDENTITY = BlockModelRotation.get(OctahedralGroup.IDENTITY);
    final OctahedralGroup orientation;
    final Transformation transformation;
    final Map<Direction, Matrix4fc> faceMapping = new EnumMap<Direction, Matrix4fc>(Direction.class);
    final Map<Direction, Matrix4fc> inverseFaceMapping = new EnumMap<Direction, Matrix4fc>(Direction.class);
    private final WithUvLock withUvLock = new WithUvLock(this);

    private BlockModelRotation(OctahedralGroup octahedralGroup) {
        this.orientation = octahedralGroup;
        this.transformation = octahedralGroup != OctahedralGroup.IDENTITY ? new Transformation((Matrix4fc)new Matrix4f(octahedralGroup.transformation())) : Transformation.identity();
        for (Direction direction : Direction.values()) {
            Matrix4fc matrix4fc = BlockMath.getFaceTransformation(this.transformation, direction).getMatrix();
            this.faceMapping.put(direction, matrix4fc);
            this.inverseFaceMapping.put(direction, (Matrix4fc)matrix4fc.invertAffine(new Matrix4f()));
        }
    }

    @Override
    public Transformation transformation() {
        return this.transformation;
    }

    public static BlockModelRotation get(OctahedralGroup octahedralGroup) {
        return BY_GROUP_ORDINAL.get(octahedralGroup);
    }

    public ModelState withUvLock() {
        return this.withUvLock;
    }

    public String toString() {
        return "simple[" + this.orientation.getSerializedName() + "]";
    }

    @Environment(value=EnvType.CLIENT)
    record WithUvLock(BlockModelRotation parent) implements ModelState
    {
        @Override
        public Transformation transformation() {
            return this.parent.transformation;
        }

        @Override
        public Matrix4fc faceTransformation(Direction direction) {
            return this.parent.faceMapping.getOrDefault(direction, NO_TRANSFORM);
        }

        @Override
        public Matrix4fc inverseFaceTransformation(Direction direction) {
            return this.parent.inverseFaceMapping.getOrDefault(direction, NO_TRANSFORM);
        }

        public String toString() {
            return "uvLocked[" + this.parent.orientation.getSerializedName() + "]";
        }
    }
}


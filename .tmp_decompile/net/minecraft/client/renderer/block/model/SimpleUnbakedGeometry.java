/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer.block.model;

import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.core.Direction;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public record SimpleUnbakedGeometry(List<BlockElement> elements) implements UnbakedGeometry
{
    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName modelDebugName) {
        return SimpleUnbakedGeometry.bake(this.elements, textureSlots, modelBaker, modelState, modelDebugName);
    }

    public static QuadCollection bake(List<BlockElement> list, TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName modelDebugName) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BlockElement blockElement : list) {
            boolean bl = true;
            boolean bl2 = true;
            boolean bl3 = true;
            Vector3fc vector3fc = blockElement.from();
            Vector3fc vector3fc2 = blockElement.to();
            if (vector3fc.x() == vector3fc2.x()) {
                bl2 = false;
                bl3 = false;
            }
            if (vector3fc.y() == vector3fc2.y()) {
                bl = false;
                bl3 = false;
            }
            if (vector3fc.z() == vector3fc2.z()) {
                bl = false;
                bl2 = false;
            }
            if (!bl && !bl2 && !bl3) continue;
            for (Map.Entry<Direction, BlockElementFace> entry : blockElement.faces().entrySet()) {
                boolean bl4;
                Direction direction = entry.getKey();
                BlockElementFace blockElementFace = entry.getValue();
                if (!(bl4 = (switch (direction.getAxis()) {
                    default -> throw new MatchException(null, null);
                    case Direction.Axis.X -> bl;
                    case Direction.Axis.Y -> bl2;
                    case Direction.Axis.Z -> bl3;
                }))) continue;
                TextureAtlasSprite textureAtlasSprite = modelBaker.sprites().resolveSlot(textureSlots, blockElementFace.texture(), modelDebugName);
                BakedQuad bakedQuad = FaceBakery.bakeQuad(modelBaker.parts(), vector3fc, vector3fc2, blockElementFace, textureAtlasSprite, direction, modelState, blockElement.rotation(), blockElement.shade(), blockElement.lightEmission());
                if (blockElementFace.cullForDirection() == null) {
                    builder.addUnculledFace(bakedQuad);
                    continue;
                }
                builder.addCulledFace(Direction.rotate(modelState.transformation().getMatrix(), blockElementFace.cullForDirection()), bakedQuad);
            }
        }
        return builder.build();
    }
}


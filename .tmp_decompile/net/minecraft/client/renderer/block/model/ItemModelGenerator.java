/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.block.model;

import com.mojang.math.Quadrant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ItemModelGenerator
implements UnbakedModel {
    public static final Identifier GENERATED_ITEM_MODEL_ID = Identifier.withDefaultNamespace("builtin/generated");
    public static final List<String> LAYERS = List.of((Object)"layer0", (Object)"layer1", (Object)"layer2", (Object)"layer3", (Object)"layer4");
    private static final float MIN_Z = 7.5f;
    private static final float MAX_Z = 8.5f;
    private static final TextureSlots.Data TEXTURE_SLOTS = new TextureSlots.Data.Builder().addReference("particle", "layer0").build();
    private static final BlockElementFace.UVs SOUTH_FACE_UVS = new BlockElementFace.UVs(0.0f, 0.0f, 16.0f, 16.0f);
    private static final BlockElementFace.UVs NORTH_FACE_UVS = new BlockElementFace.UVs(16.0f, 0.0f, 0.0f, 16.0f);
    private static final float UV_SHRINK = 0.1f;

    @Override
    public TextureSlots.Data textureSlots() {
        return TEXTURE_SLOTS;
    }

    @Override
    public UnbakedGeometry geometry() {
        return ItemModelGenerator::bake;
    }

    @Override
    public @Nullable UnbakedModel.GuiLight guiLight() {
        return UnbakedModel.GuiLight.FRONT;
    }

    private static QuadCollection bake(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName modelDebugName) {
        String string;
        Material material;
        ArrayList<BlockElement> list = new ArrayList<BlockElement>();
        for (int i = 0; i < LAYERS.size() && (material = textureSlots.getMaterial(string = LAYERS.get(i))) != null; ++i) {
            SpriteContents spriteContents = modelBaker.sprites().get(material, modelDebugName).contents();
            list.addAll(ItemModelGenerator.processFrames(i, string, spriteContents));
        }
        return SimpleUnbakedGeometry.bake(list, textureSlots, modelBaker, modelState, modelDebugName);
    }

    private static List<BlockElement> processFrames(int i, String string, SpriteContents spriteContents) {
        Map map = Map.of((Object)Direction.SOUTH, (Object)((Object)new BlockElementFace(null, i, string, SOUTH_FACE_UVS, Quadrant.R0)), (Object)Direction.NORTH, (Object)((Object)new BlockElementFace(null, i, string, NORTH_FACE_UVS, Quadrant.R0)));
        ArrayList<BlockElement> list = new ArrayList<BlockElement>();
        list.add(new BlockElement((Vector3fc)new Vector3f(0.0f, 0.0f, 7.5f), (Vector3fc)new Vector3f(16.0f, 16.0f, 8.5f), map));
        list.addAll(ItemModelGenerator.createSideElements(spriteContents, string, i));
        return list;
    }

    private static List<BlockElement> createSideElements(SpriteContents spriteContents, String string, int i) {
        float f = 16.0f / (float)spriteContents.width();
        float g = 16.0f / (float)spriteContents.height();
        ArrayList<BlockElement> list = new ArrayList<BlockElement>();
        for (SideFace sideFace : ItemModelGenerator.getSideFaces(spriteContents)) {
            float n;
            float m;
            float h = sideFace.x();
            float j = sideFace.y();
            SideDirection sideDirection = sideFace.facing();
            float k = h + 0.1f;
            float l = h + 1.0f - 0.1f;
            if (sideDirection.isHorizontal()) {
                m = j + 0.1f;
                n = j + 1.0f - 0.1f;
            } else {
                m = j + 1.0f - 0.1f;
                n = j + 0.1f;
            }
            float o = h;
            float p = j;
            float q = h;
            float r = j;
            switch (sideDirection.ordinal()) {
                case 0: {
                    q += 1.0f;
                    break;
                }
                case 1: {
                    q += 1.0f;
                    p += 1.0f;
                    r += 1.0f;
                    break;
                }
                case 2: {
                    r += 1.0f;
                    break;
                }
                case 3: {
                    o += 1.0f;
                    q += 1.0f;
                    r += 1.0f;
                }
            }
            o *= f;
            q *= f;
            p *= g;
            r *= g;
            p = 16.0f - p;
            r = 16.0f - r;
            Map map = Map.of((Object)sideDirection.getDirection(), (Object)((Object)new BlockElementFace(null, i, string, new BlockElementFace.UVs(k * f, m * f, l * g, n * g), Quadrant.R0)));
            switch (sideDirection.ordinal()) {
                case 0: {
                    list.add(new BlockElement((Vector3fc)new Vector3f(o, p, 7.5f), (Vector3fc)new Vector3f(q, p, 8.5f), map));
                    break;
                }
                case 1: {
                    list.add(new BlockElement((Vector3fc)new Vector3f(o, r, 7.5f), (Vector3fc)new Vector3f(q, r, 8.5f), map));
                    break;
                }
                case 2: {
                    list.add(new BlockElement((Vector3fc)new Vector3f(o, p, 7.5f), (Vector3fc)new Vector3f(o, r, 8.5f), map));
                    break;
                }
                case 3: {
                    list.add(new BlockElement((Vector3fc)new Vector3f(q, p, 7.5f), (Vector3fc)new Vector3f(q, r, 8.5f), map));
                }
            }
        }
        return list;
    }

    private static Collection<SideFace> getSideFaces(SpriteContents spriteContents) {
        int i = spriteContents.width();
        int j = spriteContents.height();
        HashSet<SideFace> set = new HashSet<SideFace>();
        spriteContents.getUniqueFrames().forEach(k -> {
            for (int l = 0; l < j; ++l) {
                for (int m = 0; m < i; ++m) {
                    boolean bl;
                    boolean bl2 = bl = !ItemModelGenerator.isTransparent(spriteContents, k, m, l, i, j);
                    if (!bl) continue;
                    ItemModelGenerator.checkTransition(SideDirection.UP, set, spriteContents, k, m, l, i, j);
                    ItemModelGenerator.checkTransition(SideDirection.DOWN, set, spriteContents, k, m, l, i, j);
                    ItemModelGenerator.checkTransition(SideDirection.LEFT, set, spriteContents, k, m, l, i, j);
                    ItemModelGenerator.checkTransition(SideDirection.RIGHT, set, spriteContents, k, m, l, i, j);
                }
            }
        });
        return set;
    }

    private static void checkTransition(SideDirection sideDirection, Set<SideFace> set, SpriteContents spriteContents, int i, int j, int k, int l, int m) {
        if (ItemModelGenerator.isTransparent(spriteContents, i, j - sideDirection.direction.getStepX(), k - sideDirection.direction.getStepY(), l, m)) {
            set.add(new SideFace(sideDirection, j, k));
        }
    }

    private static boolean isTransparent(SpriteContents spriteContents, int i, int j, int k, int l, int m) {
        if (j < 0 || k < 0 || j >= l || k >= m) {
            return true;
        }
        return spriteContents.isTransparent(i, j, k);
    }

    @Environment(value=EnvType.CLIENT)
    record SideFace(SideDirection facing, int x, int y) {
    }

    @Environment(value=EnvType.CLIENT)
    static enum SideDirection {
        UP(Direction.UP),
        DOWN(Direction.DOWN),
        LEFT(Direction.EAST),
        RIGHT(Direction.WEST);

        final Direction direction;

        private SideDirection(Direction direction) {
            this.direction = direction;
        }

        public Direction getDirection() {
            return this.direction;
        }

        boolean isHorizontal() {
            return this == DOWN || this == UP;
        }
    }
}


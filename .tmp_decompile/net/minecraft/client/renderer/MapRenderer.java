/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class MapRenderer {
    private static final float MAP_Z_OFFSET = -0.01f;
    private static final float DECORATION_Z_OFFSET = -0.001f;
    public static final int WIDTH = 128;
    public static final int HEIGHT = 128;
    private final TextureAtlas decorationSprites;
    private final MapTextureManager mapTextureManager;

    public MapRenderer(AtlasManager atlasManager, MapTextureManager mapTextureManager) {
        this.decorationSprites = atlasManager.getAtlasOrThrow(AtlasIds.MAP_DECORATIONS);
        this.mapTextureManager = mapTextureManager;
    }

    public void render(MapRenderState mapRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, boolean bl, int i) {
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(mapRenderState.texture), (pose, vertexConsumer) -> {
            vertexConsumer.addVertex(pose, 0.0f, 128.0f, -0.01f).setColor(-1).setUv(0.0f, 1.0f).setLight(i);
            vertexConsumer.addVertex(pose, 128.0f, 128.0f, -0.01f).setColor(-1).setUv(1.0f, 1.0f).setLight(i);
            vertexConsumer.addVertex(pose, 128.0f, 0.0f, -0.01f).setColor(-1).setUv(1.0f, 0.0f).setLight(i);
            vertexConsumer.addVertex(pose, 0.0f, 0.0f, -0.01f).setColor(-1).setUv(0.0f, 0.0f).setLight(i);
        });
        int j = 0;
        for (MapRenderState.MapDecorationRenderState mapDecorationRenderState : mapRenderState.decorations) {
            if (bl && !mapDecorationRenderState.renderOnFrame) continue;
            poseStack.pushPose();
            poseStack.translate((float)mapDecorationRenderState.x / 2.0f + 64.0f, (float)mapDecorationRenderState.y / 2.0f + 64.0f, -0.02f);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)(mapDecorationRenderState.rot * 360) / 16.0f));
            poseStack.scale(4.0f, 4.0f, 3.0f);
            poseStack.translate(-0.125f, 0.125f, 0.0f);
            TextureAtlasSprite textureAtlasSprite = mapDecorationRenderState.atlasSprite;
            if (textureAtlasSprite != null) {
                float f = (float)j * -0.001f;
                submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(textureAtlasSprite.atlasLocation()), (pose, vertexConsumer) -> {
                    vertexConsumer.addVertex(pose, -1.0f, 1.0f, f).setColor(-1).setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV0()).setLight(i);
                    vertexConsumer.addVertex(pose, 1.0f, 1.0f, f).setColor(-1).setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV0()).setLight(i);
                    vertexConsumer.addVertex(pose, 1.0f, -1.0f, f).setColor(-1).setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV1()).setLight(i);
                    vertexConsumer.addVertex(pose, -1.0f, -1.0f, f).setColor(-1).setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV1()).setLight(i);
                });
                poseStack.popPose();
            }
            if (mapDecorationRenderState.name != null) {
                Font font = Minecraft.getInstance().font;
                float g = font.width(mapDecorationRenderState.name);
                float f = 25.0f / g;
                Objects.requireNonNull(font);
                float h = Mth.clamp(f, 0.0f, 6.0f / 9.0f);
                poseStack.pushPose();
                poseStack.translate((float)mapDecorationRenderState.x / 2.0f + 64.0f - g * h / 2.0f, (float)mapDecorationRenderState.y / 2.0f + 64.0f + 4.0f, -0.025f);
                poseStack.scale(h, h, -1.0f);
                poseStack.translate(0.0f, 0.0f, 0.1f);
                submitNodeCollector.order(1).submitText(poseStack, 0.0f, 0.0f, mapDecorationRenderState.name.getVisualOrderText(), false, Font.DisplayMode.NORMAL, i, -1, Integer.MIN_VALUE, 0);
                poseStack.popPose();
            }
            ++j;
        }
    }

    public void extractRenderState(MapId mapId, MapItemSavedData mapItemSavedData, MapRenderState mapRenderState) {
        mapRenderState.texture = this.mapTextureManager.prepareMapTexture(mapId, mapItemSavedData);
        mapRenderState.decorations.clear();
        for (MapDecoration mapDecoration : mapItemSavedData.getDecorations()) {
            mapRenderState.decorations.add(this.extractDecorationRenderState(mapDecoration));
        }
    }

    private MapRenderState.MapDecorationRenderState extractDecorationRenderState(MapDecoration mapDecoration) {
        MapRenderState.MapDecorationRenderState mapDecorationRenderState = new MapRenderState.MapDecorationRenderState();
        mapDecorationRenderState.atlasSprite = this.decorationSprites.getSprite(mapDecoration.getSpriteLocation());
        mapDecorationRenderState.x = mapDecoration.x();
        mapDecorationRenderState.y = mapDecoration.y();
        mapDecorationRenderState.rot = mapDecoration.rot();
        mapDecorationRenderState.name = mapDecoration.name().orElse(null);
        mapDecorationRenderState.renderOnFrame = mapDecoration.renderOnFrame();
        return mapDecorationRenderState;
    }
}


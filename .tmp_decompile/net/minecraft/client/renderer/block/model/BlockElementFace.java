/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Quadrant;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record BlockElementFace(@Nullable Direction cullForDirection, int tintIndex, String texture, @Nullable UVs uvs, Quadrant rotation) {
    public static final int NO_TINT = -1;

    public static float getU(UVs uVs, Quadrant quadrant, int i) {
        return uVs.getVertexU(quadrant.rotateVertexIndex(i)) / 16.0f;
    }

    public static float getV(UVs uVs, Quadrant quadrant, int i) {
        return uVs.getVertexV(quadrant.rotateVertexIndex(i)) / 16.0f;
    }

    @Environment(value=EnvType.CLIENT)
    public record UVs(float minU, float minV, float maxU, float maxV) {
        public float getVertexU(int i) {
            return i == 0 || i == 1 ? this.minU : this.maxU;
        }

        public float getVertexV(int i) {
            return i == 0 || i == 3 ? this.minV : this.maxV;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Deserializer
    implements JsonDeserializer<BlockElementFace> {
        private static final int DEFAULT_TINT_INDEX = -1;
        private static final int DEFAULT_ROTATION = 0;

        protected Deserializer() {
        }

        public BlockElementFace deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Direction direction = Deserializer.getCullFacing(jsonObject);
            int i = Deserializer.getTintIndex(jsonObject);
            String string = Deserializer.getTexture(jsonObject);
            UVs uVs = Deserializer.getUVs(jsonObject);
            Quadrant quadrant = Deserializer.getRotation(jsonObject);
            return new BlockElementFace(direction, i, string, uVs, quadrant);
        }

        private static int getTintIndex(JsonObject jsonObject) {
            return GsonHelper.getAsInt(jsonObject, "tintindex", -1);
        }

        private static String getTexture(JsonObject jsonObject) {
            return GsonHelper.getAsString(jsonObject, "texture");
        }

        private static @Nullable Direction getCullFacing(JsonObject jsonObject) {
            String string = GsonHelper.getAsString(jsonObject, "cullface", "");
            return Direction.byName(string);
        }

        private static Quadrant getRotation(JsonObject jsonObject) {
            int i = GsonHelper.getAsInt(jsonObject, "rotation", 0);
            return Quadrant.parseJson(i);
        }

        private static @Nullable UVs getUVs(JsonObject jsonObject) {
            if (!jsonObject.has("uv")) {
                return null;
            }
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "uv");
            if (jsonArray.size() != 4) {
                throw new JsonParseException("Expected 4 uv values, found: " + jsonArray.size());
            }
            float f = GsonHelper.convertToFloat(jsonArray.get(0), "minU");
            float g = GsonHelper.convertToFloat(jsonArray.get(1), "minV");
            float h = GsonHelper.convertToFloat(jsonArray.get(2), "maxU");
            float i = GsonHelper.convertToFloat(jsonArray.get(3), "maxV");
            return new UVs(f, g, h, i);
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  java.lang.Record
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record BlockElement(Vector3fc from, Vector3fc to, Map<Direction, BlockElementFace> faces, @Nullable BlockElementRotation rotation, boolean shade, int lightEmission) {
    private static final boolean DEFAULT_RESCALE = false;
    private static final float MIN_EXTENT = -16.0f;
    private static final float MAX_EXTENT = 32.0f;

    public BlockElement(Vector3fc vector3fc, Vector3fc vector3fc2, Map<Direction, BlockElementFace> map) {
        this(vector3fc, vector3fc2, map, null, true, 0);
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Deserializer
    implements JsonDeserializer<BlockElement> {
        private static final boolean DEFAULT_SHADE = true;
        private static final int DEFAULT_LIGHT_EMISSION = 0;
        private static final String FIELD_SHADE = "shade";
        private static final String FIELD_LIGHT_EMISSION = "light_emission";
        private static final String FIELD_ROTATION = "rotation";
        private static final String FIELD_ORIGIN = "origin";
        private static final String FIELD_ANGLE = "angle";
        private static final String FIELD_X = "x";
        private static final String FIELD_Y = "y";
        private static final String FIELD_Z = "z";
        private static final String FIELD_AXIS = "axis";
        private static final String FIELD_RESCALE = "rescale";
        private static final String FIELD_FACES = "faces";
        private static final String FIELD_TO = "to";
        private static final String FIELD_FROM = "from";

        protected Deserializer() {
        }

        public BlockElement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vector3f vector3f = Deserializer.getPosition(jsonObject, FIELD_FROM);
            Vector3f vector3f2 = Deserializer.getPosition(jsonObject, FIELD_TO);
            BlockElementRotation blockElementRotation = this.getRotation(jsonObject);
            Map<Direction, BlockElementFace> map = this.getFaces(jsonDeserializationContext, jsonObject);
            if (jsonObject.has(FIELD_SHADE) && !GsonHelper.isBooleanValue(jsonObject, FIELD_SHADE)) {
                throw new JsonParseException("Expected 'shade' to be a Boolean");
            }
            boolean bl = GsonHelper.getAsBoolean(jsonObject, FIELD_SHADE, true);
            int i = 0;
            if (jsonObject.has(FIELD_LIGHT_EMISSION)) {
                boolean bl2 = GsonHelper.isNumberValue(jsonObject, FIELD_LIGHT_EMISSION);
                if (bl2) {
                    i = GsonHelper.getAsInt(jsonObject, FIELD_LIGHT_EMISSION);
                }
                if (!bl2 || i < 0 || i > 15) {
                    throw new JsonParseException("Expected 'light_emission' to be an Integer between (inclusive) 0 and 15");
                }
            }
            return new BlockElement((Vector3fc)vector3f, (Vector3fc)vector3f2, map, blockElementRotation, bl, i);
        }

        private @Nullable BlockElementRotation getRotation(JsonObject jsonObject) {
            if (jsonObject.has(FIELD_ROTATION)) {
                Record rotationValue;
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, FIELD_ROTATION);
                Vector3f vector3f = Deserializer.getVector3f(jsonObject2, FIELD_ORIGIN);
                vector3f.mul(0.0625f);
                if (jsonObject2.has(FIELD_AXIS) || jsonObject2.has(FIELD_ANGLE)) {
                    Direction.Axis axis = this.getAxis(jsonObject2);
                    float f = GsonHelper.getAsFloat(jsonObject2, FIELD_ANGLE);
                    rotationValue = new BlockElementRotation.SingleAxisRotation(axis, f);
                } else if (jsonObject2.has(FIELD_X) || jsonObject2.has(FIELD_Y) || jsonObject2.has(FIELD_Z)) {
                    float g = GsonHelper.getAsFloat(jsonObject2, FIELD_X, 0.0f);
                    float f = GsonHelper.getAsFloat(jsonObject2, FIELD_Y, 0.0f);
                    float h = GsonHelper.getAsFloat(jsonObject2, FIELD_Z, 0.0f);
                    rotationValue = new BlockElementRotation.EulerXYZRotation(g, f, h);
                } else {
                    throw new JsonParseException("Missing rotation value, expected either 'axis' and 'angle' or 'x', 'y' and 'z'");
                }
                boolean bl = GsonHelper.getAsBoolean(jsonObject2, FIELD_RESCALE, false);
                return new BlockElementRotation((Vector3fc)vector3f, (BlockElementRotation.RotationValue)rotationValue, bl);
            }
            return null;
        }

        private Direction.Axis getAxis(JsonObject jsonObject) {
            String string = GsonHelper.getAsString(jsonObject, FIELD_AXIS);
            Direction.Axis axis = Direction.Axis.byName(string.toLowerCase(Locale.ROOT));
            if (axis == null) {
                throw new JsonParseException("Invalid rotation axis: " + string);
            }
            return axis;
        }

        private Map<Direction, BlockElementFace> getFaces(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            Map<Direction, BlockElementFace> map = this.filterNullFromFaces(jsonDeserializationContext, jsonObject);
            if (map.isEmpty()) {
                throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
            }
            return map;
        }

        private Map<Direction, BlockElementFace> filterNullFromFaces(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            EnumMap map = Maps.newEnumMap(Direction.class);
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, FIELD_FACES);
            for (Map.Entry entry : jsonObject2.entrySet()) {
                Direction direction = this.getFacing((String)entry.getKey());
                map.put(direction, (BlockElementFace)((Object)jsonDeserializationContext.deserialize((JsonElement)entry.getValue(), BlockElementFace.class)));
            }
            return map;
        }

        private Direction getFacing(String string) {
            Direction direction = Direction.byName(string);
            if (direction == null) {
                throw new JsonParseException("Unknown facing: " + string);
            }
            return direction;
        }

        private static Vector3f getPosition(JsonObject jsonObject, String string) {
            Vector3f vector3f = Deserializer.getVector3f(jsonObject, string);
            if (vector3f.x() < -16.0f || vector3f.y() < -16.0f || vector3f.z() < -16.0f || vector3f.x() > 32.0f || vector3f.y() > 32.0f || vector3f.z() > 32.0f) {
                throw new JsonParseException("'" + string + "' specifier exceeds the allowed boundaries: " + String.valueOf(vector3f));
            }
            return vector3f;
        }

        private static Vector3f getVector3f(JsonObject jsonObject, String string) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, string);
            if (jsonArray.size() != 3) {
                throw new JsonParseException("Expected 3 " + string + " values, found: " + jsonArray.size());
            }
            float[] fs = new float[3];
            for (int i = 0; i < fs.length; ++i) {
                fs[i] = GsonHelper.convertToFloat(jsonArray.get(i), string + "[" + i + "]");
            }
            return new Vector3f(fs[0], fs[1], fs[2]);
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}


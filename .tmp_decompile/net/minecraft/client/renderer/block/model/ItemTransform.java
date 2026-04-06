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
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public record ItemTransform(Vector3fc rotation, Vector3fc translation, Vector3fc scale) {
    public static final ItemTransform NO_TRANSFORM = new ItemTransform((Vector3fc)new Vector3f(), (Vector3fc)new Vector3f(), (Vector3fc)new Vector3f(1.0f, 1.0f, 1.0f));

    public void apply(boolean bl, PoseStack.Pose pose) {
        float h;
        float g;
        float f;
        if (this == NO_TRANSFORM) {
            pose.translate(-0.5f, -0.5f, -0.5f);
            return;
        }
        if (bl) {
            f = -this.translation.x();
            g = -this.rotation.y();
            h = -this.rotation.z();
        } else {
            f = this.translation.x();
            g = this.rotation.y();
            h = this.rotation.z();
        }
        pose.translate(f, this.translation.y(), this.translation.z());
        pose.rotate((Quaternionfc)new Quaternionf().rotationXYZ(this.rotation.x() * ((float)Math.PI / 180), g * ((float)Math.PI / 180), h * ((float)Math.PI / 180)));
        pose.scale(this.scale.x(), this.scale.y(), this.scale.z());
        pose.translate(-0.5f, -0.5f, -0.5f);
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Deserializer
    implements JsonDeserializer<ItemTransform> {
        private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0f, 0.0f, 0.0f);
        private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0f, 0.0f, 0.0f);
        private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0f, 1.0f, 1.0f);
        public static final float MAX_TRANSLATION = 5.0f;
        public static final float MAX_SCALE = 4.0f;

        protected Deserializer() {
        }

        public ItemTransform deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vector3f vector3f = this.getVector3f(jsonObject, "rotation", DEFAULT_ROTATION);
            Vector3f vector3f2 = this.getVector3f(jsonObject, "translation", DEFAULT_TRANSLATION);
            vector3f2.mul(0.0625f);
            vector3f2.set(Mth.clamp(vector3f2.x, -5.0f, 5.0f), Mth.clamp(vector3f2.y, -5.0f, 5.0f), Mth.clamp(vector3f2.z, -5.0f, 5.0f));
            Vector3f vector3f3 = this.getVector3f(jsonObject, "scale", DEFAULT_SCALE);
            vector3f3.set(Mth.clamp(vector3f3.x, -4.0f, 4.0f), Mth.clamp(vector3f3.y, -4.0f, 4.0f), Mth.clamp(vector3f3.z, -4.0f, 4.0f));
            return new ItemTransform((Vector3fc)vector3f, (Vector3fc)vector3f2, (Vector3fc)vector3f3);
        }

        private Vector3f getVector3f(JsonObject jsonObject, String string, Vector3f vector3f) {
            if (!jsonObject.has(string)) {
                return vector3f;
            }
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


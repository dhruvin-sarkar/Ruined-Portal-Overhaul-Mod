/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
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

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record BlockModel(@Nullable UnbakedGeometry geometry, @Nullable UnbakedModel.GuiLight guiLight, @Nullable Boolean ambientOcclusion, @Nullable ItemTransforms transforms, TextureSlots.Data textureSlots, @Nullable Identifier parent) implements UnbakedModel
{
    @VisibleForTesting
    static final Gson GSON = new GsonBuilder().registerTypeAdapter(BlockModel.class, (Object)new Deserializer()).registerTypeAdapter(BlockElement.class, (Object)new BlockElement.Deserializer()).registerTypeAdapter(BlockElementFace.class, (Object)new BlockElementFace.Deserializer()).registerTypeAdapter(ItemTransform.class, (Object)new ItemTransform.Deserializer()).registerTypeAdapter(ItemTransforms.class, (Object)new ItemTransforms.Deserializer()).create();

    public static BlockModel fromStream(Reader reader) {
        return GsonHelper.fromJson(GSON, reader, BlockModel.class);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Deserializer
    implements JsonDeserializer<BlockModel> {
        public BlockModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            UnbakedGeometry unbakedGeometry = this.getElements(jsonDeserializationContext, jsonObject);
            String string = this.getParentName(jsonObject);
            TextureSlots.Data data = this.getTextureMap(jsonObject);
            Boolean boolean_ = this.getAmbientOcclusion(jsonObject);
            ItemTransforms itemTransforms = null;
            if (jsonObject.has("display")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "display");
                itemTransforms = (ItemTransforms)((Object)jsonDeserializationContext.deserialize((JsonElement)jsonObject2, ItemTransforms.class));
            }
            UnbakedModel.GuiLight guiLight = null;
            if (jsonObject.has("gui_light")) {
                guiLight = UnbakedModel.GuiLight.getByName(GsonHelper.getAsString(jsonObject, "gui_light"));
            }
            Identifier identifier = string.isEmpty() ? null : Identifier.parse(string);
            return new BlockModel(unbakedGeometry, guiLight, boolean_, itemTransforms, data, identifier);
        }

        private TextureSlots.Data getTextureMap(JsonObject jsonObject) {
            if (jsonObject.has("textures")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "textures");
                return TextureSlots.parseTextureMap(jsonObject2);
            }
            return TextureSlots.Data.EMPTY;
        }

        private String getParentName(JsonObject jsonObject) {
            return GsonHelper.getAsString(jsonObject, "parent", "");
        }

        protected @Nullable Boolean getAmbientOcclusion(JsonObject jsonObject) {
            if (jsonObject.has("ambientocclusion")) {
                return GsonHelper.getAsBoolean(jsonObject, "ambientocclusion");
            }
            return null;
        }

        protected @Nullable UnbakedGeometry getElements(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            if (jsonObject.has("elements")) {
                ArrayList<BlockElement> list = new ArrayList<BlockElement>();
                for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "elements")) {
                    list.add((BlockElement)((Object)jsonDeserializationContext.deserialize(jsonElement, BlockElement.class)));
                }
                return new SimpleUnbakedGeometry(list);
            }
            return null;
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}


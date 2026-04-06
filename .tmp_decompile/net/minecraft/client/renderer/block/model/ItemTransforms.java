/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.item.ItemDisplayContext;

@Environment(value=EnvType.CLIENT)
public record ItemTransforms(ItemTransform thirdPersonLeftHand, ItemTransform thirdPersonRightHand, ItemTransform firstPersonLeftHand, ItemTransform firstPersonRightHand, ItemTransform head, ItemTransform gui, ItemTransform ground, ItemTransform fixed, ItemTransform fixedFromBottom) {
    public static final ItemTransforms NO_TRANSFORMS = new ItemTransforms(ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM);

    public ItemTransform getTransform(ItemDisplayContext itemDisplayContext) {
        return switch (itemDisplayContext) {
            case ItemDisplayContext.THIRD_PERSON_LEFT_HAND -> this.thirdPersonLeftHand;
            case ItemDisplayContext.THIRD_PERSON_RIGHT_HAND -> this.thirdPersonRightHand;
            case ItemDisplayContext.FIRST_PERSON_LEFT_HAND -> this.firstPersonLeftHand;
            case ItemDisplayContext.FIRST_PERSON_RIGHT_HAND -> this.firstPersonRightHand;
            case ItemDisplayContext.HEAD -> this.head;
            case ItemDisplayContext.GUI -> this.gui;
            case ItemDisplayContext.GROUND -> this.ground;
            case ItemDisplayContext.FIXED -> this.fixed;
            case ItemDisplayContext.ON_SHELF -> this.fixedFromBottom;
            default -> ItemTransform.NO_TRANSFORM;
        };
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Deserializer
    implements JsonDeserializer<ItemTransforms> {
        protected Deserializer() {
        }

        public ItemTransforms deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            ItemTransform itemTransform = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
            ItemTransform itemTransform2 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
            if (itemTransform2 == ItemTransform.NO_TRANSFORM) {
                itemTransform2 = itemTransform;
            }
            ItemTransform itemTransform3 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
            ItemTransform itemTransform4 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
            if (itemTransform4 == ItemTransform.NO_TRANSFORM) {
                itemTransform4 = itemTransform3;
            }
            ItemTransform itemTransform5 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.HEAD);
            ItemTransform itemTransform6 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.GUI);
            ItemTransform itemTransform7 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.GROUND);
            ItemTransform itemTransform8 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.FIXED);
            ItemTransform itemTransform9 = this.getTransform(jsonDeserializationContext, jsonObject, ItemDisplayContext.ON_SHELF);
            return new ItemTransforms(itemTransform2, itemTransform, itemTransform4, itemTransform3, itemTransform5, itemTransform6, itemTransform7, itemTransform8, itemTransform9);
        }

        private ItemTransform getTransform(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject, ItemDisplayContext itemDisplayContext) {
            String string = itemDisplayContext.getSerializedName();
            if (jsonObject.has(string)) {
                return (ItemTransform)((Object)jsonDeserializationContext.deserialize(jsonObject.get(string), ItemTransform.class));
            }
            return ItemTransform.NO_TRANSFORM;
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.resources.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ResolvedModel
extends ModelDebugName {
    public static final boolean DEFAULT_AMBIENT_OCCLUSION = true;
    public static final UnbakedModel.GuiLight DEFAULT_GUI_LIGHT = UnbakedModel.GuiLight.SIDE;

    public UnbakedModel wrapped();

    public @Nullable ResolvedModel parent();

    public static TextureSlots findTopTextureSlots(ResolvedModel resolvedModel) {
        TextureSlots.Resolver resolver = new TextureSlots.Resolver();
        for (ResolvedModel resolvedModel2 = resolvedModel; resolvedModel2 != null; resolvedModel2 = resolvedModel2.parent()) {
            resolver.addLast(resolvedModel2.wrapped().textureSlots());
        }
        return resolver.resolve(resolvedModel);
    }

    default public TextureSlots getTopTextureSlots() {
        return ResolvedModel.findTopTextureSlots(this);
    }

    public static boolean findTopAmbientOcclusion(ResolvedModel resolvedModel) {
        while (resolvedModel != null) {
            Boolean boolean_ = resolvedModel.wrapped().ambientOcclusion();
            if (boolean_ != null) {
                return boolean_;
            }
            resolvedModel = resolvedModel.parent();
        }
        return true;
    }

    default public boolean getTopAmbientOcclusion() {
        return ResolvedModel.findTopAmbientOcclusion(this);
    }

    public static UnbakedModel.GuiLight findTopGuiLight(ResolvedModel resolvedModel) {
        while (resolvedModel != null) {
            UnbakedModel.GuiLight guiLight = resolvedModel.wrapped().guiLight();
            if (guiLight != null) {
                return guiLight;
            }
            resolvedModel = resolvedModel.parent();
        }
        return DEFAULT_GUI_LIGHT;
    }

    default public UnbakedModel.GuiLight getTopGuiLight() {
        return ResolvedModel.findTopGuiLight(this);
    }

    public static UnbakedGeometry findTopGeometry(ResolvedModel resolvedModel) {
        while (resolvedModel != null) {
            UnbakedGeometry unbakedGeometry = resolvedModel.wrapped().geometry();
            if (unbakedGeometry != null) {
                return unbakedGeometry;
            }
            resolvedModel = resolvedModel.parent();
        }
        return UnbakedGeometry.EMPTY;
    }

    default public UnbakedGeometry getTopGeometry() {
        return ResolvedModel.findTopGeometry(this);
    }

    default public QuadCollection bakeTopGeometry(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState) {
        return this.getTopGeometry().bake(textureSlots, modelBaker, modelState, this);
    }

    public static TextureAtlasSprite resolveParticleSprite(TextureSlots textureSlots, ModelBaker modelBaker, ModelDebugName modelDebugName) {
        return modelBaker.sprites().resolveSlot(textureSlots, "particle", modelDebugName);
    }

    default public TextureAtlasSprite resolveParticleSprite(TextureSlots textureSlots, ModelBaker modelBaker) {
        return ResolvedModel.resolveParticleSprite(textureSlots, modelBaker, this);
    }

    public static ItemTransform findTopTransform(ResolvedModel resolvedModel, ItemDisplayContext itemDisplayContext) {
        while (resolvedModel != null) {
            ItemTransform itemTransform;
            ItemTransforms itemTransforms = resolvedModel.wrapped().transforms();
            if (itemTransforms != null && (itemTransform = itemTransforms.getTransform(itemDisplayContext)) != ItemTransform.NO_TRANSFORM) {
                return itemTransform;
            }
            resolvedModel = resolvedModel.parent();
        }
        return ItemTransform.NO_TRANSFORM;
    }

    public static ItemTransforms findTopTransforms(ResolvedModel resolvedModel) {
        ItemTransform itemTransform = ResolvedModel.findTopTransform(resolvedModel, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        ItemTransform itemTransform2 = ResolvedModel.findTopTransform(resolvedModel, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        ItemTransform itemTransform3 = ResolvedModel.findTopTransform(resolvedModel, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
        ItemTransform itemTransform4 = ResolvedModel.findTopTransform(resolvedModel, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        ItemTransform itemTransform5 = ResolvedModel.findTopTransform(resolvedModel, ItemDisplayContext.HEAD);
        ItemTransform itemTransform6 = ResolvedModel.findTopTransform(resolvedModel, ItemDisplayContext.GUI);
        ItemTransform itemTransform7 = ResolvedModel.findTopTransform(resolvedModel, ItemDisplayContext.GROUND);
        ItemTransform itemTransform8 = ResolvedModel.findTopTransform(resolvedModel, ItemDisplayContext.FIXED);
        ItemTransform itemTransform9 = ResolvedModel.findTopTransform(resolvedModel, ItemDisplayContext.ON_SHELF);
        return new ItemTransforms(itemTransform, itemTransform2, itemTransform3, itemTransform4, itemTransform5, itemTransform6, itemTransform7, itemTransform8, itemTransform9);
    }

    default public ItemTransforms getTopTransforms() {
        return ResolvedModel.findTopTransforms(this);
    }
}


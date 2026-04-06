/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerDataHolderRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.VillagerMetadataSection;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;

@Environment(value=EnvType.CLIENT)
public class VillagerProfessionLayer<S extends LivingEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    private static final Int2ObjectMap<Identifier> LEVEL_LOCATIONS = (Int2ObjectMap)Util.make(new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
        int2ObjectOpenHashMap.put(1, (Object)Identifier.withDefaultNamespace("stone"));
        int2ObjectOpenHashMap.put(2, (Object)Identifier.withDefaultNamespace("iron"));
        int2ObjectOpenHashMap.put(3, (Object)Identifier.withDefaultNamespace("gold"));
        int2ObjectOpenHashMap.put(4, (Object)Identifier.withDefaultNamespace("emerald"));
        int2ObjectOpenHashMap.put(5, (Object)Identifier.withDefaultNamespace("diamond"));
    });
    private final Object2ObjectMap<ResourceKey<VillagerType>, VillagerMetadataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap();
    private final Object2ObjectMap<ResourceKey<VillagerProfession>, VillagerMetadataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap();
    private final ResourceManager resourceManager;
    private final String path;
    private final M noHatModel;
    private final M noHatBabyModel;

    public VillagerProfessionLayer(RenderLayerParent<S, M> renderLayerParent, ResourceManager resourceManager, String string, M entityModel, M entityModel2) {
        super(renderLayerParent);
        this.resourceManager = resourceManager;
        this.path = string;
        this.noHatModel = entityModel;
        this.noHatBabyModel = entityModel2;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S livingEntityRenderState, float f, float g) {
        if (((LivingEntityRenderState)livingEntityRenderState).isInvisible) {
            return;
        }
        VillagerData villagerData = ((VillagerDataHolderRenderState)livingEntityRenderState).getVillagerData();
        if (villagerData == null) {
            return;
        }
        Holder<VillagerType> holder = villagerData.type();
        Holder<VillagerProfession> holder2 = villagerData.profession();
        VillagerMetadataSection.Hat hat = this.getHatData(this.typeHatCache, "type", holder);
        VillagerMetadataSection.Hat hat2 = this.getHatData(this.professionHatCache, "profession", holder2);
        Object entityModel = this.getParentModel();
        Identifier identifier = this.getIdentifier("type", holder);
        boolean bl = hat2 == VillagerMetadataSection.Hat.NONE || hat2 == VillagerMetadataSection.Hat.PARTIAL && hat != VillagerMetadataSection.Hat.FULL;
        M entityModel2 = ((LivingEntityRenderState)livingEntityRenderState).isBaby ? this.noHatBabyModel : this.noHatModel;
        VillagerProfessionLayer.renderColoredCutoutModel(bl ? entityModel : entityModel2, identifier, poseStack, submitNodeCollector, i, livingEntityRenderState, -1, 1);
        if (!holder2.is(VillagerProfession.NONE) && !((LivingEntityRenderState)livingEntityRenderState).isBaby) {
            Identifier identifier2 = this.getIdentifier("profession", holder2);
            VillagerProfessionLayer.renderColoredCutoutModel(entityModel, identifier2, poseStack, submitNodeCollector, i, livingEntityRenderState, -1, 2);
            if (!holder2.is(VillagerProfession.NITWIT)) {
                Identifier identifier3 = this.getIdentifier("profession_level", (Identifier)LEVEL_LOCATIONS.get(Mth.clamp(villagerData.level(), 1, LEVEL_LOCATIONS.size())));
                VillagerProfessionLayer.renderColoredCutoutModel(entityModel, identifier3, poseStack, submitNodeCollector, i, livingEntityRenderState, -1, 3);
            }
        }
    }

    private Identifier getIdentifier(String string, Identifier identifier) {
        return identifier.withPath(string2 -> "textures/entity/" + this.path + "/" + string + "/" + string2 + ".png");
    }

    private Identifier getIdentifier(String string, Holder<?> holder) {
        return holder.unwrapKey().map(resourceKey -> this.getIdentifier(string, resourceKey.identifier())).orElse(MissingTextureAtlasSprite.getLocation());
    }

    public <K> VillagerMetadataSection.Hat getHatData(Object2ObjectMap<ResourceKey<K>, VillagerMetadataSection.Hat> object2ObjectMap, String string, Holder<K> holder) {
        ResourceKey resourceKey = holder.unwrapKey().orElse(null);
        if (resourceKey == null) {
            return VillagerMetadataSection.Hat.NONE;
        }
        return (VillagerMetadataSection.Hat)object2ObjectMap.computeIfAbsent((Object)resourceKey, object -> this.resourceManager.getResource(this.getIdentifier(string, resourceKey.identifier())).flatMap(resource -> {
            try {
                return resource.metadata().getSection(VillagerMetadataSection.TYPE).map(VillagerMetadataSection::hat);
            }
            catch (IOException iOException) {
                return Optional.empty();
            }
        }).orElse(VillagerMetadataSection.Hat.NONE));
    }
}


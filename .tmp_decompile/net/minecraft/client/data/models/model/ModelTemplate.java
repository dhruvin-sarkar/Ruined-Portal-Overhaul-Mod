/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Streams
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.data.models.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@Environment(value=EnvType.CLIENT)
public class ModelTemplate {
    private final Optional<Identifier> model;
    private final Set<TextureSlot> requiredSlots;
    private final Optional<String> suffix;

    public ModelTemplate(Optional<Identifier> optional, Optional<String> optional2, TextureSlot ... textureSlots) {
        this.model = optional;
        this.suffix = optional2;
        this.requiredSlots = ImmutableSet.copyOf((Object[])textureSlots);
    }

    public Identifier getDefaultModelLocation(Block block) {
        return ModelLocationUtils.getModelLocation(block, this.suffix.orElse(""));
    }

    public Identifier create(Block block, TextureMapping textureMapping, BiConsumer<Identifier, ModelInstance> biConsumer) {
        return this.create(ModelLocationUtils.getModelLocation(block, this.suffix.orElse("")), textureMapping, biConsumer);
    }

    public Identifier createWithSuffix(Block block, String string, TextureMapping textureMapping, BiConsumer<Identifier, ModelInstance> biConsumer) {
        return this.create(ModelLocationUtils.getModelLocation(block, string + this.suffix.orElse("")), textureMapping, biConsumer);
    }

    public Identifier createWithOverride(Block block, String string, TextureMapping textureMapping, BiConsumer<Identifier, ModelInstance> biConsumer) {
        return this.create(ModelLocationUtils.getModelLocation(block, string), textureMapping, biConsumer);
    }

    public Identifier create(Item item, TextureMapping textureMapping, BiConsumer<Identifier, ModelInstance> biConsumer) {
        return this.create(ModelLocationUtils.getModelLocation(item, this.suffix.orElse("")), textureMapping, biConsumer);
    }

    public Identifier create(Identifier identifier, TextureMapping textureMapping, BiConsumer<Identifier, ModelInstance> biConsumer) {
        Map<TextureSlot, Identifier> map = this.createMap(textureMapping);
        biConsumer.accept(identifier, () -> {
            JsonObject jsonObject = new JsonObject();
            this.model.ifPresent(identifier -> jsonObject.addProperty("parent", identifier.toString()));
            if (!map.isEmpty()) {
                JsonObject jsonObject2 = new JsonObject();
                map.forEach((textureSlot, identifier) -> jsonObject2.addProperty(textureSlot.getId(), identifier.toString()));
                jsonObject.add("textures", (JsonElement)jsonObject2);
            }
            return jsonObject;
        });
        return identifier;
    }

    private Map<TextureSlot, Identifier> createMap(TextureMapping textureMapping) {
        return (Map)Streams.concat((Stream[])new Stream[]{this.requiredSlots.stream(), textureMapping.getForced()}).collect(ImmutableMap.toImmutableMap(Function.identity(), textureMapping::get));
    }
}


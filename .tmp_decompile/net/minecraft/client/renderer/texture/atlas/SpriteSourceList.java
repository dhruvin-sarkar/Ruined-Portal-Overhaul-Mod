/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SpriteSourceList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter ATLAS_INFO_CONVERTER = new FileToIdConverter("atlases", ".json");
    private final List<SpriteSource> sources;

    private SpriteSourceList(List<SpriteSource> list) {
        this.sources = list;
    }

    public List<SpriteSource.Loader> list(ResourceManager resourceManager) {
        final HashMap map = new HashMap();
        SpriteSource.Output output = new SpriteSource.Output(){

            @Override
            public void add(Identifier identifier, SpriteSource.DiscardableLoader discardableLoader) {
                SpriteSource.DiscardableLoader discardableLoader2 = map.put(identifier, discardableLoader);
                if (discardableLoader2 != null) {
                    discardableLoader2.discard();
                }
            }

            @Override
            public void removeAll(Predicate<Identifier> predicate) {
                Iterator iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = iterator.next();
                    if (!predicate.test((Identifier)entry.getKey())) continue;
                    ((SpriteSource.DiscardableLoader)entry.getValue()).discard();
                    iterator.remove();
                }
            }
        };
        this.sources.forEach(spriteSource -> spriteSource.run(resourceManager, output));
        ImmutableList.Builder builder = ImmutableList.builder();
        builder.add(spriteResourceLoader -> MissingTextureAtlasSprite.create());
        builder.addAll(map.values());
        return builder.build();
    }

    public static SpriteSourceList load(ResourceManager resourceManager, Identifier identifier) {
        Identifier identifier2 = ATLAS_INFO_CONVERTER.idToFile(identifier);
        ArrayList<SpriteSource> list = new ArrayList<SpriteSource>();
        for (Resource resource : resourceManager.getResourceStack(identifier2)) {
            try {
                BufferedReader bufferedReader = resource.openAsReader();
                try {
                    Dynamic dynamic = new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)StrictJsonParser.parse(bufferedReader));
                    list.addAll((Collection)SpriteSources.FILE_CODEC.parse(dynamic).getOrThrow());
                }
                finally {
                    if (bufferedReader == null) continue;
                    bufferedReader.close();
                }
            }
            catch (Exception exception) {
                LOGGER.error("Failed to parse atlas definition {} in pack {}", new Object[]{identifier2, resource.sourcePackId(), exception});
            }
        }
        return new SpriteSourceList(list);
    }
}


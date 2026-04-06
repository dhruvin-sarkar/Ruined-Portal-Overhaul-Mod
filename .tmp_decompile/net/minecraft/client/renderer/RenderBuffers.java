/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
 *  java.util.SequencedMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SequencedMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.SectionBufferBuilderPool;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class RenderBuffers {
    private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
    private final SectionBufferBuilderPool sectionBufferPool;
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource crumblingBufferSource;
    private final OutlineBufferSource outlineBufferSource;

    public RenderBuffers(int i) {
        this.sectionBufferPool = SectionBufferBuilderPool.allocate(i);
        SequencedMap sequencedMap = (SequencedMap)Util.make(new Object2ObjectLinkedOpenHashMap(), object2ObjectLinkedOpenHashMap -> {
            object2ObjectLinkedOpenHashMap.put((Object)Sheets.solidBlockSheet(), (Object)this.fixedBufferPack.buffer(ChunkSectionLayer.SOLID));
            object2ObjectLinkedOpenHashMap.put((Object)Sheets.cutoutBlockSheet(), (Object)this.fixedBufferPack.buffer(ChunkSectionLayer.CUTOUT));
            object2ObjectLinkedOpenHashMap.put((Object)Sheets.translucentItemSheet(), (Object)this.fixedBufferPack.buffer(ChunkSectionLayer.TRANSLUCENT));
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.translucentBlockItemSheet());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.shieldSheet());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.bedSheet());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.shulkerBoxSheet());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.signSheet());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, Sheets.hangingSignSheet());
            object2ObjectLinkedOpenHashMap.put((Object)Sheets.chestSheet(), (Object)new ByteBufferBuilder(786432));
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, RenderTypes.armorEntityGlint());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, RenderTypes.glint());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, RenderTypes.glintTranslucent());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, RenderTypes.entityGlint());
            RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, RenderTypes.waterMask());
        });
        this.bufferSource = MultiBufferSource.immediateWithBuffers((SequencedMap<RenderType, ByteBufferBuilder>)sequencedMap, new ByteBufferBuilder(786432));
        this.outlineBufferSource = new OutlineBufferSource();
        SequencedMap sequencedMap2 = (SequencedMap)Util.make(new Object2ObjectLinkedOpenHashMap(), object2ObjectLinkedOpenHashMap -> ModelBakery.DESTROY_TYPES.forEach(renderType -> RenderBuffers.put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)object2ObjectLinkedOpenHashMap, renderType)));
        this.crumblingBufferSource = MultiBufferSource.immediateWithBuffers((SequencedMap<RenderType, ByteBufferBuilder>)sequencedMap2, new ByteBufferBuilder(0));
    }

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> object2ObjectLinkedOpenHashMap, RenderType renderType) {
        object2ObjectLinkedOpenHashMap.put((Object)renderType, (Object)new ByteBufferBuilder(renderType.bufferSize()));
    }

    public SectionBufferBuilderPack fixedBufferPack() {
        return this.fixedBufferPack;
    }

    public SectionBufferBuilderPool sectionBufferPool() {
        return this.sectionBufferPool;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }

    public MultiBufferSource.BufferSource crumblingBufferSource() {
        return this.crumblingBufferSource;
    }

    public OutlineBufferSource outlineBufferSource() {
        return this.outlineBufferSource;
    }
}


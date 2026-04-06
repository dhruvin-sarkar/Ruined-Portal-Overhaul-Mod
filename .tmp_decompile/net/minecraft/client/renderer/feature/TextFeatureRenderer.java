/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;

@Environment(value=EnvType.CLIENT)
public class TextFeatureRenderer {
    public void render(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource) {
        Font font = Minecraft.getInstance().font;
        for (SubmitNodeStorage.TextSubmit textSubmit : submitNodeCollection.getTextSubmits()) {
            if (textSubmit.outlineColor() == 0) {
                font.drawInBatch(textSubmit.string(), textSubmit.x(), textSubmit.y(), textSubmit.color(), textSubmit.dropShadow(), textSubmit.pose(), (MultiBufferSource)bufferSource, textSubmit.displayMode(), textSubmit.backgroundColor(), textSubmit.lightCoords());
                continue;
            }
            font.drawInBatch8xOutline(textSubmit.string(), textSubmit.x(), textSubmit.y(), textSubmit.color(), textSubmit.outlineColor(), textSubmit.pose(), bufferSource, textSubmit.lightCoords());
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface NoDataSpecialModelRenderer
extends SpecialModelRenderer<Void> {
    @Override
    default public @Nullable Void extractArgument(ItemStack itemStack) {
        return null;
    }

    @Override
    default public void submit(@Nullable Void void_, ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k) {
        this.submit(itemDisplayContext, poseStack, submitNodeCollector, i, j, bl, k);
    }

    public void submit(ItemDisplayContext var1, PoseStack var2, SubmitNodeCollector var3, int var4, int var5, boolean var6, int var7);

    @Override
    default public /* synthetic */ @Nullable Object extractArgument(ItemStack itemStack) {
        return this.extractArgument(itemStack);
    }
}


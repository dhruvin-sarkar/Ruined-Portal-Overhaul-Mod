/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3f
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SignEditScreen
extends AbstractSignEditScreen {
    public static final float MAGIC_SCALE_NUMBER = 62.500004f;
    public static final float MAGIC_TEXT_SCALE = 0.9765628f;
    private static final Vector3f TEXT_SCALE = new Vector3f(0.9765628f, 0.9765628f, 0.9765628f);
    private  @Nullable Model.Simple signModel;

    public SignEditScreen(SignBlockEntity signBlockEntity, boolean bl, boolean bl2) {
        super(signBlockEntity, bl, bl2);
    }

    @Override
    protected void init() {
        super.init();
        boolean bl = this.sign.getBlockState().getBlock() instanceof StandingSignBlock;
        this.signModel = SignRenderer.createSignModel(this.minecraft.getEntityModels(), this.woodType, bl);
    }

    @Override
    protected float getSignYOffset() {
        return 90.0f;
    }

    @Override
    protected void renderSignBackground(GuiGraphics guiGraphics) {
        if (this.signModel == null) {
            return;
        }
        int i = this.width / 2;
        int j = i - 48;
        int k = 66;
        int l = i + 48;
        int m = 168;
        guiGraphics.submitSignRenderState(this.signModel, 62.500004f, this.woodType, j, 66, l, 168);
    }

    @Override
    protected Vector3f getSignTextScale() {
        return TEXT_SCALE;
    }
}


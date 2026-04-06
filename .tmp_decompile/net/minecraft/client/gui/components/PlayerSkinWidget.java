/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerSkinWidget
extends AbstractWidget {
    private static final float MODEL_HEIGHT = 2.125f;
    private static final float FIT_SCALE = 0.97f;
    private static final float ROTATION_SENSITIVITY = 2.5f;
    private static final float DEFAULT_ROTATION_X = -5.0f;
    private static final float DEFAULT_ROTATION_Y = 30.0f;
    private static final float ROTATION_X_LIMIT = 50.0f;
    private final PlayerModel wideModel;
    private final PlayerModel slimModel;
    private final Supplier<PlayerSkin> skin;
    private float rotationX = -5.0f;
    private float rotationY = 30.0f;

    public PlayerSkinWidget(int i, int j, EntityModelSet entityModelSet, Supplier<PlayerSkin> supplier) {
        super(0, 0, i, j, CommonComponents.EMPTY);
        this.wideModel = new PlayerModel(entityModelSet.bakeLayer(ModelLayers.PLAYER), false);
        this.slimModel = new PlayerModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.skin = supplier;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        float g = 0.97f * (float)this.getHeight() / 2.125f;
        float h = -1.0625f;
        PlayerSkin playerSkin = this.skin.get();
        PlayerModel playerModel = playerSkin.model() == PlayerModelType.SLIM ? this.slimModel : this.wideModel;
        guiGraphics.submitSkinRenderState(playerModel, playerSkin.body().texturePath(), g, this.rotationX, this.rotationY, -1.0625f, this.getX(), this.getY(), this.getRight(), this.getBottom());
    }

    @Override
    protected void onDrag(MouseButtonEvent mouseButtonEvent, double d, double e) {
        this.rotationX = Mth.clamp(this.rotationX - (float)e * 2.5f, -50.0f, 50.0f);
        this.rotationY += (float)d * 2.5f;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return null;
    }
}


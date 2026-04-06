/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public abstract class ItemCombinerScreen<T extends ItemCombinerMenu>
extends AbstractContainerScreen<T>
implements ContainerListener {
    private final Identifier menuResource;

    public ItemCombinerScreen(T itemCombinerMenu, Inventory inventory, Component component, Identifier identifier) {
        super(itemCombinerMenu, inventory, component);
        this.menuResource = identifier;
    }

    protected void subInit() {
    }

    @Override
    protected void init() {
        super.init();
        this.subInit();
        ((ItemCombinerMenu)this.menu).addSlotListener(this);
    }

    @Override
    public void removed() {
        super.removed();
        ((ItemCombinerMenu)this.menu).removeSlotListener(this);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        this.renderTooltip(guiGraphics, i, j);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.menuResource, this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        this.renderErrorIcon(guiGraphics, this.leftPos, this.topPos);
    }

    protected abstract void renderErrorIcon(GuiGraphics var1, int var2, int var3);

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
    }
}


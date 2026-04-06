/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.recipebook;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GhostSlots {
    private final Reference2ObjectMap<Slot, GhostSlot> ingredients = new Reference2ObjectArrayMap();
    private final SlotSelectTime slotSelectTime;

    public GhostSlots(SlotSelectTime slotSelectTime) {
        this.slotSelectTime = slotSelectTime;
    }

    public void clear() {
        this.ingredients.clear();
    }

    private void setSlot(Slot slot, ContextMap contextMap, SlotDisplay slotDisplay, boolean bl) {
        List<ItemStack> list = slotDisplay.resolveForStacks(contextMap);
        if (!list.isEmpty()) {
            this.ingredients.put((Object)slot, (Object)new GhostSlot(list, bl));
        }
    }

    protected void setInput(Slot slot, ContextMap contextMap, SlotDisplay slotDisplay) {
        this.setSlot(slot, contextMap, slotDisplay, false);
    }

    protected void setResult(Slot slot, ContextMap contextMap, SlotDisplay slotDisplay) {
        this.setSlot(slot, contextMap, slotDisplay, true);
    }

    public void render(GuiGraphics guiGraphics, Minecraft minecraft, boolean bl) {
        this.ingredients.forEach((slot, ghostSlot) -> {
            int i = slot.x;
            int j = slot.y;
            if (ghostSlot.isResultSlot && bl) {
                guiGraphics.fill(i - 4, j - 4, i + 20, j + 20, 0x30FF0000);
            } else {
                guiGraphics.fill(i, j, i + 16, j + 16, 0x30FF0000);
            }
            ItemStack itemStack = ghostSlot.getItem(this.slotSelectTime.currentIndex());
            guiGraphics.renderFakeItem(itemStack, i, j);
            guiGraphics.fill(i, j, i + 16, j + 16, 0x30FFFFFF);
            if (ghostSlot.isResultSlot) {
                guiGraphics.renderItemDecorations(minecraft.font, itemStack, i, j);
            }
        });
    }

    public void renderTooltip(GuiGraphics guiGraphics, Minecraft minecraft, int i, int j, @Nullable Slot slot) {
        if (slot == null) {
            return;
        }
        GhostSlot ghostSlot = (GhostSlot)((Object)this.ingredients.get((Object)slot));
        if (ghostSlot != null) {
            ItemStack itemStack = ghostSlot.getItem(this.slotSelectTime.currentIndex());
            guiGraphics.setComponentTooltipForNextFrame(minecraft.font, Screen.getTooltipFromItem(minecraft, itemStack), i, j, itemStack.get(DataComponents.TOOLTIP_STYLE));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class GhostSlot
    extends Record {
        private final List<ItemStack> items;
        final boolean isResultSlot;

        GhostSlot(List<ItemStack> list, boolean bl) {
            this.items = list;
            this.isResultSlot = bl;
        }

        public ItemStack getItem(int i) {
            int j = this.items.size();
            if (j == 0) {
                return ItemStack.EMPTY;
            }
            return this.items.get(i % j);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GhostSlot.class, "items;isResultSlot", "items", "isResultSlot"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GhostSlot.class, "items;isResultSlot", "items", "isResultSlot"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GhostSlot.class, "items;isResultSlot", "items", "isResultSlot"}, this, object);
        }

        public List<ItemStack> items() {
            return this.items;
        }

        public boolean isResultSlot() {
            return this.isResultSlot;
        }
    }
}


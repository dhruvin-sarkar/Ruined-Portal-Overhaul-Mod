/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WolfRenderState
extends LivingEntityRenderState {
    private static final Identifier DEFAULT_TEXTURE = Identifier.withDefaultNamespace("textures/entity/wolf/wolf.png");
    public boolean isAngry;
    public boolean isSitting;
    public float tailAngle = 0.62831855f;
    public float headRollAngle;
    public float shakeAnim;
    public float wetShade = 1.0f;
    public Identifier texture = DEFAULT_TEXTURE;
    public @Nullable DyeColor collarColor;
    public ItemStack bodyArmorItem = ItemStack.EMPTY;

    public float getBodyRollAngle(float f) {
        float g = (this.shakeAnim + f) / 1.8f;
        if (g < 0.0f) {
            g = 0.0f;
        } else if (g > 1.0f) {
            g = 1.0f;
        }
        return Mth.sin(g * (float)Math.PI) * Mth.sin(g * (float)Math.PI * 11.0f) * 0.15f * (float)Math.PI;
    }
}


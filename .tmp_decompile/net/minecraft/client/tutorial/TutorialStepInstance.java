/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.tutorial;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

@Environment(value=EnvType.CLIENT)
public interface TutorialStepInstance {
    default public void clear() {
    }

    default public void tick() {
    }

    default public void onInput(ClientInput clientInput) {
    }

    default public void onMouse(double d, double e) {
    }

    default public void onLookAt(ClientLevel clientLevel, HitResult hitResult) {
    }

    default public void onDestroyBlock(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState, float f) {
    }

    default public void onOpenInventory() {
    }

    default public void onGetItem(ItemStack itemStack) {
    }
}


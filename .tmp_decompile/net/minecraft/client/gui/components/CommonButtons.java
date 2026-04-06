/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public class CommonButtons {
    public static SpriteIconButton language(int i, Button.OnPress onPress, boolean bl) {
        return SpriteIconButton.builder(Component.translatable("options.language"), onPress, bl).width(i).sprite(Identifier.withDefaultNamespace("icon/language"), 15, 15).build();
    }

    public static SpriteIconButton accessibility(int i, Button.OnPress onPress, boolean bl) {
        MutableComponent component = bl ? Component.translatable("options.accessibility") : Component.translatable("accessibility.onboarding.accessibility.button");
        return SpriteIconButton.builder(component, onPress, bl).width(i).sprite(Identifier.withDefaultNamespace("icon/accessibility"), 15, 15).build();
    }
}


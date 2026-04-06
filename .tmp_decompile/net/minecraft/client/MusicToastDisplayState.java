/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

@Environment(value=EnvType.CLIENT)
public enum MusicToastDisplayState implements StringRepresentable
{
    NEVER("never", "options.musicToast.never"),
    PAUSE("pause", "options.musicToast.pauseMenu"),
    PAUSE_AND_TOAST("pause_and_toast", "options.musicToast.pauseMenuAndToast");

    public static final Codec<MusicToastDisplayState> CODEC;
    private final String name;
    private final Component text;
    private final Component tooltip;

    private MusicToastDisplayState(String string2, String string3) {
        this.name = string2;
        this.text = Component.translatable(string3);
        this.tooltip = Component.translatable(string3 + ".tooltip");
    }

    public Component text() {
        return this.text;
    }

    public Component tooltip() {
        return this.tooltip;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean renderInPauseScreen() {
        return this != NEVER;
    }

    public boolean renderToast() {
        return this == PAUSE_AND_TOAST;
    }

    static {
        CODEC = StringRepresentable.fromEnum(MusicToastDisplayState::values);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.components.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringRepresentable;

@Environment(value=EnvType.CLIENT)
public enum DebugScreenProfile implements StringRepresentable
{
    DEFAULT("default", "debug.options.profile.default"),
    PERFORMANCE("performance", "debug.options.profile.performance");

    public static final StringRepresentable.EnumCodec<DebugScreenProfile> CODEC;
    private final String name;
    private final String translationKey;

    private DebugScreenProfile(String string2, String string3) {
        this.name = string2;
        this.translationKey = string3;
    }

    public String translationKey() {
        return this.translationKey;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromEnum(DebugScreenProfile::values);
    }
}


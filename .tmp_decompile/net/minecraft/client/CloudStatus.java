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
public enum CloudStatus implements StringRepresentable
{
    OFF("false", "options.off"),
    FAST("fast", "options.clouds.fast"),
    FANCY("true", "options.clouds.fancy");

    public static final Codec<CloudStatus> CODEC;
    private final String legacyName;
    private final Component caption;

    private CloudStatus(String string2, String string3) {
        this.legacyName = string2;
        this.caption = Component.translatable(string3);
    }

    public Component caption() {
        return this.caption;
    }

    @Override
    public String getSerializedName() {
        return this.legacyName;
    }

    static {
        CODEC = StringRepresentable.fromEnum(CloudStatus::values);
    }
}


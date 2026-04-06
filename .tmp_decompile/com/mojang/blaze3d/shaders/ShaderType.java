/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.DontObfuscate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
@DontObfuscate
public enum ShaderType {
    VERTEX("vertex", ".vsh"),
    FRAGMENT("fragment", ".fsh");

    private static final ShaderType[] TYPES;
    private final String name;
    private final String extension;

    private ShaderType(String string2, String string3) {
        this.name = string2;
        this.extension = string3;
    }

    public static @Nullable ShaderType byLocation(Identifier identifier) {
        for (ShaderType shaderType : TYPES) {
            if (!identifier.getPath().endsWith(shaderType.extension)) continue;
            return shaderType;
        }
        return null;
    }

    public String getName() {
        return this.name;
    }

    public FileToIdConverter idConverter() {
        return new FileToIdConverter("shaders", this.extension);
    }

    static {
        TYPES = ShaderType.values();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.ItemStackTagFix;

public class OminousBannerRenameFix
extends ItemStackTagFix {
    public OminousBannerRenameFix(Schema schema) {
        super(schema, "OminousBannerRenameFix", string -> string.equals("minecraft:white_banner"));
    }

    private <T> Dynamic<T> fixItemStackTag(Dynamic<T> dynamic) {
        return dynamic.update("display", dynamic2 -> dynamic2.update("Name", dynamic -> {
            Optional optional = dynamic.asString().result();
            if (optional.isPresent()) {
                return dynamic.createString(((String)optional.get()).replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\""));
            }
            return dynamic;
        }));
    }

    @Override
    protected Typed<?> fixItemStackTag(Typed<?> typed) {
        return Util.writeAndReadTypedOrThrow(typed, typed.getType(), this::fixItemStackTag);
    }
}


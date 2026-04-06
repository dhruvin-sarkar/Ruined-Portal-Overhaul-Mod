/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.ItemStackTagFix;

public class FilteredBooksFix
extends ItemStackTagFix {
    public FilteredBooksFix(Schema schema) {
        super(schema, "Remove filtered text from books", string -> string.equals("minecraft:writable_book") || string.equals("minecraft:written_book"));
    }

    @Override
    protected Typed<?> fixItemStackTag(Typed<?> typed) {
        return Util.writeAndReadTypedOrThrow(typed, typed.getType(), dynamic -> dynamic.remove("filtered_title").remove("filtered_pages"));
    }
}


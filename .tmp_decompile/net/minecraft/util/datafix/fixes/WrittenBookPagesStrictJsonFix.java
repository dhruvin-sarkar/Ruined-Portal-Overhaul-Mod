/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.ItemStackTagFix;
import net.minecraft.util.datafix.fixes.References;

public class WrittenBookPagesStrictJsonFix
extends ItemStackTagFix {
    public WrittenBookPagesStrictJsonFix(Schema schema) {
        super(schema, "WrittenBookPagesStrictJsonFix", string -> string.equals("minecraft:written_book"));
    }

    @Override
    protected Typed<?> fixItemStackTag(Typed<?> typed2) {
        Type type = this.getInputSchema().getType(References.TEXT_COMPONENT);
        Type type2 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder opticFinder = type2.findField("tag");
        OpticFinder opticFinder2 = opticFinder.type().findField("pages");
        OpticFinder opticFinder3 = DSL.typeFinder((Type)type);
        return typed2.updateTyped(opticFinder2, typed -> typed.update(opticFinder3, pair -> pair.mapSecond(LegacyComponentDataFixUtils::rewriteFromLenient)));
    }
}


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
import java.util.List;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class SignTextStrictJsonFix
extends NamedEntityFix {
    private static final List<String> LINE_FIELDS = List.of((Object)"Text1", (Object)"Text2", (Object)"Text3", (Object)"Text4");

    public SignTextStrictJsonFix(Schema schema) {
        super(schema, false, "SignTextStrictJsonFix", References.BLOCK_ENTITY, "Sign");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed2) {
        for (String string : LINE_FIELDS) {
            OpticFinder opticFinder = typed2.getType().findField(string);
            OpticFinder opticFinder2 = DSL.typeFinder((Type)this.getInputSchema().getType(References.TEXT_COMPONENT));
            typed2 = typed2.updateTyped(opticFinder, typed -> typed.update(opticFinder2, pair -> pair.mapSecond(LegacyComponentDataFixUtils::rewriteFromLenient)));
        }
        return typed2;
    }
}


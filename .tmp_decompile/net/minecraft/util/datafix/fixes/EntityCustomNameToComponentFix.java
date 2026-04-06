/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityCustomNameToComponentFix
extends DataFix {
    public EntityCustomNameToComponentFix(Schema schema) {
        super(schema, true);
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.ENTITY);
        Type type2 = this.getOutputSchema().getType(References.ENTITY);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", NamespacedSchema.namespacedString());
        OpticFinder opticFinder2 = type.findField("CustomName");
        Type type3 = type2.findFieldType("CustomName");
        return this.fixTypeEverywhereTyped("EntityCustomNameToComponentFix", type, type2, typed -> EntityCustomNameToComponentFix.fixEntity(typed, type2, (OpticFinder<String>)opticFinder, (OpticFinder<String>)opticFinder2, type3));
    }

    private static <T> Typed<?> fixEntity(Typed<?> typed, Type<?> type, OpticFinder<String> opticFinder, OpticFinder<String> opticFinder2, Type<T> type2) {
        Optional optional = typed.getOptional(opticFinder2);
        if (optional.isEmpty()) {
            return ExtraDataFixUtils.cast(type, typed);
        }
        if (((String)optional.get()).isEmpty()) {
            return Util.writeAndReadTypedOrThrow(typed, type, dynamic -> dynamic.remove("CustomName"));
        }
        String string = typed.getOptional(opticFinder).orElse("");
        Dynamic<T> dynamic2 = EntityCustomNameToComponentFix.fixCustomName(typed.getOps(), (String)optional.get(), string);
        return typed.set(opticFinder2, Util.readTypedOrThrow(type2, dynamic2));
    }

    private static <T> Dynamic<T> fixCustomName(DynamicOps<T> dynamicOps, String string, String string2) {
        if ("minecraft:commandblock_minecart".equals(string2)) {
            return new Dynamic(dynamicOps, dynamicOps.createString(string));
        }
        return LegacyComponentDataFixUtils.createPlainTextComponent(dynamicOps, string);
    }
}


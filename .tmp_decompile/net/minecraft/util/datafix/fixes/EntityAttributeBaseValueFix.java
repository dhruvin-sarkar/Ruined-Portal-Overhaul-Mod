/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.DoubleUnaryOperator;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityAttributeBaseValueFix
extends NamedEntityFix {
    private final String attributeId;
    private final DoubleUnaryOperator valueFixer;

    public EntityAttributeBaseValueFix(Schema schema, String string, String string2, String string3, DoubleUnaryOperator doubleUnaryOperator) {
        super(schema, false, string, References.ENTITY, string2);
        this.attributeId = string3;
        this.valueFixer = doubleUnaryOperator;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixValue);
    }

    private Dynamic<?> fixValue(Dynamic<?> dynamic) {
        return dynamic.update("attributes", dynamic22 -> dynamic.createList(dynamic22.asStream().map(dynamic -> {
            String string = NamespacedSchema.ensureNamespaced(dynamic.get("id").asString(""));
            if (!string.equals(this.attributeId)) {
                return dynamic;
            }
            double d = dynamic.get("base").asDouble(0.0);
            return dynamic.set("base", dynamic.createDouble(this.valueFixer.applyAsDouble(d)));
        })));
    }
}


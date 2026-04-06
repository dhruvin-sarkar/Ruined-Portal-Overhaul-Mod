/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  java.util.SequencedMap
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V3818_3;

public class V4059
extends NamespacedSchema {
    public V4059(int i, Schema schema) {
        super(i, schema);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema schema) {
        SequencedMap<String, Supplier<TypeTemplate>> sequencedMap = V3818_3.components(schema);
        sequencedMap.remove((Object)"minecraft:food");
        sequencedMap.put((Object)"minecraft:use_remainder", () -> References.ITEM_STACK.in(schema));
        sequencedMap.put((Object)"minecraft:equippable", () -> DSL.optionalFields((String)"allowed_entities", (TypeTemplate)DSL.or((TypeTemplate)References.ENTITY_NAME.in(schema), (TypeTemplate)DSL.list((TypeTemplate)References.ENTITY_NAME.in(schema)))));
        return sequencedMap;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, References.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(V4059.components(schema)));
    }
}


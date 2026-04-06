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
import net.minecraft.util.datafix.schemas.V4059;

public class V4307
extends NamespacedSchema {
    public V4307(int i, Schema schema) {
        super(i, schema);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema schema) {
        SequencedMap<String, Supplier<TypeTemplate>> sequencedMap = V4059.components(schema);
        sequencedMap.put((Object)"minecraft:can_place_on", () -> V4307.adventureModePredicate(schema));
        sequencedMap.put((Object)"minecraft:can_break", () -> V4307.adventureModePredicate(schema));
        return sequencedMap;
    }

    private static TypeTemplate adventureModePredicate(Schema schema) {
        TypeTemplate typeTemplate = DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_NAME.in(schema), (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema))));
        return DSL.or((TypeTemplate)typeTemplate, (TypeTemplate)DSL.list((TypeTemplate)typeTemplate));
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, References.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(V4307.components(schema)));
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V4301
extends NamespacedSchema {
    public V4301(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, References.ENTITY_EQUIPMENT, () -> DSL.optional((TypeTemplate)DSL.field((String)"equipment", (TypeTemplate)DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"mainhand", (Object)References.ITEM_STACK.in(schema)), Pair.of((Object)"offhand", (Object)References.ITEM_STACK.in(schema)), Pair.of((Object)"feet", (Object)References.ITEM_STACK.in(schema)), Pair.of((Object)"legs", (Object)References.ITEM_STACK.in(schema)), Pair.of((Object)"chest", (Object)References.ITEM_STACK.in(schema)), Pair.of((Object)"head", (Object)References.ITEM_STACK.in(schema)), Pair.of((Object)"body", (Object)References.ITEM_STACK.in(schema)), Pair.of((Object)"saddle", (Object)References.ITEM_STACK.in(schema))}))));
    }
}


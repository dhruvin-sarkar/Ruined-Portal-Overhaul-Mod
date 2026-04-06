/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class CustomModelDataExpandFix
extends DataFix {
    public CustomModelDataExpandFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.DATA_COMPONENTS);
        return this.fixTypeEverywhereTyped("Custom Model Data expansion", type, typed -> typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("minecraft:custom_model_data", dynamic -> {
            float f = dynamic.asNumber((Number)Float.valueOf(0.0f)).floatValue();
            return dynamic.createMap(Map.of((Object)dynamic.createString("floats"), (Object)dynamic.createList(Stream.of(dynamic.createFloat(f)))));
        })));
    }
}


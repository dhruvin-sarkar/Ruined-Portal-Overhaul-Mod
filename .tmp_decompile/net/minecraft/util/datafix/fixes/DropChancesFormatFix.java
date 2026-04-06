/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;
import net.minecraft.util.datafix.fixes.References;

public class DropChancesFormatFix
extends DataFix {
    private static final List<String> ARMOR_SLOT_NAMES = List.of((Object)"feet", (Object)"legs", (Object)"chest", (Object)"head");
    private static final List<String> HAND_SLOT_NAMES = List.of((Object)"mainhand", (Object)"offhand");
    private static final float DEFAULT_CHANCE = 0.085f;

    public DropChancesFormatFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("DropChancesFormatFix", this.getInputSchema().getType(References.ENTITY), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            List<Float> list = DropChancesFormatFix.parseDropChances(dynamic.get("ArmorDropChances"));
            List<Float> list2 = DropChancesFormatFix.parseDropChances(dynamic.get("HandDropChances"));
            float f = dynamic.get("body_armor_drop_chance").asNumber().result().map(Number::floatValue).orElse(Float.valueOf(0.085f)).floatValue();
            dynamic = dynamic.remove("ArmorDropChances").remove("HandDropChances").remove("body_armor_drop_chance");
            Dynamic dynamic2 = dynamic.emptyMap();
            dynamic2 = DropChancesFormatFix.addSlotChances(dynamic2, list, ARMOR_SLOT_NAMES);
            dynamic2 = DropChancesFormatFix.addSlotChances(dynamic2, list2, HAND_SLOT_NAMES);
            if (f != 0.085f) {
                dynamic2 = dynamic2.set("body", dynamic.createFloat(f));
            }
            if (!dynamic2.equals((Object)dynamic.emptyMap())) {
                return dynamic.set("drop_chances", dynamic2);
            }
            return dynamic;
        }));
    }

    private static Dynamic<?> addSlotChances(Dynamic<?> dynamic, List<Float> list, List<String> list2) {
        for (int i = 0; i < list2.size() && i < list.size(); ++i) {
            String string = list2.get(i);
            float f = list.get(i).floatValue();
            if (f == 0.085f) continue;
            dynamic = dynamic.set(string, dynamic.createFloat(f));
        }
        return dynamic;
    }

    private static List<Float> parseDropChances(OptionalDynamic<?> optionalDynamic) {
        return optionalDynamic.asStream().map(dynamic -> Float.valueOf(dynamic.asFloat(0.085f))).toList();
    }
}


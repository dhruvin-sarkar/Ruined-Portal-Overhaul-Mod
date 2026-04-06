/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.datafix.fixes.References;

public class PlayerEquipmentFix
extends DataFix {
    private static final Map<Integer, String> SLOT_TRANSLATIONS = Map.of((Object)100, (Object)"feet", (Object)101, (Object)"legs", (Object)102, (Object)"chest", (Object)103, (Object)"head", (Object)-106, (Object)"offhand");

    public PlayerEquipmentFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getTypeRaw(References.PLAYER);
        Type type2 = this.getOutputSchema().getTypeRaw(References.PLAYER);
        return this.writeFixAndRead("Player Equipment Fix", type, type2, dynamic2 -> {
            HashMap map = new HashMap();
            dynamic2 = dynamic2.update("Inventory", dynamic -> dynamic.createList(dynamic.asStream().filter(dynamic2 -> {
                int i = dynamic2.get("Slot").asInt(-1);
                String string = SLOT_TRANSLATIONS.get(i);
                if (string != null) {
                    map.put(dynamic.createString(string), dynamic2.remove("Slot"));
                }
                return string == null;
            })));
            dynamic2 = dynamic2.set("equipment", dynamic2.createMap(map));
            return dynamic2;
        });
    }
}


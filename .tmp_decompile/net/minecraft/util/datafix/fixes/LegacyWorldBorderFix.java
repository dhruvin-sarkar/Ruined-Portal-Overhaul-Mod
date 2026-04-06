/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class LegacyWorldBorderFix
extends DataFix {
    public LegacyWorldBorderFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LegacyWorldBorderFix", this.getInputSchema().getType(References.LEVEL), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            Dynamic dynamic2 = dynamic.emptyMap().set("center_x", dynamic.createDouble(dynamic.get("BorderCenterX").asDouble(0.0))).set("center_z", dynamic.createDouble(dynamic.get("BorderCenterZ").asDouble(0.0))).set("size", dynamic.createDouble(dynamic.get("BorderSize").asDouble(5.9999968E7))).set("lerp_time", dynamic.createLong(dynamic.get("BorderSizeLerpTime").asLong(0L))).set("lerp_target", dynamic.createDouble(dynamic.get("BorderSizeLerpTarget").asDouble(0.0))).set("safe_zone", dynamic.createDouble(dynamic.get("BorderSafeZone").asDouble(5.0))).set("damage_per_block", dynamic.createDouble(dynamic.get("BorderDamagePerBlock").asDouble(0.2))).set("warning_blocks", dynamic.createInt(dynamic.get("BorderWarningBlocks").asInt(5))).set("warning_time", dynamic.createInt(dynamic.get("BorderWarningTime").asInt(15)));
            dynamic = dynamic.remove("BorderCenterX").remove("BorderCenterZ").remove("BorderSize").remove("BorderSizeLerpTime").remove("BorderSizeLerpTarget").remove("BorderSafeZone").remove("BorderDamagePerBlock").remove("BorderWarningBlocks").remove("BorderWarningTime");
            return dynamic.set("world_border", dynamic2);
        }));
    }
}


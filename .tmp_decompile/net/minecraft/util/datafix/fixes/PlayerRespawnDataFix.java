/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.References;

public class PlayerRespawnDataFix
extends DataFix {
    public PlayerRespawnDataFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("PlayerRespawnDataFix", this.getInputSchema().getType(References.PLAYER), typed -> typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("respawn", dynamic -> dynamic.set("dimension", dynamic.createString(dynamic.get("dimension").asString("minecraft:overworld"))).set("yaw", dynamic.createFloat(dynamic.get("angle").asFloat(0.0f))).set("pitch", dynamic.createFloat(0.0f)).remove("angle"))));
    }
}


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
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class CopperGolemWeatherStateFix
extends NamedEntityFix {
    public CopperGolemWeatherStateFix(Schema schema) {
        super(schema, false, "CopperGolemWeatherStateFix", References.ENTITY, "minecraft:copper_golem");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("weather_state", CopperGolemWeatherStateFix::fixWeatherState));
    }

    private static Dynamic<?> fixWeatherState(Dynamic<?> dynamic) {
        return switch (dynamic.asInt(0)) {
            case 1 -> dynamic.createString("exposed");
            case 2 -> dynamic.createString("weathered");
            case 3 -> dynamic.createString("oxidized");
            default -> dynamic.createString("unaffected");
        };
    }
}


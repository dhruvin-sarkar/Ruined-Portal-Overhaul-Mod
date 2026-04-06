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

public class VillagerSetCanPickUpLootFix
extends NamedEntityFix {
    private static final String CAN_PICK_UP_LOOT = "CanPickUpLoot";

    public VillagerSetCanPickUpLootFix(Schema schema) {
        super(schema, true, "Villager CanPickUpLoot default value", References.ENTITY, "Villager");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), VillagerSetCanPickUpLootFix::fixValue);
    }

    private static Dynamic<?> fixValue(Dynamic<?> dynamic) {
        return dynamic.set(CAN_PICK_UP_LOOT, dynamic.createBoolean(true));
    }
}


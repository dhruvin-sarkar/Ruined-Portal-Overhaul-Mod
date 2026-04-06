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

public class BlockEntityFurnaceBurnTimeFix
extends NamedEntityFix {
    public BlockEntityFurnaceBurnTimeFix(Schema schema, String string) {
        super(schema, false, "BlockEntityFurnaceBurnTimeFix" + string, References.BLOCK_ENTITY, string);
    }

    public Dynamic<?> fixBurnTime(Dynamic<?> dynamic) {
        dynamic = dynamic.renameField("CookTime", "cooking_time_spent");
        dynamic = dynamic.renameField("CookTimeTotal", "cooking_total_time");
        dynamic = dynamic.renameField("BurnTime", "lit_time_remaining");
        dynamic = dynamic.setFieldIfPresent("lit_total_time", dynamic.get("lit_time_remaining").result());
        return dynamic;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixBurnTime);
    }
}


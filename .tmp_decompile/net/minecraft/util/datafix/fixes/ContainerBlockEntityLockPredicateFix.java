/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.fixes.LockComponentPredicateFix;
import net.minecraft.util.datafix.fixes.References;

public class ContainerBlockEntityLockPredicateFix
extends DataFix {
    public ContainerBlockEntityLockPredicateFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("ContainerBlockEntityLockPredicateFix", (Type)this.getInputSchema().findChoiceType(References.BLOCK_ENTITY), ContainerBlockEntityLockPredicateFix::fixBlockEntity);
    }

    private static Typed<?> fixBlockEntity(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> dynamic.renameAndFixField("Lock", "lock", LockComponentPredicateFix::fixLock));
    }
}


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
import java.util.stream.IntStream;
import net.minecraft.util.datafix.fixes.References;

public class ChunkTicketUnpackPosFix
extends DataFix {
    private static final long CHUNK_COORD_BITS = 32L;
    private static final long CHUNK_COORD_MASK = 0xFFFFFFFFL;

    public ChunkTicketUnpackPosFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("ChunkTicketUnpackPosFix", this.getInputSchema().getType(References.SAVED_DATA_TICKETS), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("data", dynamic2 -> dynamic2.update("tickets", dynamic -> dynamic.createList(dynamic.asStream().map(dynamic2 -> dynamic2.update("chunk_pos", dynamic -> {
            long l = dynamic.asLong(0L);
            int i = (int)(l & 0xFFFFFFFFL);
            int j = (int)(l >>> 32 & 0xFFFFFFFFL);
            return dynamic.createIntList(IntStream.of(i, j));
        })))))));
    }
}


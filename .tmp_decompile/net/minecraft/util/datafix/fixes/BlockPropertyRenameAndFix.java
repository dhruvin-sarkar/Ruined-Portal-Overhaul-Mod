/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.fixes.AbstractBlockPropertyFix;

public class BlockPropertyRenameAndFix
extends AbstractBlockPropertyFix {
    private final String blockId;
    private final String oldPropertyName;
    private final String newPropertyName;
    private final UnaryOperator<String> valueFixer;

    public BlockPropertyRenameAndFix(Schema schema, String string, String string2, String string3, String string4, UnaryOperator<String> unaryOperator) {
        super(schema, string);
        this.blockId = string2;
        this.oldPropertyName = string3;
        this.newPropertyName = string4;
        this.valueFixer = unaryOperator;
    }

    @Override
    protected boolean shouldFix(String string) {
        return string.equals(this.blockId);
    }

    @Override
    protected <T> Dynamic<T> fixProperties(String string, Dynamic<T> dynamic2) {
        return dynamic2.renameAndFixField(this.oldPropertyName, this.newPropertyName, dynamic -> dynamic.createString((String)this.valueFixer.apply(dynamic.asString(""))));
    }
}


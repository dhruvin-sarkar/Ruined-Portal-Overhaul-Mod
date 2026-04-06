/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import java.util.List;
import net.minecraft.util.datafix.fixes.AttributesRenameFix;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class AttributeIdPrefixFix
extends AttributesRenameFix {
    private static final List<String> PREFIXES = List.of((Object)"generic.", (Object)"horse.", (Object)"player.", (Object)"zombie.");

    public AttributeIdPrefixFix(Schema schema) {
        super(schema, "AttributeIdPrefixFix", AttributeIdPrefixFix::replaceId);
    }

    private static String replaceId(String string) {
        String string2 = NamespacedSchema.ensureNamespaced(string);
        for (String string3 : PREFIXES) {
            String string4 = NamespacedSchema.ensureNamespaced(string3);
            if (!string2.startsWith(string4)) continue;
            return "minecraft:" + string2.substring(string4.length());
        }
        return string;
    }
}


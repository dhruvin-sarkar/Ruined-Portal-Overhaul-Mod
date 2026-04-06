/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Splitter
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  org.apache.commons.lang3.math.NumberUtils
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.EntityBlockStateFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.math.NumberUtils;

public class LevelFlatGeneratorInfoFix
extends DataFix {
    private static final String GENERATOR_OPTIONS = "generatorOptions";
    @VisibleForTesting
    static final String DEFAULT = "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
    private static final Splitter SPLITTER = Splitter.on((char)';').limit(5);
    private static final Splitter LAYER_SPLITTER = Splitter.on((char)',');
    private static final Splitter OLD_AMOUNT_SPLITTER = Splitter.on((char)'x').limit(2);
    private static final Splitter AMOUNT_SPLITTER = Splitter.on((char)'*').limit(2);
    private static final Splitter BLOCK_SPLITTER = Splitter.on((char)':').limit(3);

    public LevelFlatGeneratorInfoFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LevelFlatGeneratorInfoFix", this.getInputSchema().getType(References.LEVEL), typed -> typed.update(DSL.remainderFinder(), this::fix));
    }

    private Dynamic<?> fix(Dynamic<?> dynamic2) {
        if (dynamic2.get("generatorName").asString("").equalsIgnoreCase("flat")) {
            return dynamic2.update(GENERATOR_OPTIONS, dynamic -> (Dynamic)DataFixUtils.orElse((Optional)dynamic.asString().map(this::fixString).map(arg_0 -> ((Dynamic)dynamic).createString(arg_0)).result(), (Object)dynamic));
        }
        return dynamic2;
    }

    @VisibleForTesting
    String fixString(String string2) {
        String string3;
        int i;
        if (string2.isEmpty()) {
            return DEFAULT;
        }
        Iterator iterator = SPLITTER.split((CharSequence)string2).iterator();
        String string22 = (String)iterator.next();
        if (iterator.hasNext()) {
            i = NumberUtils.toInt((String)string22, (int)0);
            string3 = (String)iterator.next();
        } else {
            i = 0;
            string3 = string22;
        }
        if (i < 0 || i > 3) {
            return DEFAULT;
        }
        StringBuilder stringBuilder = new StringBuilder();
        Splitter splitter = i < 3 ? OLD_AMOUNT_SPLITTER : AMOUNT_SPLITTER;
        stringBuilder.append(StreamSupport.stream(LAYER_SPLITTER.split((CharSequence)string3).spliterator(), false).map(string -> {
            String string2;
            int j;
            List list = splitter.splitToList((CharSequence)string);
            if (list.size() == 2) {
                j = NumberUtils.toInt((String)((String)list.get(0)));
                string2 = (String)list.get(1);
            } else {
                j = 1;
                string2 = (String)list.get(0);
            }
            List list2 = BLOCK_SPLITTER.splitToList((CharSequence)string2);
            int k = ((String)list2.get(0)).equals("minecraft") ? 1 : 0;
            String string3 = (String)list2.get(k);
            int l = i == 3 ? EntityBlockStateFix.getBlockId("minecraft:" + string3) : NumberUtils.toInt((String)string3, (int)0);
            int m = k + 1;
            int n = list2.size() > m ? NumberUtils.toInt((String)((String)list2.get(m)), (int)0) : 0;
            return (String)(j == 1 ? "" : j + "*") + BlockStateData.getTag(l << 4 | n).get("Name").asString("");
        }).collect(Collectors.joining(",")));
        while (iterator.hasNext()) {
            stringBuilder.append(';').append((String)iterator.next());
        }
        return stringBuilder.toString();
    }
}


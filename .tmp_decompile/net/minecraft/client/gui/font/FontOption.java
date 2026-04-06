/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.font;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringRepresentable;

@Environment(value=EnvType.CLIENT)
public enum FontOption implements StringRepresentable
{
    UNIFORM("uniform"),
    JAPANESE_VARIANTS("jp");

    public static final Codec<FontOption> CODEC;
    private final String name;

    private FontOption(String string2) {
        this.name = string2;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromEnum(FontOption::values);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Filter {
        private final Map<FontOption, Boolean> values;
        public static final Codec<Filter> CODEC = Codec.unboundedMap(CODEC, (Codec)Codec.BOOL).xmap(Filter::new, filter -> filter.values);
        public static final Filter ALWAYS_PASS = new Filter(Map.of());

        public Filter(Map<FontOption, Boolean> map) {
            this.values = map;
        }

        public boolean apply(Set<FontOption> set) {
            for (Map.Entry<FontOption, Boolean> entry : this.values.entrySet()) {
                if (set.contains(entry.getKey()) == entry.getValue().booleanValue()) continue;
                return false;
            }
            return true;
        }

        public Filter merge(Filter filter) {
            HashMap<FontOption, Boolean> map = new HashMap<FontOption, Boolean>(filter.values);
            map.putAll(this.values);
            return new Filter(Map.copyOf(map));
        }
    }
}


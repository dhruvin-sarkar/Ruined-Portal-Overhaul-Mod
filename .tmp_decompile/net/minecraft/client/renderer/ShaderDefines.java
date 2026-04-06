/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.LambdaMetafactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public record ShaderDefines(Map<String, String> values, Set<String> flags) {
    public static final ShaderDefines EMPTY = new ShaderDefines(Map.of(), Set.of());
    public static final Codec<ShaderDefines> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.STRING).optionalFieldOf("values", (Object)Map.of()).forGetter(ShaderDefines::values), (App)Codec.STRING.listOf().xmap((Function<List, Set>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, copyOf(java.util.Collection ), (Ljava/util/List;)Ljava/util/Set;)(), (Function<Set, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, copyOf(java.util.Collection ), (Ljava/util/Set;)Ljava/util/List;)()).optionalFieldOf("flags", (Object)Set.of()).forGetter(ShaderDefines::flags)).apply((Applicative)instance, ShaderDefines::new));

    public static Builder builder() {
        return new Builder();
    }

    public ShaderDefines withOverrides(ShaderDefines shaderDefines) {
        if (this.isEmpty()) {
            return shaderDefines;
        }
        if (shaderDefines.isEmpty()) {
            return this;
        }
        ImmutableMap.Builder builder = ImmutableMap.builderWithExpectedSize((int)(this.values.size() + shaderDefines.values.size()));
        builder.putAll(this.values);
        builder.putAll(shaderDefines.values);
        ImmutableSet.Builder builder2 = ImmutableSet.builderWithExpectedSize((int)(this.flags.size() + shaderDefines.flags.size()));
        builder2.addAll(this.flags);
        builder2.addAll(shaderDefines.flags);
        return new ShaderDefines((Map<String, String>)builder.buildKeepingLast(), (Set<String>)builder2.build());
    }

    public String asSourceDirectives() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : this.values.entrySet()) {
            String string = entry.getKey();
            String string2 = entry.getValue();
            stringBuilder.append("#define ").append(string).append(" ").append(string2).append('\n');
        }
        for (String string3 : this.flags) {
            stringBuilder.append("#define ").append(string3).append('\n');
        }
        return stringBuilder.toString();
    }

    public boolean isEmpty() {
        return this.values.isEmpty() && this.flags.isEmpty();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final ImmutableMap.Builder<String, String> values = ImmutableMap.builder();
        private final ImmutableSet.Builder<String> flags = ImmutableSet.builder();

        Builder() {
        }

        public Builder define(String string, String string2) {
            if (string2.isBlank()) {
                throw new IllegalArgumentException("Cannot define empty string");
            }
            this.values.put((Object)string, (Object)Builder.escapeNewLines(string2));
            return this;
        }

        private static String escapeNewLines(String string) {
            return string.replaceAll("\n", "\\\\\n");
        }

        public Builder define(String string, float f) {
            this.values.put((Object)string, (Object)String.valueOf(f));
            return this;
        }

        public Builder define(String string, int i) {
            this.values.put((Object)string, (Object)String.valueOf(i));
            return this;
        }

        public Builder define(String string) {
            this.flags.add((Object)string);
            return this;
        }

        public ShaderDefines build() {
            return new ShaderDefines((Map<String, String>)this.values.build(), (Set<String>)this.flags.build());
        }
    }
}


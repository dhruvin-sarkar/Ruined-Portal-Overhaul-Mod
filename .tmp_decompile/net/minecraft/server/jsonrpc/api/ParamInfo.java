/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.jsonrpc.api;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.jsonrpc.api.Schema;

public record ParamInfo<Param>(String name, Schema<Param> schema, boolean required) {
    public ParamInfo(String string, Schema<Param> schema) {
        this(string, schema, true);
    }

    public static <Param> MapCodec<ParamInfo<Param>> typedCodec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("name").forGetter(ParamInfo::name), (App)Schema.typedCodec().fieldOf("schema").forGetter(ParamInfo::schema), (App)Codec.BOOL.fieldOf("required").forGetter(ParamInfo::required)).apply((Applicative)instance, ParamInfo::new));
    }
}


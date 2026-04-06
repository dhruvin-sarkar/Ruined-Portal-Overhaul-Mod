/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public record Message(Optional<String> literal, Optional<String> translatable, Optional<List<String>> translatableParams) {
    public static final Codec<Message> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.optionalFieldOf("literal").forGetter(Message::literal), (App)Codec.STRING.optionalFieldOf("translatable").forGetter(Message::translatable), (App)Codec.STRING.listOf().lenientOptionalFieldOf("translatableParams").forGetter(Message::translatableParams)).apply((Applicative)instance, Message::new));

    public Optional<Component> asComponent() {
        if (this.translatable.isPresent()) {
            String string = this.translatable.get();
            if (this.translatableParams.isPresent()) {
                List<String> list = this.translatableParams.get();
                return Optional.of(Component.translatable(string, list.toArray()));
            }
            return Optional.of(Component.translatable(string));
        }
        return this.literal.map(Component::literal);
    }
}


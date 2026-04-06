/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.player;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.PlayerModelType;
import org.jspecify.annotations.Nullable;

public record PlayerSkin(ClientAsset.Texture body,  @Nullable ClientAsset.Texture cape,  @Nullable ClientAsset.Texture elytra, PlayerModelType model, boolean secure) {
    public static PlayerSkin insecure(ClientAsset.Texture texture,  @Nullable ClientAsset.Texture texture2,  @Nullable ClientAsset.Texture texture3, PlayerModelType playerModelType) {
        return new PlayerSkin(texture, texture2, texture3, playerModelType, false);
    }

    public PlayerSkin with(Patch patch) {
        if (patch.equals((Object)Patch.EMPTY)) {
            return this;
        }
        return PlayerSkin.insecure((ClientAsset.Texture)DataFixUtils.orElse(patch.body, (Object)this.body), (ClientAsset.Texture)DataFixUtils.orElse(patch.cape, (Object)this.cape), (ClientAsset.Texture)DataFixUtils.orElse(patch.elytra, (Object)this.elytra), patch.model.orElse(this.model));
    }

    public static final class Patch
    extends Record {
        final Optional<ClientAsset.ResourceTexture> body;
        final Optional<ClientAsset.ResourceTexture> cape;
        final Optional<ClientAsset.ResourceTexture> elytra;
        final Optional<PlayerModelType> model;
        public static final Patch EMPTY = new Patch(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        public static final MapCodec<Patch> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ClientAsset.ResourceTexture.CODEC.optionalFieldOf("texture").forGetter(Patch::body), (App)ClientAsset.ResourceTexture.CODEC.optionalFieldOf("cape").forGetter(Patch::cape), (App)ClientAsset.ResourceTexture.CODEC.optionalFieldOf("elytra").forGetter(Patch::elytra), (App)PlayerModelType.CODEC.optionalFieldOf("model").forGetter(Patch::model)).apply((Applicative)instance, Patch::create));
        public static final StreamCodec<ByteBuf, Patch> STREAM_CODEC = StreamCodec.composite(ClientAsset.ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::body, ClientAsset.ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::cape, ClientAsset.ResourceTexture.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::elytra, PlayerModelType.STREAM_CODEC.apply(ByteBufCodecs::optional), Patch::model, Patch::create);

        public Patch(Optional<ClientAsset.ResourceTexture> optional, Optional<ClientAsset.ResourceTexture> optional2, Optional<ClientAsset.ResourceTexture> optional3, Optional<PlayerModelType> optional4) {
            this.body = optional;
            this.cape = optional2;
            this.elytra = optional3;
            this.model = optional4;
        }

        public static Patch create(Optional<ClientAsset.ResourceTexture> optional, Optional<ClientAsset.ResourceTexture> optional2, Optional<ClientAsset.ResourceTexture> optional3, Optional<PlayerModelType> optional4) {
            if (optional.isEmpty() && optional2.isEmpty() && optional3.isEmpty() && optional4.isEmpty()) {
                return EMPTY;
            }
            return new Patch(optional, optional2, optional3, optional4);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Patch.class, "body;cape;elytra;model", "body", "cape", "elytra", "model"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Patch.class, "body;cape;elytra;model", "body", "cape", "elytra", "model"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Patch.class, "body;cape;elytra;model", "body", "cape", "elytra", "model"}, this, object);
        }

        public Optional<ClientAsset.ResourceTexture> body() {
            return this.body;
        }

        public Optional<ClientAsset.ResourceTexture> cape() {
            return this.cape;
        }

        public Optional<ClientAsset.ResourceTexture> elytra() {
            return this.elytra;
        }

        public Optional<PlayerModelType> model() {
            return this.model;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PowerParticleOption
implements ParticleOptions {
    private final ParticleType<PowerParticleOption> type;
    private final float power;

    public static MapCodec<PowerParticleOption> codec(ParticleType<PowerParticleOption> particleType) {
        return Codec.FLOAT.xmap(float_ -> new PowerParticleOption(particleType, float_.floatValue()), powerParticleOption -> Float.valueOf(powerParticleOption.power)).optionalFieldOf("power", (Object)PowerParticleOption.create(particleType, 1.0f));
    }

    public static StreamCodec<? super ByteBuf, PowerParticleOption> streamCodec(ParticleType<PowerParticleOption> particleType) {
        return ByteBufCodecs.FLOAT.map(float_ -> new PowerParticleOption(particleType, float_.floatValue()), powerParticleOption -> Float.valueOf(powerParticleOption.power));
    }

    private PowerParticleOption(ParticleType<PowerParticleOption> particleType, float f) {
        this.type = particleType;
        this.power = f;
    }

    public ParticleType<PowerParticleOption> getType() {
        return this.type;
    }

    public float getPower() {
        return this.power;
    }

    public static PowerParticleOption create(ParticleType<PowerParticleOption> particleType, float f) {
        return new PowerParticleOption(particleType, f);
    }
}


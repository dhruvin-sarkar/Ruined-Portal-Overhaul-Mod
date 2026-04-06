/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ComparisonChain
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.ints.Int2IntFunction
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MobEffectInstance
implements Comparable<MobEffectInstance> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int INFINITE_DURATION = -1;
    public static final int MIN_AMPLIFIER = 0;
    public static final int MAX_AMPLIFIER = 255;
    public static final Codec<MobEffectInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MobEffect.CODEC.fieldOf("id").forGetter(MobEffectInstance::getEffect), (App)Details.MAP_CODEC.forGetter(MobEffectInstance::asDetails)).apply((Applicative)instance, MobEffectInstance::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MobEffectInstance> STREAM_CODEC = StreamCodec.composite(MobEffect.STREAM_CODEC, MobEffectInstance::getEffect, Details.STREAM_CODEC, MobEffectInstance::asDetails, MobEffectInstance::new);
    private final Holder<MobEffect> effect;
    private int duration;
    private int amplifier;
    private boolean ambient;
    private boolean visible;
    private boolean showIcon;
    private @Nullable MobEffectInstance hiddenEffect;
    private final BlendState blendState = new BlendState();

    public MobEffectInstance(Holder<MobEffect> holder) {
        this(holder, 0, 0);
    }

    public MobEffectInstance(Holder<MobEffect> holder, int i) {
        this(holder, i, 0);
    }

    public MobEffectInstance(Holder<MobEffect> holder, int i, int j) {
        this(holder, i, j, false, true);
    }

    public MobEffectInstance(Holder<MobEffect> holder, int i, int j, boolean bl, boolean bl2) {
        this(holder, i, j, bl, bl2, bl2);
    }

    public MobEffectInstance(Holder<MobEffect> holder, int i, int j, boolean bl, boolean bl2, boolean bl3) {
        this(holder, i, j, bl, bl2, bl3, null);
    }

    public MobEffectInstance(Holder<MobEffect> holder, int i, int j, boolean bl, boolean bl2, boolean bl3, @Nullable MobEffectInstance mobEffectInstance) {
        this.effect = holder;
        this.duration = i;
        this.amplifier = Mth.clamp(j, 0, 255);
        this.ambient = bl;
        this.visible = bl2;
        this.showIcon = bl3;
        this.hiddenEffect = mobEffectInstance;
    }

    public MobEffectInstance(MobEffectInstance mobEffectInstance) {
        this.effect = mobEffectInstance.effect;
        this.setDetailsFrom(mobEffectInstance);
    }

    private MobEffectInstance(Holder<MobEffect> holder, Details details2) {
        this(holder, details2.duration(), details2.amplifier(), details2.ambient(), details2.showParticles(), details2.showIcon(), details2.hiddenEffect().map(details -> new MobEffectInstance(holder, (Details)((Object)details))).orElse(null));
    }

    private Details asDetails() {
        return new Details(this.getAmplifier(), this.getDuration(), this.isAmbient(), this.isVisible(), this.showIcon(), Optional.ofNullable(this.hiddenEffect).map(MobEffectInstance::asDetails));
    }

    public float getBlendFactor(LivingEntity livingEntity, float f) {
        return this.blendState.getFactor(livingEntity, f);
    }

    public ParticleOptions getParticleOptions() {
        return this.effect.value().createParticleOptions(this);
    }

    void setDetailsFrom(MobEffectInstance mobEffectInstance) {
        this.duration = mobEffectInstance.duration;
        this.amplifier = mobEffectInstance.amplifier;
        this.ambient = mobEffectInstance.ambient;
        this.visible = mobEffectInstance.visible;
        this.showIcon = mobEffectInstance.showIcon;
    }

    public boolean update(MobEffectInstance mobEffectInstance) {
        if (!this.effect.equals(mobEffectInstance.effect)) {
            LOGGER.warn("This method should only be called for matching effects!");
        }
        boolean bl = false;
        if (mobEffectInstance.amplifier > this.amplifier) {
            if (mobEffectInstance.isShorterDurationThan(this)) {
                MobEffectInstance mobEffectInstance2 = this.hiddenEffect;
                this.hiddenEffect = new MobEffectInstance(this);
                this.hiddenEffect.hiddenEffect = mobEffectInstance2;
            }
            this.amplifier = mobEffectInstance.amplifier;
            this.duration = mobEffectInstance.duration;
            bl = true;
        } else if (this.isShorterDurationThan(mobEffectInstance)) {
            if (mobEffectInstance.amplifier == this.amplifier) {
                this.duration = mobEffectInstance.duration;
                bl = true;
            } else if (this.hiddenEffect == null) {
                this.hiddenEffect = new MobEffectInstance(mobEffectInstance);
            } else {
                this.hiddenEffect.update(mobEffectInstance);
            }
        }
        if (!mobEffectInstance.ambient && this.ambient || bl) {
            this.ambient = mobEffectInstance.ambient;
            bl = true;
        }
        if (mobEffectInstance.visible != this.visible) {
            this.visible = mobEffectInstance.visible;
            bl = true;
        }
        if (mobEffectInstance.showIcon != this.showIcon) {
            this.showIcon = mobEffectInstance.showIcon;
            bl = true;
        }
        return bl;
    }

    private boolean isShorterDurationThan(MobEffectInstance mobEffectInstance) {
        return !this.isInfiniteDuration() && (this.duration < mobEffectInstance.duration || mobEffectInstance.isInfiniteDuration());
    }

    public boolean isInfiniteDuration() {
        return this.duration == -1;
    }

    public boolean endsWithin(int i) {
        return !this.isInfiniteDuration() && this.duration <= i;
    }

    public MobEffectInstance withScaledDuration(float f) {
        MobEffectInstance mobEffectInstance = new MobEffectInstance(this);
        mobEffectInstance.duration = mobEffectInstance.mapDuration(i -> Math.max(Mth.floor((float)i * f), 1));
        return mobEffectInstance;
    }

    public int mapDuration(Int2IntFunction int2IntFunction) {
        if (this.isInfiniteDuration() || this.duration == 0) {
            return this.duration;
        }
        return int2IntFunction.applyAsInt(this.duration);
    }

    public Holder<MobEffect> getEffect() {
        return this.effect;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public boolean isAmbient() {
        return this.ambient;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean showIcon() {
        return this.showIcon;
    }

    public boolean tickServer(ServerLevel serverLevel, LivingEntity livingEntity, Runnable runnable) {
        int i;
        if (!this.hasRemainingDuration()) {
            return false;
        }
        int n = i = this.isInfiniteDuration() ? livingEntity.tickCount : this.duration;
        if (this.effect.value().shouldApplyEffectTickThisTick(i, this.amplifier) && !this.effect.value().applyEffectTick(serverLevel, livingEntity, this.amplifier)) {
            return false;
        }
        this.tickDownDuration();
        if (this.downgradeToHiddenEffect()) {
            runnable.run();
        }
        return this.hasRemainingDuration();
    }

    public void tickClient() {
        if (this.hasRemainingDuration()) {
            this.tickDownDuration();
            this.downgradeToHiddenEffect();
        }
        this.blendState.tick(this);
    }

    private boolean hasRemainingDuration() {
        return this.isInfiniteDuration() || this.duration > 0;
    }

    private void tickDownDuration() {
        if (this.hiddenEffect != null) {
            this.hiddenEffect.tickDownDuration();
        }
        this.duration = this.mapDuration(i -> i - 1);
    }

    private boolean downgradeToHiddenEffect() {
        if (this.duration == 0 && this.hiddenEffect != null) {
            this.setDetailsFrom(this.hiddenEffect);
            this.hiddenEffect = this.hiddenEffect.hiddenEffect;
            return true;
        }
        return false;
    }

    public void onEffectStarted(LivingEntity livingEntity) {
        this.effect.value().onEffectStarted(livingEntity, this.amplifier);
    }

    public void onMobRemoved(ServerLevel serverLevel, LivingEntity livingEntity, Entity.RemovalReason removalReason) {
        this.effect.value().onMobRemoved(serverLevel, livingEntity, this.amplifier, removalReason);
    }

    public void onMobHurt(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource, float f) {
        this.effect.value().onMobHurt(serverLevel, livingEntity, this.amplifier, damageSource, f);
    }

    public String getDescriptionId() {
        return this.effect.value().getDescriptionId();
    }

    public String toString() {
        String string = this.amplifier > 0 ? this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.describeDuration() : this.getDescriptionId() + ", Duration: " + this.describeDuration();
        if (!this.visible) {
            string = string + ", Particles: false";
        }
        if (!this.showIcon) {
            string = string + ", Show Icon: false";
        }
        return string;
    }

    private String describeDuration() {
        if (this.isInfiniteDuration()) {
            return "infinite";
        }
        return Integer.toString(this.duration);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof MobEffectInstance) {
            MobEffectInstance mobEffectInstance = (MobEffectInstance)object;
            return this.duration == mobEffectInstance.duration && this.amplifier == mobEffectInstance.amplifier && this.ambient == mobEffectInstance.ambient && this.visible == mobEffectInstance.visible && this.showIcon == mobEffectInstance.showIcon && this.effect.equals(mobEffectInstance.effect);
        }
        return false;
    }

    public int hashCode() {
        int i = this.effect.hashCode();
        i = 31 * i + this.duration;
        i = 31 * i + this.amplifier;
        i = 31 * i + (this.ambient ? 1 : 0);
        i = 31 * i + (this.visible ? 1 : 0);
        i = 31 * i + (this.showIcon ? 1 : 0);
        return i;
    }

    @Override
    public int compareTo(MobEffectInstance mobEffectInstance) {
        int i = 32147;
        if (this.getDuration() > 32147 && mobEffectInstance.getDuration() > 32147 || this.isAmbient() && mobEffectInstance.isAmbient()) {
            return ComparisonChain.start().compare(Boolean.valueOf(this.isAmbient()), Boolean.valueOf(mobEffectInstance.isAmbient())).compare(this.getEffect().value().getColor(), mobEffectInstance.getEffect().value().getColor()).result();
        }
        return ComparisonChain.start().compareFalseFirst(this.isAmbient(), mobEffectInstance.isAmbient()).compareFalseFirst(this.isInfiniteDuration(), mobEffectInstance.isInfiniteDuration()).compare(this.getDuration(), mobEffectInstance.getDuration()).compare(this.getEffect().value().getColor(), mobEffectInstance.getEffect().value().getColor()).result();
    }

    public void onEffectAdded(LivingEntity livingEntity) {
        this.effect.value().onEffectAdded(livingEntity, this.amplifier);
    }

    public boolean is(Holder<MobEffect> holder) {
        return this.effect.equals(holder);
    }

    public void copyBlendState(MobEffectInstance mobEffectInstance) {
        this.blendState.copyFrom(mobEffectInstance.blendState);
    }

    public void skipBlending() {
        this.blendState.setImmediate(this);
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((MobEffectInstance)object);
    }

    static class BlendState {
        private float factor;
        private float factorPreviousFrame;

        BlendState() {
        }

        public void setImmediate(MobEffectInstance mobEffectInstance) {
            this.factorPreviousFrame = this.factor = BlendState.hasEffect(mobEffectInstance) ? 1.0f : 0.0f;
        }

        public void copyFrom(BlendState blendState) {
            this.factor = blendState.factor;
            this.factorPreviousFrame = blendState.factorPreviousFrame;
        }

        public void tick(MobEffectInstance mobEffectInstance) {
            int i;
            float f;
            this.factorPreviousFrame = this.factor;
            boolean bl = BlendState.hasEffect(mobEffectInstance);
            float f2 = f = bl ? 1.0f : 0.0f;
            if (this.factor == f) {
                return;
            }
            MobEffect mobEffect = mobEffectInstance.getEffect().value();
            int n = i = bl ? mobEffect.getBlendInDurationTicks() : mobEffect.getBlendOutDurationTicks();
            if (i == 0) {
                this.factor = f;
            } else {
                float g = 1.0f / (float)i;
                this.factor += Mth.clamp(f - this.factor, -g, g);
            }
        }

        private static boolean hasEffect(MobEffectInstance mobEffectInstance) {
            return !mobEffectInstance.endsWithin(mobEffectInstance.getEffect().value().getBlendOutAdvanceTicks());
        }

        public float getFactor(LivingEntity livingEntity, float f) {
            if (livingEntity.isRemoved()) {
                this.factorPreviousFrame = this.factor;
            }
            return Mth.lerp(f, this.factorPreviousFrame, this.factor);
        }
    }

    record Details(int amplifier, int duration, boolean ambient, boolean showParticles, boolean showIcon, Optional<Details> hiddenEffect) {
        public static final MapCodec<Details> MAP_CODEC = MapCodec.recursive((String)"MobEffectInstance.Details", codec -> RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("amplifier", (Object)0).forGetter(Details::amplifier), (App)Codec.INT.optionalFieldOf("duration", (Object)0).forGetter(Details::duration), (App)Codec.BOOL.optionalFieldOf("ambient", (Object)false).forGetter(Details::ambient), (App)Codec.BOOL.optionalFieldOf("show_particles", (Object)true).forGetter(Details::showParticles), (App)Codec.BOOL.optionalFieldOf("show_icon").forGetter(details -> Optional.of(details.showIcon())), (App)codec.optionalFieldOf("hidden_effect").forGetter(Details::hiddenEffect)).apply((Applicative)instance, Details::create)));
        public static final StreamCodec<ByteBuf, Details> STREAM_CODEC = StreamCodec.recursive(streamCodec -> StreamCodec.composite(ByteBufCodecs.VAR_INT, Details::amplifier, ByteBufCodecs.VAR_INT, Details::duration, ByteBufCodecs.BOOL, Details::ambient, ByteBufCodecs.BOOL, Details::showParticles, ByteBufCodecs.BOOL, Details::showIcon, streamCodec.apply(ByteBufCodecs::optional), Details::hiddenEffect, Details::new));

        private static Details create(int i, int j, boolean bl, boolean bl2, Optional<Boolean> optional, Optional<Details> optional2) {
            return new Details(i, j, bl, bl2, optional.orElse(bl2), optional2);
        }
    }
}


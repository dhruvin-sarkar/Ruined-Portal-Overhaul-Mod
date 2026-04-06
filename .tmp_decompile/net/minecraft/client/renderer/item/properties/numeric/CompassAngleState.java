/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.NeedleDirectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CompassAngleState
extends NeedleDirectionHelper {
    public static final MapCodec<CompassAngleState> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("wobble", (Object)true).forGetter(NeedleDirectionHelper::wobble), (App)CompassTarget.CODEC.fieldOf("target").forGetter(CompassAngleState::target)).apply((Applicative)instance, CompassAngleState::new));
    private final NeedleDirectionHelper.Wobbler wobbler;
    private final NeedleDirectionHelper.Wobbler noTargetWobbler;
    private final CompassTarget compassTarget;
    private final RandomSource random = RandomSource.create();

    public CompassAngleState(boolean bl, CompassTarget compassTarget) {
        super(bl);
        this.wobbler = this.newWobbler(0.8f);
        this.noTargetWobbler = this.newWobbler(0.8f);
        this.compassTarget = compassTarget;
    }

    @Override
    protected float calculate(ItemStack itemStack, ClientLevel clientLevel, int i, ItemOwner itemOwner) {
        GlobalPos globalPos = this.compassTarget.get(clientLevel, itemStack, itemOwner);
        long l = clientLevel.getGameTime();
        if (!CompassAngleState.isValidCompassTargetPos(itemOwner, globalPos)) {
            return this.getRandomlySpinningRotation(i, l);
        }
        return this.getRotationTowardsCompassTarget(itemOwner, l, globalPos.pos());
    }

    private float getRandomlySpinningRotation(int i, long l) {
        if (this.noTargetWobbler.shouldUpdate(l)) {
            this.noTargetWobbler.update(l, this.random.nextFloat());
        }
        float f = this.noTargetWobbler.rotation() + (float)CompassAngleState.hash(i) / 2.14748365E9f;
        return Mth.positiveModulo(f, 1.0f);
    }

    private float getRotationTowardsCompassTarget(ItemOwner itemOwner, long l, BlockPos blockPos) {
        float h;
        Player player;
        float f = (float)CompassAngleState.getAngleFromEntityToPos(itemOwner, blockPos);
        float g = CompassAngleState.getWrappedVisualRotationY(itemOwner);
        LivingEntity livingEntity = itemOwner.asLivingEntity();
        if (livingEntity instanceof Player && (player = (Player)livingEntity).isLocalPlayer() && player.level().tickRateManager().runsNormally()) {
            if (this.wobbler.shouldUpdate(l)) {
                this.wobbler.update(l, 0.5f - (g - 0.25f));
            }
            h = f + this.wobbler.rotation();
        } else {
            h = 0.5f - (g - 0.25f - f);
        }
        return Mth.positiveModulo(h, 1.0f);
    }

    private static boolean isValidCompassTargetPos(ItemOwner itemOwner, @Nullable GlobalPos globalPos) {
        return globalPos != null && globalPos.dimension() == itemOwner.level().dimension() && !(globalPos.pos().distToCenterSqr(itemOwner.position()) < (double)1.0E-5f);
    }

    private static double getAngleFromEntityToPos(ItemOwner itemOwner, BlockPos blockPos) {
        Vec3 vec3 = Vec3.atCenterOf(blockPos);
        Vec3 vec32 = itemOwner.position();
        return Math.atan2(vec3.z() - vec32.z(), vec3.x() - vec32.x()) / 6.2831854820251465;
    }

    private static float getWrappedVisualRotationY(ItemOwner itemOwner) {
        return Mth.positiveModulo(itemOwner.getVisualRotationYInDegrees() / 360.0f, 1.0f);
    }

    private static int hash(int i) {
        return i * 1327217883;
    }

    protected CompassTarget target() {
        return this.compassTarget;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum CompassTarget implements StringRepresentable
    {
        NONE("none"){

            @Override
            public @Nullable GlobalPos get(ClientLevel clientLevel, ItemStack itemStack, @Nullable ItemOwner itemOwner) {
                return null;
            }
        }
        ,
        LODESTONE("lodestone"){

            @Override
            public @Nullable GlobalPos get(ClientLevel clientLevel, ItemStack itemStack, @Nullable ItemOwner itemOwner) {
                LodestoneTracker lodestoneTracker = itemStack.get(DataComponents.LODESTONE_TRACKER);
                return lodestoneTracker != null ? (GlobalPos)lodestoneTracker.target().orElse(null) : null;
            }
        }
        ,
        SPAWN("spawn"){

            @Override
            public GlobalPos get(ClientLevel clientLevel, ItemStack itemStack, @Nullable ItemOwner itemOwner) {
                return clientLevel.getRespawnData().globalPos();
            }
        }
        ,
        RECOVERY("recovery"){

            @Override
            public @Nullable GlobalPos get(ClientLevel clientLevel, ItemStack itemStack, @Nullable ItemOwner itemOwner) {
                GlobalPos globalPos;
                LivingEntity livingEntity;
                LivingEntity livingEntity2 = livingEntity = itemOwner == null ? null : itemOwner.asLivingEntity();
                if (livingEntity instanceof Player) {
                    Player player = (Player)livingEntity;
                    globalPos = player.getLastDeathLocation().orElse(null);
                } else {
                    globalPos = null;
                }
                return globalPos;
            }
        };

        public static final Codec<CompassTarget> CODEC;
        private final String name;

        CompassTarget(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        abstract @Nullable GlobalPos get(ClientLevel var1, ItemStack var2, @Nullable ItemOwner var3);

        static {
            CODEC = StringRepresentable.fromEnum(CompassTarget::values);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item.properties.numeric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class NeedleDirectionHelper {
    private final boolean wobble;

    protected NeedleDirectionHelper(boolean bl) {
        this.wobble = bl;
    }

    public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
        Level level;
        if (itemOwner == null) {
            itemOwner = itemStack.getEntityRepresentation();
        }
        if (itemOwner == null) {
            return 0.0f;
        }
        if (clientLevel == null && (level = itemOwner.level()) instanceof ClientLevel) {
            ClientLevel clientLevel2;
            clientLevel = clientLevel2 = (ClientLevel)level;
        }
        if (clientLevel == null) {
            return 0.0f;
        }
        return this.calculate(itemStack, clientLevel, i, itemOwner);
    }

    protected abstract float calculate(ItemStack var1, ClientLevel var2, int var3, ItemOwner var4);

    protected boolean wobble() {
        return this.wobble;
    }

    protected Wobbler newWobbler(float f) {
        return this.wobble ? NeedleDirectionHelper.standardWobbler(f) : NeedleDirectionHelper.nonWobbler();
    }

    public static Wobbler standardWobbler(final float f) {
        return new Wobbler(){
            private float rotation;
            private float deltaRotation;
            private long lastUpdateTick;

            @Override
            public float rotation() {
                return this.rotation;
            }

            @Override
            public boolean shouldUpdate(long l) {
                return this.lastUpdateTick != l;
            }

            @Override
            public void update(long l, float f2) {
                this.lastUpdateTick = l;
                float g = Mth.positiveModulo(f2 - this.rotation + 0.5f, 1.0f) - 0.5f;
                this.deltaRotation += g * 0.1f;
                this.deltaRotation *= f;
                this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0f);
            }
        };
    }

    public static Wobbler nonWobbler() {
        return new Wobbler(){
            private float targetValue;

            @Override
            public float rotation() {
                return this.targetValue;
            }

            @Override
            public boolean shouldUpdate(long l) {
                return true;
            }

            @Override
            public void update(long l, float f) {
                this.targetValue = f;
            }
        };
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Wobbler {
        public float rotation();

        public boolean shouldUpdate(long var1);

        public void update(long var1, float var3);
    }
}


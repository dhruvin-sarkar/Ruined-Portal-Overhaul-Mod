/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.food;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FoodData {
    private static final int DEFAULT_TICK_TIMER = 0;
    private static final float DEFAULT_EXHAUSTION_LEVEL = 0.0f;
    private int foodLevel = 20;
    private float saturationLevel = 5.0f;
    private float exhaustionLevel;
    private int tickTimer;

    private void add(int i, float f) {
        this.foodLevel = Mth.clamp(i + this.foodLevel, 0, 20);
        this.saturationLevel = Mth.clamp(f + this.saturationLevel, 0.0f, (float)this.foodLevel);
    }

    public void eat(int i, float f) {
        this.add(i, FoodConstants.saturationByModifier(i, f));
    }

    public void eat(FoodProperties foodProperties) {
        this.add(foodProperties.nutrition(), foodProperties.saturation());
    }

    public void tick(ServerPlayer serverPlayer) {
        boolean bl;
        ServerLevel serverLevel = serverPlayer.level();
        Difficulty difficulty = serverLevel.getDifficulty();
        if (this.exhaustionLevel > 4.0f) {
            this.exhaustionLevel -= 4.0f;
            if (this.saturationLevel > 0.0f) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0f, 0.0f);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }
        if ((bl = serverLevel.getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION).booleanValue()) && this.saturationLevel > 0.0f && serverPlayer.isHurt() && this.foodLevel >= 20) {
            ++this.tickTimer;
            if (this.tickTimer >= 10) {
                float f = Math.min(this.saturationLevel, 6.0f);
                serverPlayer.heal(f / 6.0f);
                this.addExhaustion(f);
                this.tickTimer = 0;
            }
        } else if (bl && this.foodLevel >= 18 && serverPlayer.isHurt()) {
            ++this.tickTimer;
            if (this.tickTimer >= 80) {
                serverPlayer.heal(1.0f);
                this.addExhaustion(6.0f);
                this.tickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.tickTimer;
            if (this.tickTimer >= 80) {
                if (serverPlayer.getHealth() > 10.0f || difficulty == Difficulty.HARD || serverPlayer.getHealth() > 1.0f && difficulty == Difficulty.NORMAL) {
                    serverPlayer.hurtServer(serverLevel, serverPlayer.damageSources().starve(), 1.0f);
                }
                this.tickTimer = 0;
            }
        } else {
            this.tickTimer = 0;
        }
    }

    public void readAdditionalSaveData(ValueInput valueInput) {
        this.foodLevel = valueInput.getIntOr("foodLevel", 20);
        this.tickTimer = valueInput.getIntOr("foodTickTimer", 0);
        this.saturationLevel = valueInput.getFloatOr("foodSaturationLevel", 5.0f);
        this.exhaustionLevel = valueInput.getFloatOr("foodExhaustionLevel", 0.0f);
    }

    public void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putInt("foodLevel", this.foodLevel);
        valueOutput.putInt("foodTickTimer", this.tickTimer);
        valueOutput.putFloat("foodSaturationLevel", this.saturationLevel);
        valueOutput.putFloat("foodExhaustionLevel", this.exhaustionLevel);
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public boolean hasEnoughFood() {
        return (float)this.getFoodLevel() > 6.0f;
    }

    public boolean needsFood() {
        return this.foodLevel < 20;
    }

    public void addExhaustion(float f) {
        this.exhaustionLevel = Math.min(this.exhaustionLevel + f, 40.0f);
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public void setFoodLevel(int i) {
        this.foodLevel = i;
    }

    public void setSaturation(float f) {
        this.saturationLevel = f;
    }
}


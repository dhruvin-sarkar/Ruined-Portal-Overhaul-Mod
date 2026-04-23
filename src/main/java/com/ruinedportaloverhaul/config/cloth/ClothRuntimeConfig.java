package com.ruinedportaloverhaul.config.cloth;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.config.ModConfigAccess;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = RuinedPortalOverhaul.MOD_ID)
public final class ClothRuntimeConfig implements ConfigData, ModConfigAccess {
    @ConfigEntry.BoundedDiscrete(min = 16, max = 64)
    public int structureRarity = 32;

    public boolean enableAmbientNetherSpawns = true;
    public boolean enableOuterZoneScatter = true;
    public double raidTriggerRadius = 24.0;
    public double waveCountMultiplier = 1.0;

    @ConfigEntry.BoundedDiscrete(min = 100, max = 600)
    public int interWaveDelayTicks = 300;

    public boolean enableBossBar = true;
    public boolean enableRedStorm = true;
    public double stormIntensity = 0.6;

    @ConfigEntry.BoundedDiscrete(min = 40, max = 200)
    public int thunderFrequency = 80;

    public double mobHealthMultiplier = 1.0;
    public double mobDamageMultiplier = 1.0;

    @ConfigEntry.BoundedDiscrete(min = 50, max = 400)
    public int ambientMobCap = 180;

    public boolean enablePostRaidSuppression = true;
    public double netherStarDropRate = 1.0;
    public boolean enableNetherDragon = true;

    @Override
    public int structureRarity() {
        return clamp(this.structureRarity, 16, 64);
    }

    @Override
    public boolean enableAmbientNetherSpawns() {
        return this.enableAmbientNetherSpawns;
    }

    @Override
    public boolean enableOuterZoneScatter() {
        return this.enableOuterZoneScatter;
    }

    @Override
    public double raidTriggerRadius() {
        return clamp(this.raidTriggerRadius, 12.0, 48.0);
    }

    @Override
    public double waveCountMultiplier() {
        return clamp(this.waveCountMultiplier, 0.5, 2.0);
    }

    @Override
    public int interWaveDelayTicks() {
        return clamp(this.interWaveDelayTicks, 100, 600);
    }

    @Override
    public boolean enableBossBar() {
        return this.enableBossBar;
    }

    @Override
    public boolean enableRedStorm() {
        return this.enableRedStorm;
    }

    @Override
    public double stormIntensity() {
        return clamp(this.stormIntensity, 0.2, 1.0);
    }

    @Override
    public int thunderFrequency() {
        return clamp(this.thunderFrequency, 40, 200);
    }

    @Override
    public double mobHealthMultiplier() {
        return clamp(this.mobHealthMultiplier, 0.5, 3.0);
    }

    @Override
    public double mobDamageMultiplier() {
        return clamp(this.mobDamageMultiplier, 0.5, 3.0);
    }

    @Override
    public int ambientMobCap() {
        return clamp(this.ambientMobCap, 50, 400);
    }

    @Override
    public boolean enablePostRaidSuppression() {
        return this.enablePostRaidSuppression;
    }

    @Override
    public double netherStarDropRate() {
        return clamp(this.netherStarDropRate, 0.0, 5.0);
    }

    @Override
    public boolean enableNetherDragon() {
        return this.enableNetherDragon;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

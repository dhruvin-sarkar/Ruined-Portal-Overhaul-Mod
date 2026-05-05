package com.ruinedportaloverhaul.config.cloth;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.config.ModConfigAccess;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = RuinedPortalOverhaul.MOD_ID)
public final class ClothRuntimeConfig implements ConfigData, ModConfigAccess {
    // Fix: the config screen originally exposed every value in one flat list, so options are now grouped by gameplay system for pack-author tuning.
    @ConfigEntry.Category("world_generation")
    @ConfigEntry.BoundedDiscrete(min = 16, max = 64)
    @ConfigEntry.Gui.Tooltip
    public int structureRarity = 32;

    @ConfigEntry.Category("world_generation")
    @ConfigEntry.Gui.Tooltip
    public boolean enableAmbientNetherSpawns = true;
    @ConfigEntry.Category("world_generation")
    @ConfigEntry.Gui.Tooltip
    public boolean enableOuterZoneScatter = true;

    @ConfigEntry.Category("raid")
    @ConfigEntry.Gui.Tooltip
    public double raidTriggerRadius = 24.0;
    @ConfigEntry.Category("raid")
    @ConfigEntry.Gui.Tooltip
    public double waveCountMultiplier = 1.0;

    @ConfigEntry.Category("raid")
    @ConfigEntry.BoundedDiscrete(min = 100, max = 600)
    @ConfigEntry.Gui.Tooltip
    public int interWaveDelayTicks = 300;

    @ConfigEntry.Category("raid")
    @ConfigEntry.Gui.Tooltip
    public boolean enableBossBar = true;

    @ConfigEntry.Category("atmosphere")
    @ConfigEntry.Gui.Tooltip
    public boolean enableRedStorm = true;
    @ConfigEntry.Category("atmosphere")
    @ConfigEntry.Gui.Tooltip
    public double stormIntensity = 0.68;

    @ConfigEntry.Category("atmosphere")
    @ConfigEntry.BoundedDiscrete(min = 40, max = 200)
    @ConfigEntry.Gui.Tooltip
    public int thunderFrequency = 80;

    @ConfigEntry.Category("difficulty")
    @ConfigEntry.Gui.Tooltip
    public double mobHealthMultiplier = 1.0;
    @ConfigEntry.Category("difficulty")
    @ConfigEntry.Gui.Tooltip
    public double mobDamageMultiplier = 1.0;

    @ConfigEntry.Category("difficulty")
    @ConfigEntry.BoundedDiscrete(min = 50, max = 400)
    @ConfigEntry.Gui.Tooltip
    public int ambientMobCap = 110;

    @ConfigEntry.Category("difficulty")
    @ConfigEntry.Gui.Tooltip
    public boolean enablePostRaidSuppression = true;

    @ConfigEntry.Category("rewards")
    @ConfigEntry.Gui.Tooltip
    public double netherStarDropRate = 1.0;
    @ConfigEntry.Category("rewards")
    @ConfigEntry.Gui.Tooltip
    public boolean enableNetherDragon = true;

    @Override
    public void validatePostLoad() {
        // Fix: hand-edited config files could keep invalid values inside AutoConfig even though gameplay getters clamped them, so loaded values are normalized once before systems read them.
        this.structureRarity = clamp(this.structureRarity, 16, 64);
        this.raidTriggerRadius = clamp(this.raidTriggerRadius, 12.0, 48.0, 24.0);
        this.waveCountMultiplier = clamp(this.waveCountMultiplier, 0.5, 2.0, 1.0);
        this.interWaveDelayTicks = clamp(this.interWaveDelayTicks, 100, 600);
        this.stormIntensity = clamp(this.stormIntensity, 0.2, 1.0, 0.68);
        this.thunderFrequency = clamp(this.thunderFrequency, 40, 200);
        this.mobHealthMultiplier = clamp(this.mobHealthMultiplier, 0.5, 3.0, 1.0);
        this.mobDamageMultiplier = clamp(this.mobDamageMultiplier, 0.5, 3.0, 1.0);
        this.ambientMobCap = clamp(this.ambientMobCap, 50, 400);
        this.netherStarDropRate = clamp(this.netherStarDropRate, 0.0, 5.0, 1.0);
    }

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
        return clamp(this.raidTriggerRadius, 12.0, 48.0, 24.0);
    }

    @Override
    public double waveCountMultiplier() {
        return clamp(this.waveCountMultiplier, 0.5, 2.0, 1.0);
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
        return clamp(this.stormIntensity, 0.2, 1.0, 0.68);
    }

    @Override
    public int thunderFrequency() {
        return clamp(this.thunderFrequency, 40, 200);
    }

    @Override
    public double mobHealthMultiplier() {
        return clamp(this.mobHealthMultiplier, 0.5, 3.0, 1.0);
    }

    @Override
    public double mobDamageMultiplier() {
        return clamp(this.mobDamageMultiplier, 0.5, 3.0, 1.0);
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
        return clamp(this.netherStarDropRate, 0.0, 5.0, 1.0);
    }

    @Override
    public boolean enableNetherDragon() {
        return this.enableNetherDragon;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max, double fallback) {
        if (!Double.isFinite(value)) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }
}

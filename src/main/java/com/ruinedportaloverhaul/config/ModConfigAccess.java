package com.ruinedportaloverhaul.config;

public interface ModConfigAccess {
    boolean enableAmbientNetherSpawns();

    boolean enableOuterZoneScatter();

    double raidTriggerRadius();

    double waveCountMultiplier();

    int interWaveDelayTicks();

    boolean enableBossBar();

    boolean enableRedStorm();

    double stormIntensity();

    int thunderFrequency();

    double mobHealthMultiplier();

    double mobDamageMultiplier();

    int ambientMobCap();

    boolean enablePostRaidSuppression();

    double netherStarDropRate();

    boolean enableNetherDragon();
}

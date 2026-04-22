package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.raid.NetherDragonRituals;
import com.ruinedportaloverhaul.sound.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class NetherDragonEntity extends EnderDragon {
    private static final String PORTAL_ORIGIN_KEY = "RpoPortalOrigin";
    private static final String DEATH_REWARDS_HANDLED_KEY = "RpoDeathRewardsHandled";
    private static final float MAX_HEALTH = 300.0f;

    private BlockPos portalOrigin = BlockPos.ZERO;
    private boolean deathRewardsHandled;

    public NetherDragonEntity(EntityType<? extends EnderDragon> entityType, Level level) {
        super(entityType, level);
        this.configureNetherDragonStats();
    }

    public NetherDragonEntity(Level level, BlockPos portalOrigin) {
        this(ModEntities.NETHER_DRAGON, level);
        this.setPortalOrigin(portalOrigin);
    }

    public BlockPos portalOrigin() {
        return this.portalOrigin;
    }

    public void setPortalOrigin(BlockPos portalOrigin) {
        this.portalOrigin = portalOrigin.immutable();
        this.setFightOrigin(this.portalOrigin);
    }

    @Override
    public void setDragonFight(EndDragonFight dragonFight) {
        // Nether dragons are independent bosses and must never attach to the End fight state.
    }

    @Override
    public EndDragonFight getDragonFight() {
        return null;
    }

    @Override
    public void heal(float amount) {
        // End crystals and regeneration effects cannot heal the Nether Dragon.
    }

    @Override
    public void setHealth(float health) {
        if (!this.level().isClientSide() && this.tickCount > 0 && health > this.getHealth() && this.getHealth() > 0.0f) {
            return;
        }
        super.setHealth(health);
    }

    @Override
    public void onCrystalDestroyed(ServerLevel level, EndCrystal crystal, BlockPos pos, DamageSource damageSource) {
        if (crystal == this.nearestCrystal) {
            this.nearestCrystal = null;
        }
    }

    @Override
    protected void tickDeath() {
        if (!this.deathRewardsHandled && this.level() instanceof ServerLevel serverLevel) {
            this.deathRewardsHandled = true;
            NetherDragonRituals.onNetherDragonDeath(serverLevel, this);
        }
        super.tickDeath();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_NETHER_DRAGON_AMBIENT;
    }

    @Override
    public boolean hurt(ServerLevel level, EnderDragonPart part, DamageSource damageSource, float damage) {
        return super.hurt(level, part, damageSource, damage);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putLong(PORTAL_ORIGIN_KEY, this.portalOrigin.asLong());
        output.putBoolean(DEATH_REWARDS_HANDLED_KEY, this.deathRewardsHandled);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPortalOrigin(BlockPos.of(input.getLongOr(PORTAL_ORIGIN_KEY, BlockPos.ZERO.asLong())));
        this.deathRewardsHandled = input.getBooleanOr(DEATH_REWARDS_HANDLED_KEY, false);
        this.configureNetherDragonStats();
    }

    private void configureNetherDragonStats() {
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(MAX_HEALTH);
        }
        super.setHealth(MAX_HEALTH);
        this.setCustomName(Component.literal("The Nether Dragon").withStyle(ChatFormatting.DARK_RED));
        this.setCustomNameVisible(true);
    }
}

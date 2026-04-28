package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.network.DragonPhaseFlashPayload;
import com.ruinedportaloverhaul.raid.NetherDragonRituals;
import com.ruinedportaloverhaul.sound.ModSounds;
import com.ruinedportaloverhaul.world.ModParticles;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NetherDragonEntity extends EnderDragon implements GeoEntity {
    public static final String ACTION_CONTROLLER = "DragonActions";
    public static final String PHASE_TWO_TRANSITION = "phase2_transition";
    public static final String NETHER_SLAM = "nether_slam";

    private static final String FLIGHT_CONTROLLER = "Flight";
    private static final RawAnimation FLIGHT = RawAnimation.begin().thenLoop("misc.fly");
    private static final RawAnimation PHASE_TWO_ANIMATION = RawAnimation.begin().thenPlay("action.phase2_transition");
    private static final RawAnimation SLAM_ANIMATION = RawAnimation.begin().thenPlay("attack.nether_slam");
    private static final String PORTAL_ORIGIN_KEY = "RpoPortalOrigin";
    private static final String DEATH_REWARDS_HANDLED_KEY = "RpoDeathRewardsHandled";
    private static final String ENRAGED_KEY = "RpoEnraged";
    private static final String GUARDIANS_SUMMONED_KEY = "RpoGuardiansSummoned";
    private static final String STRAFE_COOLDOWN_KEY = "RpoStrafeCooldown";
    private static final String SLAM_COOLDOWN_KEY = "RpoSlamCooldown";
    private static final String SLAM_ACTIVE_KEY = "RpoSlamActive";
    private static final String SLAM_TICKS_KEY = "RpoSlamTicks";
    private static final String SLAM_TARGET_KEY = "RpoSlamTarget";
    private static final Identifier ENRAGED_SPEED_BONUS_ID = ModEntities.id("nether_dragon_enraged_speed");
    private static final Identifier ENRAGED_FLYING_SPEED_BONUS_ID = ModEntities.id("nether_dragon_enraged_flying_speed");
    private static final float MAX_HEALTH = 300.0f;
    private static final float PHASE_TWO_HEALTH_THRESHOLD = 150.0f;
    private static final float GUARDIAN_SUMMON_HEALTH_THRESHOLD = 120.0f;
    private static final int PHASE_TWO_FLASH_TICKS = 3;
    private static final int STRAFE_INTERVAL_TICKS = 60;
    private static final int NETHER_SLAM_INTERVAL_TICKS = 120;
    private static final int NETHER_SLAM_ASCENT_TICKS = 20;
    private static final int NETHER_SLAM_DIVE_TICKS = 18;
    private static final int NETHER_SLAM_TOTAL_TICKS = NETHER_SLAM_ASCENT_TICKS + NETHER_SLAM_DIVE_TICKS;
    private static final int DEATH_REWARD_DELAY_TICKS = 60;
    private static final int DEATH_XP_REWARD = 1500;
    private static final double PLAYER_EVENT_RANGE = 96.0;
    private static final double NETHER_SLAM_EXPLOSION_POWER = 8.0;
    private static final double NETHER_SLAM_DAMAGE_RADIUS = 6.0;
    private static final SimpleExplosionDamageCalculator NETHER_SLAM_VISUAL_EXPLOSION =
        new SimpleExplosionDamageCalculator(false, false, Optional.of(0.0f), Optional.empty());
    private static final Component BOSS_BAR_TITLE = Component.translatable("bossbar.ruined_portal_overhaul.nether_dragon")
        .withStyle(ChatFormatting.DARK_RED);
    private static final Component ENRAGED_BOSS_BAR_TITLE = Component.translatable("bossbar.ruined_portal_overhaul.nether_dragon_enraged")
        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

    private BlockPos portalOrigin = BlockPos.ZERO;
    private boolean deathRewardsHandled;
    private boolean enraged;
    private boolean guardiansSummoned;
    private int strafeCooldown = STRAFE_INTERVAL_TICKS;
    private int slamCooldown = NETHER_SLAM_INTERVAL_TICKS;
    private boolean netherSlamActive;
    private int netherSlamTicksRemaining;
    private BlockPos netherSlamTarget = BlockPos.ZERO;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public NetherDragonEntity(EntityType<? extends EnderDragon> entityType, Level level) {
        super(entityType, level);
        this.configureNetherDragonStats();
        this.setHealth(MAX_HEALTH);
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

    public static AttributeSupplier.Builder createAttributes() {
        // Fix: the boss used the base dragon constructor but never published its own defaults, so the custom type now declares the health and speed attributes phase two depends on.
        return EnderDragon.createAttributes()
            .add(Attributes.MAX_HEALTH, MAX_HEALTH)
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.FLYING_SPEED, 0.6);
    }

    public boolean isEnragedPhase() {
        return this.enraged;
    }

    public Component bossBarTitle() {
        return this.enraged ? ENRAGED_BOSS_BAR_TITLE : BOSS_BAR_TITLE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Fix: the Nether Dragon was still using the vanilla EnderDragonRenderer, so phase-two and slam presentation could not be authored as replaceable GeckoLib animations. These controllers keep the flight loop continuous while exposing triggerable cinematic attacks.
        controllers.add(
            new AnimationController<NetherDragonEntity>(FLIGHT_CONTROLLER, 5, state -> state.setAndContinue(FLIGHT)),
            new AnimationController<NetherDragonEntity>(ACTION_CONTROLLER, 0, state -> PlayState.CONTINUE)
                .triggerableAnim(PHASE_TWO_TRANSITION, PHASE_TWO_ANIMATION)
                .triggerableAnim(NETHER_SLAM, SLAM_ANIMATION)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
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
    public void aiStep() {
        // Fix: the custom dragon only inherited vanilla phase timing, so the server now drives the enraged transition, guardian summon, projectile pressure, and slam attack off health thresholds.
        super.aiStep();
        if (!(this.level() instanceof ServerLevel serverLevel) || this.isDeadOrDying()) {
            return;
        }
        if (!this.enraged && this.getHealth() <= PHASE_TWO_HEALTH_THRESHOLD) {
            this.activatePhaseTwo(serverLevel);
        }
        if (!this.guardiansSummoned && this.getHealth() <= GUARDIAN_SUMMON_HEALTH_THRESHOLD) {
            this.summonPhaseTwoGuardians(serverLevel);
        }
        this.tickPhaseTwoAttacks(serverLevel);
    }

    @Override
    protected void tickDeath() {
        // Fix: ritual rewards used to drop on the first death tick, so the dragon now holds a short cinematic before loot, pedestal shattering, and XP are released together.
        ++this.dragonDeathTime;
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.dragonDeathTime == 1 && !this.isSilent()) {
            serverLevel.globalLevelEvent(1028, this.blockPosition(), 0);
        }

        serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 2.0, this.getZ(), 8, 0.9, 1.8, 0.9, 0.02);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 2.5, this.getZ(), 4, 0.8, 1.2, 0.8, 0.01);
        if (this.dragonDeathTime % 10 == 0) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 4.0, this.getZ(), 12, 0.6, 2.4, 0.6, 0.01);
        }

        Vec3 rise = new Vec3(0.0, 0.08, 0.0);
        this.move(MoverType.SELF, rise);
        for (EnderDragonPart part : this.getSubEntities()) {
            part.setOldPosAndRot();
            part.setPos(part.position().add(rise));
        }

        if (!this.deathRewardsHandled && this.dragonDeathTime >= DEATH_REWARD_DELAY_TICKS) {
            this.deathRewardsHandled = true;
            NetherDragonRituals.onNetherDragonDeath(serverLevel, this);
            if (serverLevel.getGameRules().get(GameRules.MOB_DROPS)) {
                ExperienceOrb.award(serverLevel, this.position(), DEATH_XP_REWARD);
            }
            this.remove(RemovalReason.KILLED);
            this.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ENTITY_DIE);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_NETHER_DRAGON_AMBIENT;
    }

    @Override
    public boolean hurt(ServerLevel level, EnderDragonPart part, DamageSource damageSource, float damage) {
        float previousHealth = this.getHealth();
        boolean damaged = super.hurt(level, part, damageSource, damage);
        if (damaged && damage > 0.0f && (this.enraged || previousHealth <= PHASE_TWO_HEALTH_THRESHOLD || this.getHealth() <= PHASE_TWO_HEALTH_THRESHOLD)) {
            spawnDragonBlood(level, part);
        }
        return damaged;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putLong(PORTAL_ORIGIN_KEY, this.portalOrigin.asLong());
        output.putBoolean(DEATH_REWARDS_HANDLED_KEY, this.deathRewardsHandled);
        output.putBoolean(ENRAGED_KEY, this.enraged);
        output.putBoolean(GUARDIANS_SUMMONED_KEY, this.guardiansSummoned);
        output.putInt(STRAFE_COOLDOWN_KEY, this.strafeCooldown);
        output.putInt(SLAM_COOLDOWN_KEY, this.slamCooldown);
        output.putBoolean(SLAM_ACTIVE_KEY, this.netherSlamActive);
        output.putInt(SLAM_TICKS_KEY, this.netherSlamTicksRemaining);
        output.putLong(SLAM_TARGET_KEY, this.netherSlamTarget.asLong());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPortalOrigin(BlockPos.of(input.getLongOr(PORTAL_ORIGIN_KEY, BlockPos.ZERO.asLong())));
        this.deathRewardsHandled = input.getBooleanOr(DEATH_REWARDS_HANDLED_KEY, false);
        this.enraged = input.getBooleanOr(ENRAGED_KEY, false);
        this.guardiansSummoned = input.getBooleanOr(GUARDIANS_SUMMONED_KEY, false);
        this.strafeCooldown = input.getIntOr(STRAFE_COOLDOWN_KEY, STRAFE_INTERVAL_TICKS);
        this.slamCooldown = input.getIntOr(SLAM_COOLDOWN_KEY, NETHER_SLAM_INTERVAL_TICKS);
        this.netherSlamActive = input.getBooleanOr(SLAM_ACTIVE_KEY, false);
        this.netherSlamTicksRemaining = input.getIntOr(SLAM_TICKS_KEY, 0);
        this.netherSlamTarget = BlockPos.of(input.getLongOr(SLAM_TARGET_KEY, BlockPos.ZERO.asLong()));
        this.configureNetherDragonStats();
    }

    private void configureNetherDragonStats() {
        // Fix: save/load used to heal the dragon back to full while restoring presentation, so this setup now reapplies the title and phase-two speed state without overwriting a saved fight in progress.
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(MAX_HEALTH);
        }
        this.applyEnragedSpeedBonus();
        this.setCustomName(this.bossBarTitle());
        this.setCustomNameVisible(true);
    }

    private void activatePhaseTwo(ServerLevel level) {
        // Fix: phase-two audience checks used full 3D distance, so players still fighting inside the portal footprint from the pit or cave stack could miss the transition flash. The enraged cue now follows horizontal portal distance like the ritual titles and boss bar.
        this.enraged = true;
        this.strafeCooldown = Math.min(this.strafeCooldown, STRAFE_INTERVAL_TICKS / 2);
        this.slamCooldown = Math.min(this.slamCooldown, 30);
        this.setCustomName(this.bossBarTitle());
        this.setCustomNameVisible(true);
        this.applyEnragedSpeedBonus();
        this.triggerAnim(ACTION_CONTROLLER, PHASE_TWO_TRANSITION);
        level.playSound(null, this.blockPosition(), ModSounds.ENTITY_NETHER_DRAGON_PHASE2, SoundSource.HOSTILE, 2.4f, 0.95f);
        level.playSound(null, this.blockPosition(), ModSounds.ENTITY_NETHER_DRAGON_GROWL, SoundSource.HOSTILE, 3.0f, 0.85f);
        level.sendParticles(ModParticles.NETHER_EMBER, this.getX(), this.getY() + 2.5, this.getZ(), 200, 7.5, 7.5, 7.5, 0.05);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 2.5, this.getZ(), 100, 7.5, 7.5, 7.5, 0.02);

        double maxDistanceSqr = PLAYER_EVENT_RANGE * PLAYER_EVENT_RANGE;
        for (ServerPlayer player : level.players()) {
            if (horizontalDistanceSqr(player.blockPosition(), this.portalOrigin) > maxDistanceSqr) {
                continue;
            }
            if (ServerPlayNetworking.canSend(player, DragonPhaseFlashPayload.TYPE)) {
                ServerPlayNetworking.send(player, new DragonPhaseFlashPayload(PHASE_TWO_FLASH_TICKS));
            }
        }
    }

    private void applyEnragedSpeedBonus() {
        // Fix: the enraged phase promised faster flight, but only ground movement was boosted. Both movement and flying speed now receive stable permanent modifiers that survive save/load boundaries.
        if (!this.enraged) {
            return;
        }
        AttributeInstance movementSpeed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.addOrReplacePermanentModifier(new AttributeModifier(
                ENRAGED_SPEED_BONUS_ID,
                0.25,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
        AttributeInstance flyingSpeed = this.getAttribute(Attributes.FLYING_SPEED);
        if (flyingSpeed != null) {
            flyingSpeed.addOrReplacePermanentModifier(new AttributeModifier(
                ENRAGED_FLYING_SPEED_BONUS_ID,
                0.25,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    private void summonPhaseTwoGuardians(ServerLevel level) {
        // Fix: the low-health guardian summon used to spawn evokers at the dragon's flight altitude, where they fell out of the sky and died on impact before reaching the player. Guardians now anchor to the portal origin at ground level so they actually join the fight.
        this.guardiansSummoned = true;
        BlockPos anchor = this.portalOrigin;
        for (int i = 0; i < 3; i++) {
            double angle = (Math.PI * 2.0 / 3.0) * i;
            BlockPos spawnPos = BlockPos.containing(
                anchor.getX() + Math.cos(angle) * 8.0,
                anchor.getY(),
                anchor.getZ() + Math.sin(angle) * 8.0
            );
            PiglinEvokerEntity guardian = ModEntities.PIGLIN_EVOKER.spawn(level, spawnPos, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
            if (guardian != null) {
                Player target = level.getNearestPlayer(guardian, 48.0);
                if (target != null) {
                    guardian.setTarget(target);
                }
            }
        }
    }

    private void tickPhaseTwoAttacks(ServerLevel level) {
        // Fix: the enraged phase needed its own pressure loop, so projectile cadence and the scripted slam now run on explicit cooldowns instead of hoping vanilla phase selection escalates enough.
        if (!this.enraged) {
            return;
        }
        this.tickEnragedBreathAura(level);
        if (this.netherSlamActive) {
            this.tickNetherSlam(level);
            return;
        }

        if (this.strafeCooldown > 0) {
            this.strafeCooldown--;
        }
        if (this.slamCooldown > 0) {
            this.slamCooldown--;
        }
        if (this.strafeCooldown <= 0) {
            this.forceStrafeBurst(level);
        }
        if (this.slamCooldown <= 0) {
            this.beginNetherSlam(level);
        }
    }

    private void tickEnragedBreathAura(ServerLevel level) {
        // Fix: the enraged phase promised a deeper-red breath read, but vanilla dragon_breath particles are purple. Layering soul-fire and flame particles from the head while enraged shifts the visual signature to corrupted Nether red without needing to override vanilla breath particles.
        if (this.tickCount % 4 != 0) {
            return;
        }
        EnderDragonPart head = this.head;
        double headX = head.getX();
        double headY = head.getY() + 0.25;
        double headZ = head.getZ();
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, headX, headY, headZ, 3, 0.35, 0.25, 0.35, 0.015);
        level.sendParticles(ParticleTypes.FLAME, headX, headY, headZ, 2, 0.3, 0.2, 0.3, 0.01);
        if (this.tickCount % 20 == 0) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, headX, headY, headZ, 4, 0.5, 0.3, 0.5, 0.01);
        }
    }

    private void forceStrafeBurst(ServerLevel level) {
        // Fix: phase two needed more frequent breath pressure, so the dragon now re-enters the vanilla strafe phase on a shorter loop whenever a player is available to target.
        Player target = level.getNearestPlayer(this, PLAYER_EVENT_RANGE);
        this.strafeCooldown = STRAFE_INTERVAL_TICKS;
        if (target == null || this.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
            return;
        }

        DragonStrafePlayerPhase strafePhase = this.getPhaseManager().getPhase(EnderDragonPhase.STRAFE_PLAYER);
        strafePhase.setTarget(target);
        this.getPhaseManager().setPhase(EnderDragonPhase.STRAFE_PLAYER);
    }

    private void beginNetherSlam(ServerLevel level) {
        // Fix: the enraged dragon lacked a positional punish, so it now snapshots a player's last location and starts a scripted ascent-then-dive slam aimed at that point.
        Player target = level.getNearestPlayer(this, PLAYER_EVENT_RANGE);
        this.slamCooldown = NETHER_SLAM_INTERVAL_TICKS;
        if (target == null) {
            return;
        }

        this.netherSlamActive = true;
        this.netherSlamTicksRemaining = NETHER_SLAM_TOTAL_TICKS;
        this.netherSlamTarget = target.blockPosition();
        this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        this.triggerAnim(ACTION_CONTROLLER, NETHER_SLAM);
        level.playSound(null, this.blockPosition(), ModSounds.ENTITY_NETHER_DRAGON_PHASE2, SoundSource.HOSTILE, 1.5f, 0.75f);
        level.sendParticles(ModParticles.NETHER_EMBER, this.getX(), this.getY() + 2.0, this.getZ(), 30, 1.2, 0.8, 1.2, 0.03);
    }

    private void tickNetherSlam(ServerLevel level) {
        // Fix: the slam sequence now owns its own motion and impact timing so the dive cannot stall inside vanilla routing or quietly expire without detonating.
        int elapsedTicks = NETHER_SLAM_TOTAL_TICKS - this.netherSlamTicksRemaining;
        this.netherSlamTicksRemaining--;

        Vec3 impactTarget = Vec3.atCenterOf(this.netherSlamTarget);
        Vec3 flightTarget = elapsedTicks < NETHER_SLAM_ASCENT_TICKS
            ? impactTarget.add(0.0, 26.0, 0.0)
            : impactTarget;
        Vec3 toTarget = flightTarget.subtract(this.position());
        if (toTarget.lengthSqr() > 1.0e-4) {
            Vec3 motion = toTarget.normalize().scale(elapsedTicks < NETHER_SLAM_ASCENT_TICKS ? 1.15 : 2.1);
            this.setDeltaMovement(motion);
            float yaw = (float) (Mth.atan2(motion.x, motion.z) * (180.0 / Math.PI));
            this.setYRot(yaw);
            this.yRotA = yaw;
        }

        if (elapsedTicks < NETHER_SLAM_ASCENT_TICKS) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 2.0, this.getZ(), 4, 0.9, 0.5, 0.9, 0.01);
            return;
        }

        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.5, this.getZ(), 6, 1.0, 0.5, 1.0, 0.02);
        if (this.position().distanceToSqr(impactTarget) <= 16.0 || this.netherSlamTicksRemaining <= 0) {
            this.resolveNetherSlam(level);
        }
    }

    private void resolveNetherSlam(ServerLevel level) {
        // Fix: the slam explosion was visually non-griefing but still dealt vanilla explosion damage before the scripted 15-damage pulse. The explosion is now presentation-only and the explicit radius hit is the single damage path.
        this.netherSlamActive = false;
        this.netherSlamTicksRemaining = 0;
        Vec3 impactCenter = Vec3.atCenterOf(this.netherSlamTarget);
        level.explode(this, null, NETHER_SLAM_VISUAL_EXPLOSION, impactCenter, (float) NETHER_SLAM_EXPLOSION_POWER, false, Level.ExplosionInteraction.NONE);

        AABB impactBox = new AABB(
            impactCenter.x - NETHER_SLAM_DAMAGE_RADIUS,
            impactCenter.y - NETHER_SLAM_DAMAGE_RADIUS,
            impactCenter.z - NETHER_SLAM_DAMAGE_RADIUS,
            impactCenter.x + NETHER_SLAM_DAMAGE_RADIUS,
            impactCenter.y + NETHER_SLAM_DAMAGE_RADIUS,
            impactCenter.z + NETHER_SLAM_DAMAGE_RADIUS
        );
        DamageSource slamDamage = this.damageSources().mobAttack(this);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, impactBox, target -> target.isAlive() && target != this)) {
            entity.hurtServer(level, slamDamage, 15.0f);
        }

        level.sendParticles(ParticleTypes.LAVA, impactCenter.x, impactCenter.y + 0.5, impactCenter.z, 20, 0.8, 0.2, 0.8, 0.01);
        level.sendParticles(ModParticles.NETHER_EMBER, impactCenter.x, impactCenter.y + 0.5, impactCenter.z, 30, 1.1, 0.4, 1.1, 0.02);
        this.setDeltaMovement(Vec3.ZERO);
        this.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
    }

    private void spawnDragonBlood(ServerLevel level, EnderDragonPart part) {
        double x = part == null ? this.getX() : part.getX();
        double y = part == null ? this.getY() + 2.0 : part.getY() + 0.35;
        double z = part == null ? this.getZ() : part.getZ();
        level.sendParticles(ModParticles.DRAGON_BLOOD, x, y, z, 10, 0.35, 0.3, 0.35, 0.02);
    }

    private static double horizontalDistanceSqr(BlockPos first, BlockPos second) {
        double dx = first.getX() - second.getX();
        double dz = first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }
}

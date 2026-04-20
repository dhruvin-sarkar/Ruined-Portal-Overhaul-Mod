package com.ruinedportaloverhaul.block.entity;

import com.ruinedportaloverhaul.damage.ModDamageTypes;
import com.ruinedportaloverhaul.entity.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class NetherConduitBlockEntity extends BlockEntity {
    private static final String CONDUIT_LEVEL_TAG = "conduit_level";
    private static final int ACTIVATION_SCAN_INTERVAL_TICKS = 20;
    private static final int ATTACK_INTERVAL_TICKS = 30;
    private static final int ACTIVATION_REQUIRED_FRAME_BLOCKS = 12;
    private static final int EFFECT_RADIUS = 16;
    private static final int STATUS_RADIUS = 8;
    private static final int EFFECT_DURATION_TICKS = 40;
    private static final int MAX_CONDUIT_LEVEL = 2;

    private boolean active;
    private int frameBlockCount;
    private int conduitLevel;

    public NetherConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NETHER_CONDUIT, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NetherConduitBlockEntity blockEntity) {
        long gameTime = level.getGameTime();

        if (gameTime % ACTIVATION_SCAN_INTERVAL_TICKS == 0) {
            int frameBlocks = countFrameBlocks(level, pos);
            boolean active = frameBlocks >= ACTIVATION_REQUIRED_FRAME_BLOCKS;
            if (blockEntity.active != active || blockEntity.frameBlockCount != frameBlocks) {
                blockEntity.active = active;
                blockEntity.frameBlockCount = frameBlocks;
                blockEntity.setChanged();
            }

            if (blockEntity.active && level instanceof ServerLevel serverLevel) {
                blockEntity.applyActiveEffects(serverLevel, pos);
                blockEntity.displayStatus(serverLevel, pos);
            }
        }

        if (blockEntity.active && gameTime % ATTACK_INTERVAL_TICKS == 0 && level instanceof ServerLevel serverLevel) {
            blockEntity.attackNearbyMobs(serverLevel, pos);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.conduitLevel = Mth.clamp(input.getIntOr(CONDUIT_LEVEL_TAG, 0), 0, MAX_CONDUIT_LEVEL);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(CONDUIT_LEVEL_TAG, this.conduitLevel);
    }

    public boolean isActive() {
        return this.active;
    }

    public int conduitLevel() {
        return this.conduitLevel;
    }

    public int frameBlockCount() {
        return this.frameBlockCount;
    }

    public int nextUpgradeCost() {
        return switch (this.conduitLevel) {
            case 0 -> 1;
            case 1 -> 2;
            default -> 0;
        };
    }

    public void upgrade() {
        if (this.conduitLevel >= MAX_CONDUIT_LEVEL) {
            return;
        }
        this.conduitLevel++;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    private static int countFrameBlocks(Level level, BlockPos pos) {
        int count = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (!isFrameEdge(dx, dy, dz)) {
                        continue;
                    }
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (level.getBlockState(cursor).is(Blocks.NETHER_BRICKS)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    private void applyActiveEffects(ServerLevel level, BlockPos pos) {
        AABB range = new AABB(pos).inflate(EFFECT_RADIUS);
        double radiusSqr = EFFECT_RADIUS * EFFECT_RADIUS;
        for (ServerPlayer player : level.getPlayers(player -> range.contains(player.position()) && player.distanceToSqr(pos.getCenter()) <= radiusSqr)) {
            NetherConduitPowerTracker.grant(player, level.getGameTime(), this.conduitLevel);
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, EFFECT_DURATION_TICKS, fireResistanceAmplifier(this.conduitLevel), true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, EFFECT_DURATION_TICKS, hasteAmplifier(this.conduitLevel), true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION_TICKS, regenerationAmplifier(this.conduitLevel), true, false, true));
        }
    }

    public static boolean hasActiveConduitNear(Level level, BlockPos center, int radius) {
        int radiusSqr = radius * radius;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSqr) {
                        continue;
                    }

                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (level.isLoaded(cursor)
                        && level.getBlockEntity(cursor) instanceof NetherConduitBlockEntity conduit
                        && conduit.isActive()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void attackNearbyMobs(ServerLevel level, BlockPos pos) {
        int attackRadius = attackRadius(this.conduitLevel);
        AABB range = new AABB(pos).inflate(attackRadius);
        double radiusSqr = attackRadius * attackRadius;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, range, mob -> isTargetMob(mob) && mob.distanceToSqr(pos.getCenter()) <= radiusSqr)) {
            if (mob.hurtServer(level, level.damageSources().source(ModDamageTypes.NETHER_CONDUIT), attackDamage(this.conduitLevel))) {
                level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    mob.getX(),
                    mob.getY() + mob.getBbHeight() * 0.5,
                    mob.getZ(),
                    6,
                    0.25,
                    0.25,
                    0.25,
                    0.01
                );
            }
        }
    }

    private void displayStatus(ServerLevel level, BlockPos pos) {
        AABB range = new AABB(pos).inflate(STATUS_RADIUS);
        double radiusSqr = STATUS_RADIUS * STATUS_RADIUS;
        Component message = Component.literal("Nether Conduit L" + this.conduitLevel + ": " + effectsDescription(this.conduitLevel))
            .withStyle(this.conduitLevel >= MAX_CONDUIT_LEVEL ? ChatFormatting.GOLD : ChatFormatting.RED);
        for (ServerPlayer player : level.getPlayers(player -> range.contains(player.position()) && player.distanceToSqr(pos.getCenter()) <= radiusSqr)) {
            player.displayClientMessage(message, true);
        }
    }

    private static int fireResistanceAmplifier(int conduitLevel) {
        return conduitLevel >= 1 ? 1 : 0;
    }

    private static int hasteAmplifier(int conduitLevel) {
        return conduitLevel >= 2 ? 1 : 0;
    }

    private static int regenerationAmplifier(int conduitLevel) {
        return conduitLevel >= 2 ? 1 : 0;
    }

    private static int attackRadius(int conduitLevel) {
        return switch (conduitLevel) {
            case 1 -> 20;
            case 2 -> 24;
            default -> 16;
        };
    }

    private static float attackDamage(int conduitLevel) {
        return switch (conduitLevel) {
            case 1 -> 6.0f;
            case 2 -> 8.0f;
            default -> 4.0f;
        };
    }

    private static String effectsDescription(int conduitLevel) {
        return switch (conduitLevel) {
            case 1 -> "Fire Resistance II, Haste, Regeneration, 20-block strike";
            case 2 -> "Fire Resistance II, Haste II, Regeneration II, near-zero lava drag";
            default -> "Fire Resistance, Haste, Regeneration";
        };
    }

    private static boolean isTargetMob(Mob mob) {
        EntityType<?> type = mob.getType();
        return type == EntityType.ZOMBIFIED_PIGLIN
            || type == EntityType.PIGLIN
            || type == EntityType.PIGLIN_BRUTE
            || type == EntityType.BLAZE
            || type == EntityType.WITHER_SKELETON
            || type == EntityType.GHAST
            || type == EntityType.HOGLIN
            || type == EntityType.MAGMA_CUBE
            || type == ModEntities.PIGLIN_PILLAGER
            || type == ModEntities.PIGLIN_VINDICATOR
            || type == ModEntities.PIGLIN_BRUTE_PILLAGER
            || type == ModEntities.PIGLIN_ILLUSIONER
            || type == ModEntities.PIGLIN_EVOKER
            || type == ModEntities.PIGLIN_RAVAGER
            || type == ModEntities.PIGLIN_VEX;
    }

    private static boolean isFrameEdge(int dx, int dy, int dz) {
        int outerAxes = 0;
        if (Math.abs(dx) == 2) {
            outerAxes++;
        }
        if (Math.abs(dy) == 2) {
            outerAxes++;
        }
        if (Math.abs(dz) == 2) {
            outerAxes++;
        }
        return outerAxes == 2;
    }
}

package com.ruinedportaloverhaul.block.entity;

import com.ruinedportaloverhaul.damage.ModDamageTypes;
import com.ruinedportaloverhaul.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class NetherConduitBlockEntity extends BlockEntity {
    private static final int ACTIVATION_SCAN_INTERVAL_TICKS = 20;
    private static final int ATTACK_INTERVAL_TICKS = 30;
    private static final int ACTIVATION_REQUIRED_FRAME_BLOCKS = 12;
    private static final int EFFECT_RADIUS = 16;
    private static final int EFFECT_DURATION_TICKS = 40;
    private static final int ATTACK_RADIUS = 16;
    private static final float ATTACK_DAMAGE = 4.0f;

    private boolean active;
    private int frameBlockCount;

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
                applyActiveEffects(serverLevel, pos);
            }
        }

        if (blockEntity.active && gameTime % ATTACK_INTERVAL_TICKS == 0 && level instanceof ServerLevel serverLevel) {
            attackNearbyMobs(serverLevel, pos);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public int frameBlockCount() {
        return this.frameBlockCount;
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

    private static void applyActiveEffects(ServerLevel level, BlockPos pos) {
        AABB range = new AABB(pos).inflate(EFFECT_RADIUS);
        double radiusSqr = EFFECT_RADIUS * EFFECT_RADIUS;
        for (ServerPlayer player : level.getPlayers(player -> range.contains(player.position()) && player.distanceToSqr(pos.getCenter()) <= radiusSqr)) {
            NetherConduitPowerTracker.grant(player, level.getGameTime(), 0);
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, EFFECT_DURATION_TICKS, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, EFFECT_DURATION_TICKS, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION_TICKS, 0, true, false, true));
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

    private static void attackNearbyMobs(ServerLevel level, BlockPos pos) {
        AABB range = new AABB(pos).inflate(ATTACK_RADIUS);
        double radiusSqr = ATTACK_RADIUS * ATTACK_RADIUS;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, range, mob -> isTargetMob(mob) && mob.distanceToSqr(pos.getCenter()) <= radiusSqr)) {
            if (mob.hurtServer(level, level.damageSources().source(ModDamageTypes.NETHER_CONDUIT), ATTACK_DAMAGE)) {
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

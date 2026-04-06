/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ServerExplosion
implements Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
    private static final float LARGE_EXPLOSION_RADIUS = 2.0f;
    private final boolean fire;
    private final Explosion.BlockInteraction blockInteraction;
    private final ServerLevel level;
    private final Vec3 center;
    private final @Nullable Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final Map<Player, Vec3> hitPlayers = new HashMap<Player, Vec3>();

    public ServerExplosion(ServerLevel serverLevel, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, Vec3 vec3, float f, boolean bl, Explosion.BlockInteraction blockInteraction) {
        this.level = serverLevel;
        this.source = entity;
        this.radius = f;
        this.center = vec3;
        this.fire = bl;
        this.blockInteraction = blockInteraction;
        this.damageSource = damageSource == null ? serverLevel.damageSources().explosion(this) : damageSource;
        this.damageCalculator = explosionDamageCalculator == null ? this.makeDamageCalculator(entity) : explosionDamageCalculator;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity);
    }

    public static float getSeenPercent(Vec3 vec3, Entity entity) {
        AABB aABB = entity.getBoundingBox();
        double d = 1.0 / ((aABB.maxX - aABB.minX) * 2.0 + 1.0);
        double e = 1.0 / ((aABB.maxY - aABB.minY) * 2.0 + 1.0);
        double f = 1.0 / ((aABB.maxZ - aABB.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (d < 0.0 || e < 0.0 || f < 0.0) {
            return 0.0f;
        }
        int i = 0;
        int j = 0;
        for (double k = 0.0; k <= 1.0; k += d) {
            for (double l = 0.0; l <= 1.0; l += e) {
                for (double m = 0.0; m <= 1.0; m += f) {
                    double n = Mth.lerp(k, aABB.minX, aABB.maxX);
                    double o = Mth.lerp(l, aABB.minY, aABB.maxY);
                    double p = Mth.lerp(m, aABB.minZ, aABB.maxZ);
                    Vec3 vec32 = new Vec3(n + g, o, p + h);
                    if (entity.level().clip(new ClipContext(vec32, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
                        ++i;
                    }
                    ++j;
                }
            }
        }
        return (float)i / (float)j;
    }

    @Override
    public float radius() {
        return this.radius;
    }

    @Override
    public Vec3 center() {
        return this.center;
    }

    private List<BlockPos> calculateExplodedPositions() {
        HashSet<BlockPos> set = new HashSet<BlockPos>();
        int i = 16;
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                block2: for (int l = 0; l < 16; ++l) {
                    if (j != 0 && j != 15 && k != 0 && k != 15 && l != 0 && l != 15) continue;
                    double d = (float)j / 15.0f * 2.0f - 1.0f;
                    double e = (float)k / 15.0f * 2.0f - 1.0f;
                    double f = (float)l / 15.0f * 2.0f - 1.0f;
                    double g = Math.sqrt(d * d + e * e + f * f);
                    d /= g;
                    e /= g;
                    f /= g;
                    double m = this.center.x;
                    double n = this.center.y;
                    double o = this.center.z;
                    float p = 0.3f;
                    for (float h = this.radius * (0.7f + this.level.random.nextFloat() * 0.6f); h > 0.0f; h -= 0.22500001f) {
                        BlockPos blockPos = BlockPos.containing(m, n, o);
                        BlockState blockState = this.level.getBlockState(blockPos);
                        FluidState fluidState = this.level.getFluidState(blockPos);
                        if (!this.level.isInWorldBounds(blockPos)) continue block2;
                        Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockPos, blockState, fluidState);
                        if (optional.isPresent()) {
                            h -= (optional.get().floatValue() + 0.3f) * 0.3f;
                        }
                        if (h > 0.0f && this.damageCalculator.shouldBlockExplode(this, this.level, blockPos, blockState, h)) {
                            set.add(blockPos);
                        }
                        m += d * (double)0.3f;
                        n += e * (double)0.3f;
                        o += f * (double)0.3f;
                    }
                }
            }
        }
        return new ObjectArrayList(set);
    }

    private void hurtEntities() {
        if (this.radius < 1.0E-5f) {
            return;
        }
        float f = this.radius * 2.0f;
        int i = Mth.floor(this.center.x - (double)f - 1.0);
        int j = Mth.floor(this.center.x + (double)f + 1.0);
        int k = Mth.floor(this.center.y - (double)f - 1.0);
        int l = Mth.floor(this.center.y + (double)f + 1.0);
        int m = Mth.floor(this.center.z - (double)f - 1.0);
        int n = Mth.floor(this.center.z + (double)f + 1.0);
        List<Entity> list = this.level.getEntities(this.source, new AABB(i, k, m, j, l, n));
        for (Entity entity : list) {
            Player player;
            double d;
            float h;
            double d2;
            if (entity.ignoreExplosion(this) || (d2 = Math.sqrt(entity.distanceToSqr(this.center)) / (double)f) > 1.0) continue;
            Vec3 vec3 = entity instanceof PrimedTnt ? entity.position() : entity.getEyePosition();
            Vec3 vec32 = vec3.subtract(this.center).normalize();
            boolean bl = this.damageCalculator.shouldDamageEntity(this, entity);
            float g = this.damageCalculator.getKnockbackMultiplier(entity);
            float f2 = h = bl || g != 0.0f ? ServerExplosion.getSeenPercent(this.center, entity) : 0.0f;
            if (bl) {
                entity.hurtServer(this.level, this.damageSource, this.damageCalculator.getEntityDamageAmount(this, entity, h));
            }
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                d = livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
            } else {
                d = 0.0;
            }
            double e = d;
            double o = (1.0 - d2) * (double)h * (double)g * (1.0 - e);
            Vec3 vec33 = vec32.scale(o);
            entity.push(vec33);
            if (entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof Projectile) {
                Projectile projectile = (Projectile)entity;
                projectile.setOwner(this.damageSource.getEntity());
            } else if (!(!(entity instanceof Player) || (player = (Player)entity).isSpectator() || player.isCreative() && player.getAbilities().flying)) {
                this.hitPlayers.put(player, vec33);
            }
            entity.onExplosionHit(this.source);
        }
    }

    private void interactWithBlocks(List<BlockPos> list) {
        ArrayList list2 = new ArrayList();
        Util.shuffle(list, this.level.random);
        for (BlockPos blockPos2 : list) {
            this.level.getBlockState(blockPos2).onExplosionHit(this.level, blockPos2, this, (itemStack, blockPos) -> ServerExplosion.addOrAppendStack(list2, itemStack, blockPos));
        }
        for (StackCollector stackCollector : list2) {
            Block.popResource((Level)this.level, stackCollector.pos, stackCollector.stack);
        }
    }

    private void createFire(List<BlockPos> list) {
        for (BlockPos blockPos : list) {
            if (this.level.random.nextInt(3) != 0 || !this.level.getBlockState(blockPos).isAir() || !this.level.getBlockState(blockPos.below()).isSolidRender()) continue;
            this.level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level, blockPos));
        }
    }

    public int explode() {
        this.level.gameEvent(this.source, GameEvent.EXPLODE, this.center);
        List<BlockPos> list = this.calculateExplodedPositions();
        this.hurtEntities();
        if (this.interactsWithBlocks()) {
            ProfilerFiller profilerFiller = Profiler.get();
            profilerFiller.push("explosion_blocks");
            this.interactWithBlocks(list);
            profilerFiller.pop();
        }
        if (this.fire) {
            this.createFire(list);
        }
        return list.size();
    }

    private static void addOrAppendStack(List<StackCollector> list, ItemStack itemStack, BlockPos blockPos) {
        for (StackCollector stackCollector : list) {
            stackCollector.tryMerge(itemStack);
            if (!itemStack.isEmpty()) continue;
            return;
        }
        list.add(new StackCollector(blockPos, itemStack));
    }

    private boolean interactsWithBlocks() {
        return this.blockInteraction != Explosion.BlockInteraction.KEEP;
    }

    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
    }

    @Override
    public ServerLevel level() {
        return this.level;
    }

    @Override
    public @Nullable LivingEntity getIndirectSourceEntity() {
        return Explosion.getIndirectSourceEntity(this.source);
    }

    @Override
    public @Nullable Entity getDirectSourceEntity() {
        return this.source;
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    @Override
    public Explosion.BlockInteraction getBlockInteraction() {
        return this.blockInteraction;
    }

    @Override
    public boolean canTriggerBlocks() {
        if (this.blockInteraction != Explosion.BlockInteraction.TRIGGER_BLOCK) {
            return false;
        }
        if (this.source != null && this.source.getType() == EntityType.BREEZE_WIND_CHARGE) {
            return this.level.getGameRules().get(GameRules.MOB_GRIEFING);
        }
        return true;
    }

    @Override
    public boolean shouldAffectBlocklikeEntities() {
        boolean bl2;
        boolean bl = this.level.getGameRules().get(GameRules.MOB_GRIEFING);
        boolean bl3 = bl2 = this.source == null || this.source.getType() != EntityType.BREEZE_WIND_CHARGE && this.source.getType() != EntityType.WIND_CHARGE;
        if (bl) {
            return bl2;
        }
        return this.blockInteraction.shouldAffectBlocklikeEntities() && bl2;
    }

    public boolean isSmall() {
        return this.radius < 2.0f || !this.interactsWithBlocks();
    }

    static class StackCollector {
        final BlockPos pos;
        ItemStack stack;

        StackCollector(BlockPos blockPos, ItemStack itemStack) {
            this.pos = blockPos;
            this.stack = itemStack;
        }

        public void tryMerge(ItemStack itemStack) {
            if (ItemEntity.areMergable(this.stack, itemStack)) {
                this.stack = ItemEntity.merge(this.stack, itemStack, 16);
            }
        }
    }
}


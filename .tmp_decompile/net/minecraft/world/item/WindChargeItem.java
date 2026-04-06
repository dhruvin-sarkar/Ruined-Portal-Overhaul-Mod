/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class WindChargeItem
extends Item
implements ProjectileItem {
    public static float PROJECTILE_SHOOT_POWER = 1.5f;

    public WindChargeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack2 = player.getItemInHand(interactionHand);
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel2 = (ServerLevel)level;
            Projectile.spawnProjectileFromRotation((serverLevel, livingEntity, itemStack) -> new WindCharge(player, level, player.position().x(), player.getEyePosition().y(), player.position().z()), serverLevel2, itemStack2, player, 0.0f, PROJECTILE_SHOOT_POWER, 1.0f);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        player.awardStat(Stats.ITEM_USED.get(this));
        itemStack2.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        RandomSource randomSource = level.getRandom();
        double d = randomSource.triangle((double)direction.getStepX(), 0.11485000000000001);
        double e = randomSource.triangle((double)direction.getStepY(), 0.11485000000000001);
        double f = randomSource.triangle((double)direction.getStepZ(), 0.11485000000000001);
        Vec3 vec3 = new Vec3(d, e, f);
        WindCharge windCharge = new WindCharge(level, position.x(), position.y(), position.z(), vec3);
        windCharge.setDeltaMovement(vec3);
        return windCharge;
    }

    @Override
    public void shoot(Projectile projectile, double d, double e, double f, float g, float h) {
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {
        return ProjectileItem.DispenseConfig.builder().positionFunction((blockSource, direction) -> DispenserBlock.getDispensePosition(blockSource, 1.0, Vec3.ZERO)).uncertainty(6.6666665f).power(1.0f).overrideDispenseEvent(1051).build();
    }
}


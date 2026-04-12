package com.ruinedportaloverhaul.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ExiledPiglinTraderEntity extends WanderingTrader {
    private static final long DESPAWN_AFTER_TICKS = 72_000L;
    private static final List<Component> TRADE_MESSAGES = List.of(
        Component.literal("...the portal remembers you."),
        Component.literal("Gold speaks louder than oaths."),
        Component.literal("Take what the Nether left behind.")
    );

    private long spawnGameTime = -1L;

    public ExiledPiglinTraderEntity(EntityType<? extends ExiledPiglinTraderEntity> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true);
        this.setDespawnDelay(0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35);
    }

    public void rememberSpawnTime(long gameTime) {
        this.spawnGameTime = gameTime;
    }

    @Override
    protected void updateTrades(ServerLevel serverLevel) {
        MerchantOffers offers = this.getOffers();
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 8), new ItemStack(Items.MAGMA_CREAM, 3), 5, 1, 0.05f));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 12), new ItemStack(Items.CRYING_OBSIDIAN, 2), 4, 1, 0.05f));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_BLOCK, 2), new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), 1, 5, 0.05f));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 32), Optional.empty(), new ItemStack(Items.ANCIENT_DEBRIS), 0, 1, 5, 0.05f));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        boolean canOpenTrades = this.isAlive() && !this.isTrading() && !this.isBaby();
        InteractionResult result = super.mobInteract(player, interactionHand);
        if (canOpenTrades && !this.level().isClientSide() && this.getTradingPlayer() == player) {
            RandomSource random = this.getRandom();
            player.displayClientMessage(TRADE_MESSAGES.get(random.nextInt(TRADE_MESSAGES.size())), true);
        }
        return result;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.spawnGameTime < 0L) {
            this.spawnGameTime = this.level().getGameTime();
        }
        if (this.level().getGameTime() - this.spawnGameTime > DESPAWN_AFTER_TICKS) {
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float damageAmount) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putLong("SpawnGameTime", this.spawnGameTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.spawnGameTime = valueInput.getLongOr("SpawnGameTime", -1L);
        this.setInvulnerable(true);
        this.setDespawnDelay(0);
    }
}

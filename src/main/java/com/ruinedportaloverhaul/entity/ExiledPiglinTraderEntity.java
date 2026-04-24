package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import com.ruinedportaloverhaul.sound.ModSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ExiledPiglinTraderEntity extends WanderingTrader implements GeoEntity {
    private static final String ANCHOR_POS_KEY = "AnchorPos";
    private static final long DESPAWN_AFTER_TICKS = 72_000L;
    private static final long RESTOCK_INTERVAL_TICKS = 40_000L;
    private static final int ANCHOR_RESTRICTION_RADIUS = 4;
    private static final List<String> TRADE_MESSAGES = List.of(
        "message.ruined_portal_overhaul.trader.line_1",
        "message.ruined_portal_overhaul.trader.line_2",
        "message.ruined_portal_overhaul.trader.line_3",
        "message.ruined_portal_overhaul.trader.line_4",
        "message.ruined_portal_overhaul.trader.line_5"
    );

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private BlockPos anchorPos;
    private long spawnGameTime = -1L;
    private long lastRestockGameTime = -1L;

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

    @Override
    protected void registerGoals() {
        // Fix: the post-raid trader inherits wandering behavior, but fluid escape should still be explicit and highest-priority in corrupted terrain.
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
            RuinedPortalGeoAnimations.walkIdleController(),
            RuinedPortalGeoAnimations.deathController()
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public void rememberSpawnTime(long gameTime) {
        this.spawnGameTime = gameTime;
    }

    public void rememberAnchor(BlockPos anchorPos) {
        // Fix: the Exiled Piglin reward scene used a decorative fence post but never stored or applied the trader's anchor, so the "chained to the post" reveal immediately drifted apart. The trader now persists its fence-post anchor and keeps its home restriction centered there.
        this.anchorPos = anchorPos.immutable();
        this.setHomeTo(this.anchorPos, ANCHOR_RESTRICTION_RADIUS);
    }

    @Override
    protected void updateTrades(ServerLevel serverLevel) {
        MerchantOffers offers = this.getOffers();
        offers.clear();
        // Magma cream has no vanilla crafting path back to gold, so this trade cannot create a gold loop.
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 8), new ItemStack(Items.MAGMA_CREAM, 3), 5, 1, 0.05f));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 12), new ItemStack(Items.CRYING_OBSIDIAN, 2), 4, 1, 0.05f));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 6), new ItemStack(Items.NETHER_BRICK, 16), 8, 1, 0.05f));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 10), new ItemStack(Items.BLAZE_POWDER, 4), 6, 1, 0.05f));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 18), new ItemStack(Items.OBSIDIAN, 3), 4, 2, 0.05f));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 24), new ItemStack(Items.GOLDEN_APPLE), 2, 3, 0.05f));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_BLOCK, 2), new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), 1, 5, 0.05f));
        offers.add(new MerchantOffer(new ItemCost(Items.GOLD_INGOT, 32), Optional.empty(), new ItemStack(Items.ANCIENT_DEBRIS), 0, 1, 5, 0.05f));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        // Fix: trader chatter and busy feedback now use translation keys so the post-raid encounter flavor localizes with the rest of the mod.
        Player tradingPlayer = this.getTradingPlayer();
        if (tradingPlayer != null && tradingPlayer != player) {
            if (!this.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.ruined_portal_overhaul.trader.busy"), true);
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        boolean canOpenTrades = this.isAlive() && !this.isTrading() && !this.isBaby();
        InteractionResult result = super.mobInteract(player, interactionHand);
        if (canOpenTrades && !this.level().isClientSide() && this.getTradingPlayer() == player) {
            RandomSource random = this.getRandom();
            player.displayClientMessage(Component.translatable(TRADE_MESSAGES.get(random.nextInt(TRADE_MESSAGES.size()))), true);
        }
        return result;
    }

    @Override
    public void notifyTrade(MerchantOffer merchantOffer) {
        super.notifyTrade(merchantOffer);
        if (this.getTradingPlayer() instanceof ServerPlayer serverPlayer) {
            ModAdvancementTriggers.trigger(ModAdvancementTriggers.EXILED_TRADE, serverPlayer);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        Player tradingPlayer = this.getTradingPlayer();
        return super.stillValid(player) && (tradingPlayer == null || tradingPlayer == player);
    }

    @Override
    public void aiStep() {
        // Fix: the completion scene promised a chained trader, but the entity kept full wandering-trader behavior with no leash recovery. Server ticks now keep the trader bound to its saved fence-post anchor across reloads and leash-knot glitches.
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.spawnGameTime < 0L) {
            this.spawnGameTime = this.level().getGameTime();
        }
        if (this.lastRestockGameTime < 0L) {
            this.lastRestockGameTime = this.level().getGameTime();
        }
        this.restoreAnchorLeash();
        if (this.level().getGameTime() - this.lastRestockGameTime >= RESTOCK_INTERVAL_TICKS) {
            for (MerchantOffer offer : this.getOffers()) {
                offer.resetUses();
            }
            this.lastRestockGameTime = this.level().getGameTime();
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
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_EXILED_PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_EXILED_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_EXILED_PIGLIN_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        // Fix: the Exiled Piglin uses its own sound ids but inherited merchant volume, so dialogue and idle cues now sit at a clear post-raid level.
        return 1.0f;
    }

    @Override
    public float getVoicePitch() {
        return 0.9f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.08f;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        // Fix: the trader used to save only timing fields, so reloads forgot which fence post was meant to hold the leash. The anchor now persists with the rest of the entity state.
        super.addAdditionalSaveData(valueOutput);
        if (this.anchorPos != null) {
            valueOutput.putLong(ANCHOR_POS_KEY, this.anchorPos.asLong());
        }
        valueOutput.putLong("SpawnGameTime", this.spawnGameTime);
        valueOutput.putLong("LastRestockGameTime", this.lastRestockGameTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        // Fix: reloads restored the trader but not its fence-post anchor, so completed portals lost the chained tableau after chunk loads. Loading now restores the anchor and re-applies the trader's home restriction immediately.
        super.readAdditionalSaveData(valueInput);
        long anchorLong = valueInput.getLongOr(ANCHOR_POS_KEY, Long.MIN_VALUE);
        if (anchorLong != Long.MIN_VALUE) {
            this.anchorPos = BlockPos.of(anchorLong);
            this.setHomeTo(this.anchorPos, ANCHOR_RESTRICTION_RADIUS);
        } else {
            this.anchorPos = null;
            this.clearHome();
        }
        this.spawnGameTime = valueInput.getLongOr("SpawnGameTime", -1L);
        this.lastRestockGameTime = valueInput.getLongOr("LastRestockGameTime", -1L);
        this.setInvulnerable(true);
        this.setDespawnDelay(0);
    }

    private void restoreAnchorLeash() {
        // Fix: the fence knot can be absent during reload timing or local block updates, which used to leave the trader permanently unchained. When the anchor post still exists, the trader now recreates the knot and reattaches itself.
        if (this.anchorPos == null || this.level().isClientSide()) {
            return;
        }

        this.setHomeTo(this.anchorPos, ANCHOR_RESTRICTION_RADIUS);
        if (this.isLeashed()) {
            return;
        }

        if (this.level().getBlockState(this.anchorPos).is(Blocks.NETHER_BRICK_FENCE)) {
            LeashFenceKnotEntity leashKnot = LeashFenceKnotEntity.getOrCreateKnot(this.level(), this.anchorPos);
            this.setLeashedTo(leashKnot, true);
        }
    }
}

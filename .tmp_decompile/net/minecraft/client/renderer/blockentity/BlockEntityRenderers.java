/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityWithBoundingBoxRenderer;
import net.minecraft.client.renderer.blockentity.BrushableBlockRenderer;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.CopperGolemStatueBlockRenderer;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.client.renderer.blockentity.ShelfRenderer;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.TestInstanceRenderer;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.TrialSpawnerRenderer;
import net.minecraft.client.renderer.blockentity.VaultRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Environment(value=EnvType.CLIENT)
public class BlockEntityRenderers {
    private static final Map<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> PROVIDERS = Maps.newHashMap();

    private static <T extends BlockEntity, S extends BlockEntityRenderState> void register(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T, S> blockEntityRendererProvider) {
        PROVIDERS.put(blockEntityType, blockEntityRendererProvider);
    }

    public static Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> createEntityRenderers(BlockEntityRendererProvider.Context context) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        PROVIDERS.forEach((blockEntityType, blockEntityRendererProvider) -> {
            try {
                builder.put(blockEntityType, blockEntityRendererProvider.create(context));
            }
            catch (Exception exception) {
                throw new IllegalStateException("Failed to create model for " + String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey((BlockEntityType<?>)blockEntityType)), exception);
            }
        });
        return builder.build();
    }

    static {
        BlockEntityRenderers.register(BlockEntityType.SIGN, SignRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.HANGING_SIGN, HangingSignRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.MOB_SPAWNER, SpawnerRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.PISTON, context -> new PistonHeadRenderer());
        BlockEntityRenderers.register(BlockEntityType.CHEST, ChestRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.ENDER_CHEST, ChestRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.TRAPPED_CHEST, ChestRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.ENCHANTING_TABLE, EnchantTableRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.LECTERN, LecternRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.END_PORTAL, context -> new TheEndPortalRenderer());
        BlockEntityRenderers.register(BlockEntityType.END_GATEWAY, context -> new TheEndGatewayRenderer());
        BlockEntityRenderers.register(BlockEntityType.BEACON, context -> new BeaconRenderer());
        BlockEntityRenderers.register(BlockEntityType.SKULL, SkullBlockRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.BANNER, BannerRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.STRUCTURE_BLOCK, context -> new BlockEntityWithBoundingBoxRenderer());
        BlockEntityRenderers.register(BlockEntityType.TEST_INSTANCE_BLOCK, context -> new TestInstanceRenderer());
        BlockEntityRenderers.register(BlockEntityType.SHULKER_BOX, ShulkerBoxRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.BED, BedRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.CONDUIT, ConduitRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.BELL, BellRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.CAMPFIRE, CampfireRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.BRUSHABLE_BLOCK, BrushableBlockRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.DECORATED_POT, DecoratedPotRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.TRIAL_SPAWNER, TrialSpawnerRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.VAULT, VaultRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.COPPER_GOLEM_STATUE, CopperGolemStatueBlockRenderer::new);
        BlockEntityRenderers.register(BlockEntityType.SHELF, ShelfRenderer::new);
    }
}


package com.ruinedportaloverhaul.client;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.client.atmosphere.PortalAtmosphereClient;
import com.ruinedportaloverhaul.client.render.ExiledPiglinRenderer;
import com.ruinedportaloverhaul.client.render.PiglinIllagerRenderer;
import com.ruinedportaloverhaul.client.render.PiglinRavagerRenderer;
import com.ruinedportaloverhaul.client.render.PiglinVexRenderer;
import com.ruinedportaloverhaul.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;

public final class RuinedPortalOverhaulClient implements ClientModInitializer {
    private static final Identifier PILLAGER_TEXTURE = texture("entity/piglin_pillager.png");
    private static final Identifier VINDICATOR_TEXTURE = texture("entity/piglin_vindicator.png");
    private static final Identifier BRUTE_PILLAGER_TEXTURE = texture("entity/piglin_brute_pillager.png");
    private static final Identifier ILLUSIONER_TEXTURE = texture("entity/piglin_illusioner.png");
    private static final Identifier EVOKER_TEXTURE = texture("entity/piglin_evoker.png");
    private static final Identifier RAVAGER_TEXTURE = texture("entity/piglin_ravager.png");
    private static final Identifier VEX_TEXTURE = texture("entity/piglin_vex.png");
    private static final Identifier EXILED_PIGLIN_TEXTURE = texture("entity/exiled_piglin.png");

    @Override
    public void onInitializeClient() {
        PortalAtmosphereClient.initialize();
        NetherFireballKeybinds.initialize();
        EntityRenderers.register(
            ModEntities.PIGLIN_PILLAGER,
            context -> new PiglinIllagerRenderer<>(context, PILLAGER_TEXTURE)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_VINDICATOR,
            context -> new PiglinIllagerRenderer<>(context, VINDICATOR_TEXTURE)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_BRUTE_PILLAGER,
            context -> new PiglinIllagerRenderer<>(context, BRUTE_PILLAGER_TEXTURE)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_ILLUSIONER,
            context -> new PiglinIllagerRenderer<>(context, ILLUSIONER_TEXTURE)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_EVOKER,
            context -> new PiglinIllagerRenderer<>(context, EVOKER_TEXTURE)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_RAVAGER,
            context -> new PiglinRavagerRenderer(context, RAVAGER_TEXTURE)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_VEX,
            context -> new PiglinVexRenderer(context, VEX_TEXTURE)
        );
        EntityRenderers.register(
            ModEntities.EXILED_PIGLIN,
            context -> new ExiledPiglinRenderer(context, EXILED_PIGLIN_TEXTURE)
        );
    }

    private static Identifier texture(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "textures/" + path);
    }
}

package com.ruinedportaloverhaul.client;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.client.render.PiglinIllagerRenderer;
import com.ruinedportaloverhaul.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;

public final class RuinedPortalOverhaulClient implements ClientModInitializer {
    private static final Identifier RANGED_TEXTURE = texture("entity/piglin_illager/ranged.png");
    private static final Identifier BRUTE_TEXTURE = texture("entity/piglin_illager/brute.png");
    private static final Identifier CHIEF_TEXTURE = texture("entity/piglin_illager/chief.png");

    @Override
    public void onInitializeClient() {
        EntityRenderers.register(
            ModEntities.PIGLIN_ILLAGER_RANGED,
            context -> new PiglinIllagerRenderer<>(context, RANGED_TEXTURE)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_ILLAGER_BRUTE,
            context -> new PiglinIllagerRenderer<>(context, BRUTE_TEXTURE)
        );
        EntityRenderers.register(
            ModEntities.PIGLIN_ILLAGER_CHIEF,
            context -> new PiglinIllagerRenderer<>(context, CHIEF_TEXTURE)
        );
    }

    private static Identifier texture(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "textures/" + path);
    }
}

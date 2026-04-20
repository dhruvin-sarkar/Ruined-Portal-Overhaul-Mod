package com.ruinedportaloverhaul.damage;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> NETHER_CONDUIT = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "nether_conduit")
    );

    private ModDamageTypes() {
    }
}

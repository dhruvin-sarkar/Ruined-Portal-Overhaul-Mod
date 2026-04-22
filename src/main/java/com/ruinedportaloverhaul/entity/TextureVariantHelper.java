package com.ruinedportaloverhaul.entity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public final class TextureVariantHelper {
    private static final String VARIANT_KEY = "Variant";

    private TextureVariantHelper() {
    }

    public static int selectVariant(UUID uuid, int variantCount) {
        if (variantCount <= 1) {
            return 0;
        }

        long mixed = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        int hash = (int) (mixed ^ (mixed >>> 32));

        return Math.floorMod(hash, variantCount);
    }

    public static int readVariant(ValueInput valueInput, UUID uuid, int variantCount) {
        return normalizeVariant(valueInput.getIntOr(VARIANT_KEY, selectVariant(uuid, variantCount)), variantCount);
    }

    public static void writeVariant(ValueOutput valueOutput, int variant, int variantCount) {
        valueOutput.putInt(VARIANT_KEY, normalizeVariant(variant, variantCount));
    }

    public static int normalizeVariant(int variant, int variantCount) {
        if (variantCount <= 1) {
            return 0;
        }

        return Math.floorMod(variant, variantCount);
    }
}

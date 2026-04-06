/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import java.util.Collections;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

public interface RecipeCraftingHolder {
    public void setRecipeUsed(@Nullable RecipeHolder<?> var1);

    public @Nullable RecipeHolder<?> getRecipeUsed();

    default public void awardUsedRecipes(Player player, List<ItemStack> list) {
        RecipeHolder<?> recipeHolder = this.getRecipeUsed();
        if (recipeHolder != null) {
            player.triggerRecipeCrafted(recipeHolder, list);
            if (!recipeHolder.value().isSpecial()) {
                player.awardRecipes(Collections.singleton(recipeHolder));
                this.setRecipeUsed(null);
            }
        }
    }

    default public boolean setRecipeUsed(ServerPlayer serverPlayer, RecipeHolder<?> recipeHolder) {
        if (recipeHolder.value().isSpecial() || !serverPlayer.level().getGameRules().get(GameRules.LIMITED_CRAFTING).booleanValue() || serverPlayer.getRecipeBook().contains(recipeHolder.id())) {
            this.setRecipeUsed(recipeHolder);
            return true;
        }
        return false;
    }
}


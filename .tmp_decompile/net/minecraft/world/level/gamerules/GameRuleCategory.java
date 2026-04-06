/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.gamerules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public record GameRuleCategory(Identifier id) {
    private static final List<GameRuleCategory> SORT_ORDER = new ArrayList<GameRuleCategory>();
    public static final GameRuleCategory PLAYER = GameRuleCategory.register("player");
    public static final GameRuleCategory MOBS = GameRuleCategory.register("mobs");
    public static final GameRuleCategory SPAWNING = GameRuleCategory.register("spawning");
    public static final GameRuleCategory DROPS = GameRuleCategory.register("drops");
    public static final GameRuleCategory UPDATES = GameRuleCategory.register("updates");
    public static final GameRuleCategory CHAT = GameRuleCategory.register("chat");
    public static final GameRuleCategory MISC = GameRuleCategory.register("misc");

    public Identifier getDescriptionId() {
        return this.id;
    }

    private static GameRuleCategory register(String string) {
        return GameRuleCategory.register(Identifier.withDefaultNamespace(string));
    }

    public static GameRuleCategory register(Identifier identifier) {
        GameRuleCategory gameRuleCategory = new GameRuleCategory(identifier);
        if (SORT_ORDER.contains((Object)gameRuleCategory)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Category '%s' is already registered.", identifier));
        }
        SORT_ORDER.add(gameRuleCategory);
        return gameRuleCategory;
    }

    public MutableComponent label() {
        return Component.translatable(this.id.toLanguageKey("gamerule.category"));
    }
}


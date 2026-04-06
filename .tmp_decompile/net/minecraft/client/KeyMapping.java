/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class KeyMapping
implements Comparable<KeyMapping> {
    private static final Map<String, KeyMapping> ALL = Maps.newHashMap();
    private static final Map<InputConstants.Key, List<KeyMapping>> MAP = Maps.newHashMap();
    private final String name;
    private final InputConstants.Key defaultKey;
    private final Category category;
    protected InputConstants.Key key;
    private boolean isDown;
    private int clickCount;
    private final int order;

    public static void click(InputConstants.Key key) {
        KeyMapping.forAllKeyMappings(key, keyMapping -> ++keyMapping.clickCount);
    }

    public static void set(InputConstants.Key key, boolean bl) {
        KeyMapping.forAllKeyMappings(key, keyMapping -> keyMapping.setDown(bl));
    }

    private static void forAllKeyMappings(InputConstants.Key key, Consumer<KeyMapping> consumer) {
        List<KeyMapping> list = MAP.get(key);
        if (list != null && !list.isEmpty()) {
            for (KeyMapping keyMapping : list) {
                consumer.accept(keyMapping);
            }
        }
    }

    public static void setAll() {
        Window window = Minecraft.getInstance().getWindow();
        for (KeyMapping keyMapping : ALL.values()) {
            if (!keyMapping.shouldSetOnIngameFocus()) continue;
            keyMapping.setDown(InputConstants.isKeyDown(window, keyMapping.key.getValue()));
        }
    }

    public static void releaseAll() {
        for (KeyMapping keyMapping : ALL.values()) {
            keyMapping.release();
        }
    }

    public static void restoreToggleStatesOnScreenClosed() {
        for (KeyMapping keyMapping : ALL.values()) {
            ToggleKeyMapping toggleKeyMapping;
            if (!(keyMapping instanceof ToggleKeyMapping) || !(toggleKeyMapping = (ToggleKeyMapping)keyMapping).shouldRestoreStateOnScreenClosed()) continue;
            toggleKeyMapping.setDown(true);
        }
    }

    public static void resetToggleKeys() {
        for (KeyMapping keyMapping : ALL.values()) {
            if (!(keyMapping instanceof ToggleKeyMapping)) continue;
            ToggleKeyMapping toggleKeyMapping = (ToggleKeyMapping)keyMapping;
            toggleKeyMapping.reset();
        }
    }

    public static void resetMapping() {
        MAP.clear();
        for (KeyMapping keyMapping : ALL.values()) {
            keyMapping.registerMapping(keyMapping.key);
        }
    }

    public KeyMapping(String string, int i, Category category) {
        this(string, InputConstants.Type.KEYSYM, i, category);
    }

    public KeyMapping(String string, InputConstants.Type type, int i, Category category) {
        this(string, type, i, category, 0);
    }

    public KeyMapping(String string, InputConstants.Type type, int i, Category category, int j) {
        this.name = string;
        this.defaultKey = this.key = type.getOrCreate(i);
        this.category = category;
        this.order = j;
        ALL.put(string, this);
        this.registerMapping(this.key);
    }

    public boolean isDown() {
        return this.isDown;
    }

    public Category getCategory() {
        return this.category;
    }

    public boolean consumeClick() {
        if (this.clickCount == 0) {
            return false;
        }
        --this.clickCount;
        return true;
    }

    protected void release() {
        this.clickCount = 0;
        this.setDown(false);
    }

    protected boolean shouldSetOnIngameFocus() {
        return this.key.getType() == InputConstants.Type.KEYSYM && this.key.getValue() != InputConstants.UNKNOWN.getValue();
    }

    public String getName() {
        return this.name;
    }

    public InputConstants.Key getDefaultKey() {
        return this.defaultKey;
    }

    public void setKey(InputConstants.Key key) {
        this.key = key;
    }

    @Override
    public int compareTo(KeyMapping keyMapping) {
        if (this.category == keyMapping.category) {
            if (this.order == keyMapping.order) {
                return I18n.get(this.name, new Object[0]).compareTo(I18n.get(keyMapping.name, new Object[0]));
            }
            return Integer.compare(this.order, keyMapping.order);
        }
        return Integer.compare(Category.SORT_ORDER.indexOf((Object)this.category), Category.SORT_ORDER.indexOf((Object)keyMapping.category));
    }

    public static Supplier<Component> createNameSupplier(String string) {
        KeyMapping keyMapping = ALL.get(string);
        if (keyMapping == null) {
            return () -> Component.translatable(string);
        }
        return keyMapping::getTranslatedKeyMessage;
    }

    public boolean same(KeyMapping keyMapping) {
        return this.key.equals(keyMapping.key);
    }

    public boolean isUnbound() {
        return this.key.equals(InputConstants.UNKNOWN);
    }

    public boolean matches(KeyEvent keyEvent) {
        if (keyEvent.key() == InputConstants.UNKNOWN.getValue()) {
            return this.key.getType() == InputConstants.Type.SCANCODE && this.key.getValue() == keyEvent.scancode();
        }
        return this.key.getType() == InputConstants.Type.KEYSYM && this.key.getValue() == keyEvent.key();
    }

    public boolean matchesMouse(MouseButtonEvent mouseButtonEvent) {
        return this.key.getType() == InputConstants.Type.MOUSE && this.key.getValue() == mouseButtonEvent.button();
    }

    public Component getTranslatedKeyMessage() {
        return this.key.getDisplayName();
    }

    public boolean isDefault() {
        return this.key.equals(this.defaultKey);
    }

    public String saveString() {
        return this.key.getName();
    }

    public void setDown(boolean bl) {
        this.isDown = bl;
    }

    private void registerMapping(InputConstants.Key key2) {
        MAP.computeIfAbsent(key2, key -> new ArrayList()).add(this);
    }

    public static @Nullable KeyMapping get(String string) {
        return ALL.get(string);
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((KeyMapping)object);
    }

    @Environment(value=EnvType.CLIENT)
    public record Category(Identifier id) {
        static final List<Category> SORT_ORDER = new ArrayList<Category>();
        public static final Category MOVEMENT = Category.register("movement");
        public static final Category MISC = Category.register("misc");
        public static final Category MULTIPLAYER = Category.register("multiplayer");
        public static final Category GAMEPLAY = Category.register("gameplay");
        public static final Category INVENTORY = Category.register("inventory");
        public static final Category CREATIVE = Category.register("creative");
        public static final Category SPECTATOR = Category.register("spectator");
        public static final Category DEBUG = Category.register("debug");

        private static Category register(String string) {
            return Category.register(Identifier.withDefaultNamespace(string));
        }

        public static Category register(Identifier identifier) {
            Category category = new Category(identifier);
            if (SORT_ORDER.contains((Object)category)) {
                throw new IllegalArgumentException(String.format(Locale.ROOT, "Category '%s' is already registered.", identifier));
            }
            SORT_ORDER.add(category);
            return category;
        }

        public Component label() {
            return Component.translatable(this.id.toLanguageKey("key.category"));
        }
    }
}


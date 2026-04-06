/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.BanDetails
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TitleScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("narrator.screen.title");
    private static final Component COPYRIGHT_TEXT = Component.translatable("title.credits");
    private static final String DEMO_LEVEL_ID = "Demo_World";
    private @Nullable SplashRenderer splash;
    private @Nullable RealmsNotificationsScreen realmsNotificationsScreen;
    private boolean fading;
    private long fadeInStart;
    private final LogoRenderer logoRenderer;

    public TitleScreen() {
        this(false);
    }

    public TitleScreen(boolean bl) {
        this(bl, null);
    }

    public TitleScreen(boolean bl, @Nullable LogoRenderer logoRenderer) {
        super(TITLE);
        this.fading = bl;
        this.logoRenderer = (LogoRenderer)Objects.requireNonNullElseGet((Object)logoRenderer, () -> new LogoRenderer(false));
    }

    private boolean realmsNotificationsEnabled() {
        return this.realmsNotificationsScreen != null;
    }

    @Override
    public void tick() {
        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.tick();
        }
    }

    public static void registerTextures(TextureManager textureManager) {
        textureManager.registerForNextReload(LogoRenderer.MINECRAFT_LOGO);
        textureManager.registerForNextReload(LogoRenderer.MINECRAFT_EDITION);
        textureManager.registerForNextReload(PanoramaRenderer.PANORAMA_OVERLAY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        if (this.splash == null) {
            this.splash = this.minecraft.getSplashManager().getSplash();
        }
        int i = this.font.width(COPYRIGHT_TEXT);
        int j = this.width - i - 2;
        int k = 24;
        int l = this.height / 4 + 48;
        l = this.minecraft.isDemo() ? this.createDemoMenuOptions(l, 24) : this.createNormalMenuOptions(l, 24);
        l = this.createTestWorldButton(l, 24);
        SpriteIconButton spriteIconButton = this.addRenderableWidget(CommonButtons.language(20, button -> this.minecraft.setScreen(new LanguageSelectScreen((Screen)this, this.minecraft.options, this.minecraft.getLanguageManager())), true));
        spriteIconButton.setPosition(this.width / 2 - 124, l += 36);
        this.addRenderableWidget(Button.builder(Component.translatable("menu.options"), button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))).bounds(this.width / 2 - 100, l, 98, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), button -> this.minecraft.stop()).bounds(this.width / 2 + 2, l, 98, 20).build());
        SpriteIconButton spriteIconButton2 = this.addRenderableWidget(CommonButtons.accessibility(20, button -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), true));
        spriteIconButton2.setPosition(this.width / 2 + 104, l);
        this.addRenderableWidget(new PlainTextButton(j, this.height - 10, i, 10, COPYRIGHT_TEXT, button -> this.minecraft.setScreen(new CreditsAndAttributionScreen(this)), this.font));
        if (this.realmsNotificationsScreen == null) {
            this.realmsNotificationsScreen = new RealmsNotificationsScreen();
        }
        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.init(this.width, this.height);
        }
    }

    private int createTestWorldButton(int i, int j) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            this.addRenderableWidget(Button.builder(Component.literal("Create Test World"), button -> CreateWorldScreen.testWorld(this.minecraft, () -> this.minecraft.setScreen(this))).bounds(this.width / 2 - 100, i += j, 200, 20).build());
        }
        return i;
    }

    private int createNormalMenuOptions(int i, int j) {
        this.addRenderableWidget(Button.builder(Component.translatable("menu.singleplayer"), button -> this.minecraft.setScreen(new SelectWorldScreen(this))).bounds(this.width / 2 - 100, i, 200, 20).build());
        Component component = this.getMultiplayerDisabledReason();
        boolean bl = component == null;
        Tooltip tooltip = component != null ? Tooltip.create(component) : null;
        i += j;
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"menu.multiplayer"), (Button.OnPress)(Button.OnPress)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/components/Button;)V, method_19860(net.minecraft.client.gui.components.Button ), (Lnet/minecraft/client/gui/components/Button;)V)((TitleScreen)this)).bounds((int)(this.width / 2 - 100), (int)v0, (int)200, (int)20).tooltip((Tooltip)tooltip).build()).active = bl;
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"menu.online"), (Button.OnPress)(Button.OnPress)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/components/Button;)V, method_55814(net.minecraft.client.gui.components.Button ), (Lnet/minecraft/client/gui/components/Button;)V)((TitleScreen)this)).bounds((int)(this.width / 2 - 100), (int)v1, (int)200, (int)20).tooltip((Tooltip)tooltip).build()).active = bl;
        return i += j;
    }

    private @Nullable Component getMultiplayerDisabledReason() {
        if (this.minecraft.allowsMultiplayer()) {
            return null;
        }
        if (this.minecraft.isNameBanned()) {
            return Component.translatable("title.multiplayer.disabled.banned.name");
        }
        BanDetails banDetails = this.minecraft.multiplayerBan();
        if (banDetails != null) {
            if (banDetails.expires() != null) {
                return Component.translatable("title.multiplayer.disabled.banned.temporary");
            }
            return Component.translatable("title.multiplayer.disabled.banned.permanent");
        }
        return Component.translatable("title.multiplayer.disabled");
    }

    private int createDemoMenuOptions(int i, int j) {
        boolean bl = this.checkDemoWorldPresence();
        this.addRenderableWidget(Button.builder(Component.translatable("menu.playdemo"), button -> {
            if (bl) {
                this.minecraft.createWorldOpenFlows().openWorld(DEMO_LEVEL_ID, () -> this.minecraft.setScreen(this));
            } else {
                this.minecraft.createWorldOpenFlows().createFreshLevel(DEMO_LEVEL_ID, MinecraftServer.DEMO_SETTINGS, WorldOptions.DEMO_OPTIONS, WorldPresets::createNormalWorldDimensions, this);
            }
        }).bounds(this.width / 2 - 100, i, 200, 20).build());
        Button button2 = this.addRenderableWidget(Button.builder(Component.translatable("menu.resetdemo"), button -> {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(DEMO_LEVEL_ID);){
                if (levelStorageAccess.hasWorldData()) {
                    this.minecraft.setScreen(new ConfirmScreen(this::confirmDemo, Component.translatable("selectWorld.deleteQuestion"), Component.translatable("selectWorld.deleteWarning", MinecraftServer.DEMO_SETTINGS.levelName()), Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
                }
            }
            catch (IOException iOException) {
                SystemToast.onWorldAccessFailure(this.minecraft, DEMO_LEVEL_ID);
                LOGGER.warn("Failed to access demo world", (Throwable)iOException);
            }
        }).bounds(this.width / 2 - 100, i += j, 200, 20).build());
        button2.active = bl;
        return i;
    }

    private boolean checkDemoWorldPresence() {
        boolean bl;
        block8: {
            LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(DEMO_LEVEL_ID);
            try {
                bl = levelStorageAccess.hasWorldData();
                if (levelStorageAccess == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (levelStorageAccess != null) {
                        try {
                            levelStorageAccess.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException iOException) {
                    SystemToast.onWorldAccessFailure(this.minecraft, DEMO_LEVEL_ID);
                    LOGGER.warn("Failed to read demo world data", (Throwable)iOException);
                    return false;
                }
            }
            levelStorageAccess.close();
        }
        return bl;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }
        float g = 1.0f;
        if (this.fading) {
            float h = (float)(Util.getMillis() - this.fadeInStart) / 2000.0f;
            if (h > 1.0f) {
                this.fading = false;
            } else {
                h = Mth.clamp(h, 0.0f, 1.0f);
                g = Mth.clampedMap(h, 0.5f, 1.0f, 0.0f, 1.0f);
            }
            this.fadeWidgets(g);
        }
        this.renderPanorama(guiGraphics, f);
        super.render(guiGraphics, i, j, f);
        this.logoRenderer.renderLogo(guiGraphics, this.width, this.logoRenderer.keepLogoThroughFade() ? 1.0f : g);
        if (this.splash != null && !this.minecraft.options.hideSplashTexts().get().booleanValue()) {
            this.splash.render(guiGraphics, this.width, this.font, g);
        }
        String string = "Minecraft " + SharedConstants.getCurrentVersion().name();
        string = this.minecraft.isDemo() ? string + " Demo" : string + (String)("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            string = string + I18n.get("menu.modded", new Object[0]);
        }
        guiGraphics.drawString(this.font, string, 2, this.height - 10, ARGB.white(g));
        if (this.realmsNotificationsEnabled() && g >= 1.0f) {
            this.realmsNotificationsScreen.render(guiGraphics, i, j, f);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (super.mouseClicked(mouseButtonEvent, bl)) {
            return true;
        }
        return this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public void removed() {
        if (this.realmsNotificationsScreen != null) {
            this.realmsNotificationsScreen.removed();
        }
    }

    @Override
    public void added() {
        super.added();
        if (this.realmsNotificationsScreen != null) {
            this.realmsNotificationsScreen.added();
        }
    }

    private void confirmDemo(boolean bl) {
        if (bl) {
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(DEMO_LEVEL_ID);){
                levelStorageAccess.deleteLevel();
            }
            catch (IOException iOException) {
                SystemToast.onWorldDeleteFailure(this.minecraft, DEMO_LEVEL_ID);
                LOGGER.warn("Failed to delete demo world", (Throwable)iOException);
            }
        }
        this.minecraft.setScreen(this);
    }

    @Override
    public boolean canInterruptWithAnotherScreen() {
        return true;
    }

    private /* synthetic */ void method_55814(Button button) {
        this.minecraft.setScreen(new RealmsMainScreen(this));
    }

    private /* synthetic */ void method_19860(Button button) {
        Screen screen = this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this);
        this.minecraft.setScreen(screen);
    }
}


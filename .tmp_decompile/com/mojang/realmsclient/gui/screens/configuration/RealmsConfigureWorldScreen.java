/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.screens.configuration;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.client.RealmsError;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.PreferredRegionsDto;
import com.mojang.realmsclient.dto.RealmsRegion;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsSlot;
import com.mojang.realmsclient.dto.RegionDataDto;
import com.mojang.realmsclient.dto.RegionSelectionPreference;
import com.mojang.realmsclient.dto.RegionSelectionPreferenceDto;
import com.mojang.realmsclient.dto.ServiceQuality;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.configuration.RealmsConfigurationTab;
import com.mojang.realmsclient.gui.screens.configuration.RealmsPlayersTab;
import com.mojang.realmsclient.gui.screens.configuration.RealmsSettingsTab;
import com.mojang.realmsclient.gui.screens.configuration.RealmsSubscriptionTab;
import com.mojang.realmsclient.gui.screens.configuration.RealmsWorldsTab;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.CloseServerTask;
import com.mojang.realmsclient.util.task.OpenServerTask;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.LoadingTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.StringUtil;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsConfigureWorldScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component PLAY_TEXT = Component.translatable("mco.selectServer.play");
    private final RealmsMainScreen lastScreen;
    private @Nullable RealmsServer serverData;
    private @Nullable PreferredRegionsDto regions;
    private final Map<RealmsRegion, ServiceQuality> regionServiceQuality = new LinkedHashMap<RealmsRegion, ServiceQuality>();
    private final long serverId;
    private boolean stateChanged;
    private final TabManager tabManager = new TabManager(guiEventListener -> {
        AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
    }, guiEventListener -> this.removeWidget((GuiEventListener)guiEventListener), this::onTabSelected, this::onTabDeselected);
    private @Nullable Button playButton;
    private @Nullable TabNavigationBar tabNavigationBar;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public RealmsConfigureWorldScreen(RealmsMainScreen realmsMainScreen, long l, @Nullable RealmsServer realmsServer, @Nullable PreferredRegionsDto preferredRegionsDto) {
        super(Component.empty());
        this.lastScreen = realmsMainScreen;
        this.serverId = l;
        this.serverData = realmsServer;
        this.regions = preferredRegionsDto;
    }

    public RealmsConfigureWorldScreen(RealmsMainScreen realmsMainScreen, long l) {
        this(realmsMainScreen, l, null, null);
    }

    @Override
    public void init() {
        if (this.serverData == null) {
            this.fetchServerData(this.serverId);
        }
        if (this.regions == null) {
            this.fetchRegionData();
        }
        MutableComponent component = Component.translatable("mco.configure.world.loading");
        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(new LoadingTab(this.getFont(), RealmsWorldsTab.TITLE, component), new LoadingTab(this.getFont(), RealmsPlayersTab.TITLE, component), new LoadingTab(this.getFont(), RealmsSubscriptionTab.TITLE, component), new LoadingTab(this.getFont(), RealmsSettingsTab.TITLE, component)).build();
        this.tabNavigationBar.setTabActiveState(3, false);
        this.addRenderableWidget(this.tabNavigationBar);
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        this.playButton = linearLayout.addChild(Button.builder(PLAY_TEXT, button -> {
            this.onClose();
            RealmsMainScreen.play(this.serverData, this);
        }).width(150).build());
        this.playButton.active = false;
        linearLayout.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).build());
        this.layout.visitWidgets(abstractWidget -> {
            abstractWidget.setTabOrderGroup(1);
            this.addRenderableWidget(abstractWidget);
        });
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
        if (this.serverData != null && this.regions != null) {
            this.onRealmsDataFetched();
        }
    }

    private void onTabSelected(Tab tab) {
        if (this.serverData != null && tab instanceof RealmsConfigurationTab) {
            RealmsConfigurationTab realmsConfigurationTab = (RealmsConfigurationTab)((Object)tab);
            realmsConfigurationTab.onSelected(this.serverData);
        }
    }

    private void onTabDeselected(Tab tab) {
        if (this.serverData != null && tab instanceof RealmsConfigurationTab) {
            RealmsConfigurationTab realmsConfigurationTab = (RealmsConfigurationTab)((Object)tab);
            realmsConfigurationTab.onDeselected(this.serverData);
        }
    }

    public int getContentHeight() {
        return this.layout.getContentHeight();
    }

    public int getHeaderHeight() {
        return this.layout.getHeaderHeight();
    }

    public Screen getLastScreen() {
        return this.lastScreen;
    }

    public Screen createErrorScreen(RealmsServiceException realmsServiceException) {
        return new RealmsGenericErrorScreen(realmsServiceException, (Screen)this.lastScreen);
    }

    @Override
    public void repositionElements() {
        if (this.tabNavigationBar == null) {
            return;
        }
        this.tabNavigationBar.setWidth(this.width);
        this.tabNavigationBar.arrangeElements();
        int i = this.tabNavigationBar.getRectangle().bottom();
        ScreenRectangle screenRectangle = new ScreenRectangle(0, i, this.width, this.height - this.layout.getFooterHeight() - i);
        this.tabManager.setTabArea(screenRectangle);
        this.layout.setHeaderHeight(i);
        this.layout.arrangeElements();
    }

    private void updateButtonStates() {
        if (this.serverData != null && this.playButton != null) {
            this.playButton.active = this.serverData.shouldPlayButtonBeActive();
            if (!this.playButton.active && this.serverData.state == RealmsServer.State.CLOSED) {
                this.playButton.setTooltip(Tooltip.create(RealmsServer.WORLD_CLOSED_COMPONENT));
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight() - 2, 0.0f, 0.0f, this.width, 2, 32, 2);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (this.tabNavigationBar.keyPressed(keyEvent)) {
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.TAB_HEADER_BACKGROUND, 0, 0, 0.0f, 0.0f, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderMenuBackground(guiGraphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public void onClose() {
        Tab tab;
        if (this.serverData != null && (tab = this.tabManager.getCurrentTab()) instanceof RealmsConfigurationTab) {
            RealmsConfigurationTab realmsConfigurationTab = (RealmsConfigurationTab)((Object)tab);
            realmsConfigurationTab.onDeselected(this.serverData);
        }
        this.minecraft.setScreen(this.lastScreen);
        if (this.stateChanged) {
            this.lastScreen.resetScreen();
        }
    }

    public void fetchRegionData() {
        RealmsUtil.supplyAsync(RealmsClient::getPreferredRegionSelections, RealmsUtil.openScreenAndLogOnFailure(this::createErrorScreen, "Couldn't get realms region data")).thenAcceptAsync(preferredRegionsDto -> {
            this.regions = preferredRegionsDto;
            this.onRealmsDataFetched();
        }, (Executor)this.minecraft);
    }

    public void fetchServerData(long l) {
        RealmsUtil.supplyAsync(realmsClient -> realmsClient.getOwnRealm(l), RealmsUtil.openScreenAndLogOnFailure(this::createErrorScreen, "Couldn't get own world")).thenAcceptAsync(realmsServer -> {
            this.serverData = realmsServer;
            this.onRealmsDataFetched();
        }, (Executor)this.minecraft);
    }

    private void onRealmsDataFetched() {
        if (this.serverData == null || this.regions == null) {
            return;
        }
        this.regionServiceQuality.clear();
        for (RegionDataDto regionDataDto : this.regions.regionData()) {
            if (regionDataDto.region() == RealmsRegion.INVALID_REGION) continue;
            this.regionServiceQuality.put(regionDataDto.region(), regionDataDto.serviceQuality());
        }
        int i = -1;
        if (this.tabNavigationBar != null) {
            i = this.tabNavigationBar.getTabs().indexOf(this.tabManager.getCurrentTab());
        }
        if (this.tabNavigationBar != null) {
            this.removeWidget(this.tabNavigationBar);
        }
        this.tabNavigationBar = this.addRenderableWidget(TabNavigationBar.builder(this.tabManager, this.width).addTabs(new RealmsWorldsTab(this, Objects.requireNonNull(this.minecraft), this.serverData), new RealmsPlayersTab(this, this.minecraft, this.serverData), new RealmsSubscriptionTab(this, this.minecraft, this.serverData), new RealmsSettingsTab(this, this.minecraft, this.serverData, this.regionServiceQuality)).build());
        this.setFocused(this.tabNavigationBar);
        if (i != -1) {
            this.tabNavigationBar.selectTab(i, false);
        }
        this.tabNavigationBar.setTabActiveState(3, !this.serverData.expired);
        if (this.serverData.expired) {
            this.tabNavigationBar.setTabTooltip(3, Tooltip.create(Component.translatable("mco.configure.world.settings.expired")));
        } else {
            this.tabNavigationBar.setTabTooltip(3, null);
        }
        this.updateButtonStates();
        this.repositionElements();
    }

    public void saveSlotSettings(RealmsSlot realmsSlot) {
        RealmsSlot realmsSlot2 = this.serverData.slots.get(this.serverData.activeSlot);
        realmsSlot.options.templateId = realmsSlot2.options.templateId;
        realmsSlot.options.templateImage = realmsSlot2.options.templateImage;
        RealmsClient realmsClient = RealmsClient.getOrCreate();
        try {
            if (this.serverData.activeSlot != realmsSlot.slotId) {
                throw new RealmsServiceException(RealmsError.CustomError.configurationError());
            }
            realmsClient.updateSlot(this.serverData.id, realmsSlot.slotId, realmsSlot.options, realmsSlot.settings);
            this.serverData.slots.put(this.serverData.activeSlot, realmsSlot);
            if (realmsSlot.options.gameMode != realmsSlot2.options.gameMode || realmsSlot.isHardcore() != realmsSlot2.isHardcore()) {
                RealmsMainScreen.refreshServerList();
            }
            this.stateChanged();
        }
        catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't save slot settings", (Throwable)realmsServiceException);
            this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsServiceException, (Screen)this));
            return;
        }
        this.minecraft.setScreen(this);
    }

    public void saveSettings(String string, String string2, RegionSelectionPreference regionSelectionPreference, @Nullable RealmsRegion realmsRegion) {
        String string3 = StringUtil.isBlank(string2) ? "" : string2;
        String string4 = StringUtil.isBlank(string) ? "" : string;
        RealmsClient realmsClient = RealmsClient.getOrCreate();
        try {
            RealmsSlot realmsSlot = this.serverData.slots.get(this.serverData.activeSlot);
            RealmsRegion realmsRegion2 = regionSelectionPreference == RegionSelectionPreference.MANUAL ? realmsRegion : null;
            RegionSelectionPreferenceDto regionSelectionPreferenceDto = new RegionSelectionPreferenceDto(regionSelectionPreference, realmsRegion2);
            realmsClient.updateConfiguration(this.serverData.id, string4, string3, regionSelectionPreferenceDto, realmsSlot.slotId, realmsSlot.options, realmsSlot.settings);
            this.serverData.regionSelectionPreference = regionSelectionPreferenceDto;
            this.serverData.name = string;
            this.serverData.motd = string3;
            this.stateChanged();
        }
        catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't save settings", (Throwable)realmsServiceException);
            this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsServiceException, (Screen)this));
            return;
        }
        this.minecraft.setScreen(this);
    }

    public void openTheWorld(boolean bl) {
        RealmsConfigureWorldScreen realmsConfigureWorldScreen = this.getNewScreenWithKnownData(this.serverData);
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.getNewScreen(), new OpenServerTask(this.serverData, realmsConfigureWorldScreen, bl, this.minecraft)));
    }

    public void closeTheWorld() {
        RealmsConfigureWorldScreen realmsConfigureWorldScreen = this.getNewScreenWithKnownData(this.serverData);
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.getNewScreen(), new CloseServerTask(this.serverData, realmsConfigureWorldScreen)));
    }

    public void stateChanged() {
        this.stateChanged = true;
        if (this.tabNavigationBar != null) {
            for (Tab tab : this.tabNavigationBar.getTabs()) {
                if (!(tab instanceof RealmsConfigurationTab)) continue;
                RealmsConfigurationTab realmsConfigurationTab = (RealmsConfigurationTab)((Object)tab);
                realmsConfigurationTab.updateData(this.serverData);
            }
        }
    }

    public boolean invitePlayer(long l, String string) {
        RealmsClient realmsClient = RealmsClient.getOrCreate();
        try {
            List<PlayerInfo> list = realmsClient.invite(l, string);
            if (this.serverData != null) {
                this.serverData.players = list;
            } else {
                this.serverData = realmsClient.getOwnRealm(l);
            }
            this.stateChanged();
        }
        catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't invite user", (Throwable)realmsServiceException);
            return false;
        }
        return true;
    }

    public RealmsConfigureWorldScreen getNewScreen() {
        RealmsConfigureWorldScreen realmsConfigureWorldScreen = new RealmsConfigureWorldScreen(this.lastScreen, this.serverId);
        realmsConfigureWorldScreen.stateChanged = this.stateChanged;
        return realmsConfigureWorldScreen;
    }

    public RealmsConfigureWorldScreen getNewScreenWithKnownData(RealmsServer realmsServer) {
        RealmsConfigureWorldScreen realmsConfigureWorldScreen = new RealmsConfigureWorldScreen(this.lastScreen, this.serverId, realmsServer, this.regions);
        realmsConfigureWorldScreen.stateChanged = this.stateChanged;
        return realmsConfigureWorldScreen;
    }
}


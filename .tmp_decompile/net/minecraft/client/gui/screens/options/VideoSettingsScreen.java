/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.options;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.UnsupportedGraphicsWarningScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class VideoSettingsScreen
extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("options.videoTitle");
    private static final Component IMPROVED_TRANSPARENCY = Component.translatable("options.improvedTransparency").withStyle(ChatFormatting.ITALIC);
    private static final Component WARNING_MESSAGE = Component.translatable("options.graphics.warning.message", IMPROVED_TRANSPARENCY, IMPROVED_TRANSPARENCY);
    private static final Component WARNING_TITLE = Component.translatable("options.graphics.warning.title").withStyle(ChatFormatting.RED);
    private static final Component BUTTON_ACCEPT = Component.translatable("options.graphics.warning.accept");
    private static final Component BUTTON_CANCEL = Component.translatable("options.graphics.warning.cancel");
    private static final Component DISPLAY_HEADER = Component.translatable("options.video.display.header");
    private static final Component QUALITY_HEADER = Component.translatable("options.video.quality.header");
    private static final Component PREFERENCES_HEADER = Component.translatable("options.video.preferences.header");
    private final GpuWarnlistManager gpuWarnlistManager;
    private final int oldMipmaps;
    private final int oldAnisotropyBit;
    private final TextureFilteringMethod oldTextureFiltering;

    private static OptionInstance<?>[] qualityOptions(Options options) {
        return new OptionInstance[]{options.biomeBlendRadius(), options.renderDistance(), options.prioritizeChunkUpdates(), options.simulationDistance(), options.ambientOcclusion(), options.cloudStatus(), options.particles(), options.mipmapLevels(), options.entityShadows(), options.entityDistanceScaling(), options.menuBackgroundBlurriness(), options.cloudRange(), options.cutoutLeaves(), options.improvedTransparency(), options.textureFiltering(), options.maxAnisotropyBit(), options.weatherRadius()};
    }

    private static OptionInstance<?>[] displayOptions(Options options) {
        return new OptionInstance[]{options.framerateLimit(), options.enableVsync(), options.inactivityFpsLimit(), options.guiScale(), options.fullscreen(), options.gamma()};
    }

    private static OptionInstance<?>[] preferenceOptions(Options options) {
        return new OptionInstance[]{options.showAutosaveIndicator(), options.vignette(), options.attackIndicator(), options.chunkSectionFadeInTime()};
    }

    public VideoSettingsScreen(Screen screen, Minecraft minecraft, Options options) {
        super(screen, options, TITLE);
        this.gpuWarnlistManager = minecraft.getGpuWarnlistManager();
        this.gpuWarnlistManager.resetWarnings();
        if (options.improvedTransparency().get().booleanValue()) {
            this.gpuWarnlistManager.dismissWarning();
        }
        this.oldMipmaps = options.mipmapLevels().get();
        this.oldAnisotropyBit = options.maxAnisotropyBit().get();
        this.oldTextureFiltering = options.textureFiltering().get();
    }

    @Override
    protected void addOptions() {
        int j;
        int i = -1;
        Window window = this.minecraft.getWindow();
        Monitor monitor = window.findBestMonitor();
        if (monitor == null) {
            j = -1;
        } else {
            Optional<VideoMode> optional = window.getPreferredFullscreenVideoMode();
            j = optional.map(monitor::getVideoModeIndex).orElse(-1);
        }
        OptionInstance<Integer> optionInstance = new OptionInstance<Integer>("options.fullscreen.resolution", OptionInstance.noTooltip(), (component, integer) -> {
            if (monitor == null) {
                return Component.translatable("options.fullscreen.unavailable");
            }
            if (integer == -1) {
                return Options.genericValueLabel(component, Component.translatable("options.fullscreen.current"));
            }
            VideoMode videoMode = monitor.getMode((int)integer);
            return Options.genericValueLabel(component, Component.translatable("options.fullscreen.entry", videoMode.getWidth(), videoMode.getHeight(), videoMode.getRefreshRate(), videoMode.getRedBits() + videoMode.getGreenBits() + videoMode.getBlueBits()));
        }, new OptionInstance.IntRange(-1, monitor != null ? monitor.getModeCount() - 1 : -1), j, integer -> {
            if (monitor == null) {
                return;
            }
            window.setPreferredFullscreenVideoMode(integer == -1 ? Optional.empty() : Optional.of(monitor.getMode((int)integer)));
        });
        this.list.addHeader(DISPLAY_HEADER);
        this.list.addBig(optionInstance);
        this.list.addSmall(VideoSettingsScreen.displayOptions(this.options));
        this.list.addHeader(QUALITY_HEADER);
        this.list.addBig(this.options.graphicsPreset());
        this.list.addSmall(VideoSettingsScreen.qualityOptions(this.options));
        this.list.addHeader(PREFERENCES_HEADER);
        this.list.addSmall(VideoSettingsScreen.preferenceOptions(this.options));
    }

    @Override
    public void tick() {
        AbstractWidget abstractWidget;
        if (this.list != null && (abstractWidget = this.list.findOption(this.options.maxAnisotropyBit())) instanceof AbstractSliderButton) {
            AbstractSliderButton abstractSliderButton = (AbstractSliderButton)abstractWidget;
            abstractSliderButton.active = this.options.textureFiltering().get() == TextureFilteringMethod.ANISOTROPIC;
        }
        super.tick();
    }

    @Override
    public void onClose() {
        this.minecraft.getWindow().changeFullscreenVideoMode();
        super.onClose();
    }

    @Override
    public void removed() {
        if (this.options.mipmapLevels().get() != this.oldMipmaps || this.options.maxAnisotropyBit().get() != this.oldAnisotropyBit || this.options.textureFiltering().get() != this.oldTextureFiltering) {
            this.minecraft.updateMaxMipLevel(this.options.mipmapLevels().get());
            this.minecraft.delayTextureReload();
        }
        super.removed();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (super.mouseClicked(mouseButtonEvent, bl)) {
            if (this.gpuWarnlistManager.isShowingWarning()) {
                String string3;
                String string2;
                ArrayList list = Lists.newArrayList((Object[])new Component[]{WARNING_MESSAGE, CommonComponents.NEW_LINE});
                String string = this.gpuWarnlistManager.getRendererWarnings();
                if (string != null) {
                    list.add(CommonComponents.NEW_LINE);
                    list.add(Component.translatable("options.graphics.warning.renderer", string).withStyle(ChatFormatting.GRAY));
                }
                if ((string2 = this.gpuWarnlistManager.getVendorWarnings()) != null) {
                    list.add(CommonComponents.NEW_LINE);
                    list.add(Component.translatable("options.graphics.warning.vendor", string2).withStyle(ChatFormatting.GRAY));
                }
                if ((string3 = this.gpuWarnlistManager.getVersionWarnings()) != null) {
                    list.add(CommonComponents.NEW_LINE);
                    list.add(Component.translatable("options.graphics.warning.version", string3).withStyle(ChatFormatting.GRAY));
                }
                this.minecraft.setScreen(new UnsupportedGraphicsWarningScreen(WARNING_TITLE, list, (ImmutableList<UnsupportedGraphicsWarningScreen.ButtonOption>)ImmutableList.of((Object)new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_ACCEPT, button -> {
                    this.options.improvedTransparency().set(true);
                    Minecraft.getInstance().levelRenderer.allChanged();
                    this.gpuWarnlistManager.dismissWarning();
                    this.minecraft.setScreen(this);
                }), (Object)new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_CANCEL, button -> {
                    this.gpuWarnlistManager.dismissWarning();
                    this.options.improvedTransparency().set(false);
                    this.updateTransparencyButton();
                    this.minecraft.setScreen(this);
                }))));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (this.minecraft.hasControlDown()) {
            OptionInstance<Integer> optionInstance = this.options.guiScale();
            OptionInstance.ValueSet<Integer> valueSet = optionInstance.values();
            if (valueSet instanceof OptionInstance.ClampingLazyMaxIntRange) {
                CycleButton cycleButton;
                OptionInstance.ClampingLazyMaxIntRange clampingLazyMaxIntRange = (OptionInstance.ClampingLazyMaxIntRange)valueSet;
                int i = optionInstance.get();
                int j = i == 0 ? clampingLazyMaxIntRange.maxInclusive() + 1 : i;
                int k = j + (int)Math.signum(g);
                if (k != 0 && k <= clampingLazyMaxIntRange.maxInclusive() && k >= clampingLazyMaxIntRange.minInclusive() && (cycleButton = (CycleButton)this.list.findOption(optionInstance)) != null) {
                    optionInstance.set(k);
                    cycleButton.setValue(k);
                    this.list.setScrollAmount(0.0);
                    return true;
                }
            }
            return false;
        }
        return super.mouseScrolled(d, e, f, g);
    }

    public void updateFullscreenButton(boolean bl) {
        AbstractWidget abstractWidget;
        if (this.list != null && (abstractWidget = this.list.findOption(this.options.fullscreen())) != null) {
            CycleButton cycleButton = (CycleButton)abstractWidget;
            cycleButton.setValue(bl);
        }
    }

    public void updateTransparencyButton() {
        OptionInstance<Boolean> optionInstance;
        AbstractWidget abstractWidget;
        if (this.list != null && (abstractWidget = this.list.findOption(optionInstance = this.options.improvedTransparency())) != null) {
            CycleButton cycleButton = (CycleButton)abstractWidget;
            cycleButton.setValue(optionInstance.get());
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldCallback;
import net.minecraft.client.gui.screens.worldselection.DataPackReloadCookie;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.ExperimentsScreen;
import net.minecraft.client.gui.screens.worldselection.InitialWorldCreationOptions;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.SwitchGrid;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContextMapper;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.FileUtil;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class CreateWorldScreen
extends Screen {
    private static final int GROUP_BOTTOM = 1;
    private static final int TAB_COLUMN_WIDTH = 210;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEMP_WORLD_PREFIX = "mcworld-";
    static final Component GAME_MODEL_LABEL = Component.translatable("selectWorld.gameMode");
    static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
    static final Component EXPERIMENTS_LABEL = Component.translatable("selectWorld.experiments");
    static final Component ALLOW_COMMANDS_INFO = Component.translatable("selectWorld.allowCommands.info");
    private static final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
    private static final int HORIZONTAL_BUTTON_SPACING = 10;
    private static final int VERTICAL_BUTTON_SPACING = 8;
    public static final Identifier TAB_HEADER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/tab_header_background.png");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    final WorldCreationUiState uiState;
    private final TabManager tabManager = new TabManager(guiEventListener -> {
        AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
    }, guiEventListener -> this.removeWidget((GuiEventListener)guiEventListener));
    private boolean recreated;
    private final DirectoryValidator packValidator;
    private final CreateWorldCallback createWorldCallback;
    private final Runnable onClose;
    private @Nullable Path tempDataPackDir;
    private @Nullable PackRepository tempDataPackRepository;
    private @Nullable TabNavigationBar tabNavigationBar;

    public static void openFresh(Minecraft minecraft, Runnable runnable) {
        CreateWorldScreen.openFresh(minecraft, runnable, (createWorldScreen, layeredRegistryAccess, primaryLevelData, path) -> createWorldScreen.createNewWorld(layeredRegistryAccess, primaryLevelData));
    }

    public static void openFresh(Minecraft minecraft, Runnable runnable, CreateWorldCallback createWorldCallback) {
        WorldCreationContextMapper worldCreationContextMapper = (reloadableServerResources, layeredRegistryAccess, dataPackReloadCookie) -> new WorldCreationContext(dataPackReloadCookie.worldGenSettings(), layeredRegistryAccess, reloadableServerResources, dataPackReloadCookie.dataConfiguration());
        Function<WorldLoader.DataLoadContext, WorldGenSettings> function = dataLoadContext -> new WorldGenSettings(WorldOptions.defaultWithRandomSeed(), WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen()));
        CreateWorldScreen.openCreateWorldScreen(minecraft, runnable, function, worldCreationContextMapper, WorldPresets.NORMAL, createWorldCallback);
    }

    public static void testWorld(Minecraft minecraft, Runnable runnable) {
        WorldCreationContextMapper worldCreationContextMapper = (reloadableServerResources, layeredRegistryAccess, dataPackReloadCookie) -> new WorldCreationContext(dataPackReloadCookie.worldGenSettings().options(), dataPackReloadCookie.worldGenSettings().dimensions(), layeredRegistryAccess, reloadableServerResources, dataPackReloadCookie.dataConfiguration(), new InitialWorldCreationOptions(WorldCreationUiState.SelectedGameMode.CREATIVE, new GameRuleMap.Builder().set(GameRules.ADVANCE_TIME, false).set(GameRules.ADVANCE_WEATHER, false).set(GameRules.SPAWN_MOBS, false).build(), FlatLevelGeneratorPresets.REDSTONE_READY));
        Function<WorldLoader.DataLoadContext, WorldGenSettings> function = dataLoadContext -> new WorldGenSettings(WorldOptions.testWorldWithRandomSeed(), WorldPresets.createFlatWorldDimensions(dataLoadContext.datapackWorldgen()));
        CreateWorldScreen.openCreateWorldScreen(minecraft, runnable, function, worldCreationContextMapper, WorldPresets.FLAT, (createWorldScreen, layeredRegistryAccess, primaryLevelData, path) -> createWorldScreen.createNewWorld(layeredRegistryAccess, primaryLevelData));
    }

    private static void openCreateWorldScreen(Minecraft minecraft, Runnable runnable, Function<WorldLoader.DataLoadContext, WorldGenSettings> function, WorldCreationContextMapper worldCreationContextMapper, ResourceKey<WorldPreset> resourceKey, CreateWorldCallback createWorldCallback) {
        CreateWorldScreen.queueLoadScreen(minecraft, PREPARING_WORLD_DATA);
        PackRepository packRepository = new PackRepository(new ServerPacksSource(minecraft.directoryValidator()));
        WorldDataConfiguration worldDataConfiguration = SharedConstants.IS_RUNNING_IN_IDE ? new WorldDataConfiguration(new DataPackConfig(List.of((Object)"vanilla", (Object)"tests"), List.of()), FeatureFlags.DEFAULT_FLAGS) : WorldDataConfiguration.DEFAULT;
        WorldLoader.InitConfig initConfig = CreateWorldScreen.createDefaultLoadConfig(packRepository, worldDataConfiguration);
        CompletableFuture<WorldCreationContext> completableFuture = WorldLoader.load(initConfig, dataLoadContext -> new WorldLoader.DataLoadOutput<DataPackReloadCookie>(new DataPackReloadCookie((WorldGenSettings)((Object)((Object)function.apply(dataLoadContext))), dataLoadContext.dataConfiguration()), dataLoadContext.datapackDimensions()), (closeableResourceManager, reloadableServerResources, layeredRegistryAccess, dataPackReloadCookie) -> {
            closeableResourceManager.close();
            return worldCreationContextMapper.apply(reloadableServerResources, layeredRegistryAccess, (DataPackReloadCookie)((Object)dataPackReloadCookie));
        }, Util.backgroundExecutor(), minecraft);
        minecraft.managedBlock(completableFuture::isDone);
        minecraft.setScreen(new CreateWorldScreen(minecraft, runnable, completableFuture.join(), Optional.of(resourceKey), OptionalLong.empty(), createWorldCallback));
    }

    public static CreateWorldScreen createFromExisting(Minecraft minecraft, Runnable runnable, LevelSettings levelSettings, WorldCreationContext worldCreationContext, @Nullable Path path2) {
        CreateWorldScreen createWorldScreen2 = new CreateWorldScreen(minecraft, runnable, worldCreationContext, WorldPresets.fromSettings(worldCreationContext.selectedDimensions()), OptionalLong.of(worldCreationContext.options().seed()), (createWorldScreen, layeredRegistryAccess, primaryLevelData, path) -> createWorldScreen.createNewWorld(layeredRegistryAccess, primaryLevelData));
        createWorldScreen2.recreated = true;
        createWorldScreen2.uiState.setName(levelSettings.levelName());
        createWorldScreen2.uiState.setAllowCommands(levelSettings.allowCommands());
        createWorldScreen2.uiState.setDifficulty(levelSettings.difficulty());
        createWorldScreen2.uiState.getGameRules().setAll(levelSettings.gameRules(), null);
        if (levelSettings.hardcore()) {
            createWorldScreen2.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.HARDCORE);
        } else if (levelSettings.gameType().isSurvival()) {
            createWorldScreen2.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);
        } else if (levelSettings.gameType().isCreative()) {
            createWorldScreen2.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
        }
        createWorldScreen2.tempDataPackDir = path2;
        return createWorldScreen2;
    }

    private CreateWorldScreen(Minecraft minecraft, Runnable runnable, WorldCreationContext worldCreationContext, Optional<ResourceKey<WorldPreset>> optional, OptionalLong optionalLong, CreateWorldCallback createWorldCallback) {
        super(Component.translatable("selectWorld.create"));
        this.onClose = runnable;
        this.packValidator = minecraft.directoryValidator();
        this.createWorldCallback = createWorldCallback;
        this.uiState = new WorldCreationUiState(minecraft.getLevelSource().getBaseDir(), worldCreationContext, optional, optionalLong);
    }

    public WorldCreationUiState getUiState() {
        return this.uiState;
    }

    @Override
    protected void init() {
        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(new GameTab(), new WorldTab(), new MoreTab()).build();
        this.addRenderableWidget(this.tabNavigationBar);
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearLayout.addChild(Button.builder(Component.translatable("selectWorld.create"), button -> this.onCreate()).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.popScreen()).build());
        this.layout.visitWidgets(abstractWidget -> {
            abstractWidget.setTabOrderGroup(1);
            this.addRenderableWidget(abstractWidget);
        });
        this.tabNavigationBar.selectTab(0, false);
        this.uiState.onChanged();
        this.repositionElements();
    }

    @Override
    protected void setInitialFocus() {
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

    private static void queueLoadScreen(Minecraft minecraft, Component component) {
        minecraft.setScreenAndShow(new GenericMessageScreen(component));
    }

    private void onCreate() {
        WorldCreationContext worldCreationContext = this.uiState.getSettings();
        WorldDimensions.Complete complete = worldCreationContext.selectedDimensions().bake(worldCreationContext.datapackDimensions());
        LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = worldCreationContext.worldgenRegistries().replaceFrom(RegistryLayer.DIMENSIONS, complete.dimensionsRegistryAccess());
        Lifecycle lifecycle = FeatureFlags.isExperimental(worldCreationContext.dataConfiguration().enabledFeatures()) ? Lifecycle.experimental() : Lifecycle.stable();
        Lifecycle lifecycle2 = layeredRegistryAccess.compositeAccess().allRegistriesLifecycle();
        Lifecycle lifecycle3 = lifecycle2.add(lifecycle);
        boolean bl = !this.recreated && lifecycle2 == Lifecycle.stable();
        LevelSettings levelSettings = this.createLevelSettings(complete.specialWorldProperty() == PrimaryLevelData.SpecialWorldProperty.DEBUG);
        PrimaryLevelData primaryLevelData = new PrimaryLevelData(levelSettings, this.uiState.getSettings().options(), complete.specialWorldProperty(), lifecycle3);
        WorldOpenFlows.confirmWorldCreation(this.minecraft, this, lifecycle3, () -> this.createWorldAndCleanup(layeredRegistryAccess, primaryLevelData), bl);
    }

    private void createWorldAndCleanup(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PrimaryLevelData primaryLevelData) {
        boolean bl = this.createWorldCallback.create(this, layeredRegistryAccess, primaryLevelData, this.tempDataPackDir);
        this.removeTempDataPackDir();
        if (!bl) {
            this.popScreen();
        }
    }

    private boolean createNewWorld(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, WorldData worldData) {
        String string = this.uiState.getTargetFolder();
        WorldCreationContext worldCreationContext = this.uiState.getSettings();
        CreateWorldScreen.queueLoadScreen(this.minecraft, PREPARING_WORLD_DATA);
        Optional<LevelStorageSource.LevelStorageAccess> optional = CreateWorldScreen.createNewWorldDirectory(this.minecraft, string, this.tempDataPackDir);
        if (optional.isEmpty()) {
            SystemToast.onPackCopyFailure(this.minecraft, string);
            return false;
        }
        this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings(optional.get(), worldCreationContext.dataPackResources(), layeredRegistryAccess, worldData);
        return true;
    }

    private LevelSettings createLevelSettings(boolean bl) {
        String string = this.uiState.getName().trim();
        if (bl) {
            GameRules gameRules = new GameRules(WorldDataConfiguration.DEFAULT.enabledFeatures());
            gameRules.set(GameRules.ADVANCE_TIME, false, null);
            return new LevelSettings(string, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, gameRules, WorldDataConfiguration.DEFAULT);
        }
        return new LevelSettings(string, this.uiState.getGameMode().gameType, this.uiState.isHardcore(), this.uiState.getDifficulty(), this.uiState.isAllowCommands(), this.uiState.getGameRules(), this.uiState.getSettings().dataConfiguration());
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (this.tabNavigationBar.keyPressed(keyEvent)) {
            return true;
        }
        if (super.keyPressed(keyEvent)) {
            return true;
        }
        if (keyEvent.isConfirmation()) {
            this.onCreate();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        this.popScreen();
    }

    public void popScreen() {
        this.onClose.run();
        this.removeTempDataPackDir();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight() - 2, 0.0f, 0.0f, this.width, 2, 32, 2);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TAB_HEADER_BACKGROUND, 0, 0, 0.0f, 0.0f, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderMenuBackground(guiGraphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    private @Nullable Path getOrCreateTempDataPackDir() {
        if (this.tempDataPackDir == null) {
            try {
                this.tempDataPackDir = Files.createTempDirectory(TEMP_WORLD_PREFIX, new FileAttribute[0]);
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to create temporary dir", (Throwable)iOException);
                SystemToast.onPackCopyFailure(this.minecraft, this.uiState.getTargetFolder());
                this.popScreen();
            }
        }
        return this.tempDataPackDir;
    }

    void openExperimentsScreen(WorldDataConfiguration worldDataConfiguration) {
        Pair<Path, PackRepository> pair = this.getDataPackSelectionSettings(worldDataConfiguration);
        if (pair != null) {
            this.minecraft.setScreen(new ExperimentsScreen(this, (PackRepository)pair.getSecond(), packRepository -> this.tryApplyNewDataPacks((PackRepository)packRepository, false, this::openExperimentsScreen)));
        }
    }

    void openDataPackSelectionScreen(WorldDataConfiguration worldDataConfiguration) {
        Pair<Path, PackRepository> pair = this.getDataPackSelectionSettings(worldDataConfiguration);
        if (pair != null) {
            this.minecraft.setScreen(new PackSelectionScreen((PackRepository)pair.getSecond(), packRepository -> this.tryApplyNewDataPacks((PackRepository)packRepository, true, this::openDataPackSelectionScreen), (Path)pair.getFirst(), Component.translatable("dataPack.title")));
        }
    }

    private void tryApplyNewDataPacks(PackRepository packRepository, boolean bl2, Consumer<WorldDataConfiguration> consumer) {
        List list2;
        ImmutableList list = ImmutableList.copyOf(packRepository.getSelectedIds());
        WorldDataConfiguration worldDataConfiguration = new WorldDataConfiguration(new DataPackConfig((List<String>)list, list2 = (List)packRepository.getAvailableIds().stream().filter(arg_0 -> CreateWorldScreen.method_29983((List)list, arg_0)).collect(ImmutableList.toImmutableList())), this.uiState.getSettings().dataConfiguration().enabledFeatures());
        if (this.uiState.tryUpdateDataConfiguration(worldDataConfiguration)) {
            this.minecraft.setScreen(this);
            return;
        }
        FeatureFlagSet featureFlagSet = packRepository.getRequestedFeatureFlags();
        if (FeatureFlags.isExperimental(featureFlagSet) && bl2) {
            this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen(packRepository.getSelectedPacks(), bl -> {
                if (bl) {
                    this.applyNewPackConfig(packRepository, worldDataConfiguration, consumer);
                } else {
                    consumer.accept(this.uiState.getSettings().dataConfiguration());
                }
            }));
        } else {
            this.applyNewPackConfig(packRepository, worldDataConfiguration, consumer);
        }
    }

    private void applyNewPackConfig(PackRepository packRepository, WorldDataConfiguration worldDataConfiguration, Consumer<WorldDataConfiguration> consumer) {
        this.minecraft.setScreenAndShow(new GenericMessageScreen(Component.translatable("dataPack.validation.working")));
        WorldLoader.InitConfig initConfig = CreateWorldScreen.createDefaultLoadConfig(packRepository, worldDataConfiguration);
        ((CompletableFuture)((CompletableFuture)WorldLoader.load(initConfig, dataLoadContext -> {
            if (dataLoadContext.datapackWorldgen().lookupOrThrow(Registries.WORLD_PRESET).listElements().findAny().isEmpty()) {
                throw new IllegalStateException("Needs at least one world preset to continue");
            }
            if (dataLoadContext.datapackWorldgen().lookupOrThrow(Registries.BIOME).listElements().findAny().isEmpty()) {
                throw new IllegalStateException("Needs at least one biome continue");
            }
            WorldCreationContext worldCreationContext = this.uiState.getSettings();
            RegistryOps dynamicOps = worldCreationContext.worldgenLoadContext().createSerializationContext(JsonOps.INSTANCE);
            DataResult dataResult = WorldGenSettings.encode(dynamicOps, worldCreationContext.options(), worldCreationContext.selectedDimensions()).setLifecycle(Lifecycle.stable());
            RegistryOps dynamicOps2 = dataLoadContext.datapackWorldgen().createSerializationContext(JsonOps.INSTANCE);
            WorldGenSettings worldGenSettings = (WorldGenSettings)((Object)((Object)dataResult.flatMap(jsonElement -> WorldGenSettings.CODEC.parse(dynamicOps2, jsonElement)).getOrThrow(string -> new IllegalStateException("Error parsing worldgen settings after loading data packs: " + string))));
            return new WorldLoader.DataLoadOutput<DataPackReloadCookie>(new DataPackReloadCookie(worldGenSettings, dataLoadContext.dataConfiguration()), dataLoadContext.datapackDimensions());
        }, (closeableResourceManager, reloadableServerResources, layeredRegistryAccess, dataPackReloadCookie) -> {
            closeableResourceManager.close();
            return new WorldCreationContext(dataPackReloadCookie.worldGenSettings(), layeredRegistryAccess, reloadableServerResources, dataPackReloadCookie.dataConfiguration());
        }, Util.backgroundExecutor(), this.minecraft).thenApply(worldCreationContext -> {
            worldCreationContext.validate();
            return worldCreationContext;
        })).thenAcceptAsync(this.uiState::setSettings, (Executor)this.minecraft)).handleAsync((void_, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to validate datapack", throwable);
                this.minecraft.setScreen(new ConfirmScreen(bl -> {
                    if (bl) {
                        consumer.accept(this.uiState.getSettings().dataConfiguration());
                    } else {
                        consumer.accept(WorldDataConfiguration.DEFAULT);
                    }
                }, Component.translatable("dataPack.validation.failed"), CommonComponents.EMPTY, Component.translatable("dataPack.validation.back"), Component.translatable("dataPack.validation.reset")));
            } else {
                this.minecraft.setScreen(this);
            }
            return null;
        }, (Executor)this.minecraft);
    }

    private static WorldLoader.InitConfig createDefaultLoadConfig(PackRepository packRepository, WorldDataConfiguration worldDataConfiguration) {
        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, worldDataConfiguration, false, true);
        return new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, LevelBasedPermissionSet.GAMEMASTER);
    }

    private void removeTempDataPackDir() {
        if (this.tempDataPackDir != null && Files.exists(this.tempDataPackDir, new LinkOption[0])) {
            try (Stream<Path> stream = Files.walk(this.tempDataPackDir, new FileVisitOption[0]);){
                stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.delete(path);
                    }
                    catch (IOException iOException) {
                        LOGGER.warn("Failed to remove temporary file {}", path, (Object)iOException);
                    }
                });
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to list temporary dir {}", (Object)this.tempDataPackDir);
            }
        }
        this.tempDataPackDir = null;
    }

    private static void copyBetweenDirs(Path path, Path path2, Path path3) {
        try {
            Util.copyBetweenDirs(path, path2, path3);
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to copy datapack file from {} to {}", (Object)path3, (Object)path2);
            throw new UncheckedIOException(iOException);
        }
    }

    /*
     * WARNING - bad return control flow
     */
    private static Optional<LevelStorageSource.LevelStorageAccess> createNewWorldDirectory(Minecraft minecraft, String string, @Nullable Path path) {
        Optional<LevelStorageSource.LevelStorageAccess> optional;
        block12: {
            LevelStorageSource.LevelStorageAccess levelStorageAccess;
            block11: {
                levelStorageAccess = minecraft.getLevelSource().createAccess(string);
                if (path != null) break block11;
                return Optional.of(levelStorageAccess);
            }
            Stream<Path> stream = Files.walk(path, new FileVisitOption[0]);
            try {
                Path path22 = levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR);
                FileUtil.createDirectoriesSafe(path22);
                stream.filter(path2 -> !path2.equals(path)).forEach(path3 -> CreateWorldScreen.copyBetweenDirs(path, path22, path3));
                optional = Optional.of(levelStorageAccess);
                if (stream == null) break block12;
            }
            catch (Throwable throwable) {
                try {
                    try {
                        if (stream != null) {
                            try {
                                stream.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException | UncheckedIOException exception) {
                        LOGGER.warn("Failed to copy datapacks to world {}", (Object)string, (Object)exception);
                        levelStorageAccess.close();
                    }
                }
                catch (IOException | UncheckedIOException exception2) {
                    LOGGER.warn("Failed to create access for {}", (Object)string, (Object)exception2);
                }
            }
            stream.close();
        }
        return optional;
        return Optional.empty();
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public static @Nullable Path createTempDataPackDirFromExistingWorld(Path path, Minecraft minecraft) {
        @Nullable MutableObject mutableObject = new MutableObject();
        try (Stream<Path> stream = Files.walk(path, new FileVisitOption[0]);){
            stream.filter(path2 -> !path2.equals(path)).forEach(path2 -> {
                Path path3 = (Path)mutableObject.get();
                if (path3 == null) {
                    try {
                        path3 = Files.createTempDirectory(TEMP_WORLD_PREFIX, new FileAttribute[0]);
                    }
                    catch (IOException iOException) {
                        LOGGER.warn("Failed to create temporary dir");
                        throw new UncheckedIOException(iOException);
                    }
                    mutableObject.setValue((Object)path3);
                }
                CreateWorldScreen.copyBetweenDirs(path, path3, path2);
            });
        }
        catch (IOException | UncheckedIOException exception) {
            LOGGER.warn("Failed to copy datapacks from world {}", (Object)path, (Object)exception);
            SystemToast.onPackCopyFailure(minecraft, path.toString());
            return null;
        }
        return (Path)mutableObject.get();
    }

    private @Nullable Pair<Path, PackRepository> getDataPackSelectionSettings(WorldDataConfiguration worldDataConfiguration) {
        Path path = this.getOrCreateTempDataPackDir();
        if (path != null) {
            if (this.tempDataPackRepository == null) {
                this.tempDataPackRepository = ServerPacksSource.createPackRepository(path, this.packValidator);
                this.tempDataPackRepository.reload();
            }
            this.tempDataPackRepository.setSelected(worldDataConfiguration.dataPacks().getEnabled());
            return Pair.of((Object)path, (Object)this.tempDataPackRepository);
        }
        return null;
    }

    private static /* synthetic */ boolean method_29983(List list, String string) {
        return !list.contains(string);
    }

    @Environment(value=EnvType.CLIENT)
    class GameTab
    extends GridLayoutTab {
        private static final Component TITLE = Component.translatable("createWorld.tab.game.title");
        private static final Component ALLOW_COMMANDS = Component.translatable("selectWorld.allowCommands");
        private final EditBox nameEdit;

        GameTab() {
            super(TITLE);
            GridLayout.RowHelper rowHelper = this.layout.rowSpacing(8).createRowHelper(1);
            LayoutSettings layoutSettings = rowHelper.newCellSettings();
            this.nameEdit = new EditBox(CreateWorldScreen.this.font, 208, 20, Component.translatable("selectWorld.enterName"));
            this.nameEdit.setValue(CreateWorldScreen.this.uiState.getName());
            this.nameEdit.setResponder(CreateWorldScreen.this.uiState::setName);
            CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> this.nameEdit.setTooltip(Tooltip.create(Component.translatable("selectWorld.targetFolder", Component.literal(worldCreationUiState.getTargetFolder()).withStyle(ChatFormatting.ITALIC)))));
            CreateWorldScreen.this.setInitialFocus(this.nameEdit);
            rowHelper.addChild(CommonLayouts.labeledElement(CreateWorldScreen.this.font, this.nameEdit, NAME_LABEL), rowHelper.newCellSettings().alignHorizontallyCenter());
            CycleButton<WorldCreationUiState.SelectedGameMode> cycleButton2 = rowHelper.addChild(CycleButton.builder(selectedGameMode -> selectedGameMode.displayName, CreateWorldScreen.this.uiState.getGameMode()).withValues((WorldCreationUiState.SelectedGameMode[])new WorldCreationUiState.SelectedGameMode[]{WorldCreationUiState.SelectedGameMode.SURVIVAL, WorldCreationUiState.SelectedGameMode.HARDCORE, WorldCreationUiState.SelectedGameMode.CREATIVE}).create(0, 0, 210, 20, GAME_MODEL_LABEL, (cycleButton, selectedGameMode) -> CreateWorldScreen.this.uiState.setGameMode((WorldCreationUiState.SelectedGameMode)((Object)selectedGameMode))), layoutSettings);
            CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> {
                cycleButton2.setValue(worldCreationUiState.getGameMode());
                cycleButton.active = !worldCreationUiState.isDebug();
                cycleButton2.setTooltip(Tooltip.create(worldCreationUiState.getGameMode().getInfo()));
            });
            CycleButton<Difficulty> cycleButton22 = rowHelper.addChild(CycleButton.builder(Difficulty::getDisplayName, CreateWorldScreen.this.uiState.getDifficulty()).withValues((Difficulty[])Difficulty.values()).create(0, 0, 210, 20, Component.translatable("options.difficulty"), (cycleButton, difficulty) -> CreateWorldScreen.this.uiState.setDifficulty((Difficulty)difficulty)), layoutSettings);
            CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> {
                cycleButton22.setValue(CreateWorldScreen.this.uiState.getDifficulty());
                cycleButton.active = !CreateWorldScreen.this.uiState.isHardcore();
                cycleButton22.setTooltip(Tooltip.create(CreateWorldScreen.this.uiState.getDifficulty().getInfo()));
            });
            CycleButton<Boolean> cycleButton3 = rowHelper.addChild(CycleButton.onOffBuilder(CreateWorldScreen.this.uiState.isAllowCommands()).withTooltip(boolean_ -> Tooltip.create(ALLOW_COMMANDS_INFO)).create(0, 0, 210, 20, ALLOW_COMMANDS, (cycleButton, boolean_) -> CreateWorldScreen.this.uiState.setAllowCommands((boolean)boolean_)));
            CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> {
                cycleButton3.setValue(CreateWorldScreen.this.uiState.isAllowCommands());
                cycleButton.active = !CreateWorldScreen.this.uiState.isDebug() && !CreateWorldScreen.this.uiState.isHardcore();
            });
            if (!SharedConstants.getCurrentVersion().stable()) {
                rowHelper.addChild(Button.builder(EXPERIMENTS_LABEL, button -> CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())).width(210).build());
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class WorldTab
    extends GridLayoutTab {
        private static final Component TITLE = Component.translatable("createWorld.tab.world.title");
        private static final Component AMPLIFIED_HELP_TEXT = Component.translatable("generator.minecraft.amplified.info");
        private static final Component GENERATE_STRUCTURES = Component.translatable("selectWorld.mapFeatures");
        private static final Component GENERATE_STRUCTURES_INFO = Component.translatable("selectWorld.mapFeatures.info");
        private static final Component BONUS_CHEST = Component.translatable("selectWorld.bonusItems");
        private static final Component SEED_LABEL = Component.translatable("selectWorld.enterSeed");
        static final Component SEED_EMPTY_HINT = Component.translatable("selectWorld.seedInfo");
        private static final int WORLD_TAB_WIDTH = 310;
        private final EditBox seedEdit;
        private final Button customizeTypeButton;

        WorldTab() {
            super(TITLE);
            GridLayout.RowHelper rowHelper = this.layout.columnSpacing(10).rowSpacing(8).createRowHelper(2);
            CycleButton<WorldCreationUiState.WorldTypeEntry> cycleButton2 = rowHelper.addChild(CycleButton.builder(WorldCreationUiState.WorldTypeEntry::describePreset, CreateWorldScreen.this.uiState.getWorldType()).withValues(this.createWorldTypeValueSupplier()).withCustomNarration(WorldTab::createTypeButtonNarration).create(0, 0, 150, 20, Component.translatable("selectWorld.mapType"), (cycleButton, worldTypeEntry) -> CreateWorldScreen.this.uiState.setWorldType((WorldCreationUiState.WorldTypeEntry)((Object)worldTypeEntry))));
            cycleButton2.setValue(CreateWorldScreen.this.uiState.getWorldType());
            CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> {
                WorldCreationUiState.WorldTypeEntry worldTypeEntry = worldCreationUiState.getWorldType();
                cycleButton2.setValue(worldTypeEntry);
                if (worldTypeEntry.isAmplified()) {
                    cycleButton2.setTooltip(Tooltip.create(AMPLIFIED_HELP_TEXT));
                } else {
                    cycleButton2.setTooltip(null);
                }
                cycleButton.active = CreateWorldScreen.this.uiState.getWorldType().preset() != null;
            });
            this.customizeTypeButton = rowHelper.addChild(Button.builder(Component.translatable("selectWorld.customizeType"), button -> this.openPresetEditor()).build());
            CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> {
                this.customizeTypeButton.active = !worldCreationUiState.isDebug() && worldCreationUiState.getPresetEditor() != null;
            });
            this.seedEdit = new EditBox(this, CreateWorldScreen.this.font, 308, 20, Component.translatable("selectWorld.enterSeed")){

                @Override
                protected MutableComponent createNarrationMessage() {
                    return super.createNarrationMessage().append(CommonComponents.NARRATION_SEPARATOR).append(SEED_EMPTY_HINT);
                }
            };
            this.seedEdit.setHint(SEED_EMPTY_HINT);
            this.seedEdit.setValue(CreateWorldScreen.this.uiState.getSeed());
            this.seedEdit.setResponder(string -> CreateWorldScreen.this.uiState.setSeed(this.seedEdit.getValue()));
            rowHelper.addChild(CommonLayouts.labeledElement(CreateWorldScreen.this.font, this.seedEdit, SEED_LABEL), 2);
            SwitchGrid.Builder builder = SwitchGrid.builder(310);
            builder.addSwitch(GENERATE_STRUCTURES, CreateWorldScreen.this.uiState::isGenerateStructures, CreateWorldScreen.this.uiState::setGenerateStructures).withIsActiveCondition(() -> !CreateWorldScreen.this.uiState.isDebug()).withInfo(GENERATE_STRUCTURES_INFO);
            builder.addSwitch(BONUS_CHEST, CreateWorldScreen.this.uiState::isBonusChest, CreateWorldScreen.this.uiState::setBonusChest).withIsActiveCondition(() -> !CreateWorldScreen.this.uiState.isHardcore() && !CreateWorldScreen.this.uiState.isDebug());
            SwitchGrid switchGrid = builder.build();
            rowHelper.addChild(switchGrid.layout(), 2);
            CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> switchGrid.refreshStates());
        }

        private void openPresetEditor() {
            PresetEditor presetEditor = CreateWorldScreen.this.uiState.getPresetEditor();
            if (presetEditor != null) {
                CreateWorldScreen.this.minecraft.setScreen(presetEditor.createEditScreen(CreateWorldScreen.this, CreateWorldScreen.this.uiState.getSettings()));
            }
        }

        private CycleButton.ValueListSupplier<WorldCreationUiState.WorldTypeEntry> createWorldTypeValueSupplier() {
            return new CycleButton.ValueListSupplier<WorldCreationUiState.WorldTypeEntry>(){

                @Override
                public List<WorldCreationUiState.WorldTypeEntry> getSelectedList() {
                    return CycleButton.DEFAULT_ALT_LIST_SELECTOR.getAsBoolean() ? CreateWorldScreen.this.uiState.getAltPresetList() : CreateWorldScreen.this.uiState.getNormalPresetList();
                }

                @Override
                public List<WorldCreationUiState.WorldTypeEntry> getDefaultList() {
                    return CreateWorldScreen.this.uiState.getNormalPresetList();
                }
            };
        }

        private static MutableComponent createTypeButtonNarration(CycleButton<WorldCreationUiState.WorldTypeEntry> cycleButton) {
            if (cycleButton.getValue().isAmplified()) {
                return CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT);
            }
            return cycleButton.createDefaultNarrationMessage();
        }
    }

    @Environment(value=EnvType.CLIENT)
    class MoreTab
    extends GridLayoutTab {
        private static final Component TITLE = Component.translatable("createWorld.tab.more.title");
        private static final Component GAME_RULES_LABEL = Component.translatable("selectWorld.gameRules");
        private static final Component DATA_PACKS_LABEL = Component.translatable("selectWorld.dataPacks");

        MoreTab() {
            super(TITLE);
            GridLayout.RowHelper rowHelper = this.layout.rowSpacing(8).createRowHelper(1);
            rowHelper.addChild(Button.builder(GAME_RULES_LABEL, button -> this.openGameRulesScreen()).width(210).build());
            rowHelper.addChild(Button.builder(EXPERIMENTS_LABEL, button -> CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())).width(210).build());
            rowHelper.addChild(Button.builder(DATA_PACKS_LABEL, button -> CreateWorldScreen.this.openDataPackSelectionScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())).width(210).build());
        }

        private void openGameRulesScreen() {
            CreateWorldScreen.this.minecraft.setScreen(new EditGameRulesScreen(CreateWorldScreen.this.uiState.getGameRules().copy(CreateWorldScreen.this.uiState.getSettings().dataConfiguration().enabledFeatures()), optional -> {
                CreateWorldScreen.this.minecraft.setScreen(CreateWorldScreen.this);
                optional.ifPresent(CreateWorldScreen.this.uiState::setGameRules);
            }));
        }
    }
}


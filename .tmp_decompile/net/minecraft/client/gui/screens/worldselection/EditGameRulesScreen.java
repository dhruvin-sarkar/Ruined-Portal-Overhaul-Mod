/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.serialization.DataResult
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.DataResult;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EditGameRulesScreen
extends Screen {
    private static final Component TITLE = Component.translatable("editGamerule.title");
    private static final int SPACING = 8;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Consumer<Optional<GameRules>> exitCallback;
    private final Set<RuleEntry> invalidEntries = Sets.newHashSet();
    final GameRules gameRules;
    private @Nullable RuleList ruleList;
    private @Nullable Button doneButton;

    public EditGameRulesScreen(GameRules gameRules, Consumer<Optional<GameRules>> consumer) {
        super(TITLE);
        this.gameRules = gameRules;
        this.exitCallback = consumer;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        this.ruleList = this.layout.addToContents(new RuleList(this.gameRules));
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        this.doneButton = linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.exitCallback.accept(Optional.of(this.gameRules))).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.ruleList != null) {
            this.ruleList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        this.exitCallback.accept(Optional.empty());
    }

    private void updateDoneButton() {
        if (this.doneButton != null) {
            this.doneButton.active = this.invalidEntries.isEmpty();
        }
    }

    void markInvalid(RuleEntry ruleEntry) {
        this.invalidEntries.add(ruleEntry);
        this.updateDoneButton();
    }

    void clearInvalid(RuleEntry ruleEntry) {
        this.invalidEntries.remove(ruleEntry);
        this.updateDoneButton();
    }

    @Environment(value=EnvType.CLIENT)
    public class RuleList
    extends ContainerObjectSelectionList<RuleEntry> {
        private static final int ITEM_HEIGHT = 24;

        public RuleList(GameRules gameRules) {
            super(Minecraft.getInstance(), EditGameRulesScreen.this.width, EditGameRulesScreen.this.layout.getContentHeight(), EditGameRulesScreen.this.layout.getHeaderHeight(), 24);
            final HashMap map = Maps.newHashMap();
            gameRules.visitGameRuleTypes(new GameRuleTypeVisitor(){

                @Override
                public void visitBoolean(GameRule<Boolean> gameRule2) {
                    this.addEntry(gameRule2, (component, list, string, gameRule) -> new BooleanRuleEntry(component, list, string, gameRule));
                }

                @Override
                public void visitInteger(GameRule<Integer> gameRule2) {
                    this.addEntry(gameRule2, (component, list, string, gameRule) -> new IntegerRuleEntry(component, list, string, gameRule));
                }

                private <T> void addEntry(GameRule<T> gameRule, EntryFactory<T> entryFactory) {
                    Object string2;
                    ImmutableList list;
                    MutableComponent component = Component.translatable(gameRule.getDescriptionId());
                    MutableComponent component2 = Component.literal(gameRule.id()).withStyle(ChatFormatting.YELLOW);
                    MutableComponent component3 = Component.translatable("editGamerule.default", Component.literal(gameRule.serialize(gameRule.defaultValue()))).withStyle(ChatFormatting.GRAY);
                    String string = gameRule.getDescriptionId() + ".description";
                    if (I18n.exists(string)) {
                        ImmutableList.Builder builder = ImmutableList.builder().add((Object)component2.getVisualOrderText());
                        MutableComponent component4 = Component.translatable(string);
                        EditGameRulesScreen.this.font.split(component4, 150).forEach(arg_0 -> ((ImmutableList.Builder)builder).add(arg_0));
                        list = builder.add((Object)component3.getVisualOrderText()).build();
                        string2 = component4.getString() + "\n" + component3.getString();
                    } else {
                        list = ImmutableList.of((Object)component2.getVisualOrderText(), (Object)component3.getVisualOrderText());
                        string2 = component3.getString();
                    }
                    map.computeIfAbsent(gameRule.category(), gameRuleCategory -> Maps.newHashMap()).put(gameRule, entryFactory.create(component, (List<FormattedCharSequence>)list, (String)string2, gameRule));
                }
            });
            map.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(GameRuleCategory::getDescriptionId))).forEach(entry2 -> {
                this.addEntry(new CategoryRuleEntry(((GameRuleCategory)((Object)((Object)entry2.getKey()))).label().withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
                ((Map)entry2.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(GameRule::getDescriptionId))).forEach(entry -> this.addEntry((RuleEntry)entry.getValue()));
            });
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            super.renderWidget(guiGraphics, i, j, f);
            RuleEntry ruleEntry = (RuleEntry)this.getHovered();
            if (ruleEntry != null && ruleEntry.tooltip != null) {
                guiGraphics.setTooltipForNextFrame(ruleEntry.tooltip, i, j);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class IntegerRuleEntry
    extends GameRuleEntry {
        private final EditBox input;

        public IntegerRuleEntry(Component component, List<FormattedCharSequence> list, String string2, GameRule<Integer> gameRule) {
            super(list, component);
            this.input = new EditBox(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, 10, 5, 44, 20, component.copy().append("\n").append(string2).append("\n"));
            this.input.setValue(EditGameRulesScreen.this.gameRules.getAsString(gameRule));
            this.input.setResponder(string -> {
                DataResult dataResult = gameRule.deserialize((String)string);
                if (dataResult.isSuccess()) {
                    this.input.setTextColor(-2039584);
                    EditGameRulesScreen.this.clearInvalid(this);
                    EditGameRulesScreen.this.gameRules.set(gameRule, (Integer)dataResult.getOrThrow(), null);
                } else {
                    this.input.setTextColor(-65536);
                    EditGameRulesScreen.this.markInvalid(this);
                }
            });
            this.children.add(this.input);
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
            this.renderLabel(guiGraphics, this.getContentY(), this.getContentX());
            this.input.setX(this.getContentRight() - 45);
            this.input.setY(this.getContentY());
            this.input.render(guiGraphics, i, j, f);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class BooleanRuleEntry
    extends GameRuleEntry {
        private final CycleButton<Boolean> checkbox;

        public BooleanRuleEntry(Component component, List<FormattedCharSequence> list, String string, GameRule<Boolean> gameRule) {
            super(list, component);
            this.checkbox = CycleButton.onOffBuilder(EditGameRulesScreen.this.gameRules.get(gameRule)).displayOnlyValue().withCustomNarration(cycleButton -> cycleButton.createDefaultNarrationMessage().append("\n").append(string)).create(10, 5, 44, 20, component, (cycleButton, boolean_) -> EditGameRulesScreen.this.gameRules.set(gameRule, boolean_, null));
            this.children.add(this.checkbox);
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
            this.renderLabel(guiGraphics, this.getContentY(), this.getContentX());
            this.checkbox.setX(this.getContentRight() - 45);
            this.checkbox.setY(this.getContentY());
            this.checkbox.render(guiGraphics, i, j, f);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public abstract class GameRuleEntry
    extends RuleEntry {
        private final List<FormattedCharSequence> label;
        protected final List<AbstractWidget> children;

        public GameRuleEntry(List<FormattedCharSequence> list, Component component) {
            super(list);
            this.children = Lists.newArrayList();
            this.label = ((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font.split(component, 175);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }

        protected void renderLabel(GuiGraphics guiGraphics, int i, int j) {
            if (this.label.size() == 1) {
                guiGraphics.drawString(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, this.label.get(0), j, i + 5, -1);
            } else if (this.label.size() >= 2) {
                guiGraphics.drawString(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, this.label.get(0), j, i, -1);
                guiGraphics.drawString(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, this.label.get(1), j, i + 10, -1);
            }
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface EntryFactory<T> {
        public RuleEntry create(Component var1, List<FormattedCharSequence> var2, String var3, GameRule<T> var4);
    }

    @Environment(value=EnvType.CLIENT)
    public class CategoryRuleEntry
    extends RuleEntry {
        final Component label;

        public CategoryRuleEntry(Component component) {
            super(null);
            this.label = component;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
            guiGraphics.drawCenteredString(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, this.label, this.getContentXMiddle(), this.getContentY() + 5, -1);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of((Object)new NarratableEntry(){

                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput narrationElementOutput) {
                    narrationElementOutput.add(NarratedElementType.TITLE, CategoryRuleEntry.this.label);
                }
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class RuleEntry
    extends ContainerObjectSelectionList.Entry<RuleEntry> {
        final @Nullable List<FormattedCharSequence> tooltip;

        public RuleEntry(@Nullable List<FormattedCharSequence> list) {
            this.tooltip = list;
        }
    }
}


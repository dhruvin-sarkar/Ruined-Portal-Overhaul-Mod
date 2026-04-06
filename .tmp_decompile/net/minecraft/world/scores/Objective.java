/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.scores;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jspecify.annotations.Nullable;

public class Objective {
    private final Scoreboard scoreboard;
    private final String name;
    private final ObjectiveCriteria criteria;
    private Component displayName;
    private Component formattedDisplayName;
    private ObjectiveCriteria.RenderType renderType;
    private boolean displayAutoUpdate;
    private @Nullable NumberFormat numberFormat;

    public Objective(Scoreboard scoreboard, String string, ObjectiveCriteria objectiveCriteria, Component component, ObjectiveCriteria.RenderType renderType, boolean bl, @Nullable NumberFormat numberFormat) {
        this.scoreboard = scoreboard;
        this.name = string;
        this.criteria = objectiveCriteria;
        this.displayName = component;
        this.formattedDisplayName = this.createFormattedDisplayName();
        this.renderType = renderType;
        this.displayAutoUpdate = bl;
        this.numberFormat = numberFormat;
    }

    public Packed pack() {
        return new Packed(this.name, this.criteria, this.displayName, this.renderType, this.displayAutoUpdate, Optional.ofNullable(this.numberFormat));
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public String getName() {
        return this.name;
    }

    public ObjectiveCriteria getCriteria() {
        return this.criteria;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public boolean displayAutoUpdate() {
        return this.displayAutoUpdate;
    }

    public @Nullable NumberFormat numberFormat() {
        return this.numberFormat;
    }

    public NumberFormat numberFormatOrDefault(NumberFormat numberFormat) {
        return (NumberFormat)Objects.requireNonNullElse((Object)this.numberFormat, (Object)numberFormat);
    }

    private Component createFormattedDisplayName() {
        return ComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(this.name)))));
    }

    public Component getFormattedDisplayName() {
        return this.formattedDisplayName;
    }

    public void setDisplayName(Component component) {
        this.displayName = component;
        this.formattedDisplayName = this.createFormattedDisplayName();
        this.scoreboard.onObjectiveChanged(this);
    }

    public ObjectiveCriteria.RenderType getRenderType() {
        return this.renderType;
    }

    public void setRenderType(ObjectiveCriteria.RenderType renderType) {
        this.renderType = renderType;
        this.scoreboard.onObjectiveChanged(this);
    }

    public void setDisplayAutoUpdate(boolean bl) {
        this.displayAutoUpdate = bl;
        this.scoreboard.onObjectiveChanged(this);
    }

    public void setNumberFormat(@Nullable NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
        this.scoreboard.onObjectiveChanged(this);
    }

    public record Packed(String name, ObjectiveCriteria criteria, Component displayName, ObjectiveCriteria.RenderType renderType, boolean displayAutoUpdate, Optional<NumberFormat> numberFormat) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("Name").forGetter(Packed::name), (App)ObjectiveCriteria.CODEC.optionalFieldOf("CriteriaName", (Object)ObjectiveCriteria.DUMMY).forGetter(Packed::criteria), (App)ComponentSerialization.CODEC.fieldOf("DisplayName").forGetter(Packed::displayName), (App)ObjectiveCriteria.RenderType.CODEC.optionalFieldOf("RenderType", ObjectiveCriteria.RenderType.INTEGER).forGetter(Packed::renderType), (App)Codec.BOOL.optionalFieldOf("display_auto_update", (Object)false).forGetter(Packed::displayAutoUpdate), (App)NumberFormatTypes.CODEC.optionalFieldOf("format").forGetter(Packed::numberFormat)).apply((Applicative)instance, Packed::new));
    }
}


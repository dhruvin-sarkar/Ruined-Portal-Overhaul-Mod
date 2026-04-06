/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.advancements;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public enum AdvancementWidgetType {
    OBTAINED(Identifier.withDefaultNamespace("advancements/box_obtained"), Identifier.withDefaultNamespace("advancements/task_frame_obtained"), Identifier.withDefaultNamespace("advancements/challenge_frame_obtained"), Identifier.withDefaultNamespace("advancements/goal_frame_obtained")),
    UNOBTAINED(Identifier.withDefaultNamespace("advancements/box_unobtained"), Identifier.withDefaultNamespace("advancements/task_frame_unobtained"), Identifier.withDefaultNamespace("advancements/challenge_frame_unobtained"), Identifier.withDefaultNamespace("advancements/goal_frame_unobtained"));

    private final Identifier boxSprite;
    private final Identifier taskFrameSprite;
    private final Identifier challengeFrameSprite;
    private final Identifier goalFrameSprite;

    private AdvancementWidgetType(Identifier identifier, Identifier identifier2, Identifier identifier3, Identifier identifier4) {
        this.boxSprite = identifier;
        this.taskFrameSprite = identifier2;
        this.challengeFrameSprite = identifier3;
        this.goalFrameSprite = identifier4;
    }

    public Identifier boxSprite() {
        return this.boxSprite;
    }

    public Identifier frameSprite(AdvancementType advancementType) {
        return switch (advancementType) {
            default -> throw new MatchException(null, null);
            case AdvancementType.TASK -> this.taskFrameSprite;
            case AdvancementType.CHALLENGE -> this.challengeFrameSprite;
            case AdvancementType.GOAL -> this.goalFrameSprite;
        };
    }
}


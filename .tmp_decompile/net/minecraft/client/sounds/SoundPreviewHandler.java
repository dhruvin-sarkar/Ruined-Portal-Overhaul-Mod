/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public final class SoundPreviewHandler {
    private static @Nullable SoundInstance activePreview;
    private static @Nullable SoundSource previousCategory;

    public static void preview(SoundManager soundManager, SoundSource soundSource, float f) {
        SoundPreviewHandler.stopOtherCategoryPreview(soundManager, soundSource);
        if (SoundPreviewHandler.canPlaySound(soundManager)) {
            SoundEvent soundEvent;
            switch (soundSource) {
                case RECORDS: {
                    SoundEvent soundEvent2 = SoundEvents.NOTE_BLOCK_GUITAR.value();
                    break;
                }
                case WEATHER: {
                    SoundEvent soundEvent2 = SoundEvents.LIGHTNING_BOLT_THUNDER;
                    break;
                }
                case BLOCKS: {
                    SoundEvent soundEvent2 = SoundEvents.GRASS_PLACE;
                    break;
                }
                case HOSTILE: {
                    SoundEvent soundEvent2 = SoundEvents.ZOMBIE_AMBIENT;
                    break;
                }
                case NEUTRAL: {
                    SoundEvent soundEvent2 = SoundEvents.COW_AMBIENT;
                    break;
                }
                case PLAYERS: {
                    SoundEvent soundEvent2 = SoundEvents.GENERIC_EAT.value();
                    break;
                }
                case AMBIENT: {
                    SoundEvent soundEvent2 = SoundEvents.AMBIENT_CAVE.value();
                    break;
                }
                case UI: {
                    SoundEvent soundEvent2 = SoundEvents.UI_BUTTON_CLICK.value();
                    break;
                }
                default: {
                    SoundEvent soundEvent2 = soundEvent = SoundEvents.EMPTY;
                }
            }
            if (soundEvent != SoundEvents.EMPTY) {
                activePreview = SimpleSoundInstance.forUI(soundEvent, 1.0f, f);
                soundManager.play(activePreview);
            }
        }
    }

    private static void stopOtherCategoryPreview(SoundManager soundManager, SoundSource soundSource) {
        if (previousCategory != soundSource) {
            previousCategory = soundSource;
            if (activePreview != null) {
                soundManager.stop(activePreview);
            }
        }
    }

    private static boolean canPlaySound(SoundManager soundManager) {
        return activePreview == null || !soundManager.isActive(activePreview);
    }
}


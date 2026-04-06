/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dialog;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.body.DialogBody;

public record CommonDialogData(Component title, Optional<Component> externalTitle, boolean canCloseWithEscape, boolean pause, DialogAction afterAction, List<DialogBody> body, List<Input> inputs) {
    public static final MapCodec<CommonDialogData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ComponentSerialization.CODEC.fieldOf("title").forGetter(CommonDialogData::title), (App)ComponentSerialization.CODEC.optionalFieldOf("external_title").forGetter(CommonDialogData::externalTitle), (App)Codec.BOOL.optionalFieldOf("can_close_with_escape", (Object)true).forGetter(CommonDialogData::canCloseWithEscape), (App)Codec.BOOL.optionalFieldOf("pause", (Object)true).forGetter(CommonDialogData::pause), (App)DialogAction.CODEC.optionalFieldOf("after_action", DialogAction.CLOSE).forGetter(CommonDialogData::afterAction), (App)DialogBody.COMPACT_LIST_CODEC.optionalFieldOf("body", (Object)List.of()).forGetter(CommonDialogData::body), (App)Input.CODEC.listOf().optionalFieldOf("inputs", (Object)List.of()).forGetter(CommonDialogData::inputs)).apply((Applicative)instance, CommonDialogData::new)).validate(commonDialogData -> {
        if (commonDialogData.pause && !commonDialogData.afterAction.willUnpause()) {
            return DataResult.error(() -> "Dialogs that pause the game must use after_action values that unpause it after user action!");
        }
        return DataResult.success((Object)commonDialogData);
    });

    public Component computeExternalTitle() {
        return this.externalTitle.orElse(this.title);
    }
}


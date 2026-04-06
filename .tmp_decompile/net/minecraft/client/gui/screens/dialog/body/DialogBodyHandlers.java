/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.dialog.body;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ItemDisplayWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.gui.screens.dialog.body.DialogBodyHandler;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class DialogBodyHandlers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<MapCodec<? extends DialogBody>, DialogBodyHandler<?>> HANDLERS = new HashMap();

    private static <B extends DialogBody> void register(MapCodec<B> mapCodec, DialogBodyHandler<? super B> dialogBodyHandler) {
        HANDLERS.put(mapCodec, dialogBodyHandler);
    }

    private static <B extends DialogBody> @Nullable DialogBodyHandler<B> getHandler(B dialogBody) {
        return HANDLERS.get(dialogBody.mapCodec());
    }

    public static <B extends DialogBody> @Nullable LayoutElement createBodyElement(DialogScreen<?> dialogScreen, B dialogBody) {
        DialogBodyHandler<B> dialogBodyHandler = DialogBodyHandlers.getHandler(dialogBody);
        if (dialogBodyHandler == null) {
            LOGGER.warn("Unrecognized dialog body {}", dialogBody);
            return null;
        }
        return dialogBodyHandler.createControls(dialogScreen, dialogBody);
    }

    public static void bootstrap() {
        DialogBodyHandlers.register(PlainMessage.MAP_CODEC, new PlainMessageHandler());
        DialogBodyHandlers.register(ItemBody.MAP_CODEC, new ItemHandler());
    }

    static void runActionOnParent(DialogScreen<?> dialogScreen, @Nullable Style style) {
        ClickEvent clickEvent;
        if (style != null && (clickEvent = style.getClickEvent()) != null) {
            dialogScreen.runAction(Optional.of(clickEvent));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class PlainMessageHandler
    implements DialogBodyHandler<PlainMessage> {
        PlainMessageHandler() {
        }

        @Override
        public LayoutElement createControls(DialogScreen<?> dialogScreen, PlainMessage plainMessage) {
            return FocusableTextWidget.builder(plainMessage.contents(), dialogScreen.getFont()).maxWidth(plainMessage.width()).alwaysShowBorder(false).backgroundFill(FocusableTextWidget.BackgroundFill.NEVER).build().setCentered(true).setComponentClickHandler(style -> DialogBodyHandlers.runActionOnParent(dialogScreen, style));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ItemHandler
    implements DialogBodyHandler<ItemBody> {
        ItemHandler() {
        }

        @Override
        public LayoutElement createControls(DialogScreen<?> dialogScreen, ItemBody itemBody) {
            if (itemBody.description().isPresent()) {
                PlainMessage plainMessage = itemBody.description().get();
                LinearLayout linearLayout = LinearLayout.horizontal().spacing(2);
                linearLayout.defaultCellSetting().alignVerticallyMiddle();
                ItemDisplayWidget itemDisplayWidget = new ItemDisplayWidget(Minecraft.getInstance(), 0, 0, itemBody.width(), itemBody.height(), CommonComponents.EMPTY, itemBody.item(), itemBody.showDecorations(), itemBody.showTooltip());
                linearLayout.addChild(itemDisplayWidget);
                linearLayout.addChild(FocusableTextWidget.builder(plainMessage.contents(), dialogScreen.getFont()).maxWidth(plainMessage.width()).alwaysShowBorder(false).backgroundFill(FocusableTextWidget.BackgroundFill.NEVER).build().setComponentClickHandler(style -> DialogBodyHandlers.runActionOnParent(dialogScreen, style)));
                return linearLayout;
            }
            return new ItemDisplayWidget(Minecraft.getInstance(), 0, 0, itemBody.width(), itemBody.height(), itemBody.item().getHoverName(), itemBody.item(), itemBody.showDecorations(), itemBody.showTooltip());
        }
    }
}


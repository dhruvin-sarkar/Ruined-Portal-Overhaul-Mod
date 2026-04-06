/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.narration;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationThunk;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public interface NarrationElementOutput {
    default public void add(NarratedElementType narratedElementType, Component component) {
        this.add(narratedElementType, NarrationThunk.from(component.getString()));
    }

    default public void add(NarratedElementType narratedElementType, String string) {
        this.add(narratedElementType, NarrationThunk.from(string));
    }

    default public void add(NarratedElementType narratedElementType, Component ... components) {
        this.add(narratedElementType, NarrationThunk.from((List<Component>)ImmutableList.copyOf((Object[])components)));
    }

    public void add(NarratedElementType var1, NarrationThunk<?> var2);

    public NarrationElementOutput nest();
}


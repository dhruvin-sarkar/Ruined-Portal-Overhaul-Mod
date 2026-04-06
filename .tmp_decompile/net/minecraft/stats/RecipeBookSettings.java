/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  java.lang.MatchException
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.stats;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.function.UnaryOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {
    public static final StreamCodec<FriendlyByteBuf, RecipeBookSettings> STREAM_CODEC = StreamCodec.composite(TypeSettings.STREAM_CODEC, recipeBookSettings -> recipeBookSettings.crafting, TypeSettings.STREAM_CODEC, recipeBookSettings -> recipeBookSettings.furnace, TypeSettings.STREAM_CODEC, recipeBookSettings -> recipeBookSettings.blastFurnace, TypeSettings.STREAM_CODEC, recipeBookSettings -> recipeBookSettings.smoker, RecipeBookSettings::new);
    public static final MapCodec<RecipeBookSettings> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TypeSettings.CRAFTING_MAP_CODEC.forGetter(recipeBookSettings -> recipeBookSettings.crafting), (App)TypeSettings.FURNACE_MAP_CODEC.forGetter(recipeBookSettings -> recipeBookSettings.furnace), (App)TypeSettings.BLAST_FURNACE_MAP_CODEC.forGetter(recipeBookSettings -> recipeBookSettings.blastFurnace), (App)TypeSettings.SMOKER_MAP_CODEC.forGetter(recipeBookSettings -> recipeBookSettings.smoker)).apply((Applicative)instance, RecipeBookSettings::new));
    private TypeSettings crafting;
    private TypeSettings furnace;
    private TypeSettings blastFurnace;
    private TypeSettings smoker;

    public RecipeBookSettings() {
        this(TypeSettings.DEFAULT, TypeSettings.DEFAULT, TypeSettings.DEFAULT, TypeSettings.DEFAULT);
    }

    private RecipeBookSettings(TypeSettings typeSettings, TypeSettings typeSettings2, TypeSettings typeSettings3, TypeSettings typeSettings4) {
        this.crafting = typeSettings;
        this.furnace = typeSettings2;
        this.blastFurnace = typeSettings3;
        this.smoker = typeSettings4;
    }

    @VisibleForTesting
    public TypeSettings getSettings(RecipeBookType recipeBookType) {
        return switch (recipeBookType) {
            default -> throw new MatchException(null, null);
            case RecipeBookType.CRAFTING -> this.crafting;
            case RecipeBookType.FURNACE -> this.furnace;
            case RecipeBookType.BLAST_FURNACE -> this.blastFurnace;
            case RecipeBookType.SMOKER -> this.smoker;
        };
    }

    private void updateSettings(RecipeBookType recipeBookType, UnaryOperator<TypeSettings> unaryOperator) {
        switch (recipeBookType) {
            case CRAFTING: {
                this.crafting = (TypeSettings)((Object)unaryOperator.apply(this.crafting));
                break;
            }
            case FURNACE: {
                this.furnace = (TypeSettings)((Object)unaryOperator.apply(this.furnace));
                break;
            }
            case BLAST_FURNACE: {
                this.blastFurnace = (TypeSettings)((Object)unaryOperator.apply(this.blastFurnace));
                break;
            }
            case SMOKER: {
                this.smoker = (TypeSettings)((Object)unaryOperator.apply(this.smoker));
            }
        }
    }

    public boolean isOpen(RecipeBookType recipeBookType) {
        return this.getSettings((RecipeBookType)recipeBookType).open;
    }

    public void setOpen(RecipeBookType recipeBookType, boolean bl) {
        this.updateSettings(recipeBookType, typeSettings -> typeSettings.setOpen(bl));
    }

    public boolean isFiltering(RecipeBookType recipeBookType) {
        return this.getSettings((RecipeBookType)recipeBookType).filtering;
    }

    public void setFiltering(RecipeBookType recipeBookType, boolean bl) {
        this.updateSettings(recipeBookType, typeSettings -> typeSettings.setFiltering(bl));
    }

    public RecipeBookSettings copy() {
        return new RecipeBookSettings(this.crafting, this.furnace, this.blastFurnace, this.smoker);
    }

    public void replaceFrom(RecipeBookSettings recipeBookSettings) {
        this.crafting = recipeBookSettings.crafting;
        this.furnace = recipeBookSettings.furnace;
        this.blastFurnace = recipeBookSettings.blastFurnace;
        this.smoker = recipeBookSettings.smoker;
    }

    public static final class TypeSettings
    extends Record {
        final boolean open;
        final boolean filtering;
        public static final TypeSettings DEFAULT = new TypeSettings(false, false);
        public static final MapCodec<TypeSettings> CRAFTING_MAP_CODEC = TypeSettings.codec("isGuiOpen", "isFilteringCraftable");
        public static final MapCodec<TypeSettings> FURNACE_MAP_CODEC = TypeSettings.codec("isFurnaceGuiOpen", "isFurnaceFilteringCraftable");
        public static final MapCodec<TypeSettings> BLAST_FURNACE_MAP_CODEC = TypeSettings.codec("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable");
        public static final MapCodec<TypeSettings> SMOKER_MAP_CODEC = TypeSettings.codec("isSmokerGuiOpen", "isSmokerFilteringCraftable");
        public static final StreamCodec<ByteBuf, TypeSettings> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, TypeSettings::open, ByteBufCodecs.BOOL, TypeSettings::filtering, TypeSettings::new);

        public TypeSettings(boolean bl, boolean bl2) {
            this.open = bl;
            this.filtering = bl2;
        }

        public String toString() {
            return "[open=" + this.open + ", filtering=" + this.filtering + "]";
        }

        public TypeSettings setOpen(boolean bl) {
            return new TypeSettings(bl, this.filtering);
        }

        public TypeSettings setFiltering(boolean bl) {
            return new TypeSettings(this.open, bl);
        }

        private static MapCodec<TypeSettings> codec(String string, String string2) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf(string, (Object)false).forGetter(TypeSettings::open), (App)Codec.BOOL.optionalFieldOf(string2, (Object)false).forGetter(TypeSettings::filtering)).apply((Applicative)instance, TypeSettings::new));
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TypeSettings.class, "open;filtering", "open", "filtering"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TypeSettings.class, "open;filtering", "open", "filtering"}, this, object);
        }

        public boolean open() {
            return this.open;
        }

        public boolean filtering() {
            return this.filtering;
        }
    }
}


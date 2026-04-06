/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat.contents.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.data.DataSource;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public record EntityDataSource(String selectorPattern, @Nullable EntitySelector compiledSelector) implements DataSource
{
    public static final MapCodec<EntityDataSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("entity").forGetter(EntityDataSource::selectorPattern)).apply((Applicative)instance, EntityDataSource::new));

    public EntityDataSource(String string) {
        this(string, EntityDataSource.compileSelector(string));
    }

    private static @Nullable EntitySelector compileSelector(String string) {
        try {
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string), true);
            return entitySelectorParser.parse();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    @Override
    public Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        if (this.compiledSelector != null) {
            List<? extends Entity> list = this.compiledSelector.findEntities(commandSourceStack);
            return list.stream().map(NbtPredicate::getEntityTagToCompare);
        }
        return Stream.empty();
    }

    public MapCodec<EntityDataSource> codec() {
        return MAP_CODEC;
    }

    public String toString() {
        return "entity=" + this.selectorPattern;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof EntityDataSource)) return false;
        EntityDataSource entityDataSource = (EntityDataSource)object;
        if (!this.selectorPattern.equals(entityDataSource.selectorPattern)) return false;
        return true;
    }

    public int hashCode() {
        return this.selectorPattern.hashCode();
    }
}


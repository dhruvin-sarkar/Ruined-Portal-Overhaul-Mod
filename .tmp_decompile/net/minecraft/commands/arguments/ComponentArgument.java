/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.serialization.DynamicOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.util.parsing.packrat.commands.ParserBasedArgument;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class ComponentArgument
extends ParserBasedArgument<Component> {
    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "'hello world'", "\"\"", "{text:\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType ERROR_INVALID_COMPONENT = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.component.invalid", object));
    private static final DynamicOps<Tag> OPS = NbtOps.INSTANCE;
    private static final CommandArgumentParser<Tag> TAG_PARSER = SnbtGrammar.createParser(OPS);

    private ComponentArgument(HolderLookup.Provider provider) {
        super(TAG_PARSER.withCodec(provider.createSerializationContext(OPS), TAG_PARSER, ComponentSerialization.CODEC, ERROR_INVALID_COMPONENT));
    }

    public static Component getRawComponent(CommandContext<CommandSourceStack> commandContext, String string) {
        return (Component)commandContext.getArgument(string, Component.class);
    }

    public static Component getResolvedComponent(CommandContext<CommandSourceStack> commandContext, String string, @Nullable Entity entity) throws CommandSyntaxException {
        return ComponentUtils.updateForEntity((CommandSourceStack)commandContext.getSource(), ComponentArgument.getRawComponent(commandContext, string), entity, 0);
    }

    public static Component getResolvedComponent(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ComponentArgument.getResolvedComponent(commandContext, string, ((CommandSourceStack)commandContext.getSource()).getEntity());
    }

    public static ComponentArgument textComponent(CommandBuildContext commandBuildContext) {
        return new ComponentArgument(commandBuildContext);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}


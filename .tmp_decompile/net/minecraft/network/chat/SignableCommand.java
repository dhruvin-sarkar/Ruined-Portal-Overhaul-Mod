/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.context.CommandContextBuilder
 *  com.mojang.brigadier.context.ParsedArgument
 *  com.mojang.brigadier.context.ParsedCommandNode
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.SignedArgument;
import org.jspecify.annotations.Nullable;

public record SignableCommand<S>(List<Argument<S>> arguments) {
    public static <S> boolean hasSignableArguments(ParseResults<S> parseResults) {
        return !SignableCommand.of(parseResults).arguments().isEmpty();
    }

    public static <S> SignableCommand<S> of(ParseResults<S> parseResults) {
        CommandContextBuilder commandContextBuilder3;
        CommandContextBuilder commandContextBuilder;
        String string = parseResults.getReader().getString();
        CommandContextBuilder commandContextBuilder2 = commandContextBuilder = parseResults.getContext();
        List<Argument<S>> list = SignableCommand.collectArguments(string, commandContextBuilder2);
        while ((commandContextBuilder3 = commandContextBuilder2.getChild()) != null && commandContextBuilder3.getRootNode() != commandContextBuilder.getRootNode()) {
            list.addAll(SignableCommand.collectArguments(string, commandContextBuilder3));
            commandContextBuilder2 = commandContextBuilder3;
        }
        return new SignableCommand<S>(list);
    }

    private static <S> List<Argument<S>> collectArguments(String string, CommandContextBuilder<S> commandContextBuilder) {
        ArrayList<Argument<S>> list = new ArrayList<Argument<S>>();
        for (ParsedCommandNode parsedCommandNode : commandContextBuilder.getNodes()) {
            ParsedArgument parsedArgument;
            ArgumentCommandNode argumentCommandNode;
            CommandNode commandNode = parsedCommandNode.getNode();
            if (!(commandNode instanceof ArgumentCommandNode) || !((argumentCommandNode = (ArgumentCommandNode)commandNode).getType() instanceof SignedArgument) || (parsedArgument = (ParsedArgument)commandContextBuilder.getArguments().get(argumentCommandNode.getName())) == null) continue;
            String string2 = parsedArgument.getRange().get(string);
            list.add(new Argument(argumentCommandNode, string2));
        }
        return list;
    }

    public @Nullable Argument<S> getArgument(String string) {
        for (Argument<S> argument : this.arguments) {
            if (!string.equals(argument.name())) continue;
            return argument;
        }
        return null;
    }

    public record Argument<S>(ArgumentCommandNode<S, ?> node, String value) {
        public String name() {
            return this.node.getName();
        }
    }
}


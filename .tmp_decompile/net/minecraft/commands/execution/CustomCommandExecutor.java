/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.execution;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.TraceCallbacks;
import org.jspecify.annotations.Nullable;

public interface CustomCommandExecutor<T> {
    public void run(T var1, ContextChain<T> var2, ChainModifiers var3, ExecutionControl<T> var4);

    public static abstract class WithErrorHandling<T extends ExecutionCommandSource<T>>
    implements CustomCommandExecutor<T> {
        @Override
        public final void run(T executionCommandSource, ContextChain<T> contextChain, ChainModifiers chainModifiers, ExecutionControl<T> executionControl) {
            try {
                this.runGuarded(executionCommandSource, contextChain, chainModifiers, executionControl);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                this.onError(commandSyntaxException, executionCommandSource, chainModifiers, executionControl.tracer());
                executionCommandSource.callback().onFailure();
            }
        }

        protected void onError(CommandSyntaxException commandSyntaxException, T executionCommandSource, ChainModifiers chainModifiers, @Nullable TraceCallbacks traceCallbacks) {
            executionCommandSource.handleError(commandSyntaxException, chainModifiers.isForked(), traceCallbacks);
        }

        protected abstract void runGuarded(T var1, ContextChain<T> var2, ChainModifiers var3, ExecutionControl<T> var4) throws CommandSyntaxException;
    }

    public static interface CommandAdapter<T>
    extends Command<T>,
    CustomCommandExecutor<T> {
        default public int run(CommandContext<T> commandContext) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}


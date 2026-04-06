/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.timers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.world.level.timers.TimerCallback;
import net.minecraft.world.level.timers.TimerQueue;

public record FunctionTagCallback(Identifier tagId) implements TimerCallback<MinecraftServer>
{
    public static final MapCodec<FunctionTagCallback> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("Name").forGetter(FunctionTagCallback::tagId)).apply((Applicative)instance, FunctionTagCallback::new));

    @Override
    public void handle(MinecraftServer minecraftServer, TimerQueue<MinecraftServer> timerQueue, long l) {
        ServerFunctionManager serverFunctionManager = minecraftServer.getFunctions();
        List<CommandFunction<CommandSourceStack>> list = serverFunctionManager.getTag(this.tagId);
        for (CommandFunction<CommandSourceStack> commandFunction : list) {
            serverFunctionManager.execute(commandFunction, serverFunctionManager.getGameLoopSender());
        }
    }

    @Override
    public MapCodec<FunctionTagCallback> codec() {
        return CODEC;
    }
}


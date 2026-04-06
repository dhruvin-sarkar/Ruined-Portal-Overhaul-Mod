/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.datafixers.util.Either
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
    public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType((Message)Component.translatable("argument.pos.unloaded"));
    private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.fillbiome.toobig", object, object2));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fillbiome").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)Commands.argument("biome", ResourceArgument.resource(commandBuildContext, Registries.BIOME)).executes(commandContext -> FillBiomeCommand.fill((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to"), ResourceArgument.getResource((CommandContext<CommandSourceStack>)commandContext, "biome", Registries.BIOME), holder -> true))).then(Commands.literal("replace").then(Commands.argument("filter", ResourceOrTagArgument.resourceOrTag(commandBuildContext, Registries.BIOME)).executes(commandContext -> FillBiomeCommand.fill((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "from"), BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "to"), ResourceArgument.getResource((CommandContext<CommandSourceStack>)commandContext, "biome", Registries.BIOME), ResourceOrTagArgument.getResourceOrTag((CommandContext<CommandSourceStack>)commandContext, "filter", Registries.BIOME)))))))));
    }

    private static int quantize(int i) {
        return QuartPos.toBlock(QuartPos.fromBlock(i));
    }

    private static BlockPos quantize(BlockPos blockPos) {
        return new BlockPos(FillBiomeCommand.quantize(blockPos.getX()), FillBiomeCommand.quantize(blockPos.getY()), FillBiomeCommand.quantize(blockPos.getZ()));
    }

    private static BiomeResolver makeResolver(MutableInt mutableInt, ChunkAccess chunkAccess, BoundingBox boundingBox, Holder<Biome> holder, Predicate<Holder<Biome>> predicate) {
        return (i, j, k, sampler) -> {
            int l = QuartPos.toBlock(i);
            int m = QuartPos.toBlock(j);
            int n = QuartPos.toBlock(k);
            Holder<Biome> holder2 = chunkAccess.getNoiseBiome(i, j, k);
            if (boundingBox.isInside(l, m, n) && predicate.test(holder2)) {
                mutableInt.increment();
                return holder;
            }
            return holder2;
        };
    }

    public static Either<Integer, CommandSyntaxException> fill(ServerLevel serverLevel, BlockPos blockPos, BlockPos blockPos2, Holder<Biome> holder2) {
        return FillBiomeCommand.fill(serverLevel, blockPos, blockPos2, holder2, holder -> true, supplier -> {});
    }

    public static Either<Integer, CommandSyntaxException> fill(ServerLevel serverLevel, BlockPos blockPos, BlockPos blockPos2, Holder<Biome> holder, Predicate<Holder<Biome>> predicate, Consumer<Supplier<Component>> consumer) {
        int j;
        BlockPos blockPos4;
        BlockPos blockPos3 = FillBiomeCommand.quantize(blockPos);
        BoundingBox boundingBox = BoundingBox.fromCorners(blockPos3, blockPos4 = FillBiomeCommand.quantize(blockPos2));
        int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (i > (j = serverLevel.getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS).intValue())) {
            return Either.right((Object)((Object)ERROR_VOLUME_TOO_LARGE.create((Object)j, (Object)i)));
        }
        ArrayList<ChunkAccess> list = new ArrayList<ChunkAccess>();
        for (int k = SectionPos.blockToSectionCoord(boundingBox.minZ()); k <= SectionPos.blockToSectionCoord(boundingBox.maxZ()); ++k) {
            for (int l = SectionPos.blockToSectionCoord(boundingBox.minX()); l <= SectionPos.blockToSectionCoord(boundingBox.maxX()); ++l) {
                ChunkAccess chunkAccess = serverLevel.getChunk(l, k, ChunkStatus.FULL, false);
                if (chunkAccess == null) {
                    return Either.right((Object)((Object)ERROR_NOT_LOADED.create()));
                }
                list.add(chunkAccess);
            }
        }
        MutableInt mutableInt = new MutableInt(0);
        for (ChunkAccess chunkAccess : list) {
            chunkAccess.fillBiomesFromNoise(FillBiomeCommand.makeResolver(mutableInt, chunkAccess, boundingBox, holder, predicate), serverLevel.getChunkSource().randomState().sampler());
            chunkAccess.markUnsaved();
        }
        serverLevel.getChunkSource().chunkMap.resendBiomesForChunks(list);
        consumer.accept(() -> Component.translatable("commands.fillbiome.success.count", mutableInt.intValue(), boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ()));
        return Either.left((Object)mutableInt.intValue());
    }

    private static int fill(CommandSourceStack commandSourceStack, BlockPos blockPos, BlockPos blockPos2, Holder.Reference<Biome> reference, Predicate<Holder<Biome>> predicate) throws CommandSyntaxException {
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fill(commandSourceStack.getLevel(), blockPos, blockPos2, reference, predicate, supplier -> commandSourceStack.sendSuccess((Supplier<Component>)supplier, true));
        Optional optional = either.right();
        if (optional.isPresent()) {
            throw (CommandSyntaxException)((Object)optional.get());
        }
        return (Integer)either.left().get();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableBiMap
 *  com.google.common.collect.ImmutableList
 *  org.apache.commons.lang3.function.TriFunction
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.apache.commons.lang3.function.TriFunction;

public record WeatheringCopperBlocks(Block unaffected, Block exposed, Block weathered, Block oxidized, Block waxed, Block waxedExposed, Block waxedWeathered, Block waxedOxidized) {
    public static <WaxedBlock extends Block, WeatheringBlock extends Block> WeatheringCopperBlocks create(String string, TriFunction<String, Function<BlockBehaviour.Properties, Block>, BlockBehaviour.Properties, Block> triFunction, Function<BlockBehaviour.Properties, WaxedBlock> function, BiFunction<WeatheringCopper.WeatherState, BlockBehaviour.Properties, WeatheringBlock> biFunction, Function<WeatheringCopper.WeatherState, BlockBehaviour.Properties> function2) {
        return new WeatheringCopperBlocks((Block)triFunction.apply((Object)string, properties -> (Block)biFunction.apply(WeatheringCopper.WeatherState.UNAFFECTED, (BlockBehaviour.Properties)properties), (Object)function2.apply(WeatheringCopper.WeatherState.UNAFFECTED)), (Block)triFunction.apply((Object)("exposed_" + string), properties -> (Block)biFunction.apply(WeatheringCopper.WeatherState.EXPOSED, (BlockBehaviour.Properties)properties), (Object)function2.apply(WeatheringCopper.WeatherState.EXPOSED)), (Block)triFunction.apply((Object)("weathered_" + string), properties -> (Block)biFunction.apply(WeatheringCopper.WeatherState.WEATHERED, (BlockBehaviour.Properties)properties), (Object)function2.apply(WeatheringCopper.WeatherState.WEATHERED)), (Block)triFunction.apply((Object)("oxidized_" + string), properties -> (Block)biFunction.apply(WeatheringCopper.WeatherState.OXIDIZED, (BlockBehaviour.Properties)properties), (Object)function2.apply(WeatheringCopper.WeatherState.OXIDIZED)), (Block)triFunction.apply((Object)("waxed_" + string), function::apply, (Object)function2.apply(WeatheringCopper.WeatherState.UNAFFECTED)), (Block)triFunction.apply((Object)("waxed_exposed_" + string), function::apply, (Object)function2.apply(WeatheringCopper.WeatherState.EXPOSED)), (Block)triFunction.apply((Object)("waxed_weathered_" + string), function::apply, (Object)function2.apply(WeatheringCopper.WeatherState.WEATHERED)), (Block)triFunction.apply((Object)("waxed_oxidized_" + string), function::apply, (Object)function2.apply(WeatheringCopper.WeatherState.OXIDIZED)));
    }

    public ImmutableBiMap<Block, Block> weatheringMapping() {
        return ImmutableBiMap.of((Object)this.unaffected, (Object)this.exposed, (Object)this.exposed, (Object)this.weathered, (Object)this.weathered, (Object)this.oxidized);
    }

    public ImmutableBiMap<Block, Block> waxedMapping() {
        return ImmutableBiMap.of((Object)this.unaffected, (Object)this.waxed, (Object)this.exposed, (Object)this.waxedExposed, (Object)this.weathered, (Object)this.waxedWeathered, (Object)this.oxidized, (Object)this.waxedOxidized);
    }

    public ImmutableList<Block> asList() {
        return ImmutableList.of((Object)this.unaffected, (Object)this.waxed, (Object)this.exposed, (Object)this.waxedExposed, (Object)this.weathered, (Object)this.waxedWeathered, (Object)this.oxidized, (Object)this.waxedOxidized);
    }

    public void forEach(Consumer<Block> consumer) {
        consumer.accept(this.unaffected);
        consumer.accept(this.exposed);
        consumer.accept(this.weathered);
        consumer.accept(this.oxidized);
        consumer.accept(this.waxed);
        consumer.accept(this.waxedExposed);
        consumer.accept(this.waxedWeathered);
        consumer.accept(this.waxedOxidized);
    }
}


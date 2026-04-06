/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class FallenTreeConfiguration
implements FeatureConfiguration {
    public static final Codec<FallenTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(fallenTreeConfiguration -> fallenTreeConfiguration.trunkProvider), (App)IntProvider.codec(0, 16).fieldOf("log_length").forGetter(fallenTreeConfiguration -> fallenTreeConfiguration.logLength), (App)TreeDecorator.CODEC.listOf().fieldOf("stump_decorators").forGetter(fallenTreeConfiguration -> fallenTreeConfiguration.stumpDecorators), (App)TreeDecorator.CODEC.listOf().fieldOf("log_decorators").forGetter(fallenTreeConfiguration -> fallenTreeConfiguration.logDecorators)).apply((Applicative)instance, FallenTreeConfiguration::new));
    public final BlockStateProvider trunkProvider;
    public final IntProvider logLength;
    public final List<TreeDecorator> stumpDecorators;
    public final List<TreeDecorator> logDecorators;

    protected FallenTreeConfiguration(BlockStateProvider blockStateProvider, IntProvider intProvider, List<TreeDecorator> list, List<TreeDecorator> list2) {
        this.trunkProvider = blockStateProvider;
        this.logLength = intProvider;
        this.stumpDecorators = list;
        this.logDecorators = list2;
    }

    public static class FallenTreeConfigurationBuilder {
        private final BlockStateProvider trunkProvider;
        private final IntProvider logLength;
        private List<TreeDecorator> stumpDecorators = new ArrayList<TreeDecorator>();
        private List<TreeDecorator> logDecorators = new ArrayList<TreeDecorator>();

        public FallenTreeConfigurationBuilder(BlockStateProvider blockStateProvider, IntProvider intProvider) {
            this.trunkProvider = blockStateProvider;
            this.logLength = intProvider;
        }

        public FallenTreeConfigurationBuilder stumpDecorators(List<TreeDecorator> list) {
            this.stumpDecorators = list;
            return this;
        }

        public FallenTreeConfigurationBuilder logDecorators(List<TreeDecorator> list) {
            this.logDecorators = list;
            return this;
        }

        public FallenTreeConfiguration build() {
            return new FallenTreeConfiguration(this.trunkProvider, this.logLength, this.stumpDecorators, this.logDecorators);
        }
    }
}


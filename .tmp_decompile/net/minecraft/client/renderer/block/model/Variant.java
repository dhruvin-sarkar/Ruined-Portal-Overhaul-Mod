/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.block.model;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.math.Quadrant;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public record Variant(Identifier modelLocation, SimpleModelState modelState) implements BlockModelPart.Unbaked
{
    public static final MapCodec<Variant> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("model").forGetter(Variant::modelLocation), (App)SimpleModelState.MAP_CODEC.forGetter(Variant::modelState)).apply((Applicative)instance, Variant::new));
    public static final Codec<Variant> CODEC = MAP_CODEC.codec();

    public Variant(Identifier identifier) {
        this(identifier, SimpleModelState.DEFAULT);
    }

    public Variant withXRot(Quadrant quadrant) {
        return this.withState(this.modelState.withX(quadrant));
    }

    public Variant withYRot(Quadrant quadrant) {
        return this.withState(this.modelState.withY(quadrant));
    }

    public Variant withZRot(Quadrant quadrant) {
        return this.withState(this.modelState.withZ(quadrant));
    }

    public Variant withUvLock(boolean bl) {
        return this.withState(this.modelState.withUvLock(bl));
    }

    public Variant withModel(Identifier identifier) {
        return new Variant(identifier, this.modelState);
    }

    public Variant withState(SimpleModelState simpleModelState) {
        return new Variant(this.modelLocation, simpleModelState);
    }

    public Variant with(VariantMutator variantMutator) {
        return (Variant)variantMutator.apply(this);
    }

    @Override
    public BlockModelPart bake(ModelBaker modelBaker) {
        return SimpleModelWrapper.bake(modelBaker, this.modelLocation, this.modelState.asModelState());
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(this.modelLocation);
    }

    @Environment(value=EnvType.CLIENT)
    public record SimpleModelState(Quadrant x, Quadrant y, Quadrant z, boolean uvLock) {
        public static final MapCodec<SimpleModelState> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Quadrant.CODEC.optionalFieldOf("x", (Object)Quadrant.R0).forGetter(SimpleModelState::x), (App)Quadrant.CODEC.optionalFieldOf("y", (Object)Quadrant.R0).forGetter(SimpleModelState::y), (App)Quadrant.CODEC.optionalFieldOf("z", (Object)Quadrant.R0).forGetter(SimpleModelState::z), (App)Codec.BOOL.optionalFieldOf("uvlock", (Object)false).forGetter(SimpleModelState::uvLock)).apply((Applicative)instance, SimpleModelState::new));
        public static final SimpleModelState DEFAULT = new SimpleModelState(Quadrant.R0, Quadrant.R0, Quadrant.R0, false);

        public ModelState asModelState() {
            BlockModelRotation blockModelRotation = BlockModelRotation.get(Quadrant.fromXYZAngles(this.x, this.y, this.z));
            return this.uvLock ? blockModelRotation.withUvLock() : blockModelRotation;
        }

        public SimpleModelState withX(Quadrant quadrant) {
            return new SimpleModelState(quadrant, this.y, this.z, this.uvLock);
        }

        public SimpleModelState withY(Quadrant quadrant) {
            return new SimpleModelState(this.x, quadrant, this.z, this.uvLock);
        }

        public SimpleModelState withZ(Quadrant quadrant) {
            return new SimpleModelState(this.x, this.y, quadrant, this.uvLock);
        }

        public SimpleModelState withUvLock(boolean bl) {
            return new SimpleModelState(this.x, this.y, this.z, bl);
        }
    }
}


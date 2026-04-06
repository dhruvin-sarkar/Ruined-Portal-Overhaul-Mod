/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3f
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class KeyframeAnimation {
    private final AnimationDefinition definition;
    private final List<Entry> entries;

    private KeyframeAnimation(AnimationDefinition animationDefinition, List<Entry> list) {
        this.definition = animationDefinition;
        this.entries = list;
    }

    static KeyframeAnimation bake(ModelPart modelPart, AnimationDefinition animationDefinition) {
        ArrayList<Entry> list = new ArrayList<Entry>();
        Function<String, @Nullable ModelPart> function = modelPart.createPartLookup();
        for (Map.Entry<String, List<AnimationChannel>> entry : animationDefinition.boneAnimations().entrySet()) {
            String string = entry.getKey();
            List<AnimationChannel> list2 = entry.getValue();
            ModelPart modelPart2 = function.apply(string);
            if (modelPart2 == null) {
                throw new IllegalArgumentException("Cannot animate " + string + ", which does not exist in model");
            }
            for (AnimationChannel animationChannel : list2) {
                list.add(new Entry(modelPart2, animationChannel.target(), animationChannel.keyframes()));
            }
        }
        return new KeyframeAnimation(animationDefinition, List.copyOf(list));
    }

    public void applyStatic() {
        this.apply(0L, 1.0f);
    }

    public void applyWalk(float f, float g, float h, float i) {
        long l = (long)(f * 50.0f * h);
        float j = Math.min(g * i, 1.0f);
        this.apply(l, j);
    }

    public void apply(AnimationState animationState, float f) {
        this.apply(animationState, f, 1.0f);
    }

    public void apply(AnimationState animationState2, float f, float g) {
        animationState2.ifStarted(animationState -> this.apply((long)((float)animationState.getTimeInMillis(f) * g), 1.0f));
    }

    public void apply(long l, float f) {
        float g = this.getElapsedSeconds(l);
        Vector3f vector3f = new Vector3f();
        for (Entry entry : this.entries) {
            entry.apply(g, f, vector3f);
        }
    }

    private float getElapsedSeconds(long l) {
        float f = (float)l / 1000.0f;
        return this.definition.looping() ? f % this.definition.lengthInSeconds() : f;
    }

    @Environment(value=EnvType.CLIENT)
    record Entry(ModelPart part, AnimationChannel.Target target, Keyframe[] keyframes) {
        public void apply(float f, float g, Vector3f vector3f) {
            int i2 = Math.max(0, Mth.binarySearch(0, this.keyframes.length, i -> f <= this.keyframes[i].timestamp()) - 1);
            int j = Math.min(this.keyframes.length - 1, i2 + 1);
            Keyframe keyframe = this.keyframes[i2];
            Keyframe keyframe2 = this.keyframes[j];
            float h = f - keyframe.timestamp();
            float k = j != i2 ? Mth.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0f, 1.0f) : 0.0f;
            keyframe2.interpolation().apply(vector3f, k, this.keyframes, i2, j, g);
            this.target.apply(this.part, vector3f);
        }
    }
}


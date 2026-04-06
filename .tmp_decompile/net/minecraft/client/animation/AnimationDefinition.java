/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.animation;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public record AnimationDefinition(float lengthInSeconds, boolean looping, Map<String, List<AnimationChannel>> boneAnimations) {
    public KeyframeAnimation bake(ModelPart modelPart) {
        return KeyframeAnimation.bake(modelPart, this);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final float length;
        private final Map<String, List<AnimationChannel>> animationByBone = Maps.newHashMap();
        private boolean looping;

        public static Builder withLength(float f) {
            return new Builder(f);
        }

        private Builder(float f) {
            this.length = f;
        }

        public Builder looping() {
            this.looping = true;
            return this;
        }

        public Builder addAnimation(String string2, AnimationChannel animationChannel) {
            this.animationByBone.computeIfAbsent(string2, string -> new ArrayList()).add(animationChannel);
            return this;
        }

        public AnimationDefinition build() {
            return new AnimationDefinition(this.length, this.looping, this.animationByBone);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model;

import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;

@Environment(value=EnvType.CLIENT)
public record BabyModelTransform(boolean scaleHead, float babyYHeadOffset, float babyZHeadOffset, float babyHeadScale, float babyBodyScale, float bodyYOffset, Set<String> headParts) implements MeshTransformer
{
    public BabyModelTransform(Set<String> set) {
        this(false, 5.0f, 2.0f, set);
    }

    public BabyModelTransform(boolean bl, float f, float g, Set<String> set) {
        this(bl, f, g, 2.0f, 2.0f, 24.0f, set);
    }

    @Override
    public MeshDefinition apply(MeshDefinition meshDefinition) {
        float f = this.scaleHead ? 1.5f / this.babyHeadScale : 1.0f;
        float g = 1.0f / this.babyBodyScale;
        UnaryOperator unaryOperator = partPose -> partPose.translated(0.0f, this.babyYHeadOffset, this.babyZHeadOffset).scaled(f);
        UnaryOperator unaryOperator2 = partPose -> partPose.translated(0.0f, this.bodyYOffset, 0.0f).scaled(g);
        MeshDefinition meshDefinition2 = new MeshDefinition();
        for (Map.Entry<String, PartDefinition> entry : meshDefinition.getRoot().getChildren()) {
            String string = entry.getKey();
            PartDefinition partDefinition = entry.getValue();
            meshDefinition2.getRoot().addOrReplaceChild(string, partDefinition.transformed(this.headParts.contains(string) ? unaryOperator : unaryOperator2));
        }
        return meshDefinition2;
    }
}


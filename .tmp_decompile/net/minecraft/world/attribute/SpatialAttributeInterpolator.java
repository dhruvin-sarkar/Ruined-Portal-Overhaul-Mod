/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2DoubleMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2DoubleMaps
 */
package net.minecraft.world.attribute;

import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMaps;
import java.util.Objects;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.LerpFunction;

public class SpatialAttributeInterpolator {
    private final Reference2DoubleArrayMap<EnvironmentAttributeMap> weightsBySource = new Reference2DoubleArrayMap();

    public void clear() {
        this.weightsBySource.clear();
    }

    public SpatialAttributeInterpolator accumulate(double d, EnvironmentAttributeMap environmentAttributeMap) {
        this.weightsBySource.mergeDouble((Object)environmentAttributeMap, d, Double::sum);
        return this;
    }

    public <Value> Value applyAttributeLayer(EnvironmentAttribute<Value> environmentAttribute, Value object) {
        if (this.weightsBySource.isEmpty()) {
            return object;
        }
        if (this.weightsBySource.size() == 1) {
            EnvironmentAttributeMap environmentAttributeMap = (EnvironmentAttributeMap)this.weightsBySource.keySet().iterator().next();
            return environmentAttributeMap.applyModifier(environmentAttribute, object);
        }
        LerpFunction<Value> lerpFunction = environmentAttribute.type().spatialLerp();
        Object object2 = null;
        double d = 0.0;
        for (Reference2DoubleMap.Entry entry : Reference2DoubleMaps.fastIterable(this.weightsBySource)) {
            EnvironmentAttributeMap environmentAttributeMap2 = (EnvironmentAttributeMap)entry.getKey();
            double e = entry.getDoubleValue();
            Value object3 = environmentAttributeMap2.applyModifier(environmentAttribute, object);
            d += e;
            if (object2 == null) {
                object2 = object3;
                continue;
            }
            float f = (float)(e / d);
            object2 = lerpFunction.apply(f, object2, object3);
        }
        return Objects.requireNonNull(object2);
    }
}

